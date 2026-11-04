package hu.kriwdi.intersolarsystem.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

import hu.kriwdi.intersolarsystem.content.ModBlocks;

/**
 * Dresses one chunk of bare Martian basalt for its biome: the top block (with patches of an alternative), the layer
 * under it, hematite spherules in the rock below; towards the poles the layered water-ice caps take over, thicker the
 * closer to the pole (by latitude: {@link #POLAR_CAP} blocks from the equator).
 */
public record MarsSurfaceFeature(BlockState top, BlockState alt, float altChance, BlockState under, int depth)
		implements Feature {
	public static final MapCodec<MarsSurfaceFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockState.CODEC.fieldOf("top").forGetter(MarsSurfaceFeature::top),
			BlockState.CODEC.fieldOf("alt").forGetter(MarsSurfaceFeature::alt),
			Codec.floatRange(0.0F, 1.0F).fieldOf("alt_chance").forGetter(MarsSurfaceFeature::altChance),
			BlockState.CODEC.fieldOf("under").forGetter(MarsSurfaceFeature::under),
			Codec.intRange(1, 12).fieldOf("depth").forGetter(MarsSurfaceFeature::depth)
	).apply(instance, MarsSurfaceFeature::new));
	/** Mars is 10 600 blocks round (scaled against the 20 000-block Earth). */
	public static final int SIZE = 10_600;
	/** Latitude ~72 degrees: the polar caps begin here (blocks from the equator, z = 0). */
	public static final int POLAR_CAP = 4_250;

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int baseX = origin.getX() & ~15;
		int baseZ = origin.getZ() & ~15;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockState rock = ModBlocks.MARS_ROCK.defaultBlockState();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = baseX + dx;
				int z = baseZ + dz;
				int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
				int polar = Math.abs(z) - POLAR_CAP;
				if (polar > 0) {
					// The cap: layered ice with dusty bands, up to 9 deep near the pole, raised a little above the plain.
					int thick = Math.min(9, 2 + polar / 120);
					int raise = Math.min(4, polar / 300);
					for (int k = 0; k < thick + raise; k++) {
						pos.set(x, surface - thick + 1 + k + raise, z);
						level.setBlock(pos, (k % 4 == 3 && random.nextInt(3) == 0) ? ModBlocks.MARS_DUST.defaultBlockState()
								: ModBlocks.MARS_ICE.defaultBlockState(), 2);
					}
					continue;
				}
				for (int d = 0; d < depth + 6; d++) {
					int y = surface - d;
					if (y < level.getMinY()) {
						break;
					}
					pos.set(x, y, z);
					if (!level.getBlockState(pos).is(ModBlocks.MARS_ROCK)) {
						continue;
					}
					BlockState state;
					if (d == 0) {
						state = altChance > 0 && random.nextFloat() < altChance ? alt : top;
					} else if (d < depth) {
						state = under;
					} else {
						state = random.nextInt(55) == 0 ? ModBlocks.HEMATITE_ORE.defaultBlockState() : rock;
						if (state == rock) {
							continue;
						}
					}
					level.setBlock(pos, state, 2);
				}
			}
		}
		return true;
	}
}
