package hu.kriwdi.intersolarsystem.travel;

import java.util.Locale;
import java.util.Optional;

/**
 * One destination of the navigation console, read from {@code data/intersolarsystem/solar_system.json}.
 *
 * @param transferDays real-world one-way transfer time from Earth in days
 * @param synodicDays  real-world days between two launch windows from Earth (0: always open)
 * @param assist       a body whose gravity can be used on the way, if any
 */
public record CelestialBody(String id, Kind kind, Optional<String> parent, double transferDays, double synodicDays,
		Status status, Optional<String> assist) {
	public enum Kind {
		PLANET, MOON, DWARF_PLANET, GAS_GIANT, ICE_GIANT;

		static Kind of(String id) {
			return valueOf(id.toUpperCase(Locale.ROOT));
		}
	}

	/** SURFACE: Earth, fly to coordinates. FLYBY: reachable on a free-return loop. LOCKED: shown, not reachable yet. */
	public enum Status {
		SURFACE, FLYBY, LOCKED;

		static Status of(String id) {
			return valueOf(id.toUpperCase(Locale.ROOT));
		}
	}

	public String translationKey() {
		return "body.intersolarsystem." + id;
	}
}
