package hu.kriwdi.intersolarsystem.ship;

import java.util.Random;

/**
 * The valve minigame (overheating): a temperature needle sweeps back and forth; the player closes the valve when the
 * needle is inside the green zone, once per round. The needle's path is a pure function of the seed and the time, so
 * the server replays the player's click times and checks them.
 */
public final class ValvePuzzle {
	public static final int MIN_ROUNDS = 1;
	public static final int MAX_ROUNDS = 4;
	/** Zone width in needle units (the scale runs 0..100). */
	public static final int ZONE_WIDTH = 16;

	private final long seed;
	private final double[] speed;
	private final double[] phase;
	private final int[] zoneLow;

	private ValvePuzzle(long seed, int rounds) {
		this.seed = seed;
		Random random = new Random(seed);
		speed = new double[rounds];
		phase = new double[rounds];
		zoneLow = new int[rounds];
		for (int i = 0; i < rounds; i++) {
			speed[i] = 3.0 + random.nextDouble() * 2.5 + i * 0.6;
			phase[i] = random.nextDouble() * 200.0;
			zoneLow[i] = 15 + random.nextInt(100 - ZONE_WIDTH - 25);
		}
	}

	public static ValvePuzzle of(long seed, int rounds) {
		return new ValvePuzzle(seed, Math.max(MIN_ROUNDS, Math.min(MAX_ROUNDS, rounds)));
	}

	public long seed() {
		return seed;
	}

	public int rounds() {
		return speed.length;
	}

	public int zoneLow(int round) {
		return zoneLow[round];
	}

	public int zoneHigh(int round) {
		return zoneLow[round] + ZONE_WIDTH;
	}

	/** Needle position (0..100) in a round at {@code tick} (fractional for smooth drawing), a triangle wave. */
	public double needle(int round, double tick) {
		double t = (tick * speed[round] + phase[round]) % 200.0;
		return t <= 100.0 ? t : 200.0 - t;
	}

	public boolean hit(int round, int tick) {
		double n = needle(round, tick);
		return n >= zoneLow(round) && n <= zoneHigh(round);
	}

	/**
	 * {@code clickTicks[i]}: ticks since the board opened when round i was closed. Valid if there is one click per round,
	 * they are strictly increasing, none is later than {@code maxTick}, and every click hits its zone.
	 */
	public boolean isSolvedBy(int[] clickTicks, int maxTick) {
		if (clickTicks == null || clickTicks.length != rounds()) {
			return false;
		}
		int previous = -1;
		for (int i = 0; i < clickTicks.length; i++) {
			int t = clickTicks[i];
			if (t <= previous || t < 0 || t > maxTick || !hit(i, t)) {
				return false;
			}
			previous = t;
		}
		return true;
	}
}
