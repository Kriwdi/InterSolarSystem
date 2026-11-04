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
		// The seat a player rides while sitting is invisible.
		net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry.register(
				hu.kriwdi.intersolarsystem.content.ModEntities.SEAT, net.minecraft.client.renderer.entity.NoopRenderer::new);
		// The server is the only source of truth: the client just stores the last status and draws it.
		ClientPlayNetworking.registerGlobalReceiver(ShipStatusPayload.TYPE, (payload, context) -> {
			Minecraft minecraft = context.client();
			minecraft.execute(() -> {
				if (minecraft.level != null) {
					ClientShipStatus.set(payload.snapshot(), minecraft.level.getGameTime(), minecraft.level);
				}
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(hu.kriwdi.intersolarsystem.space.VacuumStatusPayload.TYPE,
				(payload, context) -> context.client().execute(() -> {
					if (context.client().level != null) {
						HelmetHudElement.update(payload, context.client().level.getGameTime());
					}
				}));
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
			hu.kriwdi.intersolarsystem.travel.SolarSystem.setDiscovered(payload.discovered());
			minecraft.execute(() -> minecraft.setScreenAndShow(
					new NavigationScreen(payload.engineTier(), payload.gameTime(), payload.blocker(),
							payload.here(), payload.mothershipAt())));
		});
		ClientPlayNetworking.registerGlobalReceiver(hu.kriwdi.intersolarsystem.ship.ShipViewPayload.TYPE,
				(payload, context) -> context.client().execute(() -> hu.kriwdi.intersolarsystem.client.sky.ShipView
						.accept(payload)));
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK
				.register(hu.kriwdi.intersolarsystem.client.sky.ShipView::tick);
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(ZeroGravity::tick);
		net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			var item = stack.getItem();
			String key = item == hu.kriwdi.intersolarsystem.content.ModItems.SAFETY_TETHER ? "safety_tether"
					: item == hu.kriwdi.intersolarsystem.content.ModItems.TETHER_SPOOL ? "tether_spool"
					: item == hu.kriwdi.intersolarsystem.content.ModItems.MMU_PACK ? "mmu_pack"
					: item == hu.kriwdi.intersolarsystem.content.ModItems.NITROGEN_TANK ? "nitrogen_tank"
					: item == hu.kriwdi.intersolarsystem.content.ModItems.OXYGEN_CANISTER ? "oxygen_canister"
					: item == hu.kriwdi.intersolarsystem.content.ModItems.ROVER_MEMORY_CORE ? "rover_memory_core" : null;
			if (key != null) {
				lines.add(net.minecraft.network.chat.Component.translatable("tooltip.intersolarsystem." + key)
						.withStyle(net.minecraft.ChatFormatting.GRAY));
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(hu.kriwdi.intersolarsystem.space.EvaPayloads.Tether.TYPE,
				(payload, context) -> context.client().execute(() -> ZeroGravity.accept(payload)));
		ClientPlayNetworking.registerGlobalReceiver(hu.kriwdi.intersolarsystem.space.EvaPayloads.Debris.TYPE,
				(payload, context) -> context.client().execute(() -> {
					if (context.client().level != null) {
						ZeroGravity.accept(payload, context.client().level.getGameTime());
					}
				}));
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			ClientShipStatus.clear();
			ZeroGravity.clear();
			hu.kriwdi.intersolarsystem.client.sky.ShipView.clear();
			hu.kriwdi.intersolarsystem.client.sky.ClientManeuver.clear();
		});
		HudElementRegistry.addLast(ModIds.id("ship_status"), new ShipHudElement());
		HudElementRegistry.addLast(ModIds.id("helmet"), new HelmetHudElement());
		HudElementRegistry.addLast(ModIds.id("radar"), new RadarHudElement());
		HudElementRegistry.addLast(ModIds.id("tape"), new hu.kriwdi.intersolarsystem.client.lore.TapePlayer());
		hu.kriwdi.intersolarsystem.client.lore.LoreClient.register();
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK
				.register(client -> hu.kriwdi.intersolarsystem.client.lore.TapePlayer.tick());
		ClientPlayNetworking.registerGlobalReceiver(hu.kriwdi.intersolarsystem.ship.ManeuverPayload.TYPE,
				(payload, context) -> context.client().execute(() -> hu.kriwdi.intersolarsystem.client.sky.ClientManeuver
						.accept(payload)));
	}
}
