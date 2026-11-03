package hu.kriwdi.intersolarsystem.spaceport;

import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.content.ShipFacingBlock;
import hu.kriwdi.intersolarsystem.lore.LoreItems;
import hu.kriwdi.intersolarsystem.ship.ShipRepairBoards;
import hu.kriwdi.intersolarsystem.spaceport.Facility.Need;
import hu.kriwdi.intersolarsystem.spaceport.SpaceportState.FacilityState;
import hu.kriwdi.intersolarsystem.spaceport.SpaceportState.Site;

/**
 * Restoring a Kármán spaceport and building the mothership.
 *
 * <ul>
 * <li><b>Control cabinets</b> (one per facility): empty hand shows the checklist (the facility before it, debris,
 * materials, calibration); with a needed item it takes it; when everything else is done an empty hand starts the
 * calibration board. The power plant then burns uranium fuel rods (given to its cabinet) and now and then breaks
 * down (a repair kit at the cabinet and a valve board fix it). The fuel depot takes bitumen and helium-3 regolith as
 * propellant for the mothership.</li>
 * <li><b>Fabricator</b> (needs the fabrication hall working): sneak to choose what to make; deliver the materials;
 * the kit comes out.</li>
 * <li><b>Crane console</b> (needs the crane working): give it the next section's kit and the crane builds the
 * section in the assembly hall, block by block.</li>
 * <li><b>Manual printer</b> (needs launch control working): prints the manuals, one sheet of paper each.</li>
 * </ul>
 */
public final class Spaceports {
	/** Debris (rubble and broken concrete) must be cleared within this distance of a cabinet. */
	public static final int DEBRIS_RADIUS = 8;
	public static final long FUEL_ROD_TICKS = 72_000L;
	private static final int BLOCKS_PER_TICK = 12;
	private static final int PROPELLANT_MAX = 1000;
	public static final int PROPELLANT_TO_LAUNCH = 600;
	public static final List<String> MANUALS = List.of("nav_console", "launch_checklist", "meteor_piloting", "repairs",
			"ship_systems", "space_suit", "diagnostics", "drive_kits", "ship_farm", "salvage", "spaceport", "mothership");

