package hu.kriwdi.intersolarsystem.ship;

/** Decides what using an outside hatch does. The hatch is the ship's door: only the ship's own hatch boards. */
public final class ShipHatchRules {
	private ShipHatchRules() {
	}

	public static HatchAction decide(ShipFlightPhase phase, boolean hasPosition, boolean matchesPosition,
			boolean positionHatchPresent) {
		if (phase != ShipFlightPhase.LANDED) {
			return HatchAction.BUSY;
		}
		if (!hasPosition) {
			return HatchAction.REGISTER;
		}
		if (matchesPosition) {
			return HatchAction.BOARD;
		}
		return positionHatchPresent ? HatchAction.NOT_CONNECTED : HatchAction.REDOCK;
	}

	public static boolean canLeave(ShipFlightPhase phase) {
		return phase == ShipFlightPhase.LANDED;
	}
}
