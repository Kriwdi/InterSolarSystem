package hu.kriwdi.intersolarsystem.client.sky;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;

import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipFlightPhase;
import hu.kriwdi.intersolarsystem.ship.ShipViewPayload;

/**
 * What the crew sees out of the ship's viewports, and how the ship shakes and sounds. The interior is its own
 * dimension, so its sky is painted to match where the ship really is: dusty sky and ground on Earth, the grey Moon
 * with the Earth above it, the Earth below in orbit; during a journey the ground drops away, the planet shrinks, the
 * destination grows and the stars drift past. The server only says which phase the ship is in and how far along it is.
 */
public final class ShipView {
	/** One frame's scene. Colours are 0xRRGGBB; a ceiling of -1 means open sky. */
	public record Scene(int ceiling, int ground, float groundDepth, float stars, float starAngle, Body earth,
			Body moon, float shake, String groundTexture) {
		Scene(int ceiling, int ground, float groundDepth, float stars, float starAngle, Body earth, Body moon,
				float shake) {
			this(ceiling, ground, groundDepth, stars, starAngle, earth, moon, shake, "");
		}
	}

	/** Height of the eye above the painted ground when the ship stands on it. */
	public static final float LANDED_DEPTH = 6.0F;

	/**
	 * A body on the sky. With pitch 0 the roll alone places it in the north-south plane: 0 is straight up, negative
	 * rolls towards the south (where the bridge viewport looks), -90 is the southern horizon, 180 straight down.
	 * Positive pitch moves it to the west. scale: sprite half-size at distance 100.
	 */
	public record Body(float pitch, float roll, float scale, int phase) {
	}

	private static final int EARTH_SKY = 0xB9AD9C;
	private static final int EARTH_GROUND = 0x5E5244;
	private static final int MOON_GROUND = 0x8D8B86;
	/** In orbit: the planet fills the view ahead and below the bow. */
	private static final Body EARTH_BELOW = new Body(0.0F, -150.0F, 170.0F, 0);
	/** On the Moon: the Earth hangs above the southern horizon, in front of the bridge. */
	private static final Body EARTH_FROM_MOON = new Body(0.0F, -50.0F, 38.0F, 0);
	private static final Body MOON_AHEAD = new Body(0.0F, -60.0F, 30.0F, 0);

	private static ShipViewPayload last;
	private static long receivedTick;
	private static long clientTicks;
	private static ShipFlightPhase lastPhase = ShipFlightPhase.LANDED;

	private ShipView() {
	}

	public static void accept(ShipViewPayload payload) {
		last = payload;
		receivedTick = clientTicks;
	}

	public static void clear() {
		last = null;
	}

