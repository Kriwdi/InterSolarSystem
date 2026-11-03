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
		PayloadTypeRegistry.clientboundPlay().register(ShipViewPayload.TYPE, ShipViewPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(ManeuverPayload.TYPE, ManeuverPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(hu.kriwdi.intersolarsystem.lore.LoreOpenPayload.TYPE,
				hu.kriwdi.intersolarsystem.lore.LoreOpenPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(RepairBoardPayloads.Open.TYPE, RepairBoardPayloads.Open.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(hu.kriwdi.intersolarsystem.space.VacuumStatusPayload.TYPE,
				hu.kriwdi.intersolarsystem.space.VacuumStatusPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(hu.kriwdi.intersolarsystem.lore.TapePlayPayload.TYPE,
				hu.kriwdi.intersolarsystem.lore.TapePlayPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(RepairBoardPayloads.Result.TYPE, RepairBoardPayloads.Result.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(NavigationPayloads.Open.TYPE, NavigationPayloads.Open.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(NavigationPayloads.Launch.TYPE, NavigationPayloads.Launch.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(NavigationPayloads.Launch.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			context.server().execute(() -> FlightControls.plotCourse(context.server(), player, payload.bodyId()));
		});
		ServerPlayNetworking.registerGlobalReceiver(RepairBoardPayloads.Result.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			context.server().execute(() -> ShipRepairBoards.handleResult(context.server(), player, payload));
		});
	}
}
