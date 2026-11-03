package hu.kriwdi.intersolarsystem.core;

import java.util.Locale;
import java.util.Optional;

/**
 * How hard the ship is to keep alive. The crew size adjusts it further (see {@link Crew}): alone, more runs by
 * itself; with more people aboard, more goes wrong at once and the work is shared.
 */
public enum Difficulty {
	/** Switches and the autopilot take care of themselves, fewer incidents, supplies last twice as long. */
	EASY(0.6, -1, 0.5, true),
	NORMAL(1.0, 0, 1.0, false),
	/** More incidents, more at once, denser meteor showers, harder hits. */
	REALISTIC(1.5, 1, 1.4, false);

	private final double incidentRate;
	private final int extraParallel;
	private final double damage;
	private final boolean assisted;

	Difficulty(double incidentRate, int extraParallel, double damage, boolean assisted) {
		this.incidentRate = incidentRate;
		this.extraParallel = extraParallel;
		this.damage = damage;
		this.assisted = assisted;
	}

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Optional<Difficulty> fromId(String id) {
		for (Difficulty d : values()) {
			if (d.id().equals(id)) {
				return Optional.of(d);
			}
		}
		return Optional.empty();
	}

	/** Multiplier on the chance that a flight's incident roll fails something. */
	public double incidentRate() {
		return incidentRate;
	}

	public int extraParallel() {
		return extraParallel;
	}

	/** Multiplier on meteor and failed-repair damage. */
	public double damage() {
		return damage;
	}

	/** Whether the ship helps: switches flip themselves on, the autopilot dodges by itself, supplies last longer. */
	public boolean assisted() {
		return assisted;
	}

	/** The crew aboard and the difficulty together. */
	public record Crew(Difficulty difficulty, int size) {
		/** Incidents that may be active at once: one per crew member plus one, adjusted by difficulty (1..4). */
		public int maxParallel() {
			return Math.max(1, Math.min(4, 1 + Math.max(1, size) + difficulty.extraParallel()));
		}

		/** Chance multiplier: every extra crew member adds a third (more hands, more to go wrong). */
		public double incidentRate() {
			return difficulty.incidentRate() * (1.0 + 0.35 * Math.max(0, size - 1));
		}

		/** Alone, the ship's autopilot dodges meteors when nobody flies (not on realistic). */
		public boolean autopilotByDefault() {
			return difficulty.assisted() || size <= 1 && difficulty != REALISTIC;
		}
	}
}
