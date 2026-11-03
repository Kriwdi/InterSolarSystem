package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

import hu.kriwdi.intersolarsystem.ship.FlightControls;

/**
 * The throttle lever beside the pilot's seat. Each click pushes it one notch forward; at full thrust the launch
 * starts if the checklist is complete (otherwise the lever springs back and the missing steps are named).
 */
public class ThrottleBlock extends ShipFacingBlock {
	public static final int FULL = 3;
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, FULL);

	public ThrottleBlock(Properties properties) {
		super(properties, new double[] {3, 0, 3, 13, 8, 13}, new double[] {5, 8, 4, 11, 14, 12});
		registerDefaultState(defaultBlockState().setValue(LEVEL, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LEVEL);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}
		int next = Math.min(FULL, state.getValue(LEVEL) + 1);
		level.setBlock(pos, state.setValue(LEVEL, next), Block.UPDATE_ALL);
		level.playSound(null, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.4F, 1.2F + next * 0.1F);
		if (next == FULL && !FlightControls.throttleUp(serverLevel.getServer(), serverPlayer)) {
			level.setBlock(pos, state.setValue(LEVEL, 0), Block.UPDATE_ALL);
			level.playSound(null, pos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, 0.8F);
		}
		return InteractionResult.SUCCESS_SERVER;
	}
}
