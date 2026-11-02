package hu.kriwdi.intersolarsystem.ship;

import java.util.Locale;
import java.util.Optional;

public enum ShipHullClass {
	RESCUE_CAPSULE,
	RESEARCH_VESSEL,
	CARGO_MINER,
	SCOUT,
	MOTHERSHIP;

	public String id() {
		return name().toLowerCase(Locale.ROOT);
	}

	public static Optional<ShipHullClass> fromId(String id) {
		for (ShipHullClass c : values()) {
			if (c.id().equals(id)) {
				return Optional.of(c);
			}
		}
		return Optional.empty();
	}
}
