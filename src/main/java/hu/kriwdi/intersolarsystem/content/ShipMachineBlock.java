package hu.kriwdi.intersolarsystem.content;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A ship machine with a supply that runs down with every flight and is topped up by hand: the reactor (fuel rods),
 * the coolant tank (clean water or ice), the life support unit (air filters). The level shows on its gauge.
 */
public class ShipMachineBlock extends ShipFacingBlock {
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 8);

	private final int max;
	private final Predicate<ItemStack> refill;
	private final int perItem;
	private final String key;
	private final SoundEvent sound;

	public ShipMachineBlock(Properties properties, int max, Predicate<ItemStack> refill, int perItem, String key,
			SoundEvent sound, double[]... boxes) {
		super(properties, boxes);
		this.max = max;
		this.refill = refill;
		this.perItem = perItem;
		this.key = key;
		this.sound = sound;
		registerDefaultState(defaultBlockState().setValue(LEVEL, max));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LEVEL);
	}

	public int max() {
		return max;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (!refill.test(stack)) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}
		int now = state.getValue(LEVEL);
		if (now >= max) {
			serverPlayer.sendOverlayMessage(Component.translatable("message.intersolarsystem.machine.full",
					Component.translatable("block.intersolarsystem." + key)));
			return InteractionResult.SUCCESS_SERVER;
		}
		int next = Math.min(max, now + perItem);
		level.setBlock(pos, state.setValue(LEVEL, next), Block.UPDATE_ALL);
		stack.consume(1, serverPlayer);
		level.playSound(null, pos, sound, SoundSource.BLOCKS, 0.8F, 1.0F);
		serverPlayer.sendOverlayMessage(Component.translatable("message.intersolarsystem.machine.level",
				Component.translatable("block.intersolarsystem." + key), next, max));
		return InteractionResult.SUCCESS_SERVER;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendOverlayMessage(Component.translatable("message.intersolarsystem.machine.level",
					Component.translatable("block.intersolarsystem." + key), state.getValue(LEVEL), max));
			serverPlayer.sendSystemMessage(Component.translatable("message.intersolarsystem.machine.hint." + key));
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
