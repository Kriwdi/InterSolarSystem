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
 * The asteroid belt, one chunk at a time: a few rocks floating at different heights, lumpy ellipsoids of three kinds:
 * stony (chondrite with nickel-iron grains), carbonaceous (dark, crumbly) and metallic (nickel-iron with iridium).
 * Every rock stays within {@link #MAX_RADIUS} of a centre inside the chunk, so it never reaches past the neighbours.
 */
public final class AsteroidFieldFeature implements Feature {
	public static final AsteroidFieldFeature INSTANCE = new AsteroidFieldFeature();
	public static final MapCodec<AsteroidFieldFeature> CODEC = MapCodec.unit(INSTANCE);
	public static final int MAX_RADIUS = 7;

	private AsteroidFieldFeature() {
	}

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int baseX = origin.getX() & ~15;
		int baseZ = origin.getZ() & ~15;
		int count = random.nextInt(4) == 0 ? 2 : random.nextInt(3) == 0 ? 0 : 1;
		for (int n = 0; n < count; n++) {
			int cx = baseX + 2 + random.nextInt(12);
			int cz = baseZ + 2 + random.nextInt(12);
			int cy = level.getMinY() + 30 + random.nextInt(Math.max(1, level.getHeight() - 60));
			int kind = random.nextInt(10);
			boolean big = random.nextInt(6) == 0;
			double rx = big ? 5 + random.nextInt(3) : 2 + random.nextInt(3);
			double ry = rx * (0.6 + random.nextDouble() * 0.4);
			double rz = rx * (0.7 + random.nextDouble() * 0.5);
			rz = Math.min(rz, MAX_RADIUS);
			double phase = random.nextDouble() * 6.28;
			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			int r = (int) Math.ceil(Math.max(rx, Math.max(ry, rz)));
			for (int dx = -r; dx <= r; dx++) {
				for (int dy = -r; dy <= r; dy++) {
					for (int dz = -r; dz <= r; dz++) {
						double lump = 1.0 + 0.18 * Math.sin(dx * 0.9 + phase) * Math.cos(dz * 0.8 + phase) * Math.sin(dy + phase);
						double d = (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) + (dz * dz) / (rz * rz);
						if (d > lump) {
							continue;
						}
						pos.set(cx + dx, cy + dy, cz + dz);
						level.setBlock(pos, pick(kind, random, d), 2);
					}
				}
			}
		}
		return true;
	}

	private static BlockState pick(int kind, RandomSource random, double depth) {
		if (kind < 5) {
			// Stony: chondrite, with nickel-iron grains.
			return random.nextInt(18) == 0 ? PlanetBlocks.NICKEL_IRON_ORE.defaultBlockState()
					: PlanetBlocks.CHONDRITE.defaultBlockState();
		}
		if (kind < 8) {
			return random.nextInt(25) == 0 ? PlanetBlocks.CHONDRITE.defaultBlockState()
					: PlanetBlocks.CARBONACEOUS_ROCK.defaultBlockState();
		}
		// Metallic: nickel-iron, iridium in the core.
		if (depth < 0.3 && random.nextInt(4) == 0) {
			return PlanetBlocks.IRIDIUM_ORE.defaultBlockState();
		}
		return random.nextInt(9) == 0 ? PlanetBlocks.CHONDRITE.defaultBlockState()
				: PlanetBlocks.NICKEL_IRON_ORE.defaultBlockState();
	}
}
