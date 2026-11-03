package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ShipFacingBlock;

/**
 * The research ship as it stands where it landed (or hangs at the ISS docking port): a rounded fuselage of
 * indestructible plating with a glass cockpit canopy on the nose, an orange livery band and portholes along the sides,
 * swept wings with navigation lights, two engine nacelles and a tail fin at the stern, and four landing legs with
 * footpads. It is built when the ship arrives and removed when it takes off; only the hull's own blocks are removed.
 *
 * <p>Local coordinates: {@code d} goes into the ship from the hatch wall (the fuselage is 0..8, the wings and the
 * nacelles reach out to either side), {@code a} runs along the ship (stern -12 to nose +14, the hatch at 0), {@code y} is
 * up from the hatch (the fuselage floor is at -1). The hatch is two blocks tall (y 0 and 1).
 */
public final class ShipShell {
	public static final int WIDTH = 9;
	public static final int STERN = -12;
	public static final int NOSE = 14;
	public static final int BOTTOM = -1;
	public static final int TOP = 5;
	/** Longest landing leg (blocks below the fuselage). */
	private static final int LEG_DEPTH = 12;

	/** What a part is; the local directions of stairs are resolved against the hatch direction when building. */
	public enum Kind {
		PLATING, FILL, PORTHOLE, STRIPE, CANOPY, THRUSTER, LIGHT_RED, LIGHT_GREEN, HATCH_TOP,
		/** Stairs: {@code facing} is a local direction (IN, OUT, FWD, BACK), {@code top} an upside-down stair. */
		STAIRS, SLAB_BOTTOM, SLAB_TOP, LEG
	}

	/** Local horizontal directions of a stair's high side. */
	public enum Local { IN, OUT, FWD, BACK }

	public record Part(int d, int a, int y, Kind kind, Local facing, boolean top) {
		Part(int d, int a, int y, Kind kind) {
			this(d, a, y, kind, Local.IN, false);
		}
	}

	/** Where the ship is built: on the ground (with legs) or docked in orbit (no legs, never replaces the station). */
	public enum Mode { GROUND, DOCKED }

	private static final List<Part> PARTS = layout();

	private ShipShell() {
	}

	public static void register() {
		// Worlds from before the hull existed: a landed ship gets its hull the next time the server starts.
		ServerLifecycleEvents.SERVER_STARTED.register(ShipShell::ensureBuilt);
	}

	// ---- Shape of the fuselage.

	/** Side inset of the cross-section at a point along the ship (the nose and the stern are narrower). */
	static int inset(int a) {
		if (a <= -10) {
			return 1;
		}
		return switch (a) {
			case 10, 11 -> 1;
			case 12 -> 2;
			case 13, 14 -> 3;
			default -> 0;
		};
	}

	static int top(int a) {
		if (a <= -10) {
			return 4;
		}
		return switch (a) {
			case 11, 12 -> 4;
			case 13 -> 3;
			case 14 -> 2;
			default -> TOP;
		};
	}

	static int bottom(int a) {
		return a >= 13 ? 0 : BOTTOM;
	}

	static boolean inShape(int d, int a, int y) {
		if (a < STERN || a > NOSE) {
			return false;
		}
		int k = inset(a);
		return d >= k && d <= WIDTH - 1 - k && y >= bottom(a) && y <= top(a);
	}

	static List<Part> layout() {
		List<Part> parts = new ArrayList<>();
		for (int a = STERN; a <= NOSE; a++) {
			for (int d = 0; d < WIDTH; d++) {
				for (int y = BOTTOM; y <= TOP; y++) {
					if (!inShape(d, a, y) || d == 0 && a == 0 && y == 0) {
						continue;
					}
					boolean surface = !inShape(d - 1, a, y) || !inShape(d + 1, a, y) || !inShape(d, a - 1, y)
							|| !inShape(d, a + 1, y) || !inShape(d, a, y - 1) || !inShape(d, a, y + 1);
					parts.add(surface ? surfacePart(d, a, y) : new Part(d, a, y, Kind.FILL));
				}
			}
		}
		wings(parts);
		nacelles(parts);
		tailFin(parts);
		// Landing legs under the four corners; their length is found when building.
		for (int d : new int[] {1, WIDTH - 2}) {
			for (int a : new int[] {-8, 7}) {
				parts.add(new Part(d, a, BOTTOM - 1, Kind.LEG));
			}
		}
		return List.copyOf(parts);
	}

