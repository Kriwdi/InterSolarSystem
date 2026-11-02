package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.List;

/** What the server tells players inside the ship about the ship. Built from {@link ShipState}; clients only display it. */
public record ShipStatusSnapshot(ShipFlightPhase phase, int remainingTicks, List<Integer> conditions,
		List<IncidentView> incidents) {
	public record IncidentView(ShipSubsystem subsystem, ShipIncidentType type, int remainingTicks) {
	}

	/** The parts of a snapshot that mean "something changed"; timers are left out on purpose. */
	public record StructuralKey(ShipFlightPhase phase, List<Integer> conditions, List<IncidentKey> incidents) {
	}

	public record IncidentKey(ShipSubsystem subsystem, ShipIncidentType type) {
	}

	public ShipStatusSnapshot {
		conditions = List.copyOf(conditions);
		incidents = List.copyOf(incidents);
	}

	public static ShipStatusSnapshot from(ShipState state, long nowTick) {
		List<Integer> conditions = new ArrayList<>();
		for (ShipSubsystem s : ShipSubsystem.values()) {
			conditions.add(state.condition(s));
		}
		List<IncidentView> incidents = new ArrayList<>();
		for (ShipIncident i : state.hazards().incidents()) {
			incidents.add(new IncidentView(i.subsystem(), i.type(), clampToInt(i.deadlineTick() - nowTick)));
		}
		long remaining = ShipFlightTimeline.remainingTicks(state.phase(), state.phaseStartedTick(), nowTick);
		return new ShipStatusSnapshot(state.phase(), clampToInt(remaining), conditions, incidents);
	}

	/** Shown when the server speaks a packet format this client does not know: a neutral, all-fine status. */
	public static ShipStatusSnapshot unknown() {
		List<Integer> conditions = new ArrayList<>();
		for (int i = 0; i < ShipSubsystem.values().length; i++) {
			conditions.add(ShipState.MAX_CONDITION);
		}
		return new ShipStatusSnapshot(ShipFlightPhase.LANDED, 0, conditions, List.of());
	}

	private static int clampToInt(long value) {
		return (int) Math.max(0, Math.min(Integer.MAX_VALUE, value));
	}

	public StructuralKey structuralKey() {
		return new StructuralKey(phase, conditions,
				incidents.stream().map(i -> new IncidentKey(i.subsystem(), i.type())).toList());
	}
}
