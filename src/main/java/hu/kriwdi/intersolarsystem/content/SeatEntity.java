package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * The invisible thing a player rides while sitting in a seat. It exists only while someone sits: it disappears as soon
 * as the seat is empty or the seat block is gone (also right after a world is loaded).
 */
public class SeatEntity extends Entity {
	public SeatEntity(EntityType<?> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
		setInvisible(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		return false;
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}

	private BlockPos seatPos() {
		return BlockPos.containing(getX(), getY() + 0.25, getZ());
	}

	@Override
	public void tick() {
		super.tick();
		if (level() instanceof ServerLevel && (getPassengers().isEmpty()
				|| !(level().getBlockState(seatPos()).getBlock() instanceof AstronautSeatBlock))) {
			discard();
		}
	}

	/** Getting up: stand in front of the seat. */
	@Override
	public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
		BlockState seat = level().getBlockState(seatPos());
		Direction front = seat.getBlock() instanceof AstronautSeatBlock ? seat.getValue(ShipFacingBlock.FACING)
				: Direction.NORTH;
		BlockPos stand = seatPos().relative(front);
		if (!level().getBlockState(stand).getCollisionShape(level(), stand).isEmpty()) {
			stand = seatPos().above();
		}
		return Vec3.atBottomCenterOf(stand);
	}
}
