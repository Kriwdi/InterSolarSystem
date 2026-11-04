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
 * <p>Places: "earth_orbit", "moon_orbit"; later worlds add theirs ({@link #DESTINATIONS}).
 */
public final class Mothership extends SavedData {
	public static final List<String> DESTINATIONS = List.of("earth_orbit", "moon_orbit");
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
		String choice = DESTINATIONS.get(Math.floorMod(selected, DESTINATIONS.size()));
		if (player.isShiftKeyDown()) {
			selected = Math.floorMod(selected + 1, DESTINATIONS.size());
			setDirty();
			choice = DESTINATIONS.get(selected);
			player.sendOverlayMessage(Component.translatable("mothership.intersolarsystem.course",
					Component.translatable("mothership.intersolarsystem.place." + choice)));
			return;
		}
		if (choice.equals(location)) {
			player.sendOverlayMessage(Component.translatable("mothership.intersolarsystem.here",
					Component.translatable("mothership.intersolarsystem.place." + location)));
			return;
		}
		target = choice;
		arrival = now + LEG_TICKS;
		setDirty();
		level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.5F);
		broadcast(level.getServer(), Component.translatable("mothership.intersolarsystem.departing",
				Component.translatable("mothership.intersolarsystem.place." + target), LEG_TICKS / 1200));
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
		if ("moon_orbit".equals(location)) {
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
		return t;
	}
}
