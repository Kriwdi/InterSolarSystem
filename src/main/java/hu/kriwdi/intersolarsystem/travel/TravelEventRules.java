package hu.kriwdi.intersolarsystem.travel;

import java.util.Optional;
import java.util.SplittableRandom;

/** Pure rules for travel events: when they roll, which one, and what a missed one costs. */
public final class TravelEventRules {
	/** Only flights at least this long have travel events (a Moon loop qualifies, an Earth hop does not). */
	public static final long MIN_FLIGHT_TICKS = 2400;
	/** One roll per minute of flight. */
	public static final int ROLL_INTERVAL_TICKS = 1200;
	public static final double CHANCE = 0.35;
	/** Time to respond to an event that needs it. */
	public static final int RESPONSE_TICKS = 600;
	/** Number of lore lines available for SIGNAL events (lang keys ...signal.0 .. signal.N-1). */
	public static final int SIGNAL_LINES = 8;

	private static final long SALT = 0x632BE59BD9B4E019L;
	private static final long STEP = 2 * 0x9E3779B97F4A7C15L;
	private static final TravelEventType[] TYPES = TravelEventType.values();
	private static final int[] WEIGHTS = {3, 2, 2, 2, 2};

	private TravelEventRules() {
	}

	public static int rollsDue(long flightTicks, long elapsed) {
		if (flightTicks < MIN_FLIGHT_TICKS || elapsed <= 0) {
			return 0;
		}
		// No roll in the last minute, so a response window never runs past the landing.
		long maxRolls = Math.max(0, (flightTicks - ROLL_INTERVAL_TICKS) / ROLL_INTERVAL_TICKS);
		return (int) Math.min(maxRolls, elapsed / ROLL_INTERVAL_TICKS);
	}

	public static Optional<TravelEventType> roll(long seed, int index) {
		SplittableRandom random = new SplittableRandom((seed ^ SALT) + index * STEP);
		if (random.nextDouble() >= CHANCE) {
			return Optional.empty();
		}
		int total = 0;
		for (int w : WEIGHTS) {
			total += w;
		}
		int pick = random.nextInt(total);
		for (int i = 0; i < TYPES.length; i++) {
			pick -= WEIGHTS[i];
			if (pick < 0) {
				return Optional.of(TYPES[i]);
			}
		}
		return Optional.of(TYPES[TYPES.length - 1]);
	}

	/** Which lore line a signal event shows (deterministic per roll). */
	public static int signalLine(long seed, int index) {
		return (int) Math.floorMod((seed ^ SALT) * 31 + index, (long) SIGNAL_LINES);
	}
}
