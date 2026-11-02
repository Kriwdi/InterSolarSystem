package hu.kriwdi.intersolarsystem.ship;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SplittableRandom;

/** Pure rules for when and where incidents happen. All numbers live here so balancing is one-stop. */
public final class ShipIncidentRules {
	public static final int FLIGHT_ROLL_INTERVAL_TICKS = 200;
	public static final int ROLLS_PER_FLIGHT = 5;
	public static final int INCIDENT_DEADLINE_TICKS = 1200;
	public static final int EXPIRY_DAMAGE = 25;
	public static final int MAX_ACTIVE = 2;
	public static final int SAFE_CONDITION = 70;
	public static final double MAX_CHANCE = 0.25;

	// SplittableRandom advances its seed by this "golden gamma" on every draw and a roll uses two draws, so rolls must be
	// two gammas apart: with a single gamma, roll i's second draw would equal roll i+1's first draw.
	private static final long ROLL_SEED_STEP = 2 * 0x9E3779B97F4A7C15L;

	private ShipIncidentRules() {
	}

	/** Chance that this subsystem alone causes an incident on a roll: 0 at 70% and above, 0.25 at 0%. */
	public static double failureChance(int condition) {
		int clamped = ShipState.clamp(condition);
		if (clamped >= SAFE_CONDITION) {
			return 0.0;
		}
		return MAX_CHANCE * (SAFE_CONDITION - clamped) / SAFE_CONDITION;
	}

	public static ShipIncidentType typeFor(ShipSubsystem subsystem) {
		return switch (subsystem) {
			case ENGINE -> ShipIncidentType.OVERHEAT;
			case FUEL_ENERGY -> ShipIncidentType.LEAK;
			case HULL_ARMOR, LIFE_SUPPORT -> ShipIncidentType.PRESSURE_LOSS;
			case NAVIGATION_COMMS, STABILIZER -> ShipIncidentType.SHORT_CIRCUIT;
			case LANDING_LAUNCH, CARGO -> ShipIncidentType.FIRE;
		};
	}

	/** On flights longer than this, incidents roll once a minute instead of every ten seconds. */
	public static final int LONG_FLIGHT_TICKS = 1200;
	public static final int LONG_FLIGHT_ROLL_INTERVAL_TICKS = 1200;

	/** Rolls due on a flight of the given length: every 10 s on short hops, once a minute on long journeys. */
	public static int rollsDue(long elapsedTicks, long flightTicks) {
		if (flightTicks <= LONG_FLIGHT_TICKS) {
			return rollsDue(elapsedTicks);
		}
		if (elapsedTicks <= 0) {
			return 0;
		}
		long max = flightTicks / LONG_FLIGHT_ROLL_INTERVAL_TICKS;
		return (int) Math.min(max, elapsedTicks / LONG_FLIGHT_ROLL_INTERVAL_TICKS);
	}

	/** How many of the flight's rolls are due after {@code elapsedTicks} of the flight phase. */
	public static int rollsDue(long elapsedTicks) {
		if (elapsedTicks <= 0) {
			return 0;
		}
		return (int) Math.min(ROLLS_PER_FLIGHT, elapsedTicks / FLIGHT_ROLL_INTERVAL_TICKS);
	}

	/**
	 * One deterministic roll. First decides whether anything fails (the combined chance of all inactive subsystems,
	 * capped at {@link #MAX_CHANCE}), then picks the subsystem weighted by its own chance.
	 */
	public static Optional<ShipSubsystem> roll(long seed, int rollIndex, Map<ShipSubsystem, Integer> conditions,
			Set<ShipSubsystem> activeSubsystems) {
		if (activeSubsystems.size() >= MAX_ACTIVE) {
			return Optional.empty();
		}
		double total = 0.0;
		for (ShipSubsystem s : ShipSubsystem.values()) {
			if (!activeSubsystems.contains(s)) {
				total += failureChance(conditions.getOrDefault(s, ShipState.MAX_CONDITION));
			}
		}
		if (total <= 0.0) {
			return Optional.empty();
		}
		SplittableRandom random = new SplittableRandom(seed + rollIndex * ROLL_SEED_STEP);
		if (random.nextDouble() >= Math.min(MAX_CHANCE, total)) {
			return Optional.empty();
		}
		double pick = random.nextDouble() * total;
		double cumulative = 0.0;
		ShipSubsystem last = null;
		for (ShipSubsystem s : ShipSubsystem.values()) {
			if (activeSubsystems.contains(s)) {
				continue;
			}
			double chance = failureChance(conditions.getOrDefault(s, ShipState.MAX_CONDITION));
			if (chance <= 0.0) {
				continue;
			}
			cumulative += chance;
			last = s;
			if (pick < cumulative) {
				return Optional.of(s);
			}
		}
		return Optional.ofNullable(last);
	}
}
