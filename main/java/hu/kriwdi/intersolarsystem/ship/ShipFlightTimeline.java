package hu.kriwdi.intersolarsystem.ship;

import java.util.Optional;

/** Pure timing rules of the flight state machine. Times are server game ticks (20 per second). */
public final class ShipFlightTimeline {
	private ShipFlightTimeline() {
	}

	public static long remainingTicks(ShipFlightPhase phase, long startedTick, long nowTick) {
		if (phase == ShipFlightPhase.LANDED) {
			return 0;
		}
		long elapsed = Math.max(0, nowTick - startedTick);
		return Math.max(0, phase.durationTicks() - elapsed);
	}

	public static boolean isDue(ShipFlightPhase phase, long startedTick, long nowTick) {
		return phase != ShipFlightPhase.LANDED && remainingTicks(phase, startedTick, nowTick) == 0;
	}

	public static Optional<ShipFlightPhase> next(ShipFlightPhase phase) {
		return switch (phase) {
			case TAKEOFF -> Optional.of(ShipFlightPhase.FLIGHT);
			case FLIGHT -> Optional.of(ShipFlightPhase.LANDING);
			case LANDING -> Optional.of(ShipFlightPhase.LANDED);
			case LANDED -> Optional.empty();
		};
	}

	/** The phase to switch to now, if the current one has run out. */
	public static Optional<ShipFlightPhase> dueTransition(ShipFlightPhase phase, long startedTick, long nowTick) {
		return isDue(phase, startedTick, nowTick) ? next(phase) : Optional.empty();
	}

	/** Remaining time as {@code m:ss}, rounded up to whole seconds. */
	public static String formatClock(long ticks) {
		long seconds = (Math.max(0, ticks) + 19) / 20;
		return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
	}
}
