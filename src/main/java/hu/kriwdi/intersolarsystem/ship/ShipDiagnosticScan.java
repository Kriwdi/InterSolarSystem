package hu.kriwdi.intersolarsystem.ship;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Server side of the diagnostic console: scans for hidden faults and starts test hops. */
public final class ShipDiagnosticScan {
	/** A test hop starts only on a second request from the same player within this many ticks. */
	public static final int CONFIRM_WINDOW_TICKS = 100;

	// Pending test hop confirmations; transient on purpose (a restart simply asks again).
	private static final Map<UUID, Long> PENDING_TEST = new HashMap<>();

	private ShipDiagnosticScan() {
	}

	public static void scan(MinecraftServer server, ServerPlayer player) {
		ShipState state = ShipStateAccess.get(server);
		if (state.isReadOnly()) {
			player.sendSystemMessage(Component.translatable("command.intersolarsystem.ship.read_only"));
			return;
		}
		long now = server.overworld().getGameTime();
		long since = now - state.diagnostics().lastScanTick();
		if (state.diagnostics().lastScanTick() != Long.MIN_VALUE && since >= 0
				&& since < ShipFaultRules.SCAN_COOLDOWN_TICKS) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.scan_cooldown",
					ShipFlightTimeline.formatClock(ShipFaultRules.SCAN_COOLDOWN_TICKS - since)));
			return;
		}
		List<ShipLatentFault> before = state.diagnostics().faults();
		List<ShipLatentFault> after = ShipFaultRules.scan(server.overworld().getRandom().nextLong(),
				state.diagnostics().scansDone(), state.condition(ShipSubsystem.NAVIGATION_COMMS), before);
		state.recordScan(after, now);
		List<ShipLatentFault> known = state.diagnostics().revealed();
		long fresh = known.size() - before.stream().filter(ShipLatentFault::revealed).count();
		player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.scan_done", known.size(), fresh));
		for (ShipLatentFault fault : known) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.scan_fault_line",
					Component.translatable(ShipCommands.subsystemKey(fault.subsystem()))));
		}
		int estimate = ShipFaultRules.percent(ShipFaultRules.launchRisk(ShipRepair.conditions(state), after, true, false));
		player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.scan_risk", estimate));
	}

	/** First call asks for confirmation; a second call within the window starts the test hop. */
	public static void requestTestHop(MinecraftServer server, ServerPlayer player) {
		ShipState state = ShipStateAccess.get(server);
		long now = server.overworld().getGameTime();
		if (state.isReadOnly()) {
			player.sendSystemMessage(Component.translatable("command.intersolarsystem.ship.read_only"));
			return;
		}
		if (state.phase() != ShipFlightPhase.LANDED || state.landingSite().isEmpty()) {
			player.sendSystemMessage(Component.translatable("command.intersolarsystem.ship.fly_not_landed"));
			return;
		}
		if (!ShipFlightRules.canTakeOff(state.condition(ShipSubsystem.ENGINE),
				state.condition(ShipSubsystem.LANDING_LAUNCH))) {
			player.sendSystemMessage(Component.translatable("command.intersolarsystem.ship.fly_too_damaged"));
			return;
		}
		Long asked = PENDING_TEST.get(player.getUUID());
		if (asked == null || now - asked < 0 || now - asked > CONFIRM_WINDOW_TICKS) {
			PENDING_TEST.put(player.getUUID(), now);
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.test_confirm"));
			return;
		}
		PENDING_TEST.remove(player.getUUID());
		ShipState.LandingSite site = state.landingSite().get();
		if (state.beginTakeoff(site.x(), site.z(), now, server.overworld().getRandom().nextLong(), ShipFlightKind.TEST)) {
			ShipFlightController.broadcast(server, Component.translatable("message.intersolarsystem.ship.test_started"));
		}
	}

	public static void reset() {
		PENDING_TEST.clear();
	}
}
