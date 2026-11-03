package hu.kriwdi.intersolarsystem.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

import hu.kriwdi.intersolarsystem.content.ModBlocks;

/**
 * The remains of trees on the dead Earth. {@code petrified}: stone trunks, broken off at different heights, with
 * stubby branches, or lying fallen; {@code dead}: bleached trunks with bare crooked branches; {@code stump}: a charred
 * stump one or two blocks high. Everything stays within a few blocks of the origin.
 */
public record DeadTreeFeature(String kind) implements Feature {
	public static final MapCodec<DeadTreeFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.fieldOf("kind").forGetter(DeadTreeFeature::kind)
	).apply(instance, DeadTreeFeature::new));

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		BlockState ground = level.getBlockState(origin.below());
		if (ground.isAir() || !ground.getFluidState().isEmpty() || !level.getBlockState(origin).isAir()) {
			return false;
		}
		Block log = "petrified".equals(kind) ? ModBlocks.PETRIFIED_LOG : ModBlocks.DEAD_LOG;
		if ("stump".equals(kind)) {
			int h = 1 + random.nextInt(2);
			for (int i = 0; i < h; i++) {
				set(level, origin.above(i), log, Direction.Axis.Y);
			}
			return true;
		}
		if ("petrified".equals(kind) && random.nextInt(4) == 0) {
			// A fallen trunk, half sunk into the ground.
			Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
			int length = 4 + random.nextInt(4);
			for (int i = 0; i < length; i++) {
				BlockPos p = origin.relative(dir, i);
				if (level.getBlockState(p.below()).isAir()) {
					p = p.below();
				}
				set(level, p, log, dir.getAxis());
			}
			return true;
		}
		int height = "petrified".equals(kind) ? 3 + random.nextInt(6) : 5 + random.nextInt(5);
		for (int i = 0; i < height; i++) {
			set(level, origin.above(i), log, Direction.Axis.Y);
		}
		int branches = 1 + random.nextInt("petrified".equals(kind) ? 2 : 4);
		for (int b = 0; b < branches; b++) {
			Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
			int y = height / 2 + random.nextInt(Math.max(1, height - height / 2));
			int length = 1 + random.nextInt("petrified".equals(kind) ? 2 : 3);
			BlockPos p = origin.above(y);
			for (int i = 1; i <= length; i++) {
				p = p.relative(dir);
				if (i > 1 && random.nextBoolean()) {
					p = p.above(); // dead branches reach upwards
				}
				if (level.getBlockState(p).isAir()) {
					set(level, p, log, i > 1 && random.nextBoolean() ? Direction.Axis.Y : dir.getAxis());
				}
			}
		}
		if ("petrified".equals(kind)) {
			// Chips of the broken trunk around the base.
			for (int i = 0; i < 3; i++) {
				BlockPos p = origin.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
				if (level.getBlockState(p).isAir() && !level.getBlockState(p.below()).isAir()) {
					level.setBlock(p, ModBlocks.RUBBLE.defaultBlockState(), 2);
				}
			}
		}
		return true;
	}

	private static void set(WorldGenLevel level, BlockPos pos, Block log, Direction.Axis axis) {
		level.setBlock(pos, log.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis), 2);
	}
}
