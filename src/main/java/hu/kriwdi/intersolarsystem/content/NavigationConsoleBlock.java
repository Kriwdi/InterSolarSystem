package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipNavigation;
import hu.kriwdi.intersolarsystem.ship.ShipRepair;
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
