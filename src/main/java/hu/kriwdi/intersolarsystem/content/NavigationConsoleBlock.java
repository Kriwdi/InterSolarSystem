package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipFlightPhase;
import hu.kriwdi.intersolarsystem.ship.ShipNavigation;
import hu.kriwdi.intersolarsystem.ship.ShipRepair;
import hu.kriwdi.intersolarsystem.ship.ShipState;
import hu.kriwdi.intersolarsystem.ship.ShipStateAccess;
import hu.kriwdi.intersolarsystem.ship.ShipTravelEvents;

/**
 * The navigation console: opens the star map to choose a destination; during a travel event that needs the crew (a
 * meteor shower) a click steers the ship clear instead.
 */
public class NavigationConsoleBlock extends ShipFacingBlock {
	public NavigationConsoleBlock(Properties properties) {
		// Base plate, column and the tilted chart table (matches the model).
		super(properties, new double[] {2, 0, 2, 14, 2, 14}, new double[] {5, 2, 5, 11, 10, 11},
				new double[] {1, 9, 1, 15, 13, 15});
	}

	/** A drive kit in hand installs the next engine tier (ion drive: 2, fusion drive: 3) while the ship is landed. */
	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		int kitTier = stack.is(ModItems.ION_DRIVE_KIT) ? 2 : stack.is(ModItems.FUSION_DRIVE_KIT) ? 3 : 0;
		if (kitTier == 0) {
			return stack.isEmpty() ? InteractionResult.TRY_WITH_EMPTY_HAND : InteractionResult.PASS;
		}
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			if (!level.dimension().equals(ShipDimensions.INTERIOR)) {
				ShipRepair.showOutsideShip(serverPlayer);
				return InteractionResult.SUCCESS_SERVER;
			}
			ShipState ship = ShipStateAccess.get(serverLevel.getServer());
			int current = ship.travel().engineTier();
			if (ship.isReadOnly() || ship.phase() != ShipFlightPhase.LANDED) {
				serverPlayer.sendSystemMessage(Component.translatable("message.intersolarsystem.engine.not_landed"));
			} else if (current >= kitTier) {
				serverPlayer.sendSystemMessage(Component.translatable("message.intersolarsystem.engine.already", current));
			} else if (current != kitTier - 1) {
				serverPlayer.sendSystemMessage(Component.translatable("message.intersolarsystem.engine.needs_previous",
						kitTier - 1));
			} else {
				ship.setEngineTier(kitTier);
				stack.consume(1, serverPlayer);
				serverPlayer.sendSystemMessage(Component.translatable("message.intersolarsystem.engine.installed", kitTier,
						hu.kriwdi.intersolarsystem.travel.TravelMath.multiplier(kitTier)));
				serverLevel.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8F, 1.3F);
			}
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			if (!level.dimension().equals(ShipDimensions.INTERIOR)) {
				ShipRepair.showOutsideShip(serverPlayer);
			} else if (!ShipTravelEvents.respond(serverLevel.getServer(), serverPlayer)) {
				ShipNavigation.open(serverLevel.getServer(), serverPlayer);
			}
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
