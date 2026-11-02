package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A data terminal left behind in a ruin. Using it plays one of the logs stored on it, a piece of the story of what
 * happened. The log number is part of the block state, so each terminal keeps its own story.
 */
public class DataTerminalBlock extends ShipFacingBlock {
	public static final int LOGS = 4;
	public static final IntegerProperty LOG = IntegerProperty.create("log", 0, LOGS - 1);

	public DataTerminalBlock(Properties properties) {
		// Desk with a slanted screen (matches the model).
		super(properties, new double[] {2, 0, 3, 14, 7, 13}, new double[] {3, 7, 6, 13, 14, 12});
		registerDefaultState(defaultBlockState().setValue(LOG, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LOG);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (player instanceof ServerPlayer serverPlayer) {
			int log = state.getValue(LOG);
			serverPlayer.sendSystemMessage(Component.translatable("lore.intersolarsystem.terminal.header", log + 1));
			serverPlayer.sendSystemMessage(Component.translatable("lore.intersolarsystem.terminal." + log));
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
