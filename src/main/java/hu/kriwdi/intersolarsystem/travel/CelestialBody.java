package hu.kriwdi.intersolarsystem.travel;

import java.util.Locale;
import java.util.Optional;

/**
 * One destination of the navigation console, read from {@code data/intersolarsystem/solar_system.json}.
 *
 * @param transferDays real-world one-way transfer time from Earth in days
 * @param synodicDays  real-world days between two launch windows from Earth (0: always open)
 * @param assist       a body whose gravity can be used on the way, if any
 * @param minTier      the engine tier needed to go there (1: any; beyond the Moon: the mothership's tier)
 * @param viaMothership beyond the Earth-Moon system: only the mothership goes there; the small ship lands from it
 */
public record CelestialBody(String id, Kind kind, Optional<String> parent, double transferDays, double synodicDays,
		Status status, Optional<String> assist, int minTier, boolean viaMothership) {
	public enum Kind {
		PLANET, ORBIT, MOON, DWARF_PLANET, GAS_GIANT, ICE_GIANT, BELT;

		static Kind of(String id) {
			return valueOf(id.toUpperCase(Locale.ROOT));
		}
	}

	/**
	 * SURFACE: Earth's surface (back down from orbit). ORBIT: dock in orbit. FLYBY: a free-return loop. LOCKED: shown,
	 * not reachable yet. LANDING: the body has its own world to land on. DOCK: the mothership's hangar.
	 */
	public enum Status {
		SURFACE, ORBIT, FLYBY, LANDING, DOCK, LOCKED;

		static Status of(String id) {
			return valueOf(id.toUpperCase(Locale.ROOT));
		}
	}

	/** The mothership's parking orbit for this body ("mars_orbit"; a moon of a far planet: its planet's). */
	public String station() {
		String around = parent.filter(p -> !p.equals("earth")).orElse(id);
		return around + "_orbit";
	}

	public String translationKey() {
		return "body.intersolarsystem." + id;
	}
}

