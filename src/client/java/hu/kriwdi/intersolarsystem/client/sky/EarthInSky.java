package hu.kriwdi.intersolarsystem.client.sky;

import net.minecraft.client.Minecraft;

import hu.kriwdi.intersolarsystem.ship.ShipDimensions;

/** Where the Earth appears in the sky of each dimension, and which lighting phase it shows. */
public final class EarthInSky {
	public static final int PHASES = 8;
	public static final float DISTANCE = 100.0F;
	public static final int DISC_VERTICES = 34;

	/**
	 * pitch: rotation from straight up (0) towards the horizon; 180 = straight down. scale: half-size of the sprite at
	 * distance 100 (vanilla's moon is 20).
	 */
	public enum View {
		NONE(0, 0, 0),
		/** Low orbit: the planet fills the sky below. */
		FROM_ORBIT(180.0F, 0.0F, 170.0F),
		/** From the Moon: high in the southern sky, fixed, about twice vanilla's moon. */
		FROM_MOON(-35.0F, 12.0F, 38.0F);

		private final float pitch;
		private final float roll;
		private final float scale;

		View(float pitch, float roll, float scale) {
			this.pitch = pitch;
			this.roll = roll;
			this.scale = scale;
		}

		public float pitch() {
			return pitch;
		}

		public float roll() {
			return roll;
		}

		public float scale() {
			return scale;
		}
	}

	private EarthInSky() {
	}

	public static View current() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			return View.NONE;
		}
		if (minecraft.level.dimension().equals(ShipDimensions.ORBIT)) {
			return View.FROM_ORBIT;
		}
		if (minecraft.level.dimension().equals(ShipDimensions.MOON)) {
			return View.FROM_MOON;
		}
		return View.NONE;
	}

	/**
	 * Lighting phase from the sun's position: 0 = fully lit, 4 = dark. From orbit the day side faces you at noon; from
	 * the Moon the Earth is full when the Sun is behind you, so the phase runs opposite to the Moon's own day.
	 */
	public static int phase(View view, float sunAngleRadians) {
		double turn = sunAngleRadians / (2 * Math.PI);
		if (view == View.FROM_MOON) {
			turn += 0.5;
		}
		int phase = (int) Math.round(turn * PHASES);
		return Math.floorMod(phase, PHASES);
	}
}
