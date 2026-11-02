package hu.kriwdi.intersolarsystem.ship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import hu.kriwdi.intersolarsystem.travel.CelestialBody;
import hu.kriwdi.intersolarsystem.travel.SolarSystem;
import hu.kriwdi.intersolarsystem.travel.TravelMath;

class TravelMathTest {
	private static CelestialBody body(String id) {
		return SolarSystem.byId(id).orElseThrow();
	}

	@Test
	void catalogueLoadsWithEarthAndMoon() {
		assertEquals("earth", SolarSystem.bodies().get(0).id());
		assertEquals(CelestialBody.Status.FLYBY, body("moon").status());
		assertTrue(SolarSystem.bodies().size() >= 10);
	}

	@Test
	void moonAndMarsMatchTheAgreedTimesAtTheBestWindow() {
		// Moon ~6 real minutes, Mars ~7 real hours with the first engine (1 tick = 1/20 s).
		assertEquals(3 * 2400, TravelMath.oneWayTicks(body("moon"), 1, 0));
		assertEquals(210 * 2400, TravelMath.oneWayTicks(body("mars"), 1, 0));
		assertEquals(7.0, TravelMath.oneWayTicks(body("mars"), 1, 0) / 20.0 / 3600.0, 0.01);
	}

	@Test
	void enginesDivideTheTime() {
		long one = TravelMath.oneWayTicks(body("mars"), 1, 0);
		assertEquals(Math.round(one / 3.0), TravelMath.oneWayTicks(body("mars"), 2, 0));
		assertEquals(Math.round(one / 30.0), TravelMath.oneWayTicks(body("mars"), 4, 0));
		assertEquals(30, TravelMath.multiplier(99));
	}

	@Test
	void launchWindowCostsUpToHalfMore() {
		CelestialBody mars = body("mars");
		long cycle = TravelMath.windowCycleTicks(mars);
		assertEquals(1.0, TravelMath.windowFactor(mars, 0), 1e-9);
		assertEquals(1.5, TravelMath.windowFactor(mars, cycle / 2), 1e-6);
		assertEquals(1.0, TravelMath.windowFactor(mars, cycle), 1e-9);
		assertEquals(cycle / 2, TravelMath.ticksToNextWindow(mars, cycle / 2));
		assertEquals(1.0, TravelMath.windowFactor(body("earth"), 12345), 1e-9);
	}

	@Test
	void flybyIsARoundTrip() {
		assertEquals(2 * TravelMath.oneWayTicks(body("moon"), 1, 0), TravelMath.flybyTicks(body("moon"), 1, 0));
	}

	@Test
	void durationsReadNaturally() {
		assertEquals("6:00", TravelMath.formatDuration(7200));
		assertEquals("7h 00m", TravelMath.formatDuration(504000));
		assertEquals("3d 1h", TravelMath.formatDuration(5256000));
	}
}
