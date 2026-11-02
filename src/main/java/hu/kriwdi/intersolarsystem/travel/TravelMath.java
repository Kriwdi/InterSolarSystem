package hu.kriwdi.intersolarsystem.travel;

/**
 * Converts real travel times into game time. One real day is 2400 ticks (the 10x speed-up: 2.4 Minecraft hours, two
 * real minutes); the engine tier divides it; launching outside the launch window costs up to 50% more.
 */
public final class TravelMath {
	public static final int REAL_DAY_TICKS = 2400;
	public static final int[] ENGINE_MULTIPLIERS = {1, 3, 10, 30};
	public static final double WORST_WINDOW_FACTOR = 1.5;

	private TravelMath() {
	}

	public static int clampTier(int tier) {
		return Math.max(1, Math.min(ENGINE_MULTIPLIERS.length, tier));
	}

	public static int multiplier(int engineTier) {
		return ENGINE_MULTIPLIERS[clampTier(engineTier) - 1];
	}

	/** Ticks a launch-window cycle lasts in game (synodic period / 10). */
	public static long windowCycleTicks(CelestialBody body) {
		return Math.round(body.synodicDays() * REAL_DAY_TICKS);
	}

	/**
	 * 1.0 at the best moment of the window cycle, 1.5 at the worst, a smooth cosine between. Bodies without a cycle
	 * are always 1.0.
	 */
	public static double windowFactor(CelestialBody body, long gameTime) {
		long cycle = windowCycleTicks(body);
		if (cycle <= 0) {
			return 1.0;
		}
		double phase = (double) Math.floorMod(gameTime, cycle) / cycle;
		return 1.0 + (WORST_WINDOW_FACTOR - 1.0) * (1.0 - Math.cos(2 * Math.PI * phase)) / 2.0;
	}

	/** Ticks until the next best launch moment (0 if now is the best moment or there is no cycle). */
	public static long ticksToNextWindow(CelestialBody body, long gameTime) {
		long cycle = windowCycleTicks(body);
		if (cycle <= 0) {
			return 0;
		}
		long into = Math.floorMod(gameTime, cycle);
		return into == 0 ? 0 : cycle - into;
	}

	/** One-way travel ticks to a body when launching at {@code gameTime}. */
	public static long oneWayTicks(CelestialBody body, int engineTier, long gameTime) {
		double ticks = body.transferDays() * REAL_DAY_TICKS / multiplier(engineTier) * windowFactor(body, gameTime);
		return Math.max(1, Math.round(ticks));
	}

	/** A free-return loop: out, around the body and back. */
	public static long flybyTicks(CelestialBody body, int engineTier, long gameTime) {
		return 2 * oneWayTicks(body, engineTier, gameTime);
	}

	/** A journey length for people: "m:ss" under an hour, "Hh MMm" up to two days, then "Nd Hh" (real time). */
	public static String formatDuration(long ticks) {
		long seconds = (Math.max(0, ticks) + 19) / 20;
		if (seconds < 3600) {
			return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
		}
		long minutes = seconds / 60;
		if (minutes < 48 * 60) {
			return (minutes / 60) + "h " + String.format("%02d", minutes % 60) + "m";
		}
		long hours = minutes / 60;
		return (hours / 24) + "d " + (hours % 24) + "h";
	}
}