	private Spaceports() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(Spaceports::tick);
	}

	private static Site site(ServerLevel level, BlockPos pos) {
		return SpaceportState.get(level.getServer()).site(level.dimension().identifier().toString(), pos);
	}

	private static void changed(ServerLevel level) {
		SpaceportState.get(level.getServer()).changed();
	}

	private static Item item(String id) {
		return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
	}

	private static Component name(String id) {
		return item(id).getDefaultInstance().getHoverName();
	}

	// ---------------------------------------------------------------- cabinets
	public static void cabinet(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack held, Facility facility) {
		Site site = site(level, pos);
		FacilityState state = site.facility(facility);
		Component title = Component.translatable("spaceport.intersolarsystem.facility." + facility.id());
		if (state.calibrated) {
			running(level, pos, player, held, facility, site, title);
			return;
		}
		Facility previous = facility.previous();
		if (!held.isEmpty()) {
			String id = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
			for (Need need : facility.needs()) {
				if (need.item().equals(id)) {
					int have = state.delivered.getOrDefault(id, 0);
					int take = Math.min(need.count() - have, held.getCount());
					if (take > 0) {
						held.shrink(take);
						state.delivered.put(id, have + take);
						changed(level);
						level.playSound(null, pos, SoundEvents.ARMOR_EQUIP_IRON.value(), SoundSource.BLOCKS, 0.8F, 0.8F);
						player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.delivered", name(id),
								have + take, need.count()));
					} else {
						player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.enough", name(id)));
					}
					return;
				}
			}
			player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.not_needed", title));
			return;
		}
		boolean previousOk = previous == null || site.working(previous);
		int debris = debris(level, pos);
		boolean materials = true;
		player.sendSystemMessage(Component.translatable("spaceport.intersolarsystem.checklist", title)
				.withStyle(ChatFormatting.GOLD));
		if (previous != null) {
			line(player, previousOk, Component.translatable("spaceport.intersolarsystem.needs_previous",
					Component.translatable("spaceport.intersolarsystem.facility." + previous.id())));
		}
		line(player, debris == 0, Component.translatable("spaceport.intersolarsystem.debris", debris, DEBRIS_RADIUS));
		for (Need need : facility.needs()) {
			int have = state.delivered.getOrDefault(need.item(), 0);
			materials &= have >= need.count();
			line(player, have >= need.count(), Component.translatable("spaceport.intersolarsystem.material", name(need.item()),
					have, need.count()));
		}
		boolean ready = previousOk && debris == 0 && materials;
		line(player, false, Component.translatable(ready ? "spaceport.intersolarsystem.calibrate_now"
				: "spaceport.intersolarsystem.calibrate_later"));
		if (ready) {
			ShipRepairBoards.openExternal(level.getServer(), player, facility.calibration(), facility.boardSize(), solved -> {
				if (solved) {
					state.calibrated = true;
					if (facility == Facility.POWER && site.reactorFuel <= 0) {
						site.reactorFuel = FUEL_ROD_TICKS * 2;
					}
					site.nextFaultCheck = level.getGameTime() + 24000;
					changed(level);
					level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
					player.sendSystemMessage(Component.translatable("spaceport.intersolarsystem.online", title)
							.withStyle(ChatFormatting.GREEN));
				} else {
					level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.8F, 1.0F);
					player.sendSystemMessage(Component.translatable("spaceport.intersolarsystem.calibration_failed")
							.withStyle(ChatFormatting.RED));
				}
			});
		}
	}

	private static void line(ServerPlayer player, boolean ok, Component text) {
		player.sendSystemMessage(Component.literal(ok ? " ✔ " : " • ").withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.GRAY)
				.append(text.copy().withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.WHITE)));
	}

	/** Rubble and broken concrete left near the cabinet. */
	private static int debris(ServerLevel level, BlockPos pos) {
		int n = 0;
		for (BlockPos p : BlockPos.betweenClosed(pos.offset(-DEBRIS_RADIUS, -2, -DEBRIS_RADIUS),
				pos.offset(DEBRIS_RADIUS, 4, DEBRIS_RADIUS))) {
			Block b = level.getBlockState(p).getBlock();
			if (b == ModBlocks.RUBBLE || b == hu.kriwdi.intersolarsystem.content.UrbanBlocks.REBAR_CONCRETE) {
				n++;
			}
		}
		return n;
	}

	/** A restored facility: status, and what it still takes (fuel rods, propellant, repairs). */
	private static void running(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack held, Facility facility,
			Site site, Component title) {
		if (facility == Facility.POWER) {
			if (held.getItem() == ModItems.URANIUM_FUEL_ROD) {
				held.shrink(1);
				site.reactorFuel += FUEL_ROD_TICKS;
				changed(level);
				level.playSound(null, pos, SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 0.8F, 0.6F);
			} else if (held.getItem() == ModItems.REPAIR_KIT && site.reactorFault) {
				ShipRepairBoards.openExternal(level.getServer(), player, hu.kriwdi.intersolarsystem.ship.RepairMinigame.VALVE, 3,
						solved -> {
							if (solved) {
								held.shrink(1);
								site.reactorFault = false;
								changed(level);
								player.sendSystemMessage(Component.translatable("spaceport.intersolarsystem.reactor_repaired")
										.withStyle(ChatFormatting.GREEN));
							}
						});
				return;
			}
			player.sendOverlayMessage(Component.translatable(site.reactorFault ? "spaceport.intersolarsystem.reactor_fault"
					: "spaceport.intersolarsystem.reactor_status", site.reactorFuel / 1200));
			return;
		}
		if (facility == Facility.FUEL) {
			int add = held.getItem() == ModItems.BITUMEN ? 2 : held.is(item("intersolarsystem:helium3_regolith")) ? 12 : 0;
			if (add > 0 && site.propellant < PROPELLANT_MAX) {
				int n = Math.min(held.getCount(), (PROPELLANT_MAX - site.propellant + add - 1) / add);
				held.shrink(n);
				site.propellant = Math.min(PROPELLANT_MAX, site.propellant + n * add);
				changed(level);
				level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 0.7F);
			}
			player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.propellant", site.propellant,
					PROPELLANT_MAX, PROPELLANT_TO_LAUNCH));
			return;
		}
		player.sendOverlayMessage(Component.translatable(site.working(facility) ? "spaceport.intersolarsystem.working"
				: "spaceport.intersolarsystem.no_power", title));
	}

	// ---------------------------------------------------------------- fabricator
	public static void fabricator(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack held) {
		Site site = site(level, pos);
		Section[] all = Section.values();
		Section recipe = all[Math.floorMod(site.recipe, all.length)];
		if (!site.working(Facility.FABRICATION)) {
			player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.needs_working",
					Component.translatable("spaceport.intersolarsystem.facility.fabrication")));
			return;
		}
		if (held.isEmpty() && player.isShiftKeyDown()) {
			site.recipe = (site.recipe + 1) % all.length;
			site.fabricatorDelivered.clear();
			changed(level);
			recipe = all[site.recipe];
			player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.recipe", name(recipe.kit())));
			return;
		}
		if (!held.isEmpty()) {
			String id = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
			for (Need need : recipe.needs()) {
				if (need.item().equals(id)) {
					int have = site.fabricatorDelivered.getOrDefault(id, 0);
					int take = Math.min(need.count() - have, held.getCount());
					held.shrink(take);
					site.fabricatorDelivered.put(id, have + take);
					changed(level);
					break;
				}
			}
		}
		boolean done = true;
		player.sendSystemMessage(Component.translatable("spaceport.intersolarsystem.recipe", name(recipe.kit()))
				.withStyle(ChatFormatting.GOLD));
		for (Need need : recipe.needs()) {
			int have = site.fabricatorDelivered.getOrDefault(need.item(), 0);
			done &= have >= need.count();
			line(player, have >= need.count(), Component.translatable("spaceport.intersolarsystem.material", name(need.item()),
					have, need.count()));
		}
		if (done) {
			site.fabricatorDelivered.clear();
			changed(level);
			ItemStack kit = new ItemStack(item(recipe.kit()));
			level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, kit));
			level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 0.8F);
			level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 30, 0.6, 0.4,
					0.6, 0.1);
			player.sendSystemMessage(Component.translatable("spaceport.intersolarsystem.made", name(recipe.kit()))
					.withStyle(ChatFormatting.GREEN));
		}
	}

	// ---------------------------------------------------------------- crane
	public static void crane(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack held) {
		Site site = site(level, pos);
		if (!site.working(Facility.CRANE)) {
			player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.needs_working",
					Component.translatable("spaceport.intersolarsystem.facility.crane")));
			return;
		}
		int total = Section.mothershipSections();
		if (site.building >= 0) {
			int size = Blueprint.of(Section.values()[site.building].id()).size();
			player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.crane_building",
					name(Section.values()[site.building].kit()), site.buildIndex * 100 / Math.max(1, size)));
			return;
		}
		if (site.sectionsDone >= total) {
			player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.mothership_complete", site.propellant,
					PROPELLANT_TO_LAUNCH));
			return;
		}
		Section next = Section.values()[site.sectionsDone];
		if (held.isEmpty() || !held.is(item(next.kit()))) {
			player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.crane_next", site.sectionsDone, total,
					name(next.kit())));
			return;
		}
		held.shrink(1);
		site.building = site.sectionsDone;
		site.buildIndex = 0;
		site.consolePos = pos.immutable();
		BlockState console = level.getBlockState(pos);
		Direction facing = console.hasProperty(ShipFacingBlock.FACING) ? console.getValue(ShipFacingBlock.FACING) : Direction.NORTH;
		site.consoleRotation = switch (facing) {
			case EAST -> 1;
			case SOUTH -> 2;
			case WEST -> 3;
			default -> 0;
		};
		changed(level);
		level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 0.6F);
		player.sendSystemMessage(Component.translatable("spaceport.intersolarsystem.crane_started", name(next.kit()))
				.withStyle(ChatFormatting.GREEN));
	}

	// ---------------------------------------------------------------- printer
	public static void printer(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack held) {
		Site site = site(level, pos);
		if (!site.working(Facility.CONTROL)) {
			player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.needs_working",
					Component.translatable("spaceport.intersolarsystem.facility.control")));
			return;
		}
		if (!held.is(Items.PAPER)) {
			player.sendOverlayMessage(Component.translatable("spaceport.intersolarsystem.printer_paper"));
			return;
		}
		held.shrink(1);
		int index = Math.floorMod(site.printed++, MANUALS.size());
		changed(level);
		ItemStack manual = LoreItems.with(new ItemStack(ModItems.MANUAL), MANUALS.get(index));
		level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, manual));
		level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	// ---------------------------------------------------------------- ticking
	private static void tick(MinecraftServer server) {
		SpaceportState state = SpaceportState.get(server);
		long now = server.overworld().getGameTime();
		for (Site site : state.sites()) {
			ServerLevel level = server.getLevel(net.minecraft.resources.ResourceKey.create(
					net.minecraft.core.registries.Registries.DIMENSION, Identifier.parse(site.dimension)));
			if (level == null) {
				continue;
			}
			if (site.online(Facility.POWER)) {
				site.reactorFuel--;
				if (now >= site.nextFaultCheck) {
					site.nextFaultCheck = now + 24000;
					if (level.getRandom().nextFloat() < 0.3F) {
						site.reactorFault = true;
						broadcast(level, site, Component.translatable("spaceport.intersolarsystem.reactor_fault", 0)
								.withStyle(ChatFormatting.RED));
					}
					state.changed();
				}
				if (site.reactorFuel == 0) {
					broadcast(level, site, Component.translatable("spaceport.intersolarsystem.reactor_empty")
							.withStyle(ChatFormatting.RED));
					state.changed();
				}
			}
			if (site.building >= 0 && site.working(Facility.CRANE) && level.isLoaded(site.consolePos)) {
				build(level, site, state);
			}
		}
	}

	private static void build(ServerLevel level, Site site, SpaceportState state) {
		Section section = Section.values()[site.building];
		List<Blueprint.Entry> blocks = Blueprint.of(section.id());
		Rotation rotation = Rotation.values()[site.consoleRotation & 3];
		for (int i = 0; i < BLOCKS_PER_TICK && site.buildIndex < blocks.size(); i++) {
			Blueprint.Entry e = blocks.get(site.buildIndex++);
			BlockPos p = Blueprint.place(site.consolePos, rotation, e);
			if (!level.isLoaded(p)) {
				site.buildIndex--;
				break;
			}
			level.setBlock(p, e.state().rotate(rotation), 2);
			if (i == 0 && site.buildIndex % 6 == 0) {
				level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 4, 0.3, 0.3,
						0.3, 0.05);
				level.playSound(null, p, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.6F, 0.7F);
			}
		}
		if (site.buildIndex >= blocks.size()) {
			site.building = -1;
			site.buildIndex = 0;
			site.sectionsDone++;
			broadcast(level, site, Component.translatable("spaceport.intersolarsystem.section_done", name(section.kit()),
					site.sectionsDone, Section.mothershipSections()).withStyle(ChatFormatting.GREEN));
		}
		state.changed();
	}

	private static void broadcast(ServerLevel level, Site site, Component message) {
		for (ServerPlayer p : level.players()) {
			if (Math.abs(p.getX() - site.x) < SpaceportState.SITE_RADIUS && Math.abs(p.getZ() - site.z) < SpaceportState.SITE_RADIUS) {
				p.sendSystemMessage(message);
			}
		}
	}
}
