package hu.kriwdi.intersolarsystem.ship;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import hu.kriwdi.intersolarsystem.content.ModItems;

/**
 * Server side of the wiring minigame. Using a repair kit on a short circuit opens the board; the incident is fixed and
 * the kit used up only when the server accepts the answer. A wrong wiring sparks and damages the subsystem a little.
 */
public final class ShipWiringRepair {
	/** A board stays valid this long (also bounded by the incident's own deadline). */
	public static final int BOARD_TIMEOUT_TICKS = 1200;
	public static final int WRONG_WIRING_DAMAGE = 5;

	private record Pending(WiringPuzzle puzzle, ShipIncident incident, long expiresTick) {
	}

	private static final Map<UUID, Pending> PENDING = new HashMap<>();

	private ShipWiringRepair() {
	}

	public static boolean needsMinigame(ShipIncident incident) {
		return incident.type() == ShipIncidentType.SHORT_CIRCUIT;
	}

	public static void start(MinecraftServer server, ServerPlayer player, ShipIncident incident) {
		ShipState state = ShipStateAccess.get(server);
		long now = server.overworld().getGameTime();
		int wires = WiringPuzzle.wiresFor(state.condition(ShipSubsystem.NAVIGATION_COMMS));
		WiringPuzzle puzzle = WiringPuzzle.of(server.overworld().getRandom().nextLong(), wires);
		PENDING.put(player.getUUID(), new Pending(puzzle, incident,
				Math.min(now + BOARD_TIMEOUT_TICKS, incident.deadlineTick())));
		ServerPlayNetworking.send(player, new WiringPayloads.Open(puzzle.seed(), puzzle.size()));
	}

	public static void handleResult(MinecraftServer server, ServerPlayer player, WiringPayloads.Result result) {
		Pending pending = PENDING.remove(player.getUUID());
		if (pending == null || pending.puzzle().seed() != result.seed()) {
			return;
		}
		ShipState state = ShipStateAccess.get(server);
		long now = server.overworld().getGameTime();
		if (now > pending.expiresTick() || !player.level().dimension().equals(ShipDimensions.INTERIOR)
				|| !state.hazards().incidents().contains(pending.incident())) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.wiring_expired"));
			return;
		}
		ItemStack kit = player.getMainHandItem();
		if (kit.getItem() != ModItems.REPAIR_KIT) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.wiring_no_kit"));
			return;
		}
		ShipSubsystem subsystem = pending.incident().subsystem();
		if (pending.puzzle().isSolvedBy(result.connections())) {
			if (state.resolveIncident(pending.incident())) {
				kit.consume(1, player);
				player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.repair_incident",
						Component.translatable(ShipCommands.incidentKey(pending.incident().type())),
						Component.translatable(ShipCommands.subsystemKey(subsystem))));
				ShipFlightController.playInterior(server, SoundEvents.BEACON_POWER_SELECT);
			}
		} else {
			state.setCondition(subsystem, state.condition(subsystem) - WRONG_WIRING_DAMAGE);
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.wiring_wrong",
					Component.translatable(ShipCommands.subsystemKey(subsystem)), WRONG_WIRING_DAMAGE));
			ShipFlightController.playInterior(server, SoundEvents.FIRECHARGE_USE);
		}
	}

	/** The player closed the board without finishing. */
	public static void cancel(ServerPlayer player) {
		PENDING.remove(player.getUUID());
	}

	public static void reset() {
		PENDING.clear();
	}
}
