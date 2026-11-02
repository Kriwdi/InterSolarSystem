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
import hu.kriwdi.intersolarsystem.space.MoonSites;
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
			checkMoon(server);
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

	/** Generates the Moon around the landing zone, builds the sites and samples the surface materials. */
	private static void checkMoon(MinecraftServer server) {
		ServerLevel moon = server.getLevel(ShipDimensions.MOON);
		if (moon == null) {
			LOGGER.error("[SMOKETEST-FAIL] moon dimension is missing");
			return;
		}
		MoonSites.ensureBuilt(moon);
		if (!MoonSites.built(moon)) {
			LOGGER.error("[SMOKETEST-FAIL] Tranquility Base was not built");
		}
		{
			int bx = MoonSites.BASE[0];
			int bz = MoonSites.BASE[1];
			moon.getChunk(bx >> 4, bz >> 4);
			LOGGER.info("[SMOKETEST] Moon heightmaps at the base: motion_blocking={} world_surface={} scanned={}",
					moon.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, bx, bz),
					moon.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, bx, bz),
					MoonSites.surface(moon, bx, bz).getY());
			for (MoonSites.Apollo apollo : MoonSites.APOLLO) {
				int ground = MoonSites.surface(moon, apollo.x() + 12, apollo.z() + 12).getY();
				if (ground < 40) {
					LOGGER.error("[SMOKETEST-FAIL] Moon ground near {} is at y={}", apollo, ground);
				}
			}
		}
		java.util.Map<String, Integer> top = new java.util.TreeMap<>();
		int lowest = Integer.MAX_VALUE;
		int highest = Integer.MIN_VALUE;
		for (int x = -256; x <= 256; x += 32) {
			for (int z = -256; z <= 256; z += 32) {
				moon.getChunk(x >> 4, z >> 4);
				int h = moon.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
				lowest = Math.min(lowest, h);
				highest = Math.max(highest, h);
				String block = net.minecraft.core.registries.BuiltInRegistries.BLOCK
						.getKey(moon.getBlockState(new BlockPos(x, h - 1, z)).getBlock()).toString();
				top.merge(block, 1, Integer::sum);
			}
		}
		LOGGER.info("[SMOKETEST] Moon surface heights {}..{}, top blocks {}", lowest, highest, top);
		if (!top.containsKey("intersolarsystem:lunar_regolith") || !top.containsKey("intersolarsystem:mare_regolith")) {
			LOGGER.error("[SMOKETEST-FAIL] the Moon has no regolith highlands and maria");
		}
		LOGGER.info("[SMOKETEST] Moon border {}", moon.getWorldBorder().getSize());
	}
}
