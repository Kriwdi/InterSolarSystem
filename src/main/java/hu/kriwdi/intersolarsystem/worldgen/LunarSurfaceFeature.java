package hu.kriwdi.intersolarsystem.worldgen;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

import hu.kriwdi.intersolarsystem.content.ModBlocks;

/**
 * Dresses one chunk of bare lunar rock: a regolith blanket on top, dark mare regolith and basalt where the maria lie,
 * helium-3 rich regolith near the surface and ilmenite (titanium ore) in the mare basalt.
 */
public final class LunarSurfaceFeature implements Feature {
	public static final LunarSurfaceFeature INSTANCE = new LunarSurfaceFeature();
	public static final MapCodec<LunarSurfaceFeature> CODEC = MapCodec.unit(INSTANCE);
	public static final int REGOLITH_DEPTH = 3;
	public static final int BASALT_DEPTH = 7;

	private LunarSurfaceFeature() {
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
				int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
				boolean mare = LunarTerrain.isMare(seed, x, z);
				for (int depth = 0; depth < REGOLITH_DEPTH + BASALT_DEPTH; depth++) {
					int y = top - depth;
					if (y < level.getMinY()) {
						break;
					}
					pos.set(x, y, z);
					if (!level.getBlockState(pos).is(ModBlocks.LUNAR_ROCK)) {
						continue;
					}
					BlockState state;
					if (depth < REGOLITH_DEPTH) {
						state = random.nextInt(140) == 0 ? ModBlocks.HELIUM3_REGOLITH.defaultBlockState()
								: mare ? ModBlocks.MARE_REGOLITH.defaultBlockState()
										: ModBlocks.LUNAR_REGOLITH.defaultBlockState();
					} else if (mare) {
						state = random.nextInt(45) == 0 ? ModBlocks.ILMENITE_ORE.defaultBlockState()
								: ModBlocks.MARE_BASALT.defaultBlockState();
					} else {
						break;
					}
					level.setBlock(pos, state, 2);
				}
			}
		}
		return true;
	}
}
