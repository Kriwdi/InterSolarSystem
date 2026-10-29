package hu.kriwdi.intersolarsystem.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.minecraft.client.Minecraft;

import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.ship.ShipStatusPayload;

public class InterSolarSystemClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// The server is the only source of truth: the client just stores the last status and draws it.
		ClientPlayNetworking.registerGlobalReceiver(ShipStatusPayload.TYPE, (payload, context) -> {
			Minecraft minecraft = context.client();
			minecraft.execute(() -> {
				if (minecraft.level != null) {
					ClientShipStatus.set(payload.snapshot(), minecraft.level.getGameTime(), minecraft.level);
				}
			});
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientShipStatus.clear());
		HudElementRegistry.addLast(ModIds.id("ship_status"), new ShipHudElement());
	}
}
