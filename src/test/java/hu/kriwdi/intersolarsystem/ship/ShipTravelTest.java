package hu.kriwdi.intersolarsystem.ship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.Test;

import hu.kriwdi.intersolarsystem.travel.TravelEventRules;
import hu.kriwdi.intersolarsystem.travel.TravelEventType;

class ShipTravelTest {
	private static ShipState roundTrip(ShipState state) {
		JsonElement json = ShipState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
		return ShipState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
	}

	@Test
	void flightPlanDecidesTheFlightPhaseLength() {
		ShipState state = new ShipState();
		state.beginTakeoff(0, 0, 0L, 1L, ShipFlightKind.NORMAL, "moon", 14400L);
		assertEquals(14400L, state.phaseDuration(ShipFlightPhase.FLIGHT));
		assertEquals(ShipFlightPhase.TAKEOFF.durationTicks(), state.phaseDuration(ShipFlightPhase.TAKEOFF));
		ShipState plain = new ShipState();
		plain.beginTakeoff(0, 0, 0L, 1L);
		assertEquals(ShipFlightPhase.FLIGHT.durationTicks(), plain.phaseDuration(ShipFlightPhase.FLIGHT));
	}

	@Test
	void travelStateSurvivesSavingAndLandingClearsThePlan() {
		ShipState state = new ShipState();
		state.setEngineTier(3);
		state.beginTakeoff(0, 0, 0L, 1L, ShipFlightKind.NORMAL, "moon", 2400L);
		state.setTravelEvent(Optional.of(new ShipTravel.ActiveEvent(TravelEventType.METEOR_SHOWER, 900L, true)));
		state.setTravelEventsDone(4);
		ShipState loaded = roundTrip(state);
		assertEquals(state.travel(), loaded.travel());
		assertEquals(6, loaded.formatVersion());
		for (int i = 0; i < 3; i++) {
			loaded.advancePhase(i);
		}
		assertEquals(ShipFlightPhase.LANDED, loaded.phase());
		assertEquals(new ShipTravel(3, "earth", 0, 0, Optional.empty(), ShipTravel.OVERWORLD, Optional.empty()),
				loaded.travel());
	}

	@Test
	void version5SaveGetsDefaultTravel() {
		JsonElement json = JsonParser.parseString("""
				{"format_version": 5, "phase": "landed"}""");
		ShipState state = ShipState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
		assertEquals(ShipTravel.NONE, state.travel());
	}

	@Test
	void unknownEventTypeIsDropped() {
		JsonElement json = JsonParser.parseString("""
				{"format_version": 6, "travel": {"engine_tier": 2, "event": {"type": "alien_invasion", "deadline_tick": 5}}}""");
		ShipState state = ShipState.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
		assertEquals(2, state.travel().engineTier());
		assertEquals(Optional.empty(), state.travel().event());
	}

	@Test
	void eventsOnlyOnLongFlightsAndNeverInTheLastMinute() {
		assertEquals(0, TravelEventRules.rollsDue(1200, 1200));
		long moonLoop = 14400;
		assertEquals(0, TravelEventRules.rollsDue(moonLoop, 1199));
		assertEquals(1, TravelEventRules.rollsDue(moonLoop, 1200));
		assertEquals(11, TravelEventRules.rollsDue(moonLoop, moonLoop));
	}

	@Test
	void eventRollsAreDeterministicAndRoughlyAtTheirRate() {
		Map<TravelEventType, Integer> seen = new EnumMap<>(TravelEventType.class);
		int hits = 0;
		for (int i = 1; i <= 5000; i++) {
			Optional<TravelEventType> a = TravelEventRules.roll(42L, i);
			assertEquals(a, TravelEventRules.roll(42L, i));
			if (a.isPresent()) {
				hits++;
				seen.merge(a.get(), 1, Integer::sum);
			}
		}
		double rate = hits / 5000.0;
		assertTrue(rate > 0.3 && rate < 0.4, "rate " + rate);
		assertEquals(TravelEventType.values().length, seen.size());
		int line = TravelEventRules.signalLine(42L, 3);
		assertTrue(line >= 0 && line < TravelEventRules.SIGNAL_LINES);
	}

	@Test
	void longFlightsRollIncidentsOncePerMinute() {
		assertEquals(5, ShipIncidentRules.rollsDue(1200, 1200));
		assertEquals(2, ShipIncidentRules.rollsDue(2400, 14400));
		assertEquals(12, ShipIncidentRules.rollsDue(99999, 14400));
	}
}
