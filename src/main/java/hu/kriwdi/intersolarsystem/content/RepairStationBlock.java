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

import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipRepair;

/**
 * The ship's repair station: use a repair kit on it to fix an incident or the worst subsystem. It only works inside the
 * ship; other items pass through so blocks can still be placed against it.
 */
public class RepairStationBlock extends ShipFacingBlock {
	public RepairStationBlock(Properties properties) {
		// Pegboard, bench top with legs, the toolbox and vise on top (matches the model).
		super(properties, new double[] {0, 0, 13, 16, 16, 16}, new double[] {0, 0, 1, 16, 12, 13},
				new double[] {2, 12, 4, 7, 16, 8}, new double[] {10, 12, 1, 14, 15, 8});
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (stack.getItem() != ModItems.REPAIR_KIT) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			if (!isInsideShip(level)) {
				ShipRepair.showOutsideShip(serverPlayer);
				return InteractionResult.SUCCESS_SERVER;
			}
			ShipRepair.useKit(serverLevel.getServer(), serverPlayer, stack);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			if (!isInsideShip(level)) {
				ShipRepair.showOutsideShip(serverPlayer);
				return InteractionResult.SUCCESS_SERVER;
			}
			ShipRepair.showNeedsKit(serverLevel.getServer(), serverPlayer);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}

	private static boolean isInsideShip(Level level) {
		return level.dimension().equals(ShipDimensions.INTERIOR);
	}
}
