package hu.kriwdi.intersolarsystem.ship;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import org.junit.jupiter.api.Test;

class ShipHazardsCodecTest {
	private static ShipHazards decode(String json) {
		JsonElement element = JsonParser.parseString(json);
		return ShipHazards.CODEC.parse(JsonOps.INSTANCE, element).getOrThrow();
	}

	@Test
	void roundTrip() {
		ShipHazards hazards = new ShipHazards(42L, 3, List.of(
				new ShipIncident(ShipSubsystem.ENGINE, ShipIncidentType.OVERHEAT, 1000L),
				new ShipIncident(ShipSubsystem.CARGO, ShipIncidentType.FIRE, 2000L)));
		JsonElement json = ShipHazards.CODEC.encodeStart(JsonOps.INSTANCE, hazards).getOrThrow();
		assertEquals(hazards, ShipHazards.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
	}

	@Test
	void oneBrokenIncidentKeepsTheOthers() {
		ShipHazards hazards = decode("""
				{"seed": 7, "rolls_done": 2, "incidents": [
				  {"subsystem": "engine", "type": "overheat", "deadline_tick": 100},
				  {"subsystem": "cargo", "type": "fire"},
				  {"subsystem": "stabilizer", "type": "short_circuit", "deadline_tick": 300}
				]}""");
		assertEquals(List.of(new ShipIncident(ShipSubsystem.ENGINE, ShipIncidentType.OVERHEAT, 100L),
				new ShipIncident(ShipSubsystem.STABILIZER, ShipIncidentType.SHORT_CIRCUIT, 300L)), hazards.incidents());
		assertEquals(7L, hazards.seed());
	}

	@Test
	void unknownSubsystemIsDropped() {
		ShipHazards hazards = decode("""
				{"incidents": [{"subsystem": "warp_core", "type": "fire", "deadline_tick": 1}]}""");
		assertEquals(List.of(), hazards.incidents());
	}
}
