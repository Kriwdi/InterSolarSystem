package hu.kriwdi.intersolarsystem.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

import net.minecraft.client.Minecraft;

import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.ship.ShipStatusPayload;
import hu.kriwdi.intersolarsystem.client.repair.PressureScreen;
import hu.kriwdi.intersolarsystem.client.repair.ValveScreen;
import hu.kriwdi.intersolarsystem.client.repair.WiringScreen;
import hu.kriwdi.intersolarsystem.ship.NavigationPayloads;
import hu.kriwdi.intersolarsystem.ship.RepairBoardPayloads;

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
		ClientPlayNetworking.registerGlobalReceiver(RepairBoardPayloads.Open.TYPE, (payload, context) -> {
			Minecraft minecraft = context.client();
			minecraft.execute(() -> minecraft.setScreenAndShow(switch (payload.kind()) {
				case WIRING -> new WiringScreen(payload.seed(), payload.size());
				case VALVE -> new ValveScreen(payload.seed(), payload.size());
				case PRESSURE -> new PressureScreen(payload.seed(), payload.size());
			}));
		});
		ClientPlayNetworking.registerGlobalReceiver(NavigationPayloads.Open.TYPE, (payload, context) -> {
			Minecraft minecraft = context.client();
			minecraft.execute(() -> minecraft.setScreenAndShow(
					new NavigationScreen(payload.engineTier(), payload.gameTime(), payload.canLaunch())));
		});
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientShipStatus.clear());
		HudElementRegistry.addLast(ModIds.id("ship_status"), new ShipHudElement());
	}
}
