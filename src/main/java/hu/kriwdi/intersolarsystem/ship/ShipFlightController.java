package hu.kriwdi.intersolarsystem.ship;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Cell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Drives the flight state machine and the incidents from the server tick. All timing uses the Overworld game time. */
public final class ShipFlightController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModIds.MOD_ID);

	private ShipFlightController() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(ShipFlightController::tick);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ShipStatusSync.reset();
			ShipDiagnosticScan.reset();
			ShipWiringRepair.reset();
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			ShipStatusSync.reset();
			ShipDiagnosticScan.reset();
			ShipWiringRepair.reset();
		});
	}

	private static void tick(MinecraftServer server) {
		ShipState state = ShipStateAccess.get(server);
		long now = server.overworld().getGameTime();
		advance(server, state, now);
		ShipStatusSync.tick(server, state, now);
	}

	private static void advance(MinecraftServer server, ShipState state, long now) {
		tickIncidents(server, state, now);
		ShipFlightPhase phase = state.phase();
		if (phase == ShipFlightPhase.LANDED) {
			return;
		}
		if (state.rebasePhaseStartIfInFuture(now)) {
			LOGGER.warn("Ship flight phase '{}' started in the future; restarting its timer from now", phase.id());
		}
		if (phase == ShipFlightPhase.FLIGHT) {
			rollIncidents(server, state, now);
		}
		Optional<ShipFlightPhase> due = ShipFlightTimeline.dueTransition(state.flightKind(), phase,
				state.phaseStartedTick(), now);
		if (due.isPresent()) {
			transition(server, state, due.get(), now);
		}
	}

	/** Expires incidents whose deadline passed (in any phase). */
	private static void tickIncidents(MinecraftServer server, ShipState state, long now) {
		if (state.hazards().incidents().isEmpty()) {
			return;
		}
		if (state.clampIncidentDeadlines(now)) {
			LOGGER.warn("Ship incident deadline was too far in the future; pulled it in");
		}
		for (ShipIncident expired : state.expireIncidents(now)) {
			broadcast(server, Component.translatable("message.intersolarsystem.ship.incident_expired",
					Component.translatable(ShipCommands.incidentKey(expired.type())),
					Component.translatable(ShipCommands.subsystemKey(expired.subsystem()))));
			playInterior(server, SoundEvents.ANVIL_LAND);
		}
	}

	/** Runs the flight's due rolls. The saved seed and roll counter make the outcome independent of reloads. */
	private static void rollIncidents(MinecraftServer server, ShipState state, long now) {
		int due = ShipIncidentRules.rollsDue(now - state.phaseStartedTick());
		int done = state.hazards().rollsDone();
		if (due <= done) {
			return;
		}
		for (int index = done + 1; index <= due; index++) {
			Set<ShipSubsystem> active = EnumSet.noneOf(ShipSubsystem.class);
			state.hazards().incidents().forEach(i -> active.add(i.subsystem()));
			Map<ShipSubsystem, Integer> conditions = new EnumMap<>(ShipSubsystem.class);
			for (ShipSubsystem s : ShipSubsystem.values()) {
				conditions.put(s, state.condition(s));
			}
			Optional<ShipSubsystem> hit = ShipIncidentRules.roll(state.hazards().seed(), index, conditions, active);
			if (hit.isPresent()) {
				ShipIncident incident = new ShipIncident(hit.get(), ShipIncidentRules.typeFor(hit.get()),
						now + ShipIncidentRules.INCIDENT_DEADLINE_TICKS);
				if (state.addIncident(incident)) {
					broadcast(server, Component.translatable("message.intersolarsystem.ship.incident",
							Component.translatable(ShipCommands.incidentKey(incident.type())),
							Component.translatable(ShipCommands.subsystemKey(incident.subsystem()))));
					playInterior(server, SoundEvents.BEACON_DEACTIVATE);
				}
			}
		}
		state.setRollsDone(due);
	}

	/**
	 * The critical world change runs first and the phase advances right after it, so a transition is never done twice
	 * in normal operation (an exception in a server tick is not retried: it would crash the server). Sounds and
	 * messages come last because losing them costs nothing.
	 */
	private static void transition(MinecraftServer server, ShipState state, ShipFlightPhase to, long now) {
		ShipFlightKind kind = state.flightKind();
		switch (to) {
			case FLIGHT -> {
				double risk = ShipFaultRules.launchRisk(ShipRepair.conditions(state), state.diagnostics().faults(), false,
						kind == ShipFlightKind.TEST);
				if (ShipFaultRules.isDisaster(state.hazards().seed(), risk)) {
					ShipDisaster.failTakeoff(server, state, now);
					return;
				}
				removeHatch(server, state);
				state.applyWear(ShipWear.forTransition(to, kind));
				state.advancePhase(now);
				playInterior(server, SoundEvents.FIREWORK_ROCKET_LAUNCH);
				if (kind == ShipFlightKind.TEST) {
					surfaceFaults(server, state, now);
				}
			}
			case LANDING -> {
				state.applyWear(ShipWear.forTransition(to, kind));
				state.advancePhase(now);
				playInterior(server, SoundEvents.BEACON_ACTIVATE);
			}
			case LANDED -> {
				ShipLanding.Landing landing = ShipLanding.place(server, state);
				state.applyWear(ShipWear.forTransition(to, kind));
				state.addFaults(ShipFaultRules.rollLandingFaults(state.hazards().seed(), ShipRepair.conditions(state),
						faultySubsystems(state)));
				state.advancePhase(now);
				ShipLanding.announce(server, landing);
				playInterior(server, SoundEvents.ANVIL_LAND);
			}
			default -> state.advancePhase(now);
		}
	}

	private static Set<ShipSubsystem> faultySubsystems(ShipState state) {
		Set<ShipSubsystem> set = EnumSet.noneOf(ShipSubsystem.class);
		state.diagnostics().faults().forEach(f -> set.add(f.subsystem()));
		return set;
	}

	/**
	 * A test hop shakes every hidden fault loose: all become known, and as many as the incident limit allows break out
	 * as incidents right away (worst subsystem first), so the crew can see and fix them while still near home.
	 */
	private static void surfaceFaults(MinecraftServer server, ShipState state, long now) {
		java.util.List<ShipLatentFault> fresh = state.revealAllFaults();
		java.util.List<ShipLatentFault> all = state.diagnostics().faults().stream()
				.sorted(java.util.Comparator.comparingInt((ShipLatentFault f) -> state.condition(f.subsystem()))).toList();
		for (ShipLatentFault fault : all) {
			ShipIncident incident = new ShipIncident(fault.subsystem(), ShipIncidentRules.typeFor(fault.subsystem()),
					now + ShipIncidentRules.INCIDENT_DEADLINE_TICKS);
			if (state.addIncident(incident)) {
				broadcast(server, Component.translatable("message.intersolarsystem.ship.incident",
						Component.translatable(ShipCommands.incidentKey(incident.type())),
						Component.translatable(ShipCommands.subsystemKey(incident.subsystem()))));
			}
		}
		broadcast(server, all.isEmpty()
				? Component.translatable("message.intersolarsystem.ship.test_clean")
				: Component.translatable("message.intersolarsystem.ship.test_found", all.size(), fresh.size()));
		if (!all.isEmpty()) {
			playInterior(server, SoundEvents.BEACON_DEACTIVATE);
		}
	}

	/** The hatch leaves with the ship. Only the ship's own hatch block is removed, never anything else. */
	private static void removeHatch(MinecraftServer server, ShipState state) {
		state.landingSite().ifPresent(site -> {
			ServerLevel level = ShipBoarding.levelOf(server, site);
			BlockPos pos = new BlockPos(site.x(), site.y(), site.z());
			if (level != null && level.getBlockState(pos).getBlock() == ModBlocks.SHIP_HATCH) {
				level.removeBlock(pos, false);
			}
		});
	}

	static void broadcast(MinecraftServer server, Component message) {
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior != null) {
			interior.players().forEach(p -> p.sendSystemMessage(message));
		}
	}

	static void playInterior(MinecraftServer server, SoundEvent sound) {
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior == null) {
			return;
		}
		Cell entry = ShipInteriorLayout.capsule().entry();
		BlockPos at = ShipDimensions.ANCHOR.offset(entry.dx(), entry.dy(), entry.dz());
		interior.playSound(null, at, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
	}
}
