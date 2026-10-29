package hu.kriwdi.intersolarsystem.ship;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class ShipNetworking {
	private ShipNetworking() {
	}

	/** Registers the packets. Must run on both sides before any receiver is registered. */
	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(ShipStatusPayload.TYPE, ShipStatusPayload.CODEC);
	}
}
