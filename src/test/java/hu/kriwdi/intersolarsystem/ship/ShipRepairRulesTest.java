package hu.kriwdi.intersolarsystem.ship;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ShipRepairRulesTest {
	private static Map<ShipSubsystem, Integer> all(int value) {
		Map<ShipSubsystem, Integer> map = new EnumMap<>(ShipSubsystem.class);
		for (ShipSubsystem s : ShipSubsystem.values()) {
			map.put(s, value);
		}
		return map;
	}

	@Test
	void earliestIncidentComesFirst() {
		ShipIncident late = new ShipIncident(ShipSubsystem.ENGINE, ShipIncidentType.OVERHEAT, 500);
		ShipIncident early = new ShipIncident(ShipSubsystem.CARGO, ShipIncidentType.FIRE, 100);
		ShipRepairRules.Choice choice = ShipRepairRules.choose(List.of(late, early), all(10));
		assertEquals(ShipRepairRules.Kind.INCIDENT, choice.kind());
		assertEquals(Optional.of(early), choice.incident());
	}

	@Test
	void worstSubsystemWithoutIncidents() {
		Map<ShipSubsystem, Integer> conditions = all(80);
		conditions.put(ShipSubsystem.STABILIZER, 30);
		ShipRepairRules.Choice choice = ShipRepairRules.choose(List.of(), conditions);
		assertEquals(Optional.of(ShipSubsystem.STABILIZER), choice.subsystem());
	}

	@Test
	void nothingWhenAllFull() {
		assertEquals(ShipRepairRules.Kind.NOTHING, ShipRepairRules.choose(List.of(), all(100)).kind());
	}
}
