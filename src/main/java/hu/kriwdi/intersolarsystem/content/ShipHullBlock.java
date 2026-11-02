package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.ship.ShipDimensions;

/** A block of the ship's hull: it cannot be mined while inside the ship, so nobody can dig out into the void. */
public class ShipHullBlock extends Block {
	public ShipHullBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
		if (level instanceof Level l && l.dimension().equals(ShipDimensions.INTERIOR)) {
			return 0.0F;
		}
		return super.getDestroyProgress(state, player, level, pos);
	}
}
