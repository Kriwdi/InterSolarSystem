package hu.kriwdi.intersolarsystem.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Saturn's rings from inside: a thin, crowded sheet of ice, {@code half_thickness} blocks above and below {@code
 * plane}. Many small clumps (snowball to house sized, flattened along the ring), clean water ice with dusty ones
 * among them and heavy-water ice in the cores of the bigger ones. Lanes where the density drops (ringlet gaps, from
 * the distance to the centre, like the Cassini division in miniature) give room to fly. Every clump stays within
 * {@link #MAX_RADIUS} of a centre inside its chunk.
 */
public record RingFieldFeature(BlockState ice, BlockState dusty, BlockState heavy, int plane, int halfThickness)
		implements Feature {
	public static final MapCodec<RingFieldFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockState.CODEC.fieldOf("ice").forGetter(RingFieldFeature::ice),
			BlockState.CODEC.fieldOf("dusty").forGetter(RingFieldFeature::dusty),
			BlockState.CODEC.fieldOf("heavy").forGetter(RingFieldFeature::heavy),
			Codec.INT.fieldOf("plane").forGetter(RingFieldFeature::plane),
			Codec.intRange(2, 64).fieldOf("half_thickness").forGetter(RingFieldFeature::halfThickness)
	).apply(instance, RingFieldFeature::new));
	public static final int MAX_RADIUS = 7;

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int baseX = origin.getX() & ~15;
		int baseZ = origin.getZ() & ~15;
		// Ringlets: the density rises and falls with the distance from the world's centre.
		double r = Math.hypot(baseX + 8, baseZ + 8);
		double density = 0.5 + 0.5 * Math.sin(r / 140.0) * Math.cos(r / 47.0);
		int count = density < 0.18 ? 0 : 1 + (int) (density * 5) + random.nextInt(2);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int n = 0; n < count; n++) {
			int cx = baseX + 2 + random.nextInt(12);
			int cz = baseZ + 2 + random.nextInt(12);
			int cy = plane - halfThickness + random.nextInt(halfThickness * 2 + 1);
			boolean big = random.nextInt(9) == 0;
			double rx = big ? 4 + random.nextInt(3) : 1 + random.nextInt(3);
			double ry = Math.max(1.0, rx * (0.35 + random.nextDouble() * 0.3));
			double rz = Math.min(MAX_RADIUS, rx * (0.8 + random.nextDouble() * 0.6));
			boolean dirty = random.nextInt(4) == 0;
			double phase = random.nextDouble() * 6.28;
			int reach = (int) Math.ceil(Math.max(rx, Math.max(ry, rz)));
			for (int dx = -reach; dx <= reach; dx++) {
				for (int dy = -reach; dy <= reach; dy++) {
					for (int dz = -reach; dz <= reach; dz++) {
						double lump = 1.0 + 0.2 * Math.sin(dx * 1.1 + phase) * Math.cos(dz * 0.9 - phase);
						double d = dx * dx / (rx * rx) + dy * dy / (ry * ry) + dz * dz / (rz * rz);
						if (d > lump) {
							continue;
						}
						pos.set(cx + dx, cy + dy, cz + dz);
						BlockState st = big && d < 0.25 && random.nextInt(3) == 0 ? heavy
								: dirty || random.nextInt(14) == 0 ? dusty : ice;
						level.setBlock(pos, st, 2);
					}
				}
			}
		}
		return true;
	}
}
