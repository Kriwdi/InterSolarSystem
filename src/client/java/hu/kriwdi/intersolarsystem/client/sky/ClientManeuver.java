package hu.kriwdi.intersolarsystem.client.sky;

import java.util.List;

import hu.kriwdi.intersolarsystem.ship.ManeuverPayload;
import hu.kriwdi.intersolarsystem.travel.MeteorField;

/**
 * The crew's copy of a meteor shower: the rocks come from the same seed as on the server, the ship's position from the
 * server a few times a second (smoothed between packets), and time runs on locally between packets.
 */
public final class ClientManeuver {
	private static ManeuverPayload last;
	private static List<MeteorField.Meteor> field = List.of();
	private static long fieldSeed;
	private static long receivedAt;
	private static float shownX;
	private static float shownY;
	private static int seenHits;
	private static float hitFlash;

	private ClientManeuver() {
	}

	public static void accept(ManeuverPayload payload) {
		if (payload.duration() <= 0) {
			last = null;
			return;
		}
		if (last == null || payload.seed() != fieldSeed) {
			fieldSeed = payload.seed();
			field = MeteorField.meteors(payload.seed(), payload.duration(), payload.dense());
			shownX = payload.x();
			shownY = payload.y();
			seenHits = payload.hits();
		}
		if (payload.hits() > seenHits) {
			hitFlash = 1.0F;
		}
		seenHits = payload.hits();
		last = payload;
		receivedAt = ShipView.ticks();
	}

	public static void clear() {
		last = null;
	}

	public static boolean active() {
		return last != null && ShipView.active();
	}

	public static void tick() {
		if (last == null) {
			return;
		}
		shownX += (last.x() - shownX) * 0.5F;
		shownY += (last.y() - shownY) * 0.5F;
		hitFlash = Math.max(0.0F, hitFlash - 0.08F);
	}

	/** Ticks into the shower now. */
	public static float elapsed(float partialTick) {
		return last == null ? 0.0F : last.elapsed() + (ShipView.ticks() - receivedAt) + partialTick;
	}

	public static float shipX() {
		return shownX;
	}

	public static float shipY() {
		return shownY;
	}

	public static int hits() {
		return last == null ? 0 : last.hits();
	}

	public static boolean piloted() {
		return last != null && last.piloted();
	}

	/** A short camera jolt after a hit (degrees). */
	public static float jolt() {
		return hitFlash * 1.6F;
	}

	public static List<MeteorField.Meteor> field() {
		return field;
	}
}
