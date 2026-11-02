package hu.kriwdi.intersolarsystem.space;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.loot.LootTable;

import hu.kriwdi.intersolarsystem.content.DataTerminalBlock;
import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ShipFacingBlock;
import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Humanity's traces on the Moon, placed at their real selenographic positions scaled to the Moon's 5500-block world
 * (15.3 blocks per degree, north is -z): the six Apollo landing sites (descent stage, flag, experiments, and the
 * lunar rover on the last three) and the ruins of Tranquility Base, the abandoned lunar outpost next to Apollo 11.
 * Built once, on the first landing.
 */
public final class MoonSites {
	public static final double BLOCKS_PER_DEGREE = 5500.0 / 360.0;
	public static final ResourceKey<LootTable> BASE_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
			ModIds.id("chests/lunar_base"));

	public record Apollo(int mission, double latitude, double longitude, boolean rover) {
		public int x() {
			return (int) Math.round(longitude * BLOCKS_PER_DEGREE);
		}

		public int z() {
			return (int) Math.round(-latitude * BLOCKS_PER_DEGREE);
		}
	}

	public static final List<Apollo> APOLLO = List.of(
			new Apollo(11, 0.674, 23.473, false),
			new Apollo(12, -3.012, -23.422, false),
			new Apollo(14, -3.645, -17.471, false),
			new Apollo(15, 26.132, 3.634, true),
			new Apollo(16, -8.973, 15.500, true),
			new Apollo(17, 20.191, 30.772, true));

	/** Tranquility Base: east of Apollo 11. The ship sets down between the two. */
	public static final int[] BASE = {APOLLO.get(0).x() + 34, APOLLO.get(0).z() + 6};
	public static final int[] LANDING_ZONE = {APOLLO.get(0).x() + 16, APOLLO.get(0).z() + 14};

	private MoonSites() {
	}

	public static boolean built(ServerLevel moon) {
		BlockPos marker = surface(moon, BASE[0], BASE[1]);
		return moon.getBlockState(marker.below()).is(ModBlocks.STATION_HULL)
				|| moon.getBlockState(marker).is(ModBlocks.STATION_HULL);
	}

	/** Builds every site if Tranquility Base is not there yet. */
	public static void ensureBuilt(ServerLevel moon) {
		if (built(moon)) {
			return;
		}
		for (Apollo apollo : APOLLO) {
			buildApollo(moon, apollo);
		}
		buildBase(moon);
	}

	/** First air block above the ground at (x, z), loading the chunk. */
	static BlockPos surface(ServerLevel level, int x, int z) {
		level.getChunk(x >> 4, z >> 4);
		return new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z), z);
	}

	private static void set(ServerLevel level, BlockPos pos, BlockState state) {
		level.setBlock(pos, state, Block.UPDATE_CLIENTS);
	}

	private static void buildApollo(ServerLevel moon, Apollo apollo) {
		BlockPos ground = surface(moon, apollo.x(), apollo.z());
		int y = ground.getY();
		// Level a small pad and disturb the regolith around (the astronauts walked all over it).
		for (int dx = -5; dx <= 5; dx++) {
			for (int dz = -5; dz <= 5; dz++) {
				BlockPos top = surface(moon, apollo.x() + dx, apollo.z() + dz);
				if (Math.abs(dx) <= 3 && Math.abs(dz) <= 3) {
					for (int yy = y; yy < top.getY(); yy++) {
						set(moon, new BlockPos(top.getX(), yy, top.getZ()), Blocks.AIR.defaultBlockState());
					}
					set(moon, new BlockPos(top.getX(), y - 1, top.getZ()), ModBlocks.LUNAR_REGOLITH.defaultBlockState());
				}
				if ((dx * 7 + dz * 3) % 5 == 0 && dx * dx + dz * dz > 9) {
					set(moon, new BlockPos(top.getX(), Math.min(top.getY(), y) - 1, top.getZ()),
							ModBlocks.MARE_REGOLITH.defaultBlockState());
				}
			}
		}
		BlockPos c = new BlockPos(apollo.x(), y, apollo.z());
		// Descent stage: gold-foil body on four legs with footpads, a ladder on the front leg.
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				set(moon, c.offset(dx, 1, dz), ModBlocks.STATION_FOIL.defaultBlockState());
				set(moon, c.offset(dx, 2, dz), (dx == 0 && dz == 0)
						? ModBlocks.LANDER_ENGINE.defaultBlockState() : ModBlocks.STATION_FOIL.defaultBlockState());
			}
		}
		set(moon, c.offset(0, 1, 0), ModBlocks.LANDER_ENGINE.defaultBlockState());
		for (int[] leg : new int[][] {{-2, -2}, {2, -2}, {-2, 2}, {2, 2}}) {
			set(moon, c.offset(leg[0], 0, leg[1]), Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE.defaultBlockState());
			set(moon, c.offset(leg[0], 1, leg[1]), Blocks.LIGHTNING_ROD.defaultBlockState());
		}
		set(moon, c.offset(0, 1, -2), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
		set(moon, c.offset(0, 0, -2), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
		// Flag, and the experiments package (seismometer on its solar wings, retroreflector).
		set(moon, c.offset(3, 0, -2), ModBlocks.LUNAR_FLAG.defaultBlockState()
				.setValue(ShipFacingBlock.FACING, Direction.SOUTH));
		set(moon, c.offset(-4, 0, 2), ModBlocks.SOLAR_PANEL.defaultBlockState());
		set(moon, c.offset(-3, 0, 2), ModBlocks.ANTENNA_DISH.defaultBlockState());
		set(moon, c.offset(-2, 0, 2), ModBlocks.SOLAR_PANEL.defaultBlockState());
		set(moon, c.offset(-3, 0, 4), ModBlocks.STATION_WINDOW.defaultBlockState());
		if (apollo.rover()) {
			// Lunar Roving Vehicle: four wire-mesh wheels, a deck, the high-gain antenna mast.
			BlockPos r = c.offset(4, 0, 3);
			for (int[] w : new int[][] {{0, 0}, {2, 0}, {0, 3}, {2, 3}}) {
				set(moon, r.offset(w[0], 0, w[1]), ModBlocks.ROVER_WHEEL.defaultBlockState()
						.setValue(ShipFacingBlock.FACING, w[0] == 0 ? Direction.WEST : Direction.EAST));
			}
			for (int dz = 0; dz <= 3; dz++) {
				set(moon, r.offset(1, 0, dz), ModBlocks.TRUSS.defaultBlockState());
			}
			set(moon, r.offset(1, 1, 0), ModBlocks.ANTENNA_DISH.defaultBlockState());
			set(moon, r.offset(1, 1, 3), Blocks.LIGHTNING_ROD.defaultBlockState());
		}
	}

	private static void buildBase(ServerLevel moon) {
		BlockPos ground = surface(moon, BASE[0], BASE[1]);
		int y = ground.getY();
		BlockPos o = new BlockPos(BASE[0], y, BASE[1]);
		// Two half-buried habitat tubes along x, joined by a node; regolith heaped over them for radiation shielding.
		for (int dx = -9; dx <= 9; dx++) {
			for (int dy = -1; dy <= 3; dy++) {
				for (int dz = -2; dz <= 2; dz++) {
					BlockPos p = o.offset(dx, dy, dz);
					boolean shell = dy == -1 || dy == 3 || Math.abs(dz) == 2 || Math.abs(dx) == 9;
					boolean corner = (dy == -1 || dy == 3) && Math.abs(dz) == 2;
					if (corner) {
						continue;
					}
					if (shell) {
						boolean window = dy == 1 && Math.abs(dz) == 2 && dx % 4 == 0 && dx != 0;
						set(moon, p, window ? ModBlocks.STATION_WINDOW.defaultBlockState()
								: ModBlocks.STATION_HULL.defaultBlockState());
					} else {
						set(moon, p, Blocks.AIR.defaultBlockState());
					}
				}
				// Regolith mound over the roof and the north side.
				if (dy == 3 || dy == 2) {
					set(moon, o.offset(dx, dy + 1, -2 + (dy - 2)), ModBlocks.LUNAR_REGOLITH.defaultBlockState());
				}
			}
			set(moon, o.offset(dx, 4, 0), ModBlocks.LUNAR_REGOLITH.defaultBlockState());
		}
		// Airlock on the west end (open, the inner door gone), lights, terminals and supplies.
		set(moon, o.offset(-9, 0, 0), Blocks.AIR.defaultBlockState());
		set(moon, o.offset(-9, 1, 0), Blocks.AIR.defaultBlockState());
		for (int dx : new int[] {-6, 0, 6}) {
			set(moon, o.offset(dx, 2, 0), ModBlocks.STATION_LIGHT.defaultBlockState());
		}
		int log = 0;
		for (int dx : new int[] {-5, 2, 7}) {
			set(moon, o.offset(dx, 0, 1), ModBlocks.DATA_TERMINAL.defaultBlockState()
					.setValue(ShipFacingBlock.FACING, Direction.NORTH).setValue(DataTerminalBlock.LOG, log++));
		}
		for (int dx : new int[] {-7, -2, 4, 8}) {
			BlockPos p = o.offset(dx, 0, -1);
			set(moon, p, Blocks.BARREL.defaultBlockState());
			if (moon.getBlockEntity(p) instanceof RandomizableContainerBlockEntity container) {
				container.setLootTable(BASE_LOOT);
				container.setLootTableSeed(moon.getRandom().nextLong());
			}
		}
		// Solar field south of the habitat: three rows of cells on truss stands, a few cells gone.
		for (int row = 0; row < 3; row++) {
			for (int dx = -8; dx <= 8; dx++) {
				BlockPos stand = surface(moon, o.getX() + dx, o.getZ() + 6 + row * 3);
				set(moon, stand, ModBlocks.TRUSS.defaultBlockState());
				if ((dx * 5 + row * 7) % 11 != 0) {
					set(moon, stand.above(), ModBlocks.SOLAR_PANEL.defaultBlockState());
				}
			}
		}
		set(moon, o.offset(0, 5, 0), ModBlocks.ANTENNA_DISH.defaultBlockState());
		set(moon, o.offset(-11, 0, -3), ModBlocks.LUNAR_FLAG.defaultBlockState()
				.setValue(ShipFacingBlock.FACING, Direction.EAST));
	}
}
