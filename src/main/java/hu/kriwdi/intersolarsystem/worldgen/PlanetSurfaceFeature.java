package hu.kriwdi.intersolarsystem.worldgen;

import java.util.Optional;

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

/**
 * Dresses one chunk of a world's bare bedrock ({@code base}) for its biome: the top block (patches of an alternative),
 * the layers under it, an ore in the rock below; optionally a polar cap beyond {@code cap_from} blocks from the equator
 * (z = 0). The worlds beyond Mars use it (Venus, Mercury, Ceres, Io, Europa).
 */
public record PlanetSurfaceFeature(BlockState base, BlockState top, BlockState alt, float altChance, BlockState under,
		int depth, Optional<BlockState> ore, int oreChance, Optional<BlockState> cap, int capFrom) implements Feature {
	public static final MapCodec<PlanetSurfaceFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockState.CODEC.fieldOf("base").forGetter(PlanetSurfaceFeature::base),
			BlockState.CODEC.fieldOf("top").forGetter(PlanetSurfaceFeature::top),
			BlockState.CODEC.fieldOf("alt").forGetter(PlanetSurfaceFeature::alt),
			Codec.floatRange(0.0F, 1.0F).fieldOf("alt_chance").forGetter(PlanetSurfaceFeature::altChance),
			BlockState.CODEC.fieldOf("under").forGetter(PlanetSurfaceFeature::under),
			Codec.intRange(1, 40).fieldOf("depth").forGetter(PlanetSurfaceFeature::depth),
			BlockState.CODEC.optionalFieldOf("ore").forGetter(PlanetSurfaceFeature::ore),
			Codec.intRange(0, 10_000).optionalFieldOf("ore_chance", 0).forGetter(PlanetSurfaceFeature::oreChance),
			BlockState.CODEC.optionalFieldOf("cap").forGetter(PlanetSurfaceFeature::cap),
			Codec.intRange(0, 100_000).optionalFieldOf("cap_from", 0).forGetter(PlanetSurfaceFeature::capFrom)
	).apply(instance, PlanetSurfaceFeature::new));

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int baseX = origin.getX() & ~15;
		int baseZ = origin.getZ() & ~15;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = baseX + dx;
				int z = baseZ + dz;
				int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
				if (cap.isPresent() && capFrom > 0 && Math.abs(z) > capFrom) {
					int thick = Math.min(8, 2 + (Math.abs(z) - capFrom) / 100);
					for (int k = 0; k < thick; k++) {
						pos.set(x, surface - k, z);
						level.setBlock(pos, cap.get(), 2);
					}
					continue;
				}
				for (int d = 0; d < depth + 8; d++) {
					int y = surface - d;
					if (y < level.getMinY()) {
						break;
					}
					pos.set(x, y, z);
					if (!level.getBlockState(pos).is(base.getBlock())) {
						continue;
					}
					BlockState state;
					if (d == 0) {
						state = altChance > 0 && random.nextFloat() < altChance ? alt : top;
					} else if (d < depth) {
						state = under;
					} else if (ore.isPresent() && oreChance > 0 && random.nextInt(oreChance) == 0) {
						state = ore.get();
					} else {
						continue;
					}
					level.setBlock(pos, state, 2);
				}
			}
		}
		return true;
	}
}
