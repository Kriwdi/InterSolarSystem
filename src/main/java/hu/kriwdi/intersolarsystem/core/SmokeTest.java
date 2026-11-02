package hu.kriwdi.intersolarsystem.core;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ModLoot;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.space.IssLayout;
import hu.kriwdi.intersolarsystem.space.OrbitStation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CI helper: with {@code -Dintersolarsystem.smoketest=true} the dedicated server stops right after it has fully started
 * (data packs, recipes, loot tables and dimensions loaded). The CI job then checks the log for errors from this mod.
 */
public final class SmokeTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModIds.MOD_ID);

	private SmokeTest() {
	}

	public static void register() {
		if (!Boolean.getBoolean("intersolarsystem.smoketest")) {
			return;
		}
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			int injected = ModLoot.INJECTED.get();
			if (injected < ModLoot.SALVAGE_CHESTS.size()) {
				LOGGER.error("[SMOKETEST-FAIL] salvage pool reached {} of {} chest loot tables", injected,
						ModLoot.SALVAGE_CHESTS.size());
			} else {
				LOGGER.info("[SMOKETEST] salvage pool in {} chest loot tables", injected);
			}
			checkStation(server);
			LOGGER.info("[SMOKETEST] server started, stopping");
			server.halt(false);
		});
	}

	/** Builds the ISS in the real orbit dimension and checks the docking port and the supply containers. */
	private static void checkStation(MinecraftServer server) {
		ServerLevel orbit = server.getLevel(ShipDimensions.ORBIT);
		if (orbit == null) {
			LOGGER.error("[SMOKETEST-FAIL] orbit dimension is missing");
			return;
		}
		BlockPos hatch = OrbitStation.dock(orbit);
		if (orbit.getBlockState(hatch).getBlock() != ModBlocks.SHIP_HATCH) {
			LOGGER.error("[SMOKETEST-FAIL] no hatch at the ISS docking port {}", hatch);
		}
		int supplies = 0;
		for (IssLayout.Placement p : IssLayout.build()) {
			if (p.piece() == IssLayout.Piece.SUPPLY && orbit.getBlockEntity(OrbitStation.CENTRE.offset(p.x(), p.y(), p.z()))
					instanceof RandomizableContainerBlockEntity c && OrbitStation.SUPPLY_LOOT.equals(c.getLootTable())) {
				supplies++;
			}
		}
		if (supplies != 6) {
			LOGGER.error("[SMOKETEST-FAIL] {} of 6 ISS supply containers have their loot table", supplies);
		} else {
			LOGGER.info("[SMOKETEST] ISS built, docking port and 6 supply containers ready");
		}
	}
}
