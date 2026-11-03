package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import hu.kriwdi.intersolarsystem.ship.ShipBoarding;

/**
 * The upper half of the two-block hatch. It has no model of its own (the hatch below draws the whole door) and passes
 * a click on to the hatch below it.
 */
public class ShipHatchTopBlock extends ShipFacingBlock {
	public ShipHatchTopBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			if (level.getBlockState(pos.below()).is(ModBlocks.SHIP_HATCH)) {
				ShipBoarding.use(serverLevel, pos.below(), serverPlayer);
			}
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
