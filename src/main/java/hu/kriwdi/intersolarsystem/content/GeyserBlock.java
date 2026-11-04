package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * A geyser vent. Now and then (a random tick) it erupts: a plume of ice crystals or nitrogen gas and dark dust stands
 * over it for a few seconds and throws whoever stands in it high into the low-gravity sky (Enceladus' water geysers
 * reach hundreds of kilometres; Triton's nitrogen plumes eight). {@code lift}: the upward speed it gives;
 * {@code reach}: the plume's height in blocks.
 */
public class GeyserBlock extends Block {
	public static final BooleanProperty ACTIVE = BlockStateProperties.LIT;

	private final double lift;
	private final int reach;
	private final boolean dark;

	public GeyserBlock(Properties properties, double lift, int reach, boolean dark) {
		super(properties);
		this.lift = lift;
		this.reach = reach;
		this.dark = dark;
		registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.getValue(ACTIVE) && level.getBlockState(pos.above()).isAir()) {
			level.setBlock(pos, state.setValue(ACTIVE, true), Block.UPDATE_ALL);
			level.scheduleTick(pos, this, 2);
			level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.6F, dark ? 0.4F : 0.6F);
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!state.getValue(ACTIVE)) {
			return;
		}
		erupt(level, pos, random);
		// A plume lasts a few seconds.
		if (random.nextInt(18) == 0) {
			level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
		} else {
			level.scheduleTick(pos, this, 4);
		}
	}

	private void erupt(ServerLevel level, BlockPos pos, RandomSource random) {
		ParticleOptions main = dark ? ParticleTypes.LARGE_SMOKE : ParticleTypes.CLOUD;
		ParticleOptions crystals = dark ? ParticleTypes.ASH : ParticleTypes.SNOWFLAKE;
		for (int h = 1; h < reach; h += 2) {
			double spread = 0.2 + h * 0.03;
			level.sendParticles(main, pos.getX() + 0.5, pos.getY() + h, pos.getZ() + 0.5, 2, spread, 0.6, spread, 0.02);
			level.sendParticles(crystals, pos.getX() + 0.5, pos.getY() + h, pos.getZ() + 0.5, 3, spread * 1.5, 0.8,
					spread * 1.5, 0.05);
		}
		AABB plume = new AABB(pos.getX() - 0.8, pos.getY() + 1, pos.getZ() - 0.8, pos.getX() + 1.8, pos.getY() + 1 + reach,
				pos.getZ() + 1.8);
		for (Entity e : level.getEntities((Entity) null, plume, en -> !en.isSpectator())) {
			Vec3 v = e.getDeltaMovement();
			e.setDeltaMovement(v.x + (random.nextDouble() - 0.5) * 0.1, Math.max(v.y, lift), v.z
					+ (random.nextDouble() - 0.5) * 0.1);
			e.needsSync = true;
			if (e instanceof ServerPlayer player) {
				player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
			}
		}
	}
}
