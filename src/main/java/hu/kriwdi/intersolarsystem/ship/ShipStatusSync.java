package hu.kriwdi.intersolarsystem.ship;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Sends the ship status to the players inside the ship. The server stays the only source of truth. */
public final class ShipStatusSync {
	private static final ShipStatusSyncState STATE = new ShipStatusSyncState();

	private ShipStatusSync() {
	}

	/** Forgets everything; called when a server starts or stops so nothing carries over into another world. */
	public static void reset() {
		STATE.reset();
	}

	public static void tick(MinecraftServer server, ShipState state, long nowTick) {
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior == null || interior.players().isEmpty()) {
			STATE.reset();
			return;
		}
		ShipStatusSnapshot snapshot = ShipStatusSnapshot.from(state, nowTick);
		ShipStatusSnapshot.StructuralKey key = snapshot.structuralKey();
		boolean changed = !key.equals(STATE.lastSentKey());
		Set<UUID> inShip = new HashSet<>();
		interior.players().forEach(p -> inShip.add(p.getUUID()));
		ShipStatusSyncPolicy.Result result = ShipStatusSyncPolicy.decide(inShip, STATE.known(), changed, nowTick % 20 == 0);
		if (!result.sendTo().isEmpty()) {
			ShipStatusPayload payload = new ShipStatusPayload(snapshot);
			for (ServerPlayer player : interior.players()) {
				if (result.sendTo().contains(player.getUUID())) {
					ServerPlayNetworking.send(player, payload);
				}
			}
			STATE.update(result.nextKnown(), key);
		} else {
			STATE.update(result.nextKnown(), STATE.lastSentKey());
		}
	}
}
