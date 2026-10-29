package hu.kriwdi.intersolarsystem.ship;

public final class ShipFlightRules {
	/** Below this condition the engine or the landing/launch unit cannot start a flight. */
	public static final int MIN_TAKEOFF_CONDITION = 20;

	private ShipFlightRules() {
	}

	public static boolean canTakeOff(int engine, int landingLaunch) {
		return engine >= MIN_TAKEOFF_CONDITION && landingLaunch >= MIN_TAKEOFF_CONDITION;
	}
}