	private static Part surfacePart(int d, int a, int y) {
		int k = inset(a);
		boolean nearSide = d == k;
		boolean farSide = d == WIDTH - 1 - k;
		boolean side = nearSide || farSide;
		boolean roof = y == top(a);
		boolean floor = y == bottom(a);
		if (d == 0 && a == 0 && y == 1) {
			return new Part(d, a, y, Kind.HATCH_TOP);
		}
		// Cockpit canopy: the upper nose is glass.
		if (a >= 10 && a <= 12 && y >= 2 && !floor) {
			return new Part(d, a, y, Kind.CANOPY);
		}
		// Rounded edges along the fuselage: stairs with their high side towards the middle.
		if (side && (roof || floor)) {
			return new Part(d, a, y, Kind.STAIRS, nearSide ? Local.IN : Local.OUT, floor);
		}
		// The roof slopes down where the next section forward or back is lower.
		if (roof && !inShape(d, a + 1, y)) {
			return new Part(d, a, y, Kind.STAIRS, Local.BACK, false);
		}
		if (roof && !inShape(d, a - 1, y)) {
			return new Part(d, a, y, Kind.STAIRS, Local.FWD, false);
		}
		if (a == STERN && y >= 0 && y <= 2 && (d == 2 || d == 4 || d == 6) && (y != 1 || d == 4)) {
			return new Part(d, a, y, Kind.THRUSTER);
		}
		if (side && y == 2 && (a == -7 || a == -4 || a == 3 || a == 6)) {
			return new Part(d, a, y, Kind.PORTHOLE);
		}
		if (side && y == 1 && a >= -11 && a <= 9) {
			return new Part(d, a, y, Kind.STRIPE);
		}
		return new Part(d, a, y, Kind.PLATING);
	}

	/** Swept wings at mid height on both sides, with the navigation lights at the tips (red: port, green: starboard). */
	private static void wings(List<Part> parts) {
		int[][] spans = {{-10, 3}, {-9, 4}, {-8, 5}, {-7, 6}, {-6, 6}, {-5, 5}, {-4, 3}, {-3, 2}};
		for (int[] s : spans) {
			int a = s[0];
			for (int i = 1; i <= s[1]; i++) {
				boolean tip = i == s[1] && a == -7;
				parts.add(new Part(-i, a, 3, tip ? Kind.LIGHT_RED : Kind.SLAB_BOTTOM));
				parts.add(new Part(WIDTH - 1 + i, a, 3, tip ? Kind.LIGHT_GREEN : Kind.SLAB_BOTTOM));
			}
		}
	}

	/** Two engine nacelles beside the stern (2 x 2), on pylons, thrusters at the back and rounded intakes in front. */
	private static void nacelles(List<Part> parts) {
		for (int[] side : new int[][] {{-3, -2, -1}, {WIDTH + 1, WIDTH + 2, WIDTH}}) {
			for (int a = STERN; a <= -4; a++) {
				for (int d : new int[] {side[0], side[1]}) {
					for (int y = 0; y <= 1; y++) {
						Kind kind = a == STERN ? Kind.THRUSTER : Kind.PLATING;
						if (a == -4) {
							parts.add(new Part(d, a, y, Kind.STAIRS, Local.BACK, y == 0));
							continue;
						}
						parts.add(new Part(d, a, y, a == -7 && y == 1 ? Kind.STRIPE : kind));
					}
				}
				if (a >= -10 && a <= -6) {
					parts.add(new Part(side[2], a, 1, Kind.SLAB_BOTTOM)); // pylon
				}
			}
		}
	}

