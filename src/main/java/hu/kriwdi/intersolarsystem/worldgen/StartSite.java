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
		BlockPos ground = MoonSites.surface(level, near.getX(), near.getZ());
		int y = ground.getY();
		BlockState hull = ModBlocks.RUSTED_PANEL.defaultBlockState();
		// Impact scar: a shallow ash and cinder bowl.
		for (int dx = -7; dx <= 7; dx++) {
			for (int dz = -6; dz <= 6; dz++) {
				double d = Math.sqrt(dx * dx * 0.7 + dz * dz);
				if (d > 6.5) {
					continue;
				}
				BlockPos top = MoonSites.surface(level, near.getX() + dx, near.getZ() + dz).below();
				set(level, top, (dx + dz) % 3 == 0 ? ModBlocks.CINDER_ROCK.defaultBlockState()
						: ModBlocks.ASH.defaultBlockState());
			}
		}
		// The hull: a rounded rusted shell 7 long, half buried, tilted down to the east.
		for (int dx = -3; dx <= 3; dx++) {
			int sink = dx > 1 ? 1 : 0;
			for (int dy = 0; dy <= 3; dy++) {
				for (int dz = -2; dz <= 2; dz++) {
					boolean corner = (dy == 3 || dy == 0) && Math.abs(dz) == 2;
					if (corner) {
						continue;
					}
					set(level, new BlockPos(near.getX() + dx, y + dy - sink - 1, near.getZ() + dz), hull);
				}
			}
		}
		set(level, new BlockPos(near.getX() + 4, y, near.getZ()), ModBlocks.LANDER_ENGINE.defaultBlockState());
		set(level, new BlockPos(near.getX() + 1, y + 3, near.getZ()), ModBlocks.ANTENNA_DISH.defaultBlockState());
		BlockPos hatch = new BlockPos(near.getX() - 4, y, near.getZ());
		set(level, hatch, ModBlocks.SHIP_HATCH.defaultBlockState().setValue(ShipFacingBlock.FACING, Direction.WEST));
		set(level, hatch.above(), Blocks.AIR.defaultBlockState());
		// Debris and the supply barrel.
		set(level, MoonSites.surface(level, near.getX() - 5, near.getZ() + 3), ModBlocks.SCRAP_PILE.defaultBlockState());
		set(level, MoonSites.surface(level, near.getX() + 2, near.getZ() - 4), ModBlocks.SCRAP_PILE.defaultBlockState());
		set(level, MoonSites.surface(level, near.getX() + 6, near.getZ() + 2), ModBlocks.RUBBLE.defaultBlockState());
		BlockPos barrel = MoonSites.surface(level, near.getX() - 5, near.getZ() - 2);
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
