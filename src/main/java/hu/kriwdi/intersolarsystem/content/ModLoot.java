package hu.kriwdi.intersolarsystem.content;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;

import hu.kriwdi.intersolarsystem.core.ModIds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Salvage in ruins: until the dead Earth has its own structures (phase 7), the chests of vanilla ruins carry the
 * salvaged technology the ship needs. The pool is written as loot table JSON (the same format as data packs) and
 * decoded with the game's own codec, so it follows the 26.3 format exactly.
 */
public final class ModLoot {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModIds.MOD_ID);

	/** Chests that get the salvage pool: ruins, wrecks and abandoned places. */
	public static final Set<ResourceKey<LootTable>> SALVAGE_CHESTS = Set.of(
			BuiltInLootTables.ABANDONED_MINESHAFT,
			BuiltInLootTables.SIMPLE_DUNGEON,
			BuiltInLootTables.SHIPWRECK_SUPPLY,
			BuiltInLootTables.DESERT_PYRAMID,
			BuiltInLootTables.JUNGLE_TEMPLE,
			BuiltInLootTables.RUINED_PORTAL,
			BuiltInLootTables.STRONGHOLD_CORRIDOR,
			BuiltInLootTables.ANCIENT_CITY,
			BuiltInLootTables.PILLAGER_OUTPOST,
			BuiltInLootTables.IGLOO_CHEST,
			BuiltInLootTables.VILLAGE_TOOLSMITH);

	/** One to two draws: mostly scrap, sometimes wiring or a circuit, rarely a ready repair kit. */
	static final String SALVAGE_POOL = """
			{
			  "rolls": {"type": "minecraft:uniform", "min": 1, "max": 2},
			  "entries": [
			    {"type": "minecraft:item", "name": "intersolarsystem:scrap_metal", "weight": 10,
			     "modifier": {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 4}}},
			    {"type": "minecraft:item", "name": "intersolarsystem:salvaged_wiring", "weight": 6,
			     "modifier": {"type": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 1, "max": 2}}},
			    {"type": "minecraft:item", "name": "intersolarsystem:salvaged_circuit", "weight": 4},
			    {"type": "minecraft:item", "name": "intersolarsystem:repair_kit", "weight": 1},
			    {"type": "minecraft:empty", "weight": 5}
			  ]
			}""";

	/** How many chest tables got the pool at the last data load (checked by the CI smoke test). */
	public static final AtomicInteger INJECTED = new AtomicInteger();

	private ModLoot() {
	}

	public static void initialize() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			if (!source.isBuiltin() || !SALVAGE_CHESTS.contains(key)) {
				return;
			}
			JsonElement json = JsonParser.parseString(SALVAGE_POOL);
			DataResult<LootPool> pool = LootPool.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), json);
			pool.resultOrPartial(error -> LOGGER.error("Could not decode the salvage loot pool: {}", error))
					.ifPresent(p -> {
						tableBuilder.pool(p);
						INJECTED.incrementAndGet();
					});
		});
	}
}
