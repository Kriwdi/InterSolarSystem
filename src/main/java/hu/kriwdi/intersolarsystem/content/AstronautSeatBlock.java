package hu.kriwdi.intersolarsystem.content;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * A padded flight seat with a headrest, armrests and a harness. Right click to sit down (sneak to get up, like leaving a
 * boat); the seat faces its front, so whoever sits looks the way the seat looks.
 */
public class AstronautSeatBlock extends ShipFacingBlock {
	/** Height of the cushion, where the sitter's seat entity is placed. */
	static final double SIT_HEIGHT = 0.3;

	public AstronautSeatBlock(Properties properties) {
		// Pedestal, cushion, backrest (the collision stops at the block top; the model's headrest rises above it).
		super(properties, new double[] {5, 0, 5, 11, 4, 11}, new double[] {2, 4, 1, 14, 7, 14},
				new double[] {2, 7, 13, 14, 16, 16}, new double[] {0, 7, 2, 2, 10, 13},
				new double[] {14, 7, 2, 16, 10, 13});
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		if (player.isPassenger()) {
			return InteractionResult.PASS;
		}
		List<SeatEntity> taken = serverLevel.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
		if (!taken.isEmpty()) {
			return InteractionResult.PASS;
		}
		SeatEntity seat = new SeatEntity(ModEntities.SEAT, serverLevel);
		float yaw = state.getValue(FACING).toYRot();
		seat.snapTo(pos.getX() + 0.5, pos.getY() + SIT_HEIGHT, pos.getZ() + 0.5, yaw, 0.0F);
		serverLevel.addFreshEntity(seat);
		// Forced: the seat is not a vehicle the player chose to board in the usual way (no boarding cooldown).
		boolean sat = player.startRiding(seat, true, true);
		hu.kriwdi.intersolarsystem.InterSolarSystem.LOGGER.info("[seat] {} sits down at {}: {}",
				player.getName().getString(), pos.toShortString(), sat);
		if (!sat) {
			seat.discard();
			return InteractionResult.PASS;
		}
		player.setYRot(yaw);
		player.setYHeadRot(yaw);
		return InteractionResult.SUCCESS_SERVER;
	}
}
