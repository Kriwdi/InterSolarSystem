package hu.kriwdi.intersolarsystem.client;

import java.util.Optional;

import net.minecraft.client.multiplayer.ClientLevel;

import hu.kriwdi.intersolarsystem.ship.ShipStatusHolder;
import hu.kriwdi.intersolarsystem.ship.ShipStatusSnapshot;

/**
 * The last ship status the server sent, tied to the ClientLevel it arrived in (a new level object is created for every
 * world and dimension change, so older data can never show). Only used on the client main thread.
 */
public final class ClientShipStatus {
	private static final ShipStatusHolder<ClientLevel> HOLDER = new ShipStatusHolder<>();

	private ClientShipStatus() {
	}

	public static void set(ShipStatusSnapshot snapshot, long clientGameTime, ClientLevel level) {
		HOLDER.set(snapshot, clientGameTime, level);
	}

	public static Optional<ShipStatusSnapshot> snapshot(ClientLevel level) {
		return HOLDER.snapshot(level);
	}

	public static long receivedAtTick() {
		return HOLDER.receivedAtTick();
	}

	public static void clear() {
		HOLDER.clear();
	}
}
