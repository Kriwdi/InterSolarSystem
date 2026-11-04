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
	/** Logs 0..3 are the Moon base's (told in chat); 4 and up are Earth logs, shown on the terminal's screen. */
	public static final int LOGS = 16;
	public static final int MOON_LOGS = 4;
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

	/** A data tape in hand: the terminal plays it. */
	@Override
	protected InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level,
			BlockPos pos, Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
		if (stack.getItem() != ModItems.DATA_TAPE) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level instanceof net.minecraft.server.level.ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			hu.kriwdi.intersolarsystem.lore.TapeDecks.play(serverLevel, pos, serverPlayer, stack);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (player instanceof ServerPlayer serverPlayer) {
			int log = state.getValue(LOG);
			level.playSound(null, pos, net.minecraft.sounds.SoundEvents.BEACON_ACTIVATE, net.minecraft.sounds.SoundSource.BLOCKS,
					0.4F, 1.6F);
			if (log >= MOON_LOGS) {
				net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(serverPlayer,
						new hu.kriwdi.intersolarsystem.lore.LoreOpenPayload("terminal", "terminal_" + log));
				return InteractionResult.SUCCESS_SERVER;
			}
			serverPlayer.sendSystemMessage(Component.translatable("lore.intersolarsystem.terminal.header", log + 1));
			serverPlayer.sendSystemMessage(Component.translatable("lore.intersolarsystem.terminal." + log));
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
