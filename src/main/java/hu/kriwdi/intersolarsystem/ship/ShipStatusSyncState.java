package hu.kriwdi.intersolarsystem.ship;

import java.util.Set;
import java.util.UUID;

/** What the status sync remembers between ticks. Reset whenever a server starts or stops, so nothing leaks across worlds. */
public final class ShipStatusSyncState {
	private Set<UUID> known = Set.of();
	private ShipStatusSnapshot.StructuralKey lastSentKey;

	public Set<UUID> known() {
		return known;
	}

	public ShipStatusSnapshot.StructuralKey lastSentKey() {
		return lastSentKey;
	}

	public void update(Set<UUID> newKnown, ShipStatusSnapshot.StructuralKey newLastSentKey) {
		known = Set.copyOf(newKnown);
		lastSentKey = newLastSentKey;
	}

	public void reset() {
		known = Set.of();
		lastSentKey = null;
	}
}
