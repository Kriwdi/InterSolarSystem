package hu.kriwdi.intersolarsystem.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

import hu.kriwdi.intersolarsystem.content.ModBlocks;

/**
 * A blast crater of the irradiated zone: a bowl glazed with glowing trinitite, a rim of irradiated soil, sometimes a
 * toxic barrel or scrap at the bottom. Same reach rules as the lunar {@link CraterFeature} (radius at most 9).
 */
public record FalloutCraterFeature(int minRadius, int maxRadius) implements Feature {
	public static final MapCodec<FalloutCraterFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.intRange(2, CraterFeature.LIMIT).fieldOf("min_radius").forGetter(FalloutCraterFeature::minRadius),
			Codec.intRange(2, CraterFeature.LIMIT).fieldOf("max_radius").forGetter(FalloutCraterFeature::maxRadius)
	).apply(instance, FalloutCraterFeature::new));

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int low = Math.min(minRadius, maxRadius);
		int radius = low + random.nextInt(Math.max(minRadius, maxRadius) - low + 1);
		int reach = Math.min(CraterFeature.MAX_REACH, (int) Math.ceil(radius * (1.0 + CraterFeature.RIM_WIDTH)));
		int centreTop = origin.getY() - 1;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = -reach; dx <= reach; dx++) {
			for (int dz = -reach; dz <= reach; dz++) {
				int x = origin.getX() + dx;
				int z = origin.getZ() + dz;
				double d = Math.sqrt(dx * dx + dz * dz) / radius;
				int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
				if (d < 1.0) {
					int floor = (int) Math.round(centreTop - CraterFeature.bowl(radius, d));
					for (int y = top; y > floor; y--) {
						pos.set(x, y, z);
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
					}
					pos.set(x, floor, z);
					level.setBlock(pos, random.nextInt(4) > 0 ? ModBlocks.TRINITITE.defaultBlockState()
							: ModBlocks.IRRADIATED_SOIL.defaultBlockState(), 2);
					pos.set(x, floor - 1, z);
					level.setBlock(pos, ModBlocks.TRINITITE.defaultBlockState(), 2);
				} else if (d <= 1.0 + CraterFeature.RIM_WIDTH) {
					int height = (int) Math.round(CraterFeature.rim(radius, d));
					for (int y = top; y <= top + height; y++) {
						pos.set(x, y, z);
						level.setBlock(pos, ModBlocks.IRRADIATED_SOIL.defaultBlockState(), 2);
					}
				}
			}
		}
		if (random.nextInt(3) == 0) {
			int floor = (int) Math.round(centreTop - CraterFeature.bowl(radius, 0));
			level.setBlock(new BlockPos(origin.getX(), floor + 1, origin.getZ()),
					(random.nextBoolean() ? ModBlocks.TOXIC_BARREL : ModBlocks.SCRAP_PILE).defaultBlockState(), 2);
		}
		return true;
	}
}
