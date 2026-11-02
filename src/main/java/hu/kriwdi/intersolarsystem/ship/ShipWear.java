package hu.kriwdi.intersolarsystem.ship;

import java.util.EnumMap;
import java.util.Map;

/** How much each subsystem wears out at a flight phase change. Kept in one place so balancing is easy. */
public final class ShipWear {
	private ShipWear() {
	}

	/** Positive amounts to subtract, per subsystem, when the ship switches to {@code to}. */
	public static Map<ShipSubsystem, Integer> forTransition(ShipFlightPhase to) {
		Map<ShipSubsystem, Integer> wear = new EnumMap<>(ShipSubsystem.class);
		switch (to) {
			case FLIGHT -> {
				wear.put(ShipSubsystem.ENGINE, 4);
				wear.put(ShipSubsystem.LANDING_LAUNCH, 4);
				wear.put(ShipSubsystem.FUEL_ENERGY, 3);
			}
			case LANDING -> {
				wear.put(ShipSubsystem.ENGINE, 4);
				wear.put(ShipSubsystem.FUEL_ENERGY, 6);
				wear.put(ShipSubsystem.STABILIZER, 3);
				wear.put(ShipSubsystem.HULL_ARMOR, 2);
				wear.put(ShipSubsystem.NAVIGATION_COMMS, 2);
				wear.put(ShipSubsystem.LIFE_SUPPORT, 2);
			}
			case LANDED -> {
				wear.put(ShipSubsystem.LANDING_LAUNCH, 5);
				wear.put(ShipSubsystem.STABILIZER, 3);
				wear.put(ShipSubsystem.HULL_ARMOR, 2);
			}
			case TAKEOFF -> {
			}
		}
		return wear;
	}
}