	/** A swept fin on the stern roof with a red light on top. */
	private static void tailFin(List<Part> parts) {
		int d = WIDTH / 2;
		for (int y = 5; y <= 8; y++) {
			int front = -8 - (y - 5);
			for (int a = STERN; a <= front; a++) {
				boolean leading = a == front;
				if (y == 8 && a == STERN) {
					parts.add(new Part(d, a, y, Kind.LIGHT_RED));
				} else if (leading) {
					parts.add(new Part(d, a, y, Kind.STAIRS, Local.BACK, false));
				} else {
					parts.add(new Part(d, a, y, Kind.PLATING));
				}
			}
		}
	}

	public static List<Part> parts() {
		return PARTS;
	}

	// ---- Building.

	static BlockPos at(BlockPos hatch, Direction out, int d, int a, int y) {
		Direction in = out.getOpposite();
		Direction along = out.getClockWise();
		return hatch.relative(in, d).relative(along, a).above(y);
	}

	private static Direction world(Local local, Direction out) {
		return switch (local) {
			case IN -> out.getOpposite();
			case OUT -> out;
			case FWD -> out.getClockWise();
			case BACK -> out.getCounterClockWise();
		};
	}

	private static BlockState stateOf(Part part, Direction out) {
		return switch (part.kind()) {
			case PORTHOLE -> ModBlocks.SHIP_PORTHOLE.defaultBlockState();
			case THRUSTER -> ModBlocks.SHIP_THRUSTER.defaultBlockState();
			case STRIPE -> ModBlocks.SHIP_STRIPE.defaultBlockState();
			case CANOPY -> ModBlocks.SHIP_VIEWPORT.defaultBlockState();
			case LIGHT_RED -> ModBlocks.SHIP_LIGHT_RED.defaultBlockState();
			case LIGHT_GREEN -> ModBlocks.SHIP_LIGHT_GREEN.defaultBlockState();
			case HATCH_TOP -> ModBlocks.SHIP_HATCH_TOP.defaultBlockState().setValue(ShipFacingBlock.FACING, out);
			case STAIRS -> ModBlocks.SHIP_PLATING_STAIRS.defaultBlockState()
					.setValue(StairBlock.FACING, world(part.facing(), out))
					.setValue(StairBlock.HALF, part.top() ? Half.TOP : Half.BOTTOM);
			case SLAB_BOTTOM -> ModBlocks.SHIP_PLATING_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
			case SLAB_TOP -> ModBlocks.SHIP_PLATING_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
			default -> ModBlocks.SHIP_PLATING.defaultBlockState();
		};
	}

	/** Whether a block belongs to a ship hull (and may be removed at takeoff). */
	public static boolean isHull(BlockState state) {
		return state.is(ModBlocks.SHIP_PLATING) || state.is(ModBlocks.SHIP_PORTHOLE) || state.is(ModBlocks.SHIP_THRUSTER)
				|| state.is(ModBlocks.SHIP_PLATING_STAIRS) || state.is(ModBlocks.SHIP_PLATING_SLAB)
				|| state.is(ModBlocks.SHIP_STRIPE) || state.is(ModBlocks.SHIP_LIGHT_RED)
				|| state.is(ModBlocks.SHIP_LIGHT_GREEN) || state.is(ModBlocks.SHIP_HATCH_TOP)
				|| state.is(ModBlocks.SHIP_VIEWPORT);
	}

	/** Builds the hull on the ground around a hatch that faces {@code out}. */
	public static void build(ServerLevel level, BlockPos hatch, Direction out) {
		build(level, hatch, out, Mode.GROUND);
	}

