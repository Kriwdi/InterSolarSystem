package hu.kriwdi.intersolarsystem.ship;

import java.util.Locale;
import java.util.Optional;

/** A real flight to a destination, or a short test hop that lifts off and lands back on the same spot. */
public enum ShipFlightKind {
	NORMAL,
	TEST;

	/** Test hop: 10 s takeoff, 15 s hover, 5 s landing. */
	private static final int TEST_FLIGHT_TICKS = 300;

	public int durationTicks(ShipFlightPhase phase) {
		if (this == TEST && phase == ShipFlightPhase.FLIGHT) {
			return TEST_FLIGHT_TICKS;
		}
		return phase.durationTicks();
	}

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Optional<ShipFlightKind> fromId(String id) {
		for (ShipFlightKind k : values()) {
			if (k.id().equals(id)) {
				return Optional.of(k);
			}
		}
		return Optional.empty();
	}
}
