package hu.kriwdi.intersolarsystem.ship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.Test;

class ShipStateFaultsTest {
	private static ShipState roundTrip(ShipState state) {
		JsonElement json = ShipState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
		return ShipState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
	}

	@Test
	void faultsFlightKindAndScansSurviveSaving() {
		ShipState state = new ShipState();
		state.addFaults(EnumSet.of(ShipSubsystem.ENGINE, ShipSubsystem.CARGO));
		state.recordScan(List.of(new ShipLatentFault(ShipSubsystem.ENGINE, true),
				new ShipLatentFault(ShipSubsystem.CARGO, false)), 500L);
		state.beginTakeoff(1, 2, 600L, 77L, ShipFlightKind.TEST);
		ShipState loaded = roundTrip(state);
		assertEquals(state.diagnostics(), loaded.diagnostics());
		assertEquals(ShipFlightKind.TEST, loaded.flightKind());
		assertEquals(ShipState.CURRENT_FORMAT_VERSION, loaded.formatVersion());
	}

	@Test
	void version4SaveLoadsWithNoFaults() {
		JsonElement json = JsonParser.parseString("""
				{"format_version": 4, "hull_class": "rescue_capsule", "subsystems": {"engine": 40}, "phase": "landed"}""");
		ShipState state = ShipState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
		assertEquals(ShipDiagnostics.NONE, state.diagnostics());
		assertEquals(ShipFlightKind.NORMAL, state.flightKind());
		assertEquals(40, state.condition(ShipSubsystem.ENGINE));
		assertFalse(state.isReadOnly());
	}

	@Test
	void fixingAnIncidentAlsoFixesTheFaultBehindIt() {
		ShipState state = new ShipState();
		state.addFaults(EnumSet.of(ShipSubsystem.ENGINE));
		ShipIncident incident = new ShipIncident(ShipSubsystem.ENGINE, ShipIncidentType.OVERHEAT, 100L);
		state.addIncident(incident);
		assertTrue(state.resolveIncident(incident));
		assertFalse(state.diagnostics().hasFault(ShipSubsystem.ENGINE));
	}

	@Test
	void revealAllReturnsOnlyNewOnes() {
		ShipState state = new ShipState();
		state.addFaults(EnumSet.of(ShipSubsystem.ENGINE, ShipSubsystem.STABILIZER));
		state.recordScan(List.of(new ShipLatentFault(ShipSubsystem.ENGINE, true),
				new ShipLatentFault(ShipSubsystem.STABILIZER, false)), 0L);
		List<ShipLatentFault> fresh = state.revealAllFaults();
		assertEquals(List.of(new ShipLatentFault(ShipSubsystem.STABILIZER, false)), fresh);
		assertEquals(2, state.diagnostics().revealed().size());
	}

	@Test
	void abortedTakeoffIsLandedWithoutDestination() {
		ShipState state = new ShipState();
		state.beginTakeoff(10, 20, 0L, 1L, ShipFlightKind.TEST);
		state.abortTakeoff(50L);
		assertEquals(ShipFlightPhase.LANDED, state.phase());
		assertEquals(Optional.empty(), state.destination());
		assertEquals(ShipFlightKind.NORMAL, state.flightKind());
	}

	@Test
	void testHopIsShorterAndWearsLess() {
		assertEquals(300, ShipFlightKind.TEST.durationTicks(ShipFlightPhase.FLIGHT));
		assertEquals(ShipFlightPhase.FLIGHT.durationTicks(), ShipFlightKind.NORMAL.durationTicks(ShipFlightPhase.FLIGHT));
		Map<ShipSubsystem, Integer> normal = ShipWear.forTransition(ShipFlightPhase.LANDING, ShipFlightKind.NORMAL);
		Map<ShipSubsystem, Integer> test = ShipWear.forTransition(ShipFlightPhase.LANDING, ShipFlightKind.TEST);
		assertEquals(3, test.get(ShipSubsystem.FUEL_ENERGY));
		assertEquals(6, normal.get(ShipSubsystem.FUEL_ENERGY));
	}

	@Test
	void knownFaultComesBeforeWearAtTheStation() {
		ShipRepairRules.Choice choice = ShipRepairRules.choose(List.of(),
				List.of(new ShipLatentFault(ShipSubsystem.CARGO, true), new ShipLatentFault(ShipSubsystem.ENGINE, false)),
				Map.of(ShipSubsystem.STABILIZER, 10));
		assertEquals(ShipRepairRules.Kind.FAULT, choice.kind());
		assertEquals(Optional.of(ShipSubsystem.CARGO), choice.subsystem());
	}
}
