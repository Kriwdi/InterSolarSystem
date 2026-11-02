package hu.kriwdi.intersolarsystem.ship;

/** A failure on a subsystem that must be fixed before {@code deadlineTick} (Overworld game time). */
public record ShipIncident(ShipSubsystem subsystem, ShipIncidentType type, long deadlineTick) {
}
