package hu.kriwdi.intersolarsystem.ship;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Piece;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Placement;

public final class ShipInteriorBuilder {
	private ShipInteriorBuilder() {
	}

	/**
	 * Places the capsule room at {@link ShipDimensions#ANCHOR}. With {@code clearInterior} the inside is also set to air
	 * (first build); without it only the hull, lights and hatch are re-placed, so blocks players put inside survive a repair.
	 */
	public static void build(ServerLevel interior, boolean clearInterior) {
		ShipInteriorLayout layout = ShipInteriorLayout.capsule();
		for (Placement placement : clearInterior ? layout.placements() : layout.structure()) {
			BlockPos pos = ShipDimensions.ANCHOR.offset(placement.cell().dx(), placement.cell().dy(), placement.cell().dz());
			interior.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
			interior.setBlock(pos, stateFor(placement.piece()), Block.UPDATE_ALL);
		}
	}

	/** Rooms built before the repair station existed get it later; only an empty (air) cell is filled. */
	public static void ensureStation(ServerLevel interior) {
		ShipInteriorLayout.Cell cell = ShipInteriorLayout.capsule().station();
		BlockPos pos = ShipDimensions.ANCHOR.offset(cell.dx(), cell.dy(), cell.dz());
		interior.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		if (interior.getBlockState(pos).isAir()) {
			interior.setBlock(pos, ModBlocks.REPAIR_STATION.defaultBlockState(), Block.UPDATE_ALL);
		}
	}

	private static BlockState stateFor(Piece piece) {
		return switch (piece) {
			case HULL_PANEL -> ModBlocks.RUSTED_PANEL.defaultBlockState();
			case LIGHT -> Blocks.SEA_LANTERN.defaultBlockState();
			case HATCH -> ModBlocks.SHIP_HATCH.defaultBlockState();
			case REPAIR_STATION -> ModBlocks.REPAIR_STATION.defaultBlockState();
			case AIR -> Blocks.AIR.defaultBlockState();
		};
	}
}
