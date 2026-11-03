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

import hu.kriwdi.intersolarsystem.salvage.Machines;

/** A machine of the salvage chain (recycler, its modules, generator, battery bank); the work is in {@link Machines}. */
public class SalvageMachineBlock extends ShipFacingBlock {
	public SalvageMachineBlock(Properties properties, double[]... boxes) {
		super(properties, boxes);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (stack.getItem() == ModItems.MULTITOOL || stack.getItem() == ModItems.CUTTING_TORCH) {
			return InteractionResult.PASS; // taken apart instead (see Disassembly)
		}
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			return Machines.interact(serverLevel, pos, serverPlayer, stack) ? InteractionResult.SUCCESS_SERVER
					: InteractionResult.PASS;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			Machines.interact(serverLevel, pos, serverPlayer, ItemStack.EMPTY);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
