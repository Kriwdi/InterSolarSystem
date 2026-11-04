package hu.kriwdi.intersolarsystem.worldgen;

import java.util.Optional;

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

/**
 * The landforms that make each icy moon of the outer system its own place, one chunk at a time, on top of the rolling
 * terrain and the surface dressing. {@code style} picks one:
 * <ul>
 * <li>{@code grooves} (Ganymede): patches of bright grooved terrain, parallel ridges of {@code secondary} a few
 * blocks high, each patch running its own way, between the dark old ground.</li>
 * <li>{@code pinnacles} (Callisto): sublimation spires, dark ice ({@code secondary}) with bright frost tips
 * ({@code accent}), scattered over the plains.</li>
 * <li>{@code basin_rings} (Callisto's Valhalla): concentric ridges of {@code secondary} from 300 blocks to {@code
 * size} round ({@code centre_x}, {@code centre_z}), the scars of an ancient impact.</li>
 * <li>{@code tiger_stripes} (Enceladus): four long parallel fissures {@code size} blocks long round the centre, lined
 * with blue {@code accent} ice, geyser vents ({@code special}) along their floors, fresh geyser snow ({@code
 * secondary}) on both sides.</li>
 * <li>{@code cantaloupe} (Triton): the dimpled skin of cells with low ridges of {@code secondary} between them, and
 * nitrogen geysers ({@code special}) trailing dark streaks of {@code accent} downwind (east).</li>
 * <li>{@code dunes} (Titan): long dunes of tholin sand ({@code secondary}) running east to west in dune fields, never
 * below the seas' level ({@code height}).</li>
 * <li>{@code coronae} (Miranda): patchwork regions of parallel light and dark bands.</li>
 * <li>{@code escarpment} (Miranda's Verona Rupes): from x = {@code centre_x} to {@code centre_x + size} the ground
 * stands {@code height} blocks higher, a sheer cliff of {@code accent} on its western face.</li>
 * <li>{@code canyons} (Titania's Messina Chasmata): long rifts 400 blocks apart, V-shaped and up to {@code height}
 * deep, frost ({@code secondary}) on their floors.</li>
 * </ul>
 */
