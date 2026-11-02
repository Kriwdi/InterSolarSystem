package hu.kriwdi.intersolarsystem.content;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A shaped ship block that faces the player who placed it (like a furnace). The outline and collision follow the
 * model: subclasses give their boxes for the north-facing model and they are turned for the other directions.
 */
public class ShipFacingBlock extends ShipHullBlock {
	public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

	private final Map<Direction, VoxelShape> shapes;

	/** {@code northBoxes}: boxes in model pixels (minX, minY, minZ, maxX, maxY, maxZ) for the north-facing model. */
	public ShipFacingBlock(Properties properties, double[]... northBoxes) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
		shapes = new EnumMap<>(Direction.class);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			shapes.put(direction, rotatedShape(direction, northBoxes));
		}
	}

	/** Turns north-facing boxes to face {@code direction} (clockwise seen from above, like the blockstate y rotation). */
	static VoxelShape rotatedShape(Direction direction, double[]... northBoxes) {
		if (northBoxes.length == 0) {
			return Shapes.block();
		}
		VoxelShape shape = Shapes.empty();
		for (double[] b : northBoxes) {
			double x0 = b[0];
			double z0 = b[2];
			double x1 = b[3];
			double z1 = b[5];
			double[] r = switch (direction) {
				case EAST -> new double[] {16 - z1, x0, 16 - z0, x1};
				case SOUTH -> new double[] {16 - x1, 16 - z1, 16 - x0, 16 - z0};
				case WEST -> new double[] {z0, 16 - x1, z1, 16 - x0};
				default -> new double[] {x0, z0, x1, z1};
			};
			shape = Shapes.or(shape, Block.box(r[0], b[1], r[1], r[2], b[4], r[3]));
		}
		return shape;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shapes.get(state.getValue(FACING));
	}
}
