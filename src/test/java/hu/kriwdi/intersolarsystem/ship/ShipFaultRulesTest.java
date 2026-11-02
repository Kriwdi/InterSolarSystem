package hu.kriwdi.intersolarsystem.ship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class ShipFaultRulesTest {
	private static Map<ShipSubsystem, Integer> all(int value) {
		Map<ShipSubsystem, Integer> map = new EnumMap<>(ShipSubsystem.class);
		for (ShipSubsystem s : ShipSubsystem.values()) {
			map.put(s, value);
		}
		return map;
	}

	@Test
	void faultChanceIsZeroWhenHealthyAndHalfAtZero() {
		assertEquals(0.0, ShipFaultRules.faultChance(70));
		assertEquals(0.0, ShipFaultRules.faultChance(100));
		assertEquals(0.5, ShipFaultRules.faultChance(0), 1e-9);
		assertEquals(0.25, ShipFaultRules.faultChance(35), 1e-9);
	}

	@Test
	void healthyShipNeverGetsFaults() {
		for (long seed = 0; seed < 200; seed++) {
			assertTrue(ShipFaultRules.rollLandingFaults(seed, all(80), Set.of()).isEmpty());
		}
	}

	@Test
	void landingRollIsDeterministicAndSkipsExistingFaults() {
		Map<ShipSubsystem, Integer> wrecked = all(0);
		Set<ShipSubsystem> first = ShipFaultRules.rollLandingFaults(1234L, wrecked, Set.of());
		assertEquals(first, ShipFaultRules.rollLandingFaults(1234L, wrecked, Set.of()));
		Set<ShipSubsystem> everything = EnumSet.allOf(ShipSubsystem.class);
		assertTrue(ShipFaultRules.rollLandingFaults(1234L, wrecked, everything).isEmpty());
	}

	@Test
	void wreckedShipGetsFaultsAboutHalfTheTime() {
		int hits = 0;
		int total = 0;
		for (long seed = 0; seed < 2000; seed++) {
			hits += ShipFaultRules.rollLandingFaults(seed, all(0), Set.of()).size();
			total += ShipSubsystem.values().length;
		}
		double rate = (double) hits / total;
		assertTrue(rate > 0.45 && rate < 0.55, "rate " + rate);
	}

	@Test
	void perfectNavigationScanFindsEverything() {
		List<ShipLatentFault> faults = List.of(new ShipLatentFault(ShipSubsystem.ENGINE, false),
				new ShipLatentFault(ShipSubsystem.CARGO, false));
		List<ShipLatentFault> after = ShipFaultRules.scan(99L, 0, 100, faults);
		assertTrue(after.stream().allMatch(ShipLatentFault::revealed));
	}

	@Test
	void scanNeverHidesAKnownFault() {
		List<ShipLatentFault> faults = List.of(new ShipLatentFault(ShipSubsystem.ENGINE, true));
		for (int i = 0; i < 50; i++) {
			assertTrue(ShipFaultRules.scan(i, i, 0, faults).get(0).revealed());
		}
	}

	@Test
	void riskIgnoresUnknownFaultsForTheEstimateAndHalvesForTests() {
		List<ShipLatentFault> faults = List.of(new ShipLatentFault(ShipSubsystem.ENGINE, false),
				new ShipLatentFault(ShipSubsystem.LANDING_LAUNCH, true));
		Map<ShipSubsystem, Integer> healthy = all(100);
		assertEquals(0.30, ShipFaultRules.launchRisk(healthy, faults, false, false), 1e-9);
		assertEquals(0.15, ShipFaultRules.launchRisk(healthy, faults, true, false), 1e-9);
		assertEquals(0.15, ShipFaultRules.launchRisk(healthy, faults, false, true), 1e-9);
	}

	@Test
	void riskIsCappedAndZeroForAHealthyShip() {
		assertEquals(0.0, ShipFaultRules.launchRisk(all(100), List.of(), false, false));
		List<ShipLatentFault> all = List.of(new ShipLatentFault(ShipSubsystem.ENGINE, true),
				new ShipLatentFault(ShipSubsystem.FUEL_ENERGY, true),
				new ShipLatentFault(ShipSubsystem.LANDING_LAUNCH, true));
		assertEquals(ShipFaultRules.MAX_RISK, ShipFaultRules.launchRisk(all(0), all, false, false), 1e-9);
	}

	@Test
	void noRiskMeansNoDisaster() {
		for (long seed = 0; seed < 100; seed++) {
			assertFalse(ShipFaultRules.isDisaster(seed, 0.0));
		}
	}

	@Test
	void percentRoundsUp() {
		assertEquals(0, ShipFaultRules.percent(0.0));
		assertEquals(1, ShipFaultRules.percent(0.001));
		assertEquals(15, ShipFaultRules.percent(0.15));
	}
}
