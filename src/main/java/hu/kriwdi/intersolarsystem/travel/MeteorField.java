package hu.kriwdi.intersolarsystem.travel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A meteor shower as a field of rocks rushing at the ship, the same on server and client (pure function of the
 * flight's seed). Positions are in the plane in front of the bow, -1..1 on both axes (x: port to starboard, y: down to
 * up); the ship moves in the same plane. A rock that arrives within its radius of the ship hits it.
 */
public final class MeteorField {
	/** Ticks from a rock appearing far ahead to reaching the ship. */
	public static final int APPROACH_TICKS = 80;
	/** The ship's own size in the plane. */
	public static final float SHIP_RADIUS = 0.1F;
	/** Warning time before the first rocks arrive. */
	public static final int LEAD_IN = 100;

	public record Meteor(int arrive, float x, float y, float radius) {
		public int appear() {
			return arrive - APPROACH_TICKS;
		}

		public boolean hits(float shipX, float shipY) {
			float dx = x - shipX;
			float dy = y - shipY;
			return dx * dx + dy * dy < (radius + SHIP_RADIUS) * (radius + SHIP_RADIUS);
		}
	}

	private MeteorField() {
	}

	/** The rocks of a shower lasting {@code duration} ticks: waves of one to three every second or so. */
	public static List<Meteor> meteors(long seed, int duration) {
		return meteors(seed, duration, false);
	}

	/** {@code dense}: realistic difficulty, an extra rock in every wave. */
	public static List<Meteor> meteors(long seed, int duration, boolean dense) {
		Random random = new Random(seed * 0x9E3779B97F4A7C15L + 77L);
		List<Meteor> list = new ArrayList<>();
		int t = LEAD_IN;
		while (t < duration - 10) {
			int wave = 1 + random.nextInt(3) + (dense ? 1 : 0);
			for (int i = 0; i < wave; i++) {
				float x = random.nextFloat() * 2.0F - 1.0F;
				float y = random.nextFloat() * 2.0F - 1.0F;
				float r = 0.16F + random.nextFloat() * 0.18F;
				list.add(new Meteor(t + random.nextInt(6), x, y, r));
			}
			t += 16 + random.nextInt(20);
		}
		return list;
	}

	/** Ship motion: thrust from the pilot's keys, damping, kept inside the field. Returns {x, y, vx, vy}. */
	public static float[] step(float x, float y, float vx, float vy, int inX, int inY) {
		vx = (vx + inX * 0.012F) * 0.86F;
		vy = (vy + inY * 0.012F) * 0.86F;
		x = Math.max(-1.0F, Math.min(1.0F, x + vx));
		y = Math.max(-1.0F, Math.min(1.0F, y + vy));
		return new float[] {x, y, vx, vy};
	}
}
