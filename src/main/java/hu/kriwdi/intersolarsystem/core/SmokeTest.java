package hu.kriwdi.intersolarsystem.core;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ModLoot;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipShell;
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
			checkEarth(server);
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
		long expected = IssLayout.build().stream().filter(p -> p.piece() == IssLayout.Piece.SUPPLY).count();
		if (supplies != expected) {
			LOGGER.error("[SMOKETEST-FAIL] {} of {} ISS supply containers have their loot table", supplies, expected);
		} else {
			LOGGER.info("[SMOKETEST] ISS built, docking port and {} supply containers ready", supplies);
		}
		if (!ShipShell.isHull(orbit.getBlockState(hatch.above()))) {
			LOGGER.error("[SMOKETEST-FAIL] the docked ship has no hull at the ISS");
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

	/** The dead Earth: only the Overworld exists, every biome is ours and generates, and no vanilla ore is left. */
	private static void checkEarth(MinecraftServer server) {
		if (server.getLevel(net.minecraft.world.level.Level.NETHER) != null
				|| server.getLevel(net.minecraft.world.level.Level.END) != null) {
			LOGGER.error("[SMOKETEST-FAIL] the Nether or the End still exists");
		}
		ServerLevel earth = server.overworld();
		BlockPos spawn = earth.getRespawnData().pos();
		java.util.Map<String, String> found = new java.util.TreeMap<>();
		for (var key : hu.kriwdi.intersolarsystem.worldgen.DeadEarth.ALL) {
			var hit = earth.findClosestBiome3d(h -> h.is(key), spawn, 6400, 32, 64);
			found.put(key.identifier().getPath(), hit == null ? "MISSING" : hit.getFirst().toShortString());
		}
		LOGGER.info("[SMOKETEST] Earth biomes nearest to spawn {}: {}", spawn.toShortString(), found);
		if (found.values().stream().filter("MISSING"::equals).count() > 2) {
			LOGGER.error("[SMOKETEST-FAIL] more than two dead Earth biomes are missing within 6400 blocks");
		}
		java.util.Map<String, Integer> biomes = new java.util.TreeMap<>();
		java.util.Map<String, Integer> top = new java.util.TreeMap<>();
		for (int x = -128; x <= 128; x += 64) {
			for (int z = -128; z <= 128; z += 64) {
				int bx = spawn.getX() + x;
				int bz = spawn.getZ() + z;
				earth.getChunk(bx >> 4, bz >> 4);
				int h = earth.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, bx, bz);
				BlockPos pos = new BlockPos(bx, h - 1, bz);
				biomes.merge(earth.getBiome(pos).unwrapKey().map(k -> k.identifier().toString()).orElse("?"), 1,
						Integer::sum);
				top.merge(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(earth.getBlockState(pos).getBlock())
						.toString(), 1, Integer::sum);
			}
		}
		LOGGER.info("[SMOKETEST] Earth around spawn: biomes {}, top blocks {}", biomes, top);
		if (biomes.keySet().stream().anyMatch(b -> b.startsWith("minecraft:"))) {
			LOGGER.error("[SMOKETEST-FAIL] a vanilla biome generated on the dead Earth");
		}
		java.util.Map<String, Integer> ores = new java.util.TreeMap<>();
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int cx = -1; cx <= 1; cx++) {
			for (int cz = -1; cz <= 1; cz++) {
				int ox = (spawn.getX() & ~15) + cx * 16;
				int oz = (spawn.getZ() & ~15) + cz * 16;
				earth.getChunk(ox >> 4, oz >> 4);
				for (int x = ox; x < ox + 16; x++) {
					for (int z = oz; z < oz + 16; z++) {
						for (int y = earth.getMinY(); y < 120; y++) {
							String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK
									.getKey(earth.getBlockState(p.set(x, y, z)).getBlock()).toString();
							if (id.endsWith("_ore")) {
								ores.merge(id, 1, Integer::sum);
							}
						}
					}
				}
			}
		}
		LOGGER.info("[SMOKETEST] ores in the 3x3 chunks at spawn: {}", ores);
		if (ores.keySet().stream().anyMatch(o -> o.startsWith("minecraft:"))) {
			LOGGER.error("[SMOKETEST-FAIL] vanilla ore generated on the dead Earth");
		}
		if (!ores.keySet().stream().anyMatch(o -> o.startsWith("intersolarsystem:"))) {
			LOGGER.error("[SMOKETEST-FAIL] no space ore around spawn");
		}
		var site = hu.kriwdi.intersolarsystem.ship.ShipStateAccess.get(server).landingSite();
		if (site.isEmpty() || earth.getBlockState(new BlockPos(site.get().x(), site.get().y(), site.get().z()))
				.getBlock() != ModBlocks.SHIP_HATCH) {
			LOGGER.error("[SMOKETEST-FAIL] the start site (ship hatch near spawn) was not built: {}", site);
		} else {
			LOGGER.info("[SMOKETEST] start site: ship hatch at {} {} {}", site.get().x(), site.get().y(), site.get().z());
		}
		java.util.Map<String, String> structures = new java.util.TreeMap<>();
		server.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).listElements()
				.filter(h -> h.key().identifier().getNamespace().equals(ModIds.MOD_ID))
				.filter(h -> !h.key().identifier().getPath().endsWith("_layout"))
				.forEach(h -> {
					BlockPos at = earth.findNearestMapStructure(net.minecraft.core.HolderSet.direct(h), spawn, 100, false);
					if (at == null) {
						// Rare ones: anywhere inside the 20 000-block Earth (625 chunks from the centre).
						at = earth.findNearestMapStructure(net.minecraft.core.HolderSet.direct(h), spawn, 640, false);
					}
					structures.put(h.key().identifier().getPath(), at == null ? "MISSING" : at.toShortString());
				});
		LOGGER.info("[SMOKETEST] Earth structures nearest to spawn: {}", structures);
		LOGGER.info("[SMOKETEST] Earth border {}", earth.getWorldBorder().getSize());
		StringBuilder blueprints = new StringBuilder();
		for (hu.kriwdi.intersolarsystem.spaceport.Section section : hu.kriwdi.intersolarsystem.spaceport.Section.values()) {
			if (section.mothership()) {
				int n = hu.kriwdi.intersolarsystem.spaceport.Blueprint.of(section.id()).size();
				blueprints.append(section.id()).append('=').append(n).append(' ');
				if (n == 0) {
					LOGGER.error("[SMOKETEST-FAIL] blueprint {} is empty", section.id());
				}
			}
		}
		LOGGER.info("[SMOKETEST] mothership blueprints: {}", blueprints);
		StringBuilder sites = new StringBuilder();
		for (var orbitSite : hu.kriwdi.intersolarsystem.space.SpaceSites.ORBIT) {
			if (!hu.kriwdi.intersolarsystem.space.SiteTemplates.exists(earth, orbitSite.template())) {
				LOGGER.error("[SMOKETEST-FAIL] space site template {} is missing", orbitSite.template());
			}
			sites.append(orbitSite.template()).append(' ');
		}
		for (var moonSite : hu.kriwdi.intersolarsystem.space.SpaceSites.MOON) {
			if (!hu.kriwdi.intersolarsystem.space.SiteTemplates.exists(earth, moonSite.template())) {
				LOGGER.error("[SMOKETEST-FAIL] space site template {} is missing", moonSite.template());
			}
			sites.append(moonSite.template()).append(' ');
		}
		LOGGER.info("[SMOKETEST] space site templates: {}", sites);
		long missing = structures.values().stream().filter("MISSING"::equals).count();
		if (structures.size() < 20 || missing > 0) {
			LOGGER.error("[SMOKETEST-FAIL] {} of {} structures not found anywhere on the Earth", missing, structures.size());
		}
		if (earth.getGameRules().get(net.minecraft.world.level.gamerules.GameRules.SPAWN_PHANTOMS)) {
			LOGGER.error("[SMOKETEST-FAIL] phantoms still spawn");
		}
	}
}
