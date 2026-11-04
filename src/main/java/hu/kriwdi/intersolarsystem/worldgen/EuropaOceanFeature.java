package hu.kriwdi.intersolarsystem.worldgen;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

import hu.kriwdi.intersolarsystem.content.PlanetBlocks;

/**
 * Europa's hidden ocean: under an ice shell some twenty-five blocks thick, dark water down to a salty sea floor. Where
 * the floor is warm (hydrothermal vents: smooth noise) it glows with microbial mats, the only life found beyond
 * Earth; the vents themselves are columns of hot rock.
 */
public final class EuropaOceanFeature implements Feature {
	public static final EuropaOceanFeature INSTANCE = new EuropaOceanFeature();
	public static final MapCodec<EuropaOceanFeature> CODEC = MapCodec.unit(INSTANCE);
	public static final int FLOOR = 10;
	public static final int SHELL = 24;

	private EuropaOceanFeature() {
	}

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int baseX = origin.getX() & ~15;
		int baseZ = origin.getZ() & ~15;
		long seed = level.getSeed();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = baseX + dx;
				int z = baseZ + dz;
				int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
				int ceiling = surface - SHELL;
				if (ceiling <= FLOOR + 4) {
					continue;
				}
				for (int y = FLOOR + 1; y < ceiling; y++) {
					pos.set(x, y, z);
					level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
				}
				double warm = CloudDeckFeature.noise(seed ^ 0xE0L, x, z, 40.0);
				pos.set(x, FLOOR, z);
				level.setBlock(pos, warm > 0.68 ? PlanetBlocks.MICROBIAL_MAT.defaultBlockState()
						: PlanetBlocks.EUROPA_SALT.defaultBlockState(), 2);
				if (warm > 0.8 && random.nextInt(90) == 0) {
					for (int y = FLOOR + 1; y < FLOOR + 4 + random.nextInt(4); y++) {
						pos.set(x, y, z);
						level.setBlock(pos, Blocks.MAGMA_BLOCK.defaultBlockState(), 2);
					}
				}
			}
		}
		return true;
	}
}
