package hu.kriwdi.intersolarsystem.space;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.core.ModIds;

/** Builds the ISS in the orbit dimension the first time a ship arrives, and gives the docking port position. */
public final class OrbitStation {
	/** The station's centre (the module axis at the truss column). */
	public static final BlockPos CENTRE = new BlockPos(0, 120, 0);
	public static final ResourceKey<LootTable> SUPPLY_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
			ModIds.id("chests/iss_supply"));

	private OrbitStation() {
	}

	public static BlockPos dockHatch() {
		int[] d = IssLayout.DOCK_HATCH;
		return CENTRE.offset(d[0], d[1], d[2]);
	}

	/** The ship docks here; the station is built first if it is not there yet. Returns the hatch position. */
	public static BlockPos dock(ServerLevel orbit) {
		BlockPos below = dockHatch().below();
		orbit.getChunk(below.getX() >> 4, below.getZ() >> 4);
		if (orbit.getBlockState(below).getBlock() != ModBlocks.STATION_HULL) {
			build(orbit);
		}
		orbit.setBlock(dockHatch(), ModBlocks.SHIP_HATCH.defaultBlockState(), Block.UPDATE_ALL);
		return dockHatch();
	}

	public static void build(ServerLevel orbit) {
		for (IssLayout.Placement p : IssLayout.build()) {
			BlockPos pos = CENTRE.offset(p.x(), p.y(), p.z());
			orbit.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
			orbit.setBlock(pos, stateFor(p.piece()), Block.UPDATE_CLIENTS);
			if (p.piece() == IssLayout.Piece.SUPPLY) {
				if (orbit.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
					container.setLootTable(SUPPLY_LOOT);
					container.setLootTableSeed(orbit.getRandom().nextLong());
				}
			}
		}
	}

	private static BlockState stateFor(IssLayout.Piece piece) {
		return switch (piece) {
			case HULL, DOCK_FLOOR -> ModBlocks.STATION_HULL.defaultBlockState();
			case FOIL_HULL -> ModBlocks.STATION_FOIL.defaultBlockState();
			case WINDOW -> ModBlocks.STATION_WINDOW.defaultBlockState();
			case LIGHT -> ModBlocks.STATION_LIGHT.defaultBlockState();
			case TRUSS -> ModBlocks.TRUSS.defaultBlockState();
			case SOLAR -> ModBlocks.SOLAR_PANEL.defaultBlockState();
			case SUPPLY -> Blocks.BARREL.defaultBlockState();
			case AIR -> Blocks.AIR.defaultBlockState();
		};
	}
}
