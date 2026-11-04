package hu.kriwdi.intersolarsystem.spaceport;

import java.util.List;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;

import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.space.MoonSites;

/**
 * The mothership after launch: it lives in its own dimension (built from the same blueprints as in the assembly
 * hall), orbits a body and travels between them from the helm on the bridge. The hangar lift takes the crew down to
 * the body below and back up. One mothership per world (the first one launched); a second launch is refused.
 *
 * <p>Places: "earth_orbit", "moon_orbit", and the parking orbit of every body that is reached only through the
 * mothership ({@link #destinations()}, from solar_system.json). The mothership is the mobile base: the small ship docks
 * at its side port ({@link #dockingPort()}) and lands on the body below from there.
 */
public final class Mothership extends SavedData {
	/** The Earth-Moon stations; the far ones come from the solar system catalogue. */
	public static final List<String> DESTINATIONS = List.of("earth_orbit", "moon_orbit");
	/**
	 * The Ark-class engines: fusion (tier 3) from the start; the belt's drive kit makes them tier 4, Jupiter's metallic
	 * hydrogen 5, Saturn's heavy water 6, and the turbo drive 7.
	 */
	public static final int BASE_TIER = 3;
	/** The crane console's place in the mothership dimension: the blueprints are laid out around it. */
	public static final BlockPos ORIGIN = new BlockPos(5, 60, 48);
	/** Travel time between two places (ticks). */
	public static final long LEG_TICKS = 12_000L;

	public static final Codec<Mothership> CODEC = CompoundTag.CODEC.xmap(Mothership::load, Mothership::save);
	public static final SavedDataType<Mothership> TYPE = new SavedDataType<>(ModIds.id("mothership"), Mothership::new, CODEC,
			null);

	private boolean built;
	private String location = "earth_orbit";
	private String target = "";
	private long arrival;
	private int selected;
	private int driveTier = BASE_TIER;
	/** A fault aboard during a journey ("" none): the ship holds its course until it is repaired at the helm. */
	private String fault = "";
	/** Faults the journey can throw, and the repair board each needs. */
	private static final List<String> FAULTS = List.of("coolant", "power", "air");

