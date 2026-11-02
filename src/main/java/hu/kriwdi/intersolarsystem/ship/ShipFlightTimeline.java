package hu.kriwdi.intersolarsystem.ship;

import java.util.Optional;

/** Pure timing rules of the flight state machine. Times are server game ticks (20 per second). */
public final class ShipFlightTimeline {
	private ShipFlightTimeline() {
	}

	public static long remainingTicks(ShipFlightPhase phase, long startedTick, long nowTick) {
		return remainingTicks(ShipFlightKind.NORMAL, phase, startedTick, nowTick);
	}

	public static long remainingTicks(ShipFlightKind kind, ShipFlightPhase phase, long startedTick, long nowTick) {
		if (phase == ShipFlightPhase.LANDED) {
			return 0;
		}
		long elapsed = Math.max(0, nowTick - startedTick);
		return Math.max(0, kind.durationTicks(phase) - elapsed);
	}

	/** Time left in a phase of known length. */
	public static long remainingTicks(ShipFlightPhase phase, long duration, long startedTick, long nowTick) {
		if (phase == ShipFlightPhase.LANDED) {
			return 0;
		}
		return Math.max(0, duration - Math.max(0, nowTick - startedTick));
	}

	/** Time left in the ship's current phase, following its flight plan. */
	public static long remainingTicks(ShipState state, long nowTick) {
		return remainingTicks(state.phase(), state.phaseDuration(state.phase()), state.phaseStartedTick(), nowTick);
	}

	/** The phase to switch to now, following the ship's flight plan. */
	public static Optional<ShipFlightPhase> dueTransition(ShipState state, long nowTick) {
		if (state.phase() == ShipFlightPhase.LANDED || remainingTicks(state, nowTick) > 0) {
			return Optional.empty();
		}
		return next(state.phase());
	}

	public static boolean isDue(ShipFlightPhase phase, long startedTick, long nowTick) {
		return isDue(ShipFlightKind.NORMAL, phase, startedTick, nowTick);
	}

	public static boolean isDue(ShipFlightKind kind, ShipFlightPhase phase, long startedTick, long nowTick) {
		return phase != ShipFlightPhase.LANDED && remainingTicks(kind, phase, startedTick, nowTick) == 0;
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
		return dueTransition(ShipFlightKind.NORMAL, phase, startedTick, nowTick);
	}

	public static Optional<ShipFlightPhase> dueTransition(ShipFlightKind kind, ShipFlightPhase phase, long startedTick,
			long nowTick) {
		return isDue(kind, phase, startedTick, nowTick) ? next(phase) : Optional.empty();
	}

	/** Remaining time as {@code m:ss}, rounded up to whole seconds. */
	public static String formatClock(long ticks) {
		long seconds = (Math.max(0, ticks) + 19) / 20;
		return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
	}
}