	/**
	 * Builds the hull around a hatch that faces {@code out}. Blocks with contents (chests...) are never replaced; docked,
	 * only air is replaced (the station stays as it is) and there are no legs.
	 */
	public static void build(ServerLevel level, BlockPos hatch, Direction out, Mode mode) {
		for (Part part : PARTS) {
			BlockPos pos = at(hatch, out, part.d(), part.a(), part.y());
			level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
			if (part.kind() == Kind.LEG) {
				if (mode == Mode.GROUND) {
					buildLeg(level, pos);
				}
				continue;
			}
			BlockState current = level.getBlockState(pos);
			if (level.getBlockEntity(pos) != null || current.is(ModBlocks.SHIP_HATCH)) {
				continue;
			}
			if (mode == Mode.DOCKED && !current.isAir() && !isHull(current)) {
				continue;
			}
			boolean fuselage = inShape(part.d(), part.a(), part.y());
			boolean free = current.canBeReplaced() || isHull(current);
			if (!free && (part.kind() == Kind.FILL || !fuselage)) {
				// Terrain inside the hull stays, and it cuts into wings and nacelles; the fuselage skin always goes on.
				continue;
			}
			level.setBlock(pos, stateOf(part, out), Block.UPDATE_CLIENTS);
		}
	}

	/** A leg down to the ground (at most {@link #LEG_DEPTH} blocks) with a cross-shaped footpad of slabs. */
	private static void buildLeg(ServerLevel level, BlockPos top) {
		BlockPos last = null;
		for (int i = 0; i < LEG_DEPTH; i++) {
			BlockPos p = top.below(i);
			BlockState st = level.getBlockState(p);
			if (!st.canBeReplaced() && !isHull(st)) {
				break;
			}
			level.setBlock(p, ModBlocks.SHIP_PLATING.defaultBlockState(), Block.UPDATE_CLIENTS);
			last = p;
		}
		if (last == null) {
			return;
		}
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos pad = last.relative(dir);
			if (level.getBlockState(pad).canBeReplaced() && !level.getBlockState(pad.below()).canBeReplaced()) {
				level.setBlock(pad, ModBlocks.SHIP_PLATING_SLAB.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
	}

	/**
	 * Takes the hull away (takeoff): every hull block in the space the ship can take, including the legs and their
	 * footpads, and also the blocks of older hull shapes.
	 */
	public static void remove(ServerLevel level, BlockPos hatch, Direction out) {
		for (int a = STERN; a <= NOSE; a++) {
			for (int d = -8; d <= WIDTH + 7; d++) {
				for (int y = BOTTOM - LEG_DEPTH - 1; y <= 9; y++) {
					BlockPos pos = at(hatch, out, d, a, y);
					if (isHull(level.getBlockState(pos))) {
						level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
					}
				}
			}
		}
	}

	/** Whether the hull stands around this hatch (checked on the hatch's upper half). */
	public static boolean isBuilt(ServerLevel level, BlockPos hatch, Direction out) {
		return level.getBlockState(at(hatch, out, 1, 0, 0)).is(ModBlocks.SHIP_PLATING)
				|| isHull(level.getBlockState(at(hatch, out, 0, 1, 0)));
	}

	static void ensureBuilt(MinecraftServer server) {
		ShipState state = ShipStateAccess.get(server);
		if (state.isReadOnly() || state.phase() != ShipFlightPhase.LANDED || state.landingSite().isEmpty()) {
			return;
		}
		ShipState.LandingSite site = state.landingSite().get();
		ServerLevel level = ShipBoarding.levelOf(server, site);
		BlockPos hatch = new BlockPos(site.x(), site.y(), site.z());
		if (level == null) {
			return;
		}
		level.getChunk(hatch.getX() >> 4, hatch.getZ() >> 4);
		BlockState hatchState = level.getBlockState(hatch);
		if (!hatchState.is(ModBlocks.SHIP_HATCH)) {
			return;
		}
		Direction out = hatchState.getValue(ShipFacingBlock.FACING);
		boolean docked = ShipDimensions.ORBIT.identifier().toString().equals(site.dimension());
		// The hull is rebuilt every start: older hulls (the box-shaped one) get the current shape.
		remove(level, hatch, out);
		build(level, hatch, out, docked ? Mode.DOCKED : Mode.GROUND);
	}
}
