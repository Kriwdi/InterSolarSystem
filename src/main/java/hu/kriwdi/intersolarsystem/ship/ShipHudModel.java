package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** What the HUD shows, computed from a snapshot and the time. Pure, so the display rules are testable. */
public record ShipHudModel(PhaseLine phase, List<SubsystemRow> subsystems, List<IncidentRow> incidents) {
	/** Incident rows blink in the last 15 seconds. */
	public static final int BLINK_THRESHOLD_TICKS = 300;

	public enum Severity {
		SAFE, WARN, CRITICAL;

		public static Severity of(int condition) {
			if (condition >= ShipIncidentRules.SAFE_CONDITION) {
				return SAFE;
			}
			return condition < ShipFlightRules.MIN_TAKEOFF_CONDITION ? CRITICAL : WARN;
		}
	}

	public record PhaseLine(String translationKey, Optional<String> clock) {
	}

	/** {@code knownFault}: a diagnostic scan or a test hop found a hidden fault here that is not fixed yet. */
	public record SubsystemRow(ShipSubsystem subsystem, int percent, Severity severity, boolean knownFault) {
	}

	public record IncidentRow(ShipSubsystem subsystem, ShipIncidentType type, String clock, boolean blinking) {
	}

	/** Time left now, given a value received at {@code receivedAtTick}; never negative, never grows if the clock jumps back. */
	public static int remainingNow(int remainingAtReceive, long receivedAtTick, long nowTick) {
		long elapsed = Math.max(0, nowTick - receivedAtTick);
		return (int) Math.max(0, remainingAtReceive - elapsed);
	}

	public static ShipHudModel from(ShipStatusSnapshot snapshot, long receivedAtTick, long nowTick) {
		PhaseLine phase;
		if (snapshot.phase() == ShipFlightPhase.LANDED) {
			phase = new PhaseLine("ship.intersolarsystem.phase.landed", Optional.empty());
		} else {
			phase = new PhaseLine("message.intersolarsystem.ship.countdown." + snapshot.phase().id(),
					Optional.of(ShipFlightTimeline.formatClock(
							remainingNow(snapshot.remainingTicks(), receivedAtTick, nowTick))));
		}
		List<SubsystemRow> subsystems = new ArrayList<>();
		for (ShipSubsystem s : ShipSubsystem.values()) {
			int percent = snapshot.conditions().get(s.ordinal());
			subsystems.add(new SubsystemRow(s, percent, Severity.of(percent), snapshot.knownFaults().contains(s)));
		}
		List<IncidentRow> incidents = snapshot.incidents().stream()
				.map(i -> new Object[] {i, remainingNow(i.remainingTicks(), receivedAtTick, nowTick)})
				.sorted(Comparator.comparingInt(o -> (int) o[1]))
				.map(o -> {
					ShipStatusSnapshot.IncidentView view = (ShipStatusSnapshot.IncidentView) o[0];
					int remaining = (int) o[1];
					return new IncidentRow(view.subsystem(), view.type(), ShipFlightTimeline.formatClock(remaining),
							remaining <= BLINK_THRESHOLD_TICKS);
				}).toList();
		return new ShipHudModel(phase, List.copyOf(subsystems), incidents);
	}
}