	public static Mothership get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % 20 == 0) {
				get(server).tick(server);
			}
		});
	}

	public boolean built() {
		return built;
	}

	/** Where the small ship can find the mothership: its station, or "" while it travels (or before it exists). */
	public String station() {
		return built && target.isEmpty() ? location : "";
	}

	public int driveTier() {
		return driveTier;
	}

	public void setDriveTier(int tier) {
		driveTier = Math.max(driveTier, tier);
		setDirty();
	}

	/** The engine tier a drive kit gives (0: not a drive kit). */
	public static int kitTier(net.minecraft.world.item.ItemStack stack) {
		if (stack.is(hu.kriwdi.intersolarsystem.content.ModItems.PLASMA_DRIVE_KIT)) {
			return 4;
		}
		if (stack.is(hu.kriwdi.intersolarsystem.content.ModItems.METALLIC_HYDROGEN_DRIVE_KIT)) {
			return 5;
		}
		if (stack.is(hu.kriwdi.intersolarsystem.content.ModItems.DEUTERIUM_DRIVE_KIT)) {
			return 6;
		}
		return stack.is(hu.kriwdi.intersolarsystem.content.ModItems.TURBO_DRIVE_KIT) ? 7 : 0;
	}

	/** Every station the mothership can be sent to, nearest first. */
	public static List<String> destinations() {
		java.util.List<String> out = new java.util.ArrayList<>(DESTINATIONS);
		hu.kriwdi.intersolarsystem.travel.SolarSystem.bodies().stream()
				.filter(b -> b.viaMothership() && b.status() != hu.kriwdi.intersolarsystem.travel.CelestialBody.Status.LOCKED)
				.sorted(java.util.Comparator.comparingDouble(hu.kriwdi.intersolarsystem.travel.CelestialBody::transferDays))
				.map(hu.kriwdi.intersolarsystem.travel.CelestialBody::station)
				.distinct().forEach(out::add);
		return out;
	}

	/** The engine tier a station needs (the hardest body there). */
	static int tierFor(String station) {
		return hu.kriwdi.intersolarsystem.travel.SolarSystem.bodies().stream()
				.filter(b -> b.viaMothership() && b.station().equals(station))
				.mapToInt(hu.kriwdi.intersolarsystem.travel.CelestialBody::minTier).min().orElse(1);
	}

	/** Place id of a station for the travel maths ("moon_orbit" lies at the Moon). */
	private static String placeOf(String station) {
		return "moon_orbit".equals(station) ? "moon" : station;
	}

	/** Ticks the mothership takes between two stations. */
	long legTicks(String from, String to, long now) {
		long ticks = hu.kriwdi.intersolarsystem.travel.TravelMath.legTicks(placeOf(from), placeOf(to), driveTier, now)
				/ hu.kriwdi.intersolarsystem.travel.TravelMath.MOTHERSHIP_SPEEDUP;
		return Math.max(LEG_TICKS / 2, ticks);
	}

	/**
	 * The side port where the small ship docks: on the lower deck's level, outside the hull on the starboard side, a
	 * short gangway through the hull.
	 */
	public static BlockPos dockingPort() {
		return ORIGIN.offset(270 - 235, 32 - 1, 80 + 26 - 132);
	}

	/** Docks the small ship at the port (hull built around the hatch); returns the hatch. */
	public static BlockPos dockShip(ServerLevel space) {
		BlockPos port = dockingPort();
		net.minecraft.world.level.block.state.BlockState plating =
				hu.kriwdi.intersolarsystem.content.ModBlocks.SHIP_PLATING.defaultBlockState();
		// The gangway: a plated tube from inside the lower deck to the hatch.
		for (int z = port.getZ() - 12; z < port.getZ(); z++) {
			for (int dx = -1; dx <= 1; dx++) {
				for (int dy = -1; dy <= 3; dy++) {
					BlockPos p = new BlockPos(port.getX() + dx, port.getY() + dy, z);
					space.getChunk(p.getX() >> 4, p.getZ() >> 4);
					boolean inside = dx == 0 && (dy == 0 || dy == 1);
					if (inside) {
						space.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
					} else if (space.getBlockState(p).isAir()) {
						space.setBlock(p, plating, 2);
					}
				}
			}
		}
		space.setBlock(port, hu.kriwdi.intersolarsystem.content.ModBlocks.SHIP_HATCH.defaultBlockState()
				.setValue(hu.kriwdi.intersolarsystem.content.ShipFacingBlock.FACING, net.minecraft.core.Direction.NORTH),
				net.minecraft.world.level.block.Block.UPDATE_ALL);
		hu.kriwdi.intersolarsystem.ship.ShipShell.build(space, port, net.minecraft.core.Direction.NORTH,
				hu.kriwdi.intersolarsystem.ship.ShipShell.Mode.DOCKED);
		return port;
	}

	/** Launch from a spaceport: builds the ship in its dimension and takes everybody near the console aboard. */
	public static boolean launch(ServerLevel hall, BlockPos console, ServerPlayer player) {
		MinecraftServer server = hall.getServer();
		Mothership m = get(server);
		if (m.built) {
			player.sendSystemMessage(Component.translatable("spaceport.intersolarsystem.mothership_exists")
					.withStyle(ChatFormatting.RED));
			return false;
		}
		ServerLevel space = server.getLevel(ShipDimensions.MOTHERSHIP);
		if (space == null) {
			return false;
		}
		for (Section s : Section.values()) {
			if (!s.mothership()) {
				continue;
			}
			for (Blueprint.Entry e : Blueprint.of(s.id())) {
				BlockPos p = Blueprint.place(ORIGIN, Rotation.NONE, e);
				space.getChunk(p.getX() >> 4, p.getZ() >> 4);
				space.setBlock(p, e.state(), 2);
			}
		}
		m.built = true;
		m.location = "earth_orbit";
		m.setDirty();
		for (ServerPlayer p : java.util.List.copyOf(hall.players())) {
			if (p.blockPosition().distSqr(console) < 200 * 200) {
				board(p);
			}
		}
		return true;
	}

	/** The bridge: where people arrive aboard. */
	public static Vec3 bridge() {
		// The bridge deck lies forward of the ship's centre (see tools/worldgen/karman.py: SHIP_X1 - 40).
		return Vec3.atBottomCenterOf(ORIGIN.offset(402 - 40 - 235, 46 - 1, 80 - 132));
	}

	public static Vec3 hangar() {
		return Vec3.atBottomCenterOf(ORIGIN.offset(58 + 200 - 235, 32 - 1, 80 - 132));
	}

	public static void board(ServerPlayer player) {
		ServerLevel space = player.level().getServer().getLevel(ShipDimensions.MOTHERSHIP);
		if (space != null) {
			player.teleport(new TeleportTransition(space, hangar(), Vec3.ZERO, player.getYRot(), 0.0F, TeleportTransition.DO_NOTHING));
			player.sendSystemMessage(Component.translatable("mothership.intersolarsystem.aboard").withStyle(ChatFormatting.AQUA));
		}
	}

	// ---------------------------------------------------------------- helm
	public void helm(ServerLevel level, BlockPos pos, ServerPlayer player) {
		long now = level.getServer().overworld().getGameTime();
		if (!fault.isEmpty()) {
			net.minecraft.world.item.ItemStack held = player.getMainHandItem();
			if (!held.is(hu.kriwdi.intersolarsystem.content.ModItems.REPAIR_KIT)) {
				player.sendOverlayMessage(Component.translatable("mothership.intersolarsystem.fault_need_kit",
						Component.translatable("mothership.intersolarsystem.fault." + fault)).withStyle(ChatFormatting.RED));
				return;
			}
			hu.kriwdi.intersolarsystem.ship.RepairMinigame board = switch (fault) {
				case "coolant" -> hu.kriwdi.intersolarsystem.ship.RepairMinigame.VALVE;
				case "power" -> hu.kriwdi.intersolarsystem.ship.RepairMinigame.WIRING;
				default -> hu.kriwdi.intersolarsystem.ship.RepairMinigame.PRESSURE;
			};
			MinecraftServer server = level.getServer();
			hu.kriwdi.intersolarsystem.ship.ShipRepairBoards.openExternal(server, player, board, 5, ok -> {
				if (ok && !fault.isEmpty()) {
					player.getMainHandItem().shrink(1);
					broadcast(server, Component.translatable("mothership.intersolarsystem.fault_fixed",
							Component.translatable("mothership.intersolarsystem.fault." + fault)).withStyle(ChatFormatting.GREEN));
					fault = "";
					setDirty();
				}
			});
			return;
		}
		if (!target.isEmpty()) {
			player.sendOverlayMessage(Component.translatable("mothership.intersolarsystem.travelling",
					Component.translatable("mothership.intersolarsystem.place." + target), Math.max(0, (arrival - now) / 1200)));
			return;
		}
		// A drive kit in hand upgrades the engines (each kit is built from the one before it).
		net.minecraft.world.item.ItemStack kit = player.getMainHandItem();
		int kitTier = kitTier(kit);
		if (kitTier > driveTier) {
			kit.shrink(1);
			setDriveTier(kitTier);
			level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 0.6F);
			broadcast(level.getServer(), Component.translatable("mothership.intersolarsystem.drive_upgraded", kitTier)
					.withStyle(ChatFormatting.GREEN));
			if (kitTier >= 7) {
				broadcast(level.getServer(), Component.translatable("mothership.intersolarsystem.turbo_installed")
						.withStyle(ChatFormatting.LIGHT_PURPLE));
			}
			return;
		}
		List<String> all = destinations();
		String choice = all.get(Math.floorMod(selected, all.size()));
		if (player.isShiftKeyDown()) {
			selected = Math.floorMod(selected + 1, all.size());
			setDirty();
			choice = all.get(selected);
			player.sendOverlayMessage(Component.translatable("mothership.intersolarsystem.course_time",
					Component.translatable("mothership.intersolarsystem.place." + choice),
					hu.kriwdi.intersolarsystem.travel.TravelMath.formatDuration(legTicks(location, choice, now))));
			return;
		}
		if (tierFor(choice) > driveTier) {
			player.sendOverlayMessage(Component.translatable("mothership.intersolarsystem.needs_tier", tierFor(choice))
					.withStyle(ChatFormatting.RED));
			return;
		}
		if (choice.equals(location)) {
			player.sendOverlayMessage(Component.translatable("mothership.intersolarsystem.here",
					Component.translatable("mothership.intersolarsystem.place." + location)));
			return;
		}
		long leg = legTicks(location, choice, now);
		target = choice;
		arrival = now + leg;
		setDirty();
		level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.5F);
		broadcast(level.getServer(), Component.translatable("mothership.intersolarsystem.departing",
				Component.translatable("mothership.intersolarsystem.place." + target), Math.max(1, leg / 1200)));
	}

	private void tick(MinecraftServer server) {
		if (target.isEmpty()) {
			return;
		}
		long now = server.overworld().getGameTime();
		long left = arrival - now;
		ServerLevel space = server.getLevel(ShipDimensions.MOTHERSHIP);
		if (!fault.isEmpty()) {
			// Holding course until the fault is fixed: the arrival moves back with the clock.
			arrival += 20;
			setDirty();
			if (now % 600 < 20) {
				broadcast(server, Component.translatable("mothership.intersolarsystem.fault_reminder",
						Component.translatable("mothership.intersolarsystem.fault." + fault)).withStyle(ChatFormatting.RED));
			}
			return;
		}
		if (space != null && left > 0 && now % 2400 < 20) {
			// Something happens on the way; sometimes it breaks something that needs hands.
			String event = List.of("micrometeors", "solar_flare", "drift", "systems")
					.get(space.getRandom().nextInt(4));
			broadcast(server, Component.translatable("mothership.intersolarsystem.event." + event)
					.withStyle(ChatFormatting.GOLD));
			if (!event.equals("drift") && space.getRandom().nextFloat() < 0.5F && !space.players().isEmpty()) {
				fault = FAULTS.get(space.getRandom().nextInt(FAULTS.size()));
				setDirty();
				broadcast(server, Component.translatable("mothership.intersolarsystem.fault_new",
						Component.translatable("mothership.intersolarsystem.fault." + fault)).withStyle(ChatFormatting.RED));
				space.players().forEach(p -> space.playSound(null, p.blockPosition(),
						hu.kriwdi.intersolarsystem.content.ModSounds.OXYGEN_ALARM, SoundSource.PLAYERS, 0.8F, 0.6F));
			}
		}
		if (left <= 0) {
			location = target;
			target = "";
			setDirty();
			broadcast(server, Component.translatable("mothership.intersolarsystem.arrived",
					Component.translatable("mothership.intersolarsystem.place." + location)).withStyle(ChatFormatting.GREEN));
		}
	}

	private static void broadcast(MinecraftServer server, Component message) {
		ServerLevel space = server.getLevel(ShipDimensions.MOTHERSHIP);
		if (space != null) {
			space.players().forEach(p -> p.sendSystemMessage(message));
		}
	}

	// ---------------------------------------------------------------- hangar lift
	/** Down to the body the mothership orbits (aboard: down; elsewhere: back up to the hangar). */
	public void lift(ServerLevel level, ServerPlayer player) {
		MinecraftServer server = level.getServer();
		if (!level.dimension().equals(ShipDimensions.MOTHERSHIP)) {
			board(player);
			return;
		}
		if (!target.isEmpty()) {
			player.sendOverlayMessage(Component.translatable("mothership.intersolarsystem.lift_travelling"));
			return;
		}
		if ("mars_orbit".equals(location)) {
			ServerLevel mars = server.getLevel(ShipDimensions.MARS);
			if (mars != null) {
				hu.kriwdi.intersolarsystem.space.MarsSites.ensureBuilt(mars);
				int[] zone = hu.kriwdi.intersolarsystem.space.MarsSites.LANDING_ZONE;
				BlockPos at = MoonSites.surface(mars, zone[0] - 8, zone[1]);
				player.teleport(new TeleportTransition(mars, Vec3.atBottomCenterOf(at), Vec3.ZERO, player.getYRot(), 0.0F,
						TeleportTransition.DO_NOTHING));
			}
		} else if (hu.kriwdi.intersolarsystem.space.Worlds.byStation(location).isPresent()) {
			var w = hu.kriwdi.intersolarsystem.space.Worlds.byStation(location).get();
			ServerLevel far = server.getLevel(w.dimension());
			if (far != null) {
				hu.kriwdi.intersolarsystem.space.Worlds.ensureSites(far, w);
				BlockPos at = MoonSites.surface(far, w.landX() - 8, w.landZ());
				if (at.getY() <= far.getMinY() + 1) {
					at = hu.kriwdi.intersolarsystem.space.Worlds.emergencyPad(far, w);
				}
				player.teleport(new TeleportTransition(far, Vec3.atBottomCenterOf(at), Vec3.ZERO, player.getYRot(), 0.0F,
						TeleportTransition.DO_NOTHING));
			}
		} else if ("moon_orbit".equals(location)) {
			ServerLevel moon = server.getLevel(ShipDimensions.MOON);
			if (moon != null) {
				int[] zone = MoonSites.LANDING_ZONE;
				BlockPos at = MoonSites.surface(moon, zone[0] + 6, zone[1] + 6);
				player.teleport(new TeleportTransition(moon, Vec3.atBottomCenterOf(at.above()), Vec3.ZERO, player.getYRot(), 0.0F,
						TeleportTransition.DO_NOTHING));
			}
		} else {
			ServerLevel orbit = server.getLevel(ShipDimensions.ORBIT);
			if (orbit != null) {
				player.teleport(new TeleportTransition(orbit, new Vec3(0.5, 119, -17.5), Vec3.ZERO, player.getYRot(), 0.0F,
						TeleportTransition.DO_NOTHING));
			}
		}
		player.sendSystemMessage(Component.translatable("mothership.intersolarsystem.lift_down"));
	}

	private static Mothership load(CompoundTag t) {
		Mothership m = new Mothership();
		m.built = t.getBooleanOr("built", false);
		m.location = t.getStringOr("location", "earth_orbit");
		m.target = t.getStringOr("target", "");
		m.arrival = t.getLongOr("arrival", 0L);
		m.selected = t.getIntOr("selected", 0);
		m.fault = t.getStringOr("fault", "");
		m.driveTier = t.getIntOr("driveTier", BASE_TIER);
		return m;
	}

	private CompoundTag save() {
		CompoundTag t = new CompoundTag();
		t.putBoolean("built", built);
		t.putString("location", location);
		t.putString("target", target);
		t.putLong("arrival", arrival);
		t.putInt("selected", selected);
		t.putString("fault", fault);
		t.putInt("driveTier", driveTier);
		return t;
	}
}
