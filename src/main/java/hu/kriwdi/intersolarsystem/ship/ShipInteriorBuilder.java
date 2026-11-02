package hu.kriwdi.intersolarsystem.ship;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ShipFacingBlock;
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

	/**
	 * Rooms built before the repair station or the diagnostic console existed get them later; only an empty (air) cell
	 * is filled, so nothing a player put there is replaced.
	 */
	public static void ensureStation(ServerLevel interior) {
		ShipInteriorLayout layout = ShipInteriorLayout.capsule();
		fillIfAir(interior, layout.station(), ModBlocks.REPAIR_STATION.defaultBlockState());
		fillIfAir(interior, layout.console(), ModBlocks.DIAGNOSTIC_CONSOLE.defaultBlockState());
		// Rooms built before blocks could face a direction: turn the hatch to face into the room.
		ShipInteriorLayout.Cell cell = layout.hatch();
		BlockPos pos = ShipDimensions.ANCHOR.offset(cell.dx(), cell.dy(), cell.dz());
		BlockState hatch = interior.getBlockState(pos);
		if (hatch.getBlock() == ModBlocks.SHIP_HATCH && hatch.getValue(ShipFacingBlock.FACING) != Direction.SOUTH) {
			interior.setBlock(pos, hatch.setValue(ShipFacingBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
		}
	}

	private static void fillIfAir(ServerLevel interior, ShipInteriorLayout.Cell cell, BlockState state) {
		BlockPos pos = ShipDimensions.ANCHOR.offset(cell.dx(), cell.dy(), cell.dz());
		interior.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		if (interior.getBlockState(pos).isAir()) {
			interior.setBlock(pos, state, Block.UPDATE_ALL);
		}
	}

	private static BlockState stateFor(Piece piece) {
		return switch (piece) {
			case HULL_PANEL -> ModBlocks.RUSTED_PANEL.defaultBlockState();
			case LIGHT -> Blocks.SEA_LANTERN.defaultBlockState();
			// The hatch is in the wall at z = 0 and faces into the room.
			case HATCH -> ModBlocks.SHIP_HATCH.defaultBlockState().setValue(ShipFacingBlock.FACING, Direction.SOUTH);
			case REPAIR_STATION -> ModBlocks.REPAIR_STATION.defaultBlockState();
			case DIAGNOSTIC_CONSOLE -> ModBlocks.DIAGNOSTIC_CONSOLE.defaultBlockState();
			case AIR -> Blocks.AIR.defaultBlockState();
		};
	}
}
