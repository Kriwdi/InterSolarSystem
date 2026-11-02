package hu.kriwdi.intersolarsystem.ship;

/**
 * A hidden defect in a subsystem. It shows nowhere until a diagnostic scan or a test hop reveals it; until it is fixed it
 * adds to the takeoff risk. At most one per subsystem.
 */
public record ShipLatentFault(ShipSubsystem subsystem, boolean revealed) {
	public ShipLatentFault reveal() {
		return revealed ? this : new ShipLatentFault(subsystem, true);
	}
}
