package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import hu.kriwdi.intersolarsystem.spaceport.Mothership;

/** Part of the mothership (see {@link Mothership}). */
public class HangarLiftBlock extends ShipFacingBlock {
	public HangarLiftBlock(Properties properties, double[]... boxes) {
		super(properties, boxes);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			if (serverLevel.dimension().equals(hu.kriwdi.intersolarsystem.space.Worlds.VENUS.dimension())) {
				// Aphrodite's own lift between the cloud city and the surface (the mothership is reached by ship).
				hu.kriwdi.intersolarsystem.space.Worlds.venusLift(serverLevel, serverPlayer);
				return InteractionResult.SUCCESS_SERVER;
			}
			Mothership.get(serverLevel.getServer()).lift(serverLevel, serverPlayer);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}
}
