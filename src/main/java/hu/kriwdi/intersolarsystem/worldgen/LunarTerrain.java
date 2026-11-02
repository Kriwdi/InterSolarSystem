package hu.kriwdi.intersolarsystem.worldgen;

/**
 * Pure maths of the lunar surface: where the dark maria lie. Smooth value noise over the world seed, so maria form large
 * organic patches with soft shores, the same on every load.
 */
public final class LunarTerrain {
	/** Above this the column is mare (dark basalt plain); below, bright highland. */
	public static final double MARE_THRESHOLD = 0.58;
	private static final double SCALE = 1.0 / 220.0;

	private LunarTerrain() {
	}

	/** 0..1, large-scale; two octaves. */
	public static double mareFactor(long seed, int x, int z) {
		return 0.7 * noise(seed, x * SCALE, z * SCALE) + 0.3 * noise(seed ^ 0x5DEECE66DL, x * SCALE * 3, z * SCALE * 3);
	}

	public static boolean isMare(long seed, int x, int z) {
		return mareFactor(seed, x, z) > MARE_THRESHOLD;
	}

	private static double noise(long seed, double x, double z) {
		long ix = (long) Math.floor(x);
		long iz = (long) Math.floor(z);
		double fx = x - ix;
		double fz = z - iz;
		double sx = fx * fx * (3 - 2 * fx);
		double sz = fz * fz * (3 - 2 * fz);
		double a = lerp(hash(seed, ix, iz), hash(seed, ix + 1, iz), sx);
		double b = lerp(hash(seed, ix, iz + 1), hash(seed, ix + 1, iz + 1), sx);
		return lerp(a, b, sz);
	}

	private static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	/** Deterministic 0..1 per lattice point. */
	static double hash(long seed, long x, long z) {
		long h = seed ^ (x * 0x9E3779B97F4A7C15L) ^ (z * 0xC2B2AE3D27D4EB4FL);
		h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
		h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
		h ^= h >>> 31;
		return (h >>> 11) * 0x1.0p-53;
	}
}
