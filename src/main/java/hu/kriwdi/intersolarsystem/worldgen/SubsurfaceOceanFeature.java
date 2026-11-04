package hu.kriwdi.intersolarsystem.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * A sea under an ice shell (Enceladus), like Europa's ({@link EuropaOceanFeature}) but configurable: dark water from
 * {@code floor_y} up to {@code shell} blocks under the surface, a rocky sea floor, and where hydrothermal vents warm
 * it, life: glowing mats ({@code life}) and mounds of vent colonies ({@code colony}, a few blocks high) round the
 * vents' hot rock ({@code vent}).
 */
public record SubsurfaceOceanFeature(BlockState floor, BlockState life, BlockState colony, BlockState vent, int floorY,
		int shell) implements Feature {
	public static final MapCodec<SubsurfaceOceanFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockState.CODEC.fieldOf("floor").forGetter(SubsurfaceOceanFeature::floor),
			BlockState.CODEC.fieldOf("life").forGetter(SubsurfaceOceanFeature::life),
			BlockState.CODEC.fieldOf("colony").forGetter(SubsurfaceOceanFeature::colony),
			BlockState.CODEC.fieldOf("vent").forGetter(SubsurfaceOceanFeature::vent),
			Codec.INT.fieldOf("floor_y").forGetter(SubsurfaceOceanFeature::floorY),
			Codec.intRange(4, 200).fieldOf("shell").forGetter(SubsurfaceOceanFeature::shell)
	).apply(instance, SubsurfaceOceanFeature::new));

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int baseX = origin.getX() & ~15;
		int baseZ = origin.getZ() & ~15;
		long seed = level.getSeed() ^ 0xE7C3L;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = baseX + dx;
				int z = baseZ + dz;
				int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
				int ceiling = surface - shell;
				if (ceiling <= floorY + 4) {
					continue;
				}
				for (int y = floorY + 1; y < ceiling; y++) {
					pos.set(x, y, z);
					level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
				}
				double warm = CloudDeckFeature.noise(seed, x, z, 36.0);
				pos.set(x, floorY, z);
				level.setBlock(pos, warm > 0.64 ? life : floor, 2);
				if (warm > 0.74 && random.nextInt(14) == 0) {
					int h = 1 + random.nextInt(3);
					for (int y = floorY + 1; y <= floorY + h; y++) {
						pos.set(x, y, z);
						level.setBlock(pos, colony, 2);
					}
				}
				if (warm > 0.82 && random.nextInt(70) == 0) {
					for (int y = floorY + 1; y < floorY + 4 + random.nextInt(5); y++) {
						pos.set(x, y, z);
						level.setBlock(pos, vent, 2);
					}
				}
			}
		}
		return true;
	}
}
