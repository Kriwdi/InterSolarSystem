package hu.kriwdi.intersolarsystem.ship;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Decides what one repair kit fixes at the repair station. */
public final class ShipRepairRules {
	public static final int REPAIR_AMOUNT = 25;

	public enum Kind { INCIDENT, FAULT, SUBSYSTEM, NOTHING }

	/** For FAULT and SUBSYSTEM the subsystem is set; for INCIDENT the incident. */
	public record Choice(Kind kind, Optional<ShipIncident> incident, Optional<ShipSubsystem> subsystem) {
	}

	private ShipRepairRules() {
	}

	public static Choice choose(List<ShipIncident> incidents, Map<ShipSubsystem, Integer> conditions) {
		return choose(incidents, List.of(), conditions);
	}

	/** Order: the most urgent incident, then a known hidden fault (worst subsystem first), then the worst subsystem. */
	public static Choice choose(List<ShipIncident> incidents, List<ShipLatentFault> faults,
			Map<ShipSubsystem, Integer> conditions) {
		Optional<ShipIncident> earliest = incidents.stream().min(Comparator.comparingLong(ShipIncident::deadlineTick));
		if (earliest.isPresent()) {
			return new Choice(Kind.INCIDENT, earliest, Optional.empty());
		}
		Optional<ShipSubsystem> knownFault = faults.stream().filter(ShipLatentFault::revealed)
				.map(ShipLatentFault::subsystem)
				.min(Comparator.comparingInt((ShipSubsystem s) -> conditions.getOrDefault(s, ShipState.MAX_CONDITION))
						.thenComparingInt(Enum::ordinal));
		if (knownFault.isPresent()) {
			return new Choice(Kind.FAULT, Optional.empty(), knownFault);
		}
		ShipSubsystem worst = null;
		int worstCondition = ShipState.MAX_CONDITION;
		for (ShipSubsystem s : ShipSubsystem.values()) {
			int condition = conditions.getOrDefault(s, ShipState.MAX_CONDITION);
			if (condition < worstCondition) {
				worst = s;
				worstCondition = condition;
			}
		}
		if (worst != null) {
			return new Choice(Kind.SUBSYSTEM, Optional.empty(), Optional.of(worst));
		}
		return new Choice(Kind.NOTHING, Optional.empty(), Optional.empty());
	}
}
