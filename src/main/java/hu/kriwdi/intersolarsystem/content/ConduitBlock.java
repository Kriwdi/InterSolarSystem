package hu.kriwdi.intersolarsystem.content;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A thin pipe or cable that joins up with its neighbours by itself: other conduits of the same kind and the machines
 * it serves. The ship's systems only work while their conduit network is whole (see ShipNetworks).
 */
public class ConduitBlock extends Block {
	public static final Map<Direction, BooleanProperty> SIDES = new EnumMap<>(Direction.class);

	static {
		SIDES.put(Direction.NORTH, BooleanProperty.create("north"));
		SIDES.put(Direction.EAST, BooleanProperty.create("east"));
		SIDES.put(Direction.SOUTH, BooleanProperty.create("south"));
		SIDES.put(Direction.WEST, BooleanProperty.create("west"));
		SIDES.put(Direction.UP, BooleanProperty.create("up"));
		SIDES.put(Direction.DOWN, BooleanProperty.create("down"));
	}

	private final double radius;
	private final Predicate<BlockState> joins;
	private final Map<Integer, VoxelShape> shapes = new java.util.HashMap<>();

	/** {@code radius}: half the thickness in pixels; {@code joins}: machines this conduit connects to. */
	public ConduitBlock(Properties properties, double radius, Predicate<BlockState> joins) {
		super(properties);
		this.radius = radius;
		this.joins = joins;
		BlockState state = stateDefinition.any();
		for (BooleanProperty p : SIDES.values()) {
			state = state.setValue(p, false);
		}
		registerDefaultState(state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(SIDES.values().toArray(new BooleanProperty[0]));
	}

	public boolean connectsTo(BlockState other) {
		return other.getBlock() == this || joins.test(other);
	}

	/** The state with every side joined that has something to join. */
	public BlockState connected(BlockGetter level, BlockPos pos) {
		BlockState state = defaultBlockState();
		for (Map.Entry<Direction, BooleanProperty> e : SIDES.entrySet()) {
			state = state.setValue(e.getValue(), connectsTo(level.getBlockState(pos.relative(e.getKey()))));
		}
		return state;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return connected(context.getLevel(), context.getClickedPos());
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
			Direction direction, BlockPos neighbourPos, BlockState neighbour, RandomSource random) {
		return state.setValue(SIDES.get(direction), connectsTo(neighbour));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		int key = 0;
		int bit = 0;
		for (BooleanProperty p : SIDES.values()) {
			if (state.getValue(p)) {
				key |= 1 << bit;
			}
			bit++;
		}
		return shapes.computeIfAbsent(key, k -> shapeFor(state));
	}

	private VoxelShape shapeFor(BlockState state) {
		double lo = 8 - radius;
		double hi = 8 + radius;
		VoxelShape shape = Block.box(lo, lo, lo, hi, hi, hi);
		for (Map.Entry<Direction, BooleanProperty> e : SIDES.entrySet()) {
			if (!state.getValue(e.getValue())) {
				continue;
			}
			shape = Shapes.or(shape, switch (e.getKey()) {
				case NORTH -> Block.box(lo, lo, 0, hi, hi, lo);
				case SOUTH -> Block.box(lo, lo, hi, hi, hi, 16);
				case WEST -> Block.box(0, lo, lo, lo, hi, hi);
				case EAST -> Block.box(hi, lo, lo, 16, hi, hi);
				case DOWN -> Block.box(lo, 0, lo, hi, lo, hi);
				case UP -> Block.box(lo, hi, lo, hi, 16, hi);
			});
		}
		return shape;
	}
}
