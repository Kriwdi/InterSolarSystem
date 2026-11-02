package hu.kriwdi.intersolarsystem.travel;

import java.util.Locale;
import java.util.Optional;

/** Things that happen on a long flight. Some wait for the crew's response before their deadline. */
public enum TravelEventType {
	/** Debris ahead: evade at the navigation console before the deadline, or the hull takes the hits. */
	METEOR_SHOWER(true),
	/** A burst of radiation: electronics and life support take damage. */
	SOLAR_FLARE(false),
	/** Something breaks: an incident on a worn subsystem. */
	SYSTEM_FAILURE(false),
	/** A drifting wreck within reach: salvage drifts into the ship. */
	DRIFTING_WRECK(false),
	/** A strange signal: a fragment of what happened to the others. */
	SIGNAL(false);

	private final boolean needsResponse;

	TravelEventType(boolean needsResponse) {
		this.needsResponse = needsResponse;
	}

	public boolean needsResponse() {
		return needsResponse;
	}

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Optional<TravelEventType> fromId(String id) {
		for (TravelEventType t : values()) {
			if (t.id().equals(id)) {
				return Optional.of(t);
			}
		}
		return Optional.empty();
	}
}
