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

import hu.kriwdi.intersolarsystem.content.ModBlocks;

/**
 * An impact crater: a bowl carved into the surface, a raised rim, and bright fresh ejecta around it (young craters
 * also throw rays). A feature may only touch the chunks next to its own, so everything stays within
 * {@link #MAX_REACH} blocks of the centre: radius at most 9, rays only where they still fit.
 */
public record CraterFeature(int minRadius, int maxRadius, java.util.Optional<BlockState> floor,
		java.util.Optional<BlockState> freshEjecta, java.util.Optional<BlockState> rimRock) implements Feature {
	public static final int LIMIT = 9;
	/** The origin lies inside its chunk, and the neighbouring chunks reach 16 blocks further. */
	public static final int MAX_REACH = 16;
	public static final MapCodec<CraterFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.intRange(2, LIMIT).fieldOf("min_radius").forGetter(CraterFeature::minRadius),
			Codec.intRange(2, LIMIT).fieldOf("max_radius").forGetter(CraterFeature::maxRadius),
			// Other bodies give their own ground (the Moon's regolith when left out).
			BlockState.CODEC.optionalFieldOf("floor").forGetter(CraterFeature::floor),
			BlockState.CODEC.optionalFieldOf("fresh").forGetter(CraterFeature::freshEjecta),
			BlockState.CODEC.optionalFieldOf("rim").forGetter(CraterFeature::rimRock)
	).apply(instance, CraterFeature::new));

	/** Depth of the bowl and height of the rim, as a fraction of the radius. */
	public static final double DEPTH = 0.4;
	public static final double RIM = 0.18;
	public static final double RIM_WIDTH = 0.35;

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	/** Floor depth below the original surface at distance ratio d (0 centre, 1 edge), parabolic bowl. */
	public static double bowl(double radius, double d) {
		return d >= 1.0 ? 0.0 : radius * DEPTH * (1.0 - d * d);
	}

	/** Rim height above the original surface just outside the bowl. */
	public static double rim(double radius, double d) {
		if (d < 1.0 || d > 1.0 + RIM_WIDTH) {
			return 0.0;
		}
		return radius * RIM * (1.0 - (d - 1.0) / RIM_WIDTH);
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int low = Math.min(minRadius, maxRadius);
		int high = Math.max(minRadius, maxRadius);
		int radius = low + random.nextInt(high - low + 1);
		boolean young = random.nextInt(3) == 0;
		int rimReach = (int) Math.ceil(radius * (1.0 + RIM_WIDTH));
		boolean rays = young && rimReach + 3 <= MAX_REACH;
		int reach = Math.min(MAX_REACH, rimReach + (rays ? 3 : 0));
		int centreTop = origin.getY() - 1;
		BlockState fresh = freshEjecta.orElseGet(ModBlocks.FRESH_REGOLITH::defaultBlockState);
		BlockState ground = floor.orElseGet(ModBlocks.LUNAR_REGOLITH::defaultBlockState);
		// Old craters are weathered back to ordinary ground; only young ones keep bright ejecta.
		BlockState rimBlock = young ? fresh : rimRock.orElse(ground);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		double rayPhase = random.nextDouble() * Math.PI;
		for (int dx = -reach; dx <= reach; dx++) {
			for (int dz = -reach; dz <= reach; dz++) {
				int x = origin.getX() + dx;
				int z = origin.getZ() + dz;
				double d = Math.sqrt(dx * dx + dz * dz) / radius;
				int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
				if (d < 1.0) {
					int floor = (int) Math.round(centreTop - bowl(radius, d));
					for (int y = top; y > floor; y--) {
						pos.set(x, y, z);
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
					}
					pos.set(x, floor, z);
					level.setBlock(pos, young && d < 0.5 ? fresh : ground, 2);
				} else if (d <= 1.0 + RIM_WIDTH) {
					int height = (int) Math.round(rim(radius, d));
					for (int y = top + 1; y <= top + height; y++) {
						pos.set(x, y, z);
						level.setBlock(pos, rimBlock, 2);
					}
					if (young) {
						pos.set(x, top, z);
						level.setBlock(pos, fresh, 2);
					}
				} else if (rays) {
					// Rays of bright ejecta: thin spokes reaching past the rim.
					double angle = Math.atan2(dz, dx);
					if (Math.abs(Math.sin(angle * 4 + rayPhase)) > 0.94 && random.nextInt(3) > 0) {
						pos.set(x, top, z);
						level.setBlock(pos, fresh, 2);
					}
				}
			}
		}
		return true;
	}
}
