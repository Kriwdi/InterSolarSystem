package hu.kriwdi.intersolarsystem.ship;

import java.util.Optional;

/** The repair minigames and which incident needs which. A fire has none: the kit puts it out. */
public enum RepairMinigame {
	WIRING,
	VALVE,
	PRESSURE;

	public static Optional<RepairMinigame> forIncident(ShipIncidentType type) {
		return switch (type) {
			case SHORT_CIRCUIT -> Optional.of(WIRING);
			case OVERHEAT -> Optional.of(VALVE);
			case LEAK, PRESSURE_LOSS -> Optional.of(PRESSURE);
			case FIRE -> Optional.empty();
		};
	}

	/**
	 * Board size from the damaged subsystem's condition: the worse it is, the harder the repair. With a multitool the
	 * board is one step smaller.
	 */
	public int size(int condition, boolean multitool) {
		int c = ShipState.clamp(condition);
		int step = c >= 60 ? 0 : c >= 30 ? 1 : 2;
		if (multitool) {
			step = Math.max(-1, step - 1);
		}
		return switch (this) {
			case WIRING -> 4 + step;
			case VALVE -> 2 + step;
			case PRESSURE -> 4 + Math.min(1, step);
		};
	}

	/** Server check of an answer. {@code maxTick}: ticks the board has been open on the server (with some slack). */
	public boolean isSolvedBy(long seed, int size, int[] answer, int maxTick) {
		return switch (this) {
			case WIRING -> WiringPuzzle.of(seed, size).isSolvedBy(answer);
			case VALVE -> ValvePuzzle.of(seed, size).isSolvedBy(answer, maxTick);
			case PRESSURE -> PressurePuzzle.of(seed, size).isSolvedBy(answer);
		};
	}

	/** With a multitool in the off hand a light fault (this condition and above) is fixed without a minigame. */
	public static final int MULTITOOL_SKIP_CONDITION = 40;
}