public record MoonTerrainFeature(String style, BlockState primary, BlockState secondary, BlockState accent,
		Optional<BlockState> special, int centreX, int centreZ, int size, int height) implements Feature {
	public static final MapCodec<MoonTerrainFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Codec.STRING.fieldOf("style").forGetter(MoonTerrainFeature::style),
			BlockState.CODEC.fieldOf("primary").forGetter(MoonTerrainFeature::primary),
			BlockState.CODEC.fieldOf("secondary").forGetter(MoonTerrainFeature::secondary),
			BlockState.CODEC.fieldOf("accent").forGetter(MoonTerrainFeature::accent),
			BlockState.CODEC.optionalFieldOf("special").forGetter(MoonTerrainFeature::special),
			Codec.INT.optionalFieldOf("centre_x", 0).forGetter(MoonTerrainFeature::centreX),
			Codec.INT.optionalFieldOf("centre_z", 0).forGetter(MoonTerrainFeature::centreZ),
			Codec.intRange(0, 100_000).optionalFieldOf("size", 0).forGetter(MoonTerrainFeature::size),
			Codec.intRange(0, 400).optionalFieldOf("height", 0).forGetter(MoonTerrainFeature::height)
	).apply(instance, MoonTerrainFeature::new));

	@Override
	public MapCodec<? extends Feature> codec() {
		return CODEC;
	}

	@Override
	public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
		int baseX = origin.getX() & ~15;
		int baseZ = origin.getZ() & ~15;
		long seed = level.getSeed() ^ style.hashCode();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int x = baseX + dx;
				int z = baseZ + dz;
				int top = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
				if (top <= level.getMinY() + 2) {
					continue;
				}
				switch (style) {
					case "grooves" -> grooves(level, pos, seed, x, z, top);
					case "pinnacles" -> pinnacle(level, pos, random, baseX, baseZ, x, z, top);
					case "basin_rings" -> basinRing(level, pos, x, z, top);
					case "tiger_stripes" -> tigerStripes(level, pos, random, x, z, top);
					case "cantaloupe" -> cantaloupe(level, pos, seed, random, baseX, x, z, top);
					case "dunes" -> dune(level, pos, seed, x, z, top);
					case "coronae" -> corona(level, pos, seed, x, z, top);
					case "escarpment" -> escarpment(level, pos, x, z, top);
					case "canyons" -> canyon(level, pos, x, z, top);
					default -> {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static void set(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z, BlockState st) {
		pos.set(x, y, z);
		level.setBlock(pos, st, 2);
	}

	private void raise(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int top, int h, BlockState st) {
		for (int k = 1; k <= h; k++) {
			set(level, pos, x, top + k, z, st);
		}
	}

	// ---------------------------------------------------------------- Ganymede
	private void grooves(WorldGenLevel level, BlockPos.MutableBlockPos pos, long seed, int x, int z, int top) {
		double zone = CloudDeckFeature.noise(seed, x, z, 220.0);
		if (zone < 0.52) {
			return;
		}
		// Each patch runs its own way: the angle drifts slowly across the world.
		double a = CloudDeckFeature.noise(seed ^ 0x6A0L, x, z, 900.0) * Math.PI * 2;
		double ridge = Math.sin((x * Math.cos(a) + z * Math.sin(a)) / 5.5);
		set(level, pos, x, top, z, secondary);
		if (ridge > 0.25) {
			int h = 1 + (int) (3 * (ridge - 0.25) / 0.75 * Math.min(1.0, (zone - 0.52) * 6));
			raise(level, pos, x, z, top, h, secondary);
		}
	}

	// ---------------------------------------------------------------- Callisto
	private void pinnacle(WorldGenLevel level, BlockPos.MutableBlockPos pos, RandomSource random, int baseX, int baseZ,
			int x, int z, int top) {
		if (random.nextInt(260) != 0) {
			return;
		}
		int h = 3 + random.nextInt(8);
		raise(level, pos, x, z, top, h - 1, secondary);
		set(level, pos, x, top + h, z, accent);
		// A wider foot where the chunk allows it.
		int[][] around = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
		for (int[] d : around) {
			int nx = x + d[0];
			int nz = z + d[1];
			if (nx >> 4 == baseX >> 4 && nz >> 4 == baseZ >> 4) {
				int ntop = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, nx, nz) - 1;
				raise(level, pos, nx, nz, ntop, Math.max(1, h / 3), secondary);
			}
		}
	}

	private void basinRing(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int top) {
		double d = Math.hypot(x - centreX, z - centreZ);
		if (d < 300 || d > size) {
			return;
		}
		double ring = Math.sin(d / 23.0 + 0.4 * Math.sin((x - centreX) / 61.0));
		if (ring > 0.72) {
			int h = 1 + (int) ((ring - 0.72) / 0.28 * 2.5 * (1.0 - (d - 300) / (size - 300.0) * 0.6));
			set(level, pos, x, top, z, secondary);
			raise(level, pos, x, z, top, h, secondary);
		}
	}

	// ---------------------------------------------------------------- Enceladus
	private double stripeOffset(int k, int x) {
		return centreZ + (k - 1.5) * 38 + 7 * Math.sin((x - centreX) / 47.0 + k * 1.3);
	}

	private void tigerStripes(WorldGenLevel level, BlockPos.MutableBlockPos pos, RandomSource random, int x, int z,
			int top) {
		if (Math.abs(x - centreX) > size) {
			return;
		}
		double nearest = Double.MAX_VALUE;
		for (int k = 0; k < 4; k++) {
			nearest = Math.min(nearest, Math.abs(z - stripeOffset(k, x)));
		}
		double taper = 1.0 - Math.abs(x - centreX) / (double) size;
		double width = 1.0 + 2.2 * taper;
		if (nearest <= width) {
			int depth = 2 + (int) (5 * taper);
			for (int k = 0; k < depth; k++) {
				set(level, pos, x, top - k, z, Blocks.AIR.defaultBlockState());
			}
			set(level, pos, x, top - depth, z, accent);
			if (special.isPresent() && nearest < 0.8 && Math.floorMod(x - centreX, 31) == 0) {
				set(level, pos, x, top - depth, z, special.get());
			}
		} else if (nearest <= width + 2) {
			set(level, pos, x, top, z, accent);
		} else if (nearest < 30 * taper + 4) {
			set(level, pos, x, top, z, secondary);
			if (random.nextInt(3) == 0) {
				set(level, pos, x, top + 1, z, secondary);
			}
		}
	}

	// ---------------------------------------------------------------- Triton
	private void cantaloupe(WorldGenLevel level, BlockPos.MutableBlockPos pos, long seed, RandomSource random, int baseX,
			int x, int z, int top) {
		final int cell = 14;
		int cx = Math.floorDiv(x, cell);
		int cz = Math.floorDiv(z, cell);
		double f1 = Double.MAX_VALUE;
		double f2 = Double.MAX_VALUE;
		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				double px = (cx + i) * cell + LunarTerrain.hash(seed, cx + i, cz + j) * cell;
				double pz = (cz + j) * cell + LunarTerrain.hash(seed ^ 0x77L, cx + i, cz + j) * cell;
				double d = Math.hypot(x - px, z - pz);
				if (d < f1) {
					f2 = f1;
					f1 = d;
				} else if (d < f2) {
					f2 = d;
				}
			}
		}
		if (f2 - f1 < 1.6) {
			raise(level, pos, x, z, top, f2 - f1 < 0.7 ? 2 : 1, secondary);
		} else if (f1 < 3.0) {
			// The dimple's floor sits a block lower.
			set(level, pos, x, top, z, Blocks.AIR.defaultBlockState());
		}
		if (special.isPresent() && random.nextInt(5200) == 0) {
			set(level, pos, x, top, z, special.get());
			// The plume's fall-out: a dark streak blown east, as far as the chunk goes.
			for (int k = 1; x + k < baseX + 16 && k < 18; k++) {
				int t = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x + k, z) - 1;
				set(level, pos, x + k, t, z, accent);
			}
		}
	}

	// ---------------------------------------------------------------- Titan
	private void dune(WorldGenLevel level, BlockPos.MutableBlockPos pos, long seed, int x, int z, int top) {
		if (top <= height + 1) {
			return;
		}
		double field = CloudDeckFeature.noise(seed, x, z, 260.0);
		if (field < 0.42) {
			return;
		}
		double crest = 0.5 + 0.5 * Math.sin(z / 8.0 + 1.6 * Math.sin(x / 63.0));
		int h = (int) Math.round(5.0 * crest * crest * Math.min(1.0, (field - 0.42) * 5));
		set(level, pos, x, top, z, secondary);
		raise(level, pos, x, z, top, h, secondary);
	}

	// ---------------------------------------------------------------- Miranda
	private void corona(WorldGenLevel level, BlockPos.MutableBlockPos pos, long seed, int x, int z, int top) {
		double zone = CloudDeckFeature.noise(seed, x, z, 180.0);
		if (zone < 0.55) {
			return;
		}
		double a = Math.floor(CloudDeckFeature.noise(seed ^ 0x3CL, x, z, 400.0) * 4) * Math.PI / 4;
		double t = x * Math.cos(a) + z * Math.sin(a);
		boolean light = Math.floorMod((int) Math.floor(t / 4.0), 2) == 0;
		set(level, pos, x, top, z, light ? secondary : primary);
		if (Math.floorMod((int) Math.floor(t), 8) == 0) {
			raise(level, pos, x, z, top, 1, light ? secondary : primary);
		}
	}

	private void escarpment(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int top) {
		int wobble = (int) Math.round(6 * Math.sin(z / 37.0) + 3 * Math.sin(z / 11.0));
		int edge = centreX + wobble;
		if (x < edge || x > centreX + size) {
			return;
		}
		// The far side slopes back down over 80 blocks; the near side is a sheer face.
		int h = (int) Math.round(height * Math.min(1.0, (centreX + size - x) / 80.0));
		boolean face = x - edge < 3;
		for (int k = 1; k <= h; k++) {
			set(level, pos, x, top + k, z, face ? accent : k == h ? secondary : primary);
		}
	}

	// ---------------------------------------------------------------- Titania
	private void canyon(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int top) {
		int k = Math.floorDiv(z - centreZ + 200, 400);
		double mid = centreZ + k * 400 + 18 * Math.sin(x / 90.0 + k * 2.1);
		double width = 11 + 5 * Math.sin(x / 140.0 + k);
		double off = Math.abs(z - mid);
		if (off > width) {
			return;
		}
		int depth = (int) Math.round(height * (1.0 - (off / width) * (off / width)));
		for (int d = 0; d < depth && top - d > level.getMinY() + 2; d++) {
			set(level, pos, x, top - d, z, Blocks.AIR.defaultBlockState());
		}
		set(level, pos, x, Math.max(level.getMinY() + 2, top - depth), z, secondary);
	}
}
