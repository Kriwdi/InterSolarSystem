package hu.kriwdi.intersolarsystem.worldgen;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

import hu.kriwdi.intersolarsystem.content.PlanetBlocks;

/**
 * Jupiter's cloud decks over a bottomless atmosphere: the white ammonia ice deck high up, the orange-brown ammonium
 * hydrosulfide deck below it, the blue-grey water clouds deeper still (with crystals of metallic hydrogen in their
 * underside, where the pressure is crushing). Each deck is a patchwork of drifting banks from smooth noise; inside
 * the Great Red Spot the middle deck turns red and towers up.
 */
public final class CloudDeckFeature implements Feature {
	public static final CloudDeckFeature INSTANCE = new CloudDeckFeature();
	public static final MapCodec<CloudDeckFeature> CODEC = MapCodec.unit(INSTANCE);
	public static final int AMMONIA_Y = 210;
	public static final int SULFIDE_Y = 140;
	public static final int WATER_Y = 70;
	/** The Great Red Spot: an oval storm south of the equator. */
	public static final int SPOT_X = 2400;
	public static final int SPOT_Z = 800;

	private CloudDeckFeature() {
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
				double spot = Math.pow((x - SPOT_X) / 380.0, 2) + Math.pow((z - SPOT_Z) / 180.0, 2);
				boolean storm = spot < 1.0;
				// Bands: the decks are thicker in the light zones, thinner in the dark belts (latitude stripes).
				double band = 0.5 + 0.5 * Math.sin(z / 90.0);
				deck(level, pos, x, z, AMMONIA_Y, noise(seed, x, z, 70.0), 0.48 - 0.12 * band, 4,
						PlanetBlocks.AMMONIA_CLOUD.defaultBlockState(), null, random);
				deck(level, pos, x, z, storm ? SULFIDE_Y + (int) (24 * (1 - spot)) : SULFIDE_Y,
						noise(seed ^ 0x51L, x, z, 55.0), storm ? 0.0 : 0.40 - 0.1 * (1 - band), storm ? 9 : 6,
						storm ? PlanetBlocks.STORM_CLOUD.defaultBlockState() : PlanetBlocks.SULFIDE_CLOUD.defaultBlockState(),
						null, random);
				deck(level, pos, x, z, WATER_Y, noise(seed ^ 0xA7L, x, z, 45.0), 0.32, 8,
						PlanetBlocks.WATER_CLOUD.defaultBlockState(), PlanetBlocks.METALLIC_HYDROGEN.defaultBlockState(),
						random);
			}
		}
		return true;
	}

	/** One deck at one column: cloud from centre-thickness to centre+thickness where the noise is above the cut. */
	private static void deck(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int centre, double n,
			double cut, int maxThick, BlockState cloud, BlockState underside, RandomSource random) {
		if (n <= cut) {
			return;
		}
		int thick = 1 + (int) Math.round((n - cut) / (1.0 - cut) * maxThick);
		int bottom = centre - thick / 2;
		for (int y = bottom; y < bottom + thick; y++) {
			pos.set(x, y, z);
			level.setBlock(pos, cloud, 2);
		}
		if (underside != null && random.nextInt(260) == 0) {
			pos.set(x, bottom, z);
			level.setBlock(pos, underside, 2);
		}
	}

	/** Smooth value noise, 0..1, two octaves. */
	static double noise(long seed, int x, int z, double scale) {
		return 0.7 * value(seed, x / scale, z / scale) + 0.3 * value(seed ^ 0x9E37L, x / (scale / 3), z / (scale / 3));
	}

	private static double value(long seed, double x, double z) {
		long ix = (long) Math.floor(x);
		long iz = (long) Math.floor(z);
		double fx = x - ix;
		double fz = z - iz;
		double sx = fx * fx * (3 - 2 * fx);
		double sz = fz * fz * (3 - 2 * fz);
		double a = LunarTerrain.hash(seed, ix, iz) + (LunarTerrain.hash(seed, ix + 1, iz) - LunarTerrain.hash(seed, ix, iz)) * sx;
		double b = LunarTerrain.hash(seed, ix, iz + 1)
				+ (LunarTerrain.hash(seed, ix + 1, iz + 1) - LunarTerrain.hash(seed, ix, iz + 1)) * sx;
		return a + (b - a) * sz;
	}
}
