package hu.kriwdi.intersolarsystem.worldgen;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * The dead Earth's biome source: the climate placement (an ordinary multi-noise source, written by
 * tools/worldgen/make_worldgen.py) plus one guaranteed oasis in every world.
 *
 * <p>The oasis climate box is a narrow corner (hot, humid, a slice of weirdness), so many seeds had none within
 * reach. Here the last clean water always exists: an irregular patch about {@value #RADIUS_BLOCKS} blocks across,
 * between 900 and 1500 blocks from the world's centre, on the hottest and most humid dry land among the candidate
 * spots. The choice is made from the seed's own climate noise, so it differs per world but never changes for one.
 * Natural oases from the climate box still appear on top of this one.
 */
public final class OasisBiomeSource extends BiomeSource {
	public static final MapCodec<OasisBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BiomeSource.CODEC.fieldOf("climate").forGetter(source -> source.climate),
			Biome.CODEC.fieldOf("oasis").forGetter(source -> source.oasis))
			.apply(instance, OasisBiomeSource::new));

	/** Mean radius of the guaranteed oasis, in blocks. */
	static final int RADIUS_BLOCKS = 104;
	private static final int[] DISTANCES_BLOCKS = {900, 1200, 1500};
	private static final int ANGLES = 16;
	/** Land starts at this continentalness (make_worldgen.py LAND), quantized like {@link Climate.TargetPoint}. */
	private static final long LAND = Climate.quantizeCoord(-0.19F);

	private final BiomeSource climate;
	private final Holder<Biome> oasis;
	/** The centre chosen for the last sampler (one per world); recomputed only if the sampler changes. */
	private volatile Centre centre;

	public OasisBiomeSource(BiomeSource climate, Holder<Biome> oasis) {
		this.climate = climate;
		this.oasis = oasis;
	}

	public static void register() {
		Registry.register(BuiltInRegistries.BIOME_SOURCE, ModIds.id("dead_earth"), CODEC);
	}

	@Override
	protected MapCodec<? extends BiomeSource> codec() {
		return CODEC;
	}

	@Override
	protected Stream<Holder<Biome>> collectPossibleBiomes() {
		return Stream.concat(climate.possibleBiomes().stream(), Stream.of(oasis)).distinct();
	}

	@Override
	public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
		Centre c = centre(sampler);
		if (insideOasis(quartX * 4 - c.x, quartZ * 4 - c.z)) {
			return oasis;
		}
		return climate.getNoiseBiome(quartX, quartY, quartZ, sampler);
	}

	/** Whether a block offset from the oasis centre lies inside its wavy edge. */
	static boolean insideOasis(int dx, int dz) {
		double max = RADIUS_BLOCKS * 1.3;
		double d2 = (double) dx * dx + (double) dz * dz;
		if (d2 > max * max) {
			return false;
		}
		double a = Math.atan2(dz, dx);
		double r = RADIUS_BLOCKS * (1.0 + 0.18 * Math.sin(3 * a + 0.7) + 0.1 * Math.sin(5 * a + 2.1));
		return d2 <= r * r;
	}

	private Centre centre(Climate.Sampler sampler) {
		Centre c = centre;
		if (c != null && c.sampler == sampler) {
			return c;
		}
		c = choose(sampler);
		centre = c;
		return c;
	}

	/** The candidate spot with the best oasis climate; land beats sea, then hot and humid wins. */
	private static Centre choose(Climate.Sampler sampler) {
		int bestX = DISTANCES_BLOCKS[1];
		int bestZ = 0;
		long bestScore = Long.MIN_VALUE;
		for (int dist : DISTANCES_BLOCKS) {
			for (int i = 0; i < ANGLES; i++) {
				double a = 2 * Math.PI * i / ANGLES + dist * 0.001;
				int x = (int) Math.round(Math.cos(a) * dist);
				int z = (int) Math.round(Math.sin(a) * dist);
				Climate.TargetPoint p = sampler.sample(x >> 2, 0, z >> 2);
				long score = p.temperature() + p.humidity() + p.erosion() / 2;
				if (p.continentalness() < LAND) {
					score -= 100_000L;
				}
				if (score > bestScore) {
					bestScore = score;
					bestX = x;
					bestZ = z;
				}
			}
		}
		return new Centre(sampler, bestX, bestZ);
	}

	/** Block position of the oasis centre in the world of {@code sampler}. */
	public int[] centreBlock(Climate.Sampler sampler) {
		Centre c = centre(sampler);
		return new int[] {c.x, c.z};
	}

	private record Centre(Climate.Sampler sampler, int x, int z) {
	}
}
