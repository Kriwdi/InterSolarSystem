package hu.kriwdi.intersolarsystem.ship;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedDataType;

import hu.kriwdi.intersolarsystem.core.ModIds;

public final class ShipStateAccess {
	// Data fixer is null: the Fabric docs say mod data does not need one. Reload behavior is verified in phase 1 task 4.
	public static final SavedDataType<ShipState> TYPE = new SavedDataType<>(
			ModIds.id("ship_state"),
			ShipState::new,
			ShipState.CODEC,
			null
	);

	private ShipStateAccess() {
	}

	/** The world's single ship. Server-side only. */
	public static ShipState get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}
}
