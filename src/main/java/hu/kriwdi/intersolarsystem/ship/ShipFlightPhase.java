package hu.kriwdi.intersolarsystem.ship;

import java.util.Locale;
import java.util.Optional;

public enum ShipFlightPhase {
	LANDED(0),
	TAKEOFF(200),
	FLIGHT(1200),
	LANDING(100);

	private final int durationTicks;

	ShipFlightPhase(int durationTicks) {
		this.durationTicks = durationTicks;
	}

	public int durationTicks() {
		return durationTicks;
	}

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Optional<ShipFlightPhase> fromId(String id) {
		for (ShipFlightPhase p : values()) {
			if (p.id().equals(id)) {
				return Optional.of(p);
			}
		}
		return Optional.empty();
	}
}
