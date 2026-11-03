package hu.kriwdi.intersolarsystem.ship;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ShipFacingBlock;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Piece;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Placement;

public final class ShipInteriorBuilder {
	private ShipInteriorBuilder() {
	}

	/**
	 * Places the ship's interior at {@link ShipDimensions#ANCHOR}. With {@code clearInterior} the inside is also set to
	 * air (first build); without it only the structure is re-placed, so blocks players put inside survive a repair.
	 */
	public static void build(ServerLevel interior, boolean clearInterior) {
		ShipInteriorLayout layout = ShipInteriorLayout.capsule();
		for (Placement placement : clearInterior ? layout.placements() : layout.structure()) {
			BlockPos pos = at(placement.cell());
			interior.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
			// Beds are placed as two halves; the second half must not be re-placed through a neighbour update.
			interior.setBlock(pos, stateFor(placement.piece()), Block.UPDATE_CLIENTS);
		}
		if (clearInterior) {
			paintSpace(interior);
		}
	}

	/**
	 * Sets the biome around the ship to the black space biome. Interiors generated before had the vanilla void biome,
	 * whose blue sky and fog would show through the viewports.
	 */
	public static void paintSpace(ServerLevel interior) {
		var server = interior.getServer();
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(),
				"execute in " + ShipDimensions.INTERIOR.identifier() + " run fillbiome 0 56 0 15 79 31 "
						+ "intersolarsystem:orbital_void");
	}

	/** Whether the interior is the current ship (the panoramic viewport is there), not an older one-room capsule. */
	public static boolean isCurrent(ServerLevel interior) {
		BlockPos marker = at(ShipInteriorLayout.capsule().marker());
		interior.getChunk(marker.getX() >> 4, marker.getZ() >> 4);
		return interior.getBlockState(marker).getBlock() == ModBlocks.SHIP_VIEWPORT;
	}

	/**
	 * Keeps the interior current: an older capsule is replaced by the standard ship (its inside is cleared: the old
	 * room overlaps the new one), and missing consoles are put back into empty cells.
	 */
	public static void ensureStation(ServerLevel interior) {
		if (!isCurrent(interior)) {
			build(interior, true);
			return;
		}
		ShipInteriorLayout layout = ShipInteriorLayout.capsule();
		// Newer versions of the fixed parts (the two-block hatch, turned consoles, seats) replace older ones in place;
		// containers and anything players built in the free cells stay.
		for (ShipInteriorLayout.Placement p : layout.placements()) {
			if (!p.piece().structural() && p.piece() != Piece.SEAT) {
				continue;
			}
			BlockPos pos = at(p.cell());
			BlockState want = stateFor(p.piece());
			BlockState have = interior.getBlockState(pos);
			boolean controls = p.piece() == Piece.FLIGHT_PANEL || p.piece() == Piece.THROTTLE;
			if (controls && have.getBlock() == want.getBlock()) {
				continue; // keep the switches and the lever where the crew left them
			}
			if (have != want && interior.getBlockEntity(pos) == null
					&& (p.piece() != Piece.HULL_PANEL || !have.is(ModBlocks.RUSTED_PANEL))) {
				interior.setBlock(pos, want, Block.UPDATE_ALL);
			}
		}
		fillIfAir(interior, layout.station(), stateFor(Piece.REPAIR_STATION));
		fillIfAir(interior, layout.console(), stateFor(Piece.DIAGNOSTIC_CONSOLE));
		fillIfAir(interior, layout.navigation(), stateFor(Piece.NAVIGATION_CONSOLE));
	}

	public static BlockPos at(ShipInteriorLayout.Cell cell) {
		return ShipDimensions.ANCHOR.offset(cell.dx(), cell.dy(), cell.dz());
	}

	private static void fillIfAir(ServerLevel interior, ShipInteriorLayout.Cell cell, BlockState state) {
		BlockPos pos = at(cell);
		interior.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		if (interior.getBlockState(pos).isAir()) {
			interior.setBlock(pos, state, Block.UPDATE_ALL);
		}
	}

	/** Dyed blocks are looked up by id (they are not all single fields of {@code Blocks} any more). */
	private static BlockState bed() {
		return net.minecraft.core.registries.BuiltInRegistries.BLOCK
				.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("light_gray_bed")).defaultBlockState();
	}

	private static BlockState stateFor(Piece piece) {
		return switch (piece) {
			case HULL_PANEL -> ModBlocks.RUSTED_PANEL.defaultBlockState();
			case FLOOR -> ModBlocks.STATION_HULL.defaultBlockState();
			case VIEWPORT -> ModBlocks.SHIP_VIEWPORT.defaultBlockState();
			case LIGHT -> ModBlocks.STATION_LIGHT.defaultBlockState();
			// The hatch is in the left wall and faces into the ship.
			case HATCH -> ModBlocks.SHIP_HATCH.defaultBlockState().setValue(ShipFacingBlock.FACING, Direction.EAST);
			case HATCH_TOP -> ModBlocks.SHIP_HATCH_TOP.defaultBlockState().setValue(ShipFacingBlock.FACING, Direction.EAST);
			case REPAIR_STATION -> ModBlocks.REPAIR_STATION.defaultBlockState()
					.setValue(ShipFacingBlock.FACING, Direction.WEST);
			// The consoles on the bridge face the pilot's seats (north of them).
			case DIAGNOSTIC_CONSOLE -> ModBlocks.DIAGNOSTIC_CONSOLE.defaultBlockState()
					.setValue(ShipFacingBlock.FACING, Direction.NORTH);
			case NAVIGATION_CONSOLE -> ModBlocks.NAVIGATION_CONSOLE.defaultBlockState()
					.setValue(ShipFacingBlock.FACING, Direction.NORTH);
			// The seats face the consoles (south of them).
			// Beside the pilot's seat, facing the seats (north): the switch panel to the left, the throttle to the right.
			case FLIGHT_PANEL -> ModBlocks.FLIGHT_PANEL.defaultBlockState().setValue(ShipFacingBlock.FACING, Direction.NORTH);
			case THROTTLE -> ModBlocks.THROTTLE.defaultBlockState().setValue(ShipFacingBlock.FACING, Direction.NORTH);
			case SEAT -> ModBlocks.ASTRONAUT_SEAT.defaultBlockState().setValue(ShipFacingBlock.FACING, Direction.SOUTH);
			case BED_HEAD -> bed().setValue(BedBlock.FACING, Direction.SOUTH)
					.setValue(BedBlock.PART, BedPart.HEAD);
			case BED_FOOT -> bed().setValue(BedBlock.FACING, Direction.SOUTH)
					.setValue(BedBlock.PART, BedPart.FOOT);
			case BARREL -> Blocks.BARREL.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP);
			case CHEST -> Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST);
			case ENGINE_PORT, REACTOR_CORE -> ModBlocks.LANDER_ENGINE.defaultBlockState();
			case REACTOR_CAGE -> Blocks.IRON_BARS.defaultBlockState();
			case PIPE -> ModBlocks.TRUSS.defaultBlockState();
			case AIR -> Blocks.AIR.defaultBlockState();
		};
	}
}
