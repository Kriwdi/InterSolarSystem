package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import hu.kriwdi.intersolarsystem.ship.ShipFaultSites;

/**
 * Where a ship incident shows itself: a sparking wall panel (short circuit), a hissing steam vent (overheating), a
 * spraying pipe (leak, pressure loss) or an electrical fire. It is fixed right there with the matching tool (or a
 * repair kit), mostly through the repair minigame; when the incident is gone the ship's own part comes back.
 */
public class FaultBlock extends ShipHullBlock {
	public enum Kind { SPARKS, STEAM, LEAK, FIRE }

	private final Kind kind;

	public FaultBlock(Properties properties, Kind kind) {
		super(properties);
		this.kind = kind;
	}

	public Kind kind() {
		return kind;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (kind) {
			case LEAK -> Block.box(5, 5, 0, 11, 11, 16);
			case FIRE -> Block.box(1, 0, 1, 15, 10, 15);
			default -> Shapes.block();
		};
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return kind == Kind.FIRE ? Shapes.empty() : getShape(state, level, pos, context);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			ShipFaultSites.repairAt(serverLevel.getServer(), serverPlayer, pos, kind, stack);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			ShipFaultSites.repairAt(serverLevel.getServer(), serverPlayer, pos, kind, ItemStack.EMPTY);
			return InteractionResult.SUCCESS_SERVER;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.5;
		double z = pos.getZ() + 0.5;
		switch (kind) {
			case SPARKS -> {
				for (int i = 0; i < 3; i++) {
					level.addParticle(ParticleTypes.ELECTRIC_SPARK, x + random.nextGaussian() * 0.4,
							y + random.nextGaussian() * 0.3, z + random.nextGaussian() * 0.4, random.nextGaussian() * 0.1,
							0.05, random.nextGaussian() * 0.1);
				}
				if (random.nextInt(4) == 0) {
					level.playLocalSound(pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.4F,
							1.5F + random.nextFloat() * 0.5F, false);
				}
			}
			case STEAM -> {
				for (int i = 0; i < 3; i++) {
					level.addParticle(ParticleTypes.CLOUD, x + random.nextGaussian() * 0.3, y + 0.2,
							z + random.nextGaussian() * 0.3, random.nextGaussian() * 0.05, 0.08, random.nextGaussian() * 0.05);
				}
				if (random.nextInt(3) == 0) {
					level.playLocalSound(pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.3F,
							0.6F + random.nextFloat() * 0.3F, false);
				}
			}
			case LEAK -> {
				for (int i = 0; i < 2; i++) {
					level.addParticle(ParticleTypes.DRIPPING_WATER, x + random.nextGaussian() * 0.15, y - 0.2,
							z + random.nextGaussian() * 0.15, 0, 0, 0);
					level.addParticle(ParticleTypes.WHITE_SMOKE, x, y, z, random.nextGaussian() * 0.12, 0.02,
							random.nextGaussian() * 0.12);
				}
				if (random.nextInt(3) == 0) {
					level.playLocalSound(pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.25F, 1.6F, false);
				}
			}
			case FIRE -> {
				for (int i = 0; i < 4; i++) {
					level.addParticle(ParticleTypes.FLAME, x + random.nextGaussian() * 0.3, pos.getY() + 0.2,
							z + random.nextGaussian() * 0.3, 0, 0.04, 0);
					level.addParticle(ParticleTypes.LARGE_SMOKE, x + random.nextGaussian() * 0.3, y + 0.3,
							z + random.nextGaussian() * 0.3, 0, 0.05, 0);
				}
				if (random.nextInt(2) == 0) {
					level.playLocalSound(pos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 0.8F, 1.0F, false);
				}
			}
		}
	}
}
