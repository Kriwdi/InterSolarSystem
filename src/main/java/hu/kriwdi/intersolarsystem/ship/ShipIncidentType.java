package hu.kriwdi.intersolarsystem.ship;

import java.util.Locale;
import java.util.Optional;

public enum ShipIncidentType {
	LEAK,
	OVERHEAT,
	SHORT_CIRCUIT,
	FIRE,
	PRESSURE_LOSS;

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Optional<ShipIncidentType> fromId(String id) {
		for (ShipIncidentType t : values()) {
			if (t.id().equals(id)) {
				return Optional.of(t);
			}
		}
		return Optional.empty();
	}
}
