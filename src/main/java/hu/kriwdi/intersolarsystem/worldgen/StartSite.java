package hu.kriwdi.intersolarsystem.worldgen;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ShipFacingBlock;
import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.ship.ShipState;
import hu.kriwdi.intersolarsystem.ship.ShipStateAccess;
import hu.kriwdi.intersolarsystem.space.MoonSites;

/**
 * The start of a new dead Earth world: the ship sits half buried a few blocks from the world spawn, where it came
 * down, with its hatch facing the spawn, scrap around it and a supply barrel. The hatch is registered as the ship's
 * landing site, so the first player can board right away. Built once: a world whose ship already has a landing site
 * (or an old world with vanilla terrain at spawn) is left alone.
 */
public final class StartSite {
	public static final ResourceKey<LootTable> START_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
			ModIds.id("chests/start_kit"));

	private StartSite() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(StartSite::ensureBuilt);
	}

	static void ensureBuilt(MinecraftServer server) {
		ShipState ship = ShipStateAccess.get(server);
		if (ship.isReadOnly() || ship.landingSite().isPresent()) {
			return;
		}
		ServerLevel earth = server.overworld();
		BlockPos spawn = earth.getRespawnData().pos();
		boolean deadEarth = earth.getBiome(MoonSites.surface(earth, spawn.getX(), spawn.getZ())).unwrapKey()
				.map(k -> k.identifier().getNamespace().equals(ModIds.MOD_ID)).orElse(false);
		if (!deadEarth) {
			return;
		}
		BlockPos hatch = build(earth, spawn.offset(7, 0, 0));
		ship.setLandingSite(new ShipState.LandingSite(earth.dimension().identifier().toString(), hatch.getX(),
				hatch.getY(), hatch.getZ()));
	}

	/** Builds the wreck around a centre column and returns the hatch position. The hatch faces west (the spawn). */
	static BlockPos build(ServerLevel level, BlockPos near) {
		// Impact scar: a shallow ash and cinder bowl.
		for (int dx = -8; dx <= 8; dx++) {
			for (int dz = -15; dz <= 15; dz++) {
				double d = Math.sqrt(dx * dx + dz * dz * 0.25);
				if (d > 8) {
					continue;
				}
				BlockPos top = MoonSites.surface(level, near.getX() + dx, near.getZ() + dz).below();
				set(level, top, (dx + dz) % 3 == 0 ? ModBlocks.CINDER_ROCK.defaultBlockState()
						: ModBlocks.ASH.defaultBlockState());
			}
		}
		// The ship itself, landed hard: its hull around the hatch, which faces west, towards the spawn.
		BlockPos hatch = MoonSites.surface(level, near.getX() - 4, near.getZ());
		set(level, hatch, ModBlocks.SHIP_HATCH.defaultBlockState().setValue(ShipFacingBlock.FACING, Direction.WEST));
		hu.kriwdi.intersolarsystem.ship.ShipShell.build(level, hatch, Direction.WEST);
		// Debris and the supply barrel.
		set(level, MoonSites.surface(level, near.getX() - 6, near.getZ() + 3), ModBlocks.SCRAP_PILE.defaultBlockState());
		set(level, MoonSites.surface(level, near.getX() - 7, near.getZ() - 5), ModBlocks.SCRAP_PILE.defaultBlockState());
		set(level, MoonSites.surface(level, near.getX() + 7, near.getZ() + 14), ModBlocks.RUBBLE.defaultBlockState());
		BlockPos barrel = MoonSites.surface(level, near.getX() - 6, near.getZ() - 2);
		set(level, barrel, Blocks.BARREL.defaultBlockState());
		if (level.getBlockEntity(barrel) instanceof RandomizableContainerBlockEntity container) {
			container.setLootTable(START_LOOT);
			container.setLootTableSeed(level.getSeed());
		}
		return hatch;
	}

	private static void set(ServerLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, Block.UPDATE_CLIENTS);
	}
}
