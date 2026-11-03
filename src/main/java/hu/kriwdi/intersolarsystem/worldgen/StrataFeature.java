package hu.kriwdi.intersolarsystem.worldgen;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Colours the rock of the deep canyons in sediment bands (rust, ochre, cream and brown terracotta), so the walls the
 * canyon carver cuts show the layers. Works on the 16x16 columns of its own chunk, only where the biome is the canyon.
 */
public final class StrataFeature implements Feature {
	public static final StrataFeature INSTANCE = new StrataFeature();
	public static final MapCodec<StrataFeature> CODEC = MapCodec.unit(INSTANCE);

	/** Band colours by id (the coloured terracotta blocks are no longer single fields of {@code Blocks}). */
	private static final String[] BAND_IDS = {"terracotta", "orange_terracotta", "terracotta", "red_terracotta",
			"yellow_terracotta", "terracotta", "brown_terracotta", "orange_terracotta", "white_terracotta",
			"light_gray_terracotta", "red_terracotta", "terracotta"};
	private static Block[] bands;

	private static Block[] bands() {
		if (bands == null) {
			Block[] b = new Block[BAND_IDS.length];
			for (int i = 0; i < b.length; i++) {
				b[i] = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(BAND_IDS[i]));
			}
			bands = b;
		}
		return bands;
	}

	private StrataFeature() {
	}

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	/** Band for a height: 1-3 block thick layers with a slow wobble. */
	static Block band(int y, int x, int z) {
		int wobble = (int) Math.round(Math.sin(x * 0.05) * 1.5 + Math.cos(z * 0.04) * 1.5);
		int layer = Math.floorMod((y + wobble) / 2 + (y + wobble) / 5, BAND_IDS.length);
		return bands()[layer];
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		ResourceKey<Biome> canyon = DeadEarth.DEEP_CANYONS;
		int cx = origin.getX() & ~15;
		int cz = origin.getZ() & ~15;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = cx; x < cx + 16; x++) {
			for (int z = cz; z < cz + 16; z++) {
				int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
				pos.set(x, top, z);
				if (!level.getBiome(pos).is(canyon)) {
					continue;
				}
				for (int y = top; y > Math.max(level.getMinY() + 8, 0); y--) {
					pos.setY(y);
					BlockState state = level.getBlockState(pos);
					if (state.is(Blocks.STONE) || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE)
							|| state.is(Blocks.GRANITE) || state.is(Blocks.TUFF)) {
						level.setBlock(pos, band(y, x, z).defaultBlockState(), 2);
					}
				}
			}
		}
		return true;
	}
}
