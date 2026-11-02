package hu.kriwdi.intersolarsystem.ship;

import java.util.Optional;

/**
 * Keeps the last received ship status together with the object it was received in (on the client: the ClientLevel).
 * A snapshot is only handed out for that same owner, so data from another world, server or dimension visit never shows.
 */
public final class ShipStatusHolder<O> {
	private ShipStatusSnapshot snapshot;
	private long receivedAtTick;
	private O owner;

	public void set(ShipStatusSnapshot newSnapshot, long tick, O newOwner) {
		snapshot = newSnapshot;
		receivedAtTick = tick;
		owner = newOwner;
	}

	public Optional<ShipStatusSnapshot> snapshot(O forOwner) {
		if (snapshot == null || forOwner == null || forOwner != owner) {
			return Optional.empty();
		}
		return Optional.of(snapshot);
	}

	public long receivedAtTick() {
		return receivedAtTick;
	}

	public void clear() {
		snapshot = null;
		owner = null;
	}
}
