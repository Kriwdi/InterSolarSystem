package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import hu.kriwdi.intersolarsystem.ship.ShipDiagnosticScan;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipRepair;

/**
 * The ship's diagnostic console. Right-click: scan for hidden faults and estimate the takeoff risk. Sneak + right-click
 * (twice, to confirm): start a test hop. Works only inside the ship.
 */
public class DiagnosticConsoleBlock extends ShipHullBlock {
	public DiagnosticConsoleBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			if (!level.dimension().equals(ShipDimensions.INTERIOR)) {
				ShipRepair.showOutsideShip(serverPlayer);
			} else if (player.isShiftKeyDown()) {
				ShipDiagnosticScan.requestTestHop(serverLevel.getServer(), serverPlayer);
			} else {
				ShipDiagnosticScan.scan(serverLevel.getServer(), serverPlayer);
			}
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
