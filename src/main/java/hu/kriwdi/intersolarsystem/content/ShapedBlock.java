package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A non-rotating block whose outline and collision follow its model (boxes in model pixels). */
public class ShapedBlock extends Block {
	private final VoxelShape shape;

	public ShapedBlock(Properties properties, double[]... boxes) {
		super(properties);
		VoxelShape s = Shapes.empty();
		for (double[] b : boxes) {
			s = Shapes.or(s, Block.box(b[0], b[1], b[2], b[3], b[4], b[5]));
		}
		shape = s;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shape;
	}
}
