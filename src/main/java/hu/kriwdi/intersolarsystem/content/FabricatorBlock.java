package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import hu.kriwdi.intersolarsystem.spaceport.Spaceports;

/** A spaceport machine (see {@link Spaceports#fabricator}). */
public class FabricatorBlock extends ShipFacingBlock {
	public FabricatorBlock(Properties properties, double[]... boxes) {
		super(properties, boxes);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (stack.getItem() == ModItems.MULTITOOL || stack.getItem() == ModItems.CUTTING_TORCH) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			Spaceports.fabricator(serverLevel, pos, serverPlayer, stack);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			Spaceports.fabricator(serverLevel, pos, serverPlayer, ItemStack.EMPTY);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
