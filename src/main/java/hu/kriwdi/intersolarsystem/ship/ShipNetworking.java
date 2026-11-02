package hu.kriwdi.intersolarsystem.ship;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;

public final class ShipNetworking {
	private ShipNetworking() {
	}

	/** Registers the packets. Must run on both sides before any receiver is registered. */
	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(ShipStatusPayload.TYPE, ShipStatusPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(WiringPayloads.Open.TYPE, WiringPayloads.Open.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(WiringPayloads.Result.TYPE, WiringPayloads.Result.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(WiringPayloads.Result.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			context.server().execute(() -> ShipWiringRepair.handleResult(context.server(), player, payload));
		});
	}
}
