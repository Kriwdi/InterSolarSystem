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

/** A spaceport facility's control cabinet (see {@link Spaceports#cabinet}). */
public class FacilityCabinetBlock extends ShipFacingBlock {
	public enum Facility { POWER, CONTROL, FABRICATION, FUEL, CRANE }

	private final hu.kriwdi.intersolarsystem.spaceport.Facility facility;

	public FacilityCabinetBlock(Properties properties, Facility facility, double[]... boxes) {
		super(properties, boxes);
		this.facility = hu.kriwdi.intersolarsystem.spaceport.Facility.values()[facility.ordinal()];
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (stack.getItem() == ModItems.MULTITOOL || stack.getItem() == ModItems.CUTTING_TORCH) {
			return InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			Spaceports.cabinet(serverLevel, pos, serverPlayer, stack, facility);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			Spaceports.cabinet(serverLevel, pos, serverPlayer, ItemStack.EMPTY, facility);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
