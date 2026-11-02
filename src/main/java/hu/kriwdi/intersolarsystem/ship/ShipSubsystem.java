package hu.kriwdi.intersolarsystem.ship;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public enum ShipSubsystem {
	ENGINE,
	FUEL_ENERGY,
	HULL_ARMOR,
	LIFE_SUPPORT,
	NAVIGATION_COMMS,
	STABILIZER,
	LANDING_LAUNCH,
	CARGO;

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Optional<ShipSubsystem> fromId(String id) {
		for (ShipSubsystem s : values()) {
			if (s.id().equals(id)) {
				return Optional.of(s);
			}
		}
		return Optional.empty();
	}

	public static List<String> ids() {
		return Arrays.stream(values()).map(ShipSubsystem::id).toList();
	}
}
