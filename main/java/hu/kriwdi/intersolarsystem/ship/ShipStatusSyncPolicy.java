package hu.kriwdi.intersolarsystem.ship;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Decides who gets a ship status packet this tick. Pure, so the rules are testable without a server. */
public final class ShipStatusSyncPolicy {
	public record Result(Set<UUID> sendTo, Set<UUID> nextKnown) {
	}

	private ShipStatusSyncPolicy() {
	}

	/**
	 * Everyone inside the ship gets a packet when something changed or once per second; otherwise only players who just
	 * arrived (not in {@code known}). Players who left are forgotten, so they get a packet again when they return.
	 */
	public static Result decide(Set<UUID> inShip, Set<UUID> known, boolean changed, boolean periodic) {
		Set<UUID> sendTo = new HashSet<>();
		if (changed || periodic) {
			sendTo.addAll(inShip);
		} else {
			for (UUID id : inShip) {
				if (!known.contains(id)) {
					sendTo.add(id);
				}
			}
		}
		return new Result(Set.copyOf(sendTo), Set.copyOf(inShip));
	}
}
