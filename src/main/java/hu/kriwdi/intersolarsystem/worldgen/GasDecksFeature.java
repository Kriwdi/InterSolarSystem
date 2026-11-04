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
 * The cloud decks of Saturn, Uranus and Neptune, at Jupiter's heights ({@link CloudDeckFeature}) but each in its own
 * colours: a high haze deck, a middle deck and the deep water clouds, whose underside grows crystals ({@code
 * underside}: metallic hydrogen on Saturn, the diamonds that rain down inside the ice giants).
 *
 * <ul>
 * <li>{@code storm}: an oval storm (Neptune's Great Dark Spot) where the middle deck turns {@code storm_block} and
 * towers up, with a clear eye in the middle.</li>
 * <li>{@code hexagon}: Saturn's north polar hexagon, a six-sided jet stream: a towering wall of {@code storm_block}
 * along a hexagon of radius {@code storm_rx} round the storm centre, calm decks inside it and the polar vortex, a
 * hole through every deck, at its heart.</li>
 * </ul>
 * Band width ({@code bands}, blocks) sets how wide the light zones and dark belts are.
 */
public record GasDecksFeature(BlockState high, BlockState middle, BlockState low, BlockState stormBlock,
		BlockState underside, int undersideRarity, int stormX, int stormZ, int stormRx, int stormRz, boolean hexagon,
		double bands) implements Feature {
	public static final MapCodec<GasDecksFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BlockState.CODEC.fieldOf("high").forGetter(GasDecksFeature::high),
			BlockState.CODEC.fieldOf("middle").forGetter(GasDecksFeature::middle),
			BlockState.CODEC.fieldOf("low").forGetter(GasDecksFeature::low),
			BlockState.CODEC.fieldOf("storm_block").forGetter(GasDecksFeature::stormBlock),
			BlockState.CODEC.fieldOf("underside").forGetter(GasDecksFeature::underside),
			Codec.intRange(1, 100_000).fieldOf("underside_rarity").forGetter(GasDecksFeature::undersideRarity),
			Codec.INT.fieldOf("storm_x").forGetter(GasDecksFeature::stormX),
			Codec.INT.fieldOf("storm_z").forGetter(GasDecksFeature::stormZ),
			Codec.intRange(1, 20_000).fieldOf("storm_rx").forGetter(GasDecksFeature::stormRx),
			Codec.intRange(1, 20_000).fieldOf("storm_rz").forGetter(GasDecksFeature::stormRz),
			Codec.BOOL.optionalFieldOf("hexagon", false).forGetter(GasDecksFeature::hexagon),
			Codec.doubleRange(10.0, 1000.0).optionalFieldOf("bands", 90.0).forGetter(GasDecksFeature::bands)
	).apply(instance, GasDecksFeature::new));

	/** Half the thickness of the hexagon's wall, and the polar vortex's radius. */
	static final int HEX_WALL = 18;
	static final int VORTEX = 70;

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int baseX = origin.getX() & ~15;
		int baseZ = origin.getZ() & ~15;
		long seed = level.getSeed() ^ high.hashCode();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = baseX + dx;
				int z = baseZ + dz;
				double band = 0.5 + 0.5 * Math.sin(z / bands);
				int sx = x - stormX;
				int sz = z - stormZ;
				if (hexagon) {
					double h = hexDistance(sx, sz);
					if (Math.hypot(sx, sz) < VORTEX) {
						// The polar vortex: a clear hole straight down through every deck.
						continue;
					}
					if (Math.abs(h - stormRx) < HEX_WALL) {
						double wall = 1.0 - Math.abs(h - stormRx) / HEX_WALL;
						column(level, pos, x, z, CloudDeckFeature.SULFIDE_Y + (int) (34 * wall), 6 + (int) (16 * wall),
								stormBlock);
						deck(level, pos, x, z, CloudDeckFeature.WATER_Y, CloudDeckFeature.noise(seed ^ 0xA7L, x, z, 45.0),
								0.32, 8, low, null, random);
						continue;
					}
				} else {
					double spot = Math.pow(sx / (double) stormRx, 2) + Math.pow(sz / (double) stormRz, 2);
					if (spot < 1.0) {
						// The storm: the middle deck towers up in a dark oval, a clear eye at its heart.
						if (spot > 0.06) {
							column(level, pos, x, z, CloudDeckFeature.SULFIDE_Y + (int) (30 * (1 - spot)),
									4 + (int) (10 * (1 - spot)), stormBlock);
						}
						deck(level, pos, x, z, CloudDeckFeature.WATER_Y, CloudDeckFeature.noise(seed ^ 0xA7L, x, z, 45.0),
								0.32, 8, low, underside, random);
						continue;
					}
				}
				deck(level, pos, x, z, CloudDeckFeature.AMMONIA_Y, CloudDeckFeature.noise(seed, x, z, 70.0),
						0.48 - 0.12 * band, 4, high, null, random);
				deck(level, pos, x, z, CloudDeckFeature.SULFIDE_Y, CloudDeckFeature.noise(seed ^ 0x51L, x, z, 55.0),
						0.40 - 0.1 * (1 - band), 6, middle, null, random);
				deck(level, pos, x, z, CloudDeckFeature.WATER_Y, CloudDeckFeature.noise(seed ^ 0xA7L, x, z, 45.0), 0.32,
						8, low, underside, random);
			}
		}
		return true;
	}

	/** Distance in the hexagon's own metric (a regular hexagon of "radius" r is the set where this equals r). */
	static double hexDistance(int x, int z) {
		double best = 0.0;
		for (int i = 0; i < 3; i++) {
			double a = Math.PI / 6 + i * Math.PI / 3;
			best = Math.max(best, Math.abs(x * Math.cos(a) + z * Math.sin(a)));
		}
		return best;
	}

	private static void column(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int centre, int thick,
			BlockState cloud) {
		int bottom = centre - thick / 2;
		for (int y = bottom; y < bottom + thick; y++) {
			pos.set(x, y, z);
			level.setBlock(pos, cloud, 2);
		}
	}

	private void deck(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int centre, double n, double cut,
			int maxThick, BlockState cloud, BlockState crystals, RandomSource random) {
		if (n <= cut) {
			return;
		}
		int thick = 1 + (int) Math.round((n - cut) / (1.0 - cut) * maxThick);
		int bottom = centre - thick / 2;
		for (int y = bottom; y < bottom + thick; y++) {
			pos.set(x, y, z);
			level.setBlock(pos, cloud, 2);
		}
		if (crystals != null && random.nextInt(undersideRarity) == 0) {
			pos.set(x, bottom - 1, z);
			level.setBlock(pos, crystals, 2);
		}
	}
}