	private static boolean inShip() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.level != null && minecraft.level.dimension().equals(ShipDimensions.INTERIOR) && last != null;
	}

	public static boolean active() {
		return inShip();
	}

	private static ShipFlightPhase phase() {
		ShipFlightPhase[] all = ShipFlightPhase.values();
		return last.phase() >= 0 && last.phase() < all.length ? all[last.phase()] : ShipFlightPhase.LANDED;
	}

	/** Ticks left in the current phase, counted down locally between packets. */
	private static float remaining(float partialTick) {
		return Math.max(0.0F, last.remainingTicks() - (clientTicks - receivedTick) - partialTick);
	}

	/** 0 at the start of the phase, 1 at its end. */
	private static float progress(float partialTick) {
		if (last.totalTicks() <= 0) {
			return 1.0F;
		}
		return Mth.clamp(1.0F - remaining(partialTick) / last.totalTicks(), 0.0F, 1.0F);
	}

	private static Scene place(String id) {
		return place(id, last.groundFrom());
	}

	/** The scene at a place, with the painted ground of the biome the ship stands on. */
	private static Scene place(String id, String ground) {
		return switch (id) {
			case "moon" -> new Scene(-1, MOON_GROUND, LANDED_DEPTH, 1.0F, 0.0F, EARTH_FROM_MOON, null, 0.0F, "moon");
			case "earth_orbit" -> new Scene(-1, -1, 0.0F, 1.0F, 0.0F, EARTH_BELOW, null, 0.0F);
			case "earth" -> new Scene(EARTH_SKY, EARTH_GROUND, LANDED_DEPTH, 0.0F, 0.0F, null, null, 0.0F,
					ground.isEmpty() ? "ash_desert" : ground);
			default -> new Scene(-1, -1, 0.0F, 1.0F, 0.0F, null, null, 0.0F);
		};
	}

	/** Whether the ship is in its cruise (free flight between bodies): dust streaks past the bow. */
	public static boolean cruising() {
		return last != null && phase() == ShipFlightPhase.FLIGHT;
	}

	public static Scene scene(float partialTick) {
		ShipFlightPhase phase = phase();
		float time = clientTicks + partialTick;
		switch (phase) {
			case TAKEOFF -> {
				Scene base = place(last.from());
				float left = remaining(partialTick);
				float shake = left < 60.0F ? 0.15F + 0.5F * (1.0F - left / 60.0F) : 0.04F;
				return with(base, shake, base.starAngle());
			}
			case FLIGHT -> {
				return flight(progress(partialTick), time);
			}
			case LANDING -> {
				float p = progress(partialTick);
				boolean arriving = !last.to().isEmpty();
				Scene end = arriving ? place(destination(last.to()), last.groundTo()) : place(last.from());
				float depth = end.ground() == -1 ? 0.0F : LANDED_DEPTH + 1500.0F * (1.0F - p) * (1.0F - p);
				int ceiling = end.ceiling() == -1 || p < 0.6F ? -1 : end.ceiling();
				return new Scene(ceiling, end.ground(), depth, ceiling == -1 ? 1.0F : 0.0F, time * 0.05F, end.earth(),
						end.moon(), 0.25F * (1.0F - p) + 0.12F, end.groundTexture());
			}
			default -> {
				return place(last.from());
			}
		}
	}

	/** Where a journey's target leaves the ship ("earth_orbit" docks, "moon" lands, "earth" lands, others fly by). */
	private static String destination(String target) {
		return switch (target) {
			case "moon", "earth_orbit", "earth" -> target;
			default -> last.from();
		};
	}

	private static Scene flight(float p, float time) {
		String from = last.from();
		String to = last.to();
		float shake = p < 0.08F ? 0.5F * (1.0F - p / 0.08F) + 0.06F : 0.05F;
		float starAngle = time * 0.4F;
		boolean fromSurface = "earth".equals(from) || "moon".equals(from);
		// First part: the ground falls away and the sky turns black.
		if (fromSurface && p < 0.25F) {
			Scene base = place(from);
			float q = p / 0.25F;
			int ceiling = base.ceiling() == -1 ? -1 : lerpColour(base.ceiling(), 0x000000, Math.min(1.0F, q * 1.6F));
			return new Scene(q > 0.6F ? -1 : ceiling, base.ground(), LANDED_DEPTH + 3000.0F * q * q,
					Math.min(1.0F, q * 1.5F), starAngle, base.earth(), null, shake, base.groundTexture());
		}
		if ("earth_orbit".equals(to) || "earth".equals(to) && "earth_orbit".equals(from)) {
			return new Scene(-1, -1, 0.0F, 1.0F, starAngle, EARTH_BELOW, null, shake);
		}
		// Deep space: Earth shrinks behind, the Moon (if that is the target) grows ahead.
		float leg = fromSurface ? (p - 0.25F) / 0.75F : p;
		Body earth;
		Body moon = null;
		if ("moon".equals(from)) {
			// Leaving the Moon: it sinks away below the bow, the Earth grows ahead.
			earth = new Body(0.0F, -60.0F, 38.0F + 132.0F * leg * leg, 0);
			moon = new Body(0.0F, -150.0F, 60.0F * (1.0F - leg) + 4.0F, 0);
		} else {
			// Leaving the Earth: it shrinks below the bow; the Moon (if that is the target) grows ahead.
			earth = new Body(0.0F, -150.0F + 30.0F * leg, 170.0F * (1.0F - leg) * (1.0F - leg) + 8.0F, 0);
			if ("moon".equals(to)) {
				moon = new Body(0.0F, MOON_AHEAD.roll(), 4.0F + 90.0F * leg * leg, 0);
			}
		}
		return new Scene(-1, -1, 0.0F, 1.0F, starAngle, earth, moon, shake);
	}

	private static Scene with(Scene s, float shake, float starAngle) {
		return new Scene(s.ceiling(), s.ground(), s.groundDepth(), s.stars(), starAngle, s.earth(), s.moon(), shake,
				s.groundTexture());
	}

	private static int lerpColour(int a, int b, float t) {
		int r = (int) Mth.lerp(t, (a >> 16) & 0xFF, (b >> 16) & 0xFF);
		int g = (int) Mth.lerp(t, (a >> 8) & 0xFF, (b >> 8) & 0xFF);
		int bl = (int) Mth.lerp(t, a & 0xFF, b & 0xFF);
		return (r << 16) | (g << 8) | bl;
	}

	/** Shake strength now (0 none): the camera trembles by about this many degrees. */
	public static float shake(float partialTick) {
		return inShip() ? scene(partialTick).shake() + ClientManeuver.jolt() : 0.0F;
	}

	public static long ticks() {
		return clientTicks;
	}

	public static float time(float partialTick) {
		return clientTicks + partialTick;
	}

	/** Client tick: count time and play the ship's sounds (countdown beeps, engine roar, the hum of the drive). */
	public static void tick(Minecraft minecraft) {
		clientTicks++;
		ClientManeuver.tick();
		if (!inShip() || minecraft.player == null) {
			lastPhase = ShipFlightPhase.LANDED;
			return;
		}
		ShipFlightPhase phase = phase();
		float left = remaining(0.0F);
		if (phase == ShipFlightPhase.TAKEOFF && left <= 100 && ((int) left) % 20 == 0) {
			play(minecraft, SoundEvents.NOTE_BLOCK_PLING.value(), 0.8F, left <= 20 ? 2.0F : 1.2F);
		}
		boolean burning = phase == ShipFlightPhase.TAKEOFF && left < 60
				|| phase == ShipFlightPhase.FLIGHT && progress(0.0F) < 0.1F
				|| phase == ShipFlightPhase.LANDING;
		if (burning && clientTicks % 6 == 0) {
			play(minecraft, SoundEvents.BLAZE_BURN, 0.6F, 0.5F);
		}
		if (phase == ShipFlightPhase.FLIGHT && clientTicks % 50 == 0) {
			play(minecraft, SoundEvents.BEACON_AMBIENT, 0.7F, 0.6F);
		}
		if (phase != lastPhase && phase == ShipFlightPhase.FLIGHT) {
			play(minecraft, SoundEvents.GENERIC_EXPLODE.value(), 0.5F, 0.6F);
		}
		lastPhase = phase;
	}

	private static void play(Minecraft minecraft, SoundEvent sound, float volume, float pitch) {
		minecraft.level.playLocalSound(minecraft.player.getX(), minecraft.player.getY(), minecraft.player.getZ(), sound,
				SoundSource.AMBIENT, volume, pitch, false);
	}
}
