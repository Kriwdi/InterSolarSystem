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
 * The research ship as it stands where it landed (or hangs at the ISS docking port): upright like a rocket, a white
 * cylinder of indestructible plating with livery bands, portholes, a band of tinted cockpit windows under the nose cone
 * and a beacon on its tip, a ring of engine nozzles at the base, and three swept fins with landing legs and navigation
 * lights. It is built when the ship arrives and removed when it takes off; only the hull's own blocks are removed.
 *
 * <p>Local coordinates: {@code d} goes into the ship from the hatch wall (the body is 0..6 around its axis at 3),
 * {@code a} runs sideways (the hatch at 0), {@code y} is up from the hatch (the body starts at -1). The hatch is two
 * blocks tall (y 0 and 1). The interior is a separate room and stays level.
 */
public final class ShipShell {
	/** The body is 7 across (d 0..6), its axis at d = 3, a = 0. */
	public static final int WIDTH = 7;
	static final int AXIS_D = 3;
	public static final int BOTTOM = -1;
	/** Top of the cylindrical body; the nose cone rises above it to {@link #NOSE_TIP}. */
	public static final int TOP = 17;
	static final int NOSE_TIP = 24;
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

	// ---- Shape: a rocket standing on three fins.

	/** Radius of the body at a height (the nose cone narrows above {@link #TOP}). */
	static double radius(int y) {
		if (y < BOTTOM || y > NOSE_TIP) {
			return -1;
		}
		if (y <= TOP) {
			return 3.3;
		}
		return 3.3 * (1.0 - (double) (y - TOP) / (NOSE_TIP - TOP + 1));
	}

	static boolean inShape(int d, int a, int y) {
		double r = radius(y);
		if (r < 0) {
			return false;
		}
		double dd = d - AXIS_D;
		return dd * dd + a * a <= r * r + 0.01 || y >= NOSE_TIP - 1 && d == AXIS_D && a == 0;
	}

	static List<Part> layout() {
		List<Part> parts = new ArrayList<>();
		for (int y = BOTTOM; y <= NOSE_TIP; y++) {
			for (int d = 0; d < WIDTH; d++) {
				for (int a = -AXIS_D; a <= AXIS_D; a++) {
					if (!inShape(d, a, y) || d == 0 && a == 0 && y == 0) {
						continue;
					}
					boolean surface = !inShape(d - 1, a, y) || !inShape(d + 1, a, y) || !inShape(d, a - 1, y)
							|| !inShape(d, a + 1, y) || !inShape(d, a, y - 1) || !inShape(d, a, y + 1);
					parts.add(surface ? surfacePart(d, a, y) : new Part(d, a, y, Kind.FILL));
				}
			}
		}
		parts.add(new Part(AXIS_D, 0, NOSE_TIP + 1, Kind.LIGHT_RED)); // beacon on the nose
		fins(parts);
		return List.copyOf(parts);
	}

	private static Part surfacePart(int d, int a, int y) {
		if (d == 0 && a == 0 && y == 1) {
			return new Part(d, a, y, Kind.HATCH_TOP);
		}
		double dd = d - AXIS_D;
		boolean side = inShape(d, a, y + 1) && inShape(d, a, y - 1);
		boolean cardinal = dd == 0 || a == 0;
		boolean nearHatch = Math.abs(a) <= 1 && d <= 1;
		// Cockpit: a band of tinted windows near the top, between frame pillars.
		if (side && (y == TOP - 3 || y == TOP - 2) && !cardinal) {
			return new Part(d, a, y, Kind.CANOPY);
		}
		if (side && (y == 4 || y == 9) && cardinal && !nearHatch) {
			return new Part(d, a, y, Kind.PORTHOLE);
		}
		if (side && (y == 2 || y == TOP - 5)) {
			return new Part(d, a, y, Kind.STRIPE);
		}
		// The engine skirt: nozzles all round the base, except beside the hatch.
		if (side && y == 0 && !nearHatch && !cardinal) {
			return new Part(d, a, y, Kind.THRUSTER);
		}
		// The nose cone's steps are smoothed with stairs facing the axis.
		if (y > TOP && !inShape(d, a, y + 1) && !(d == AXIS_D && a == 0)) {
			Local towards = Math.abs(dd) >= Math.abs(a) ? (dd < 0 ? Local.IN : Local.OUT) : (a < 0 ? Local.FWD : Local.BACK);
			return new Part(d, a, y, Kind.STAIRS, towards, false);
		}
		return new Part(d, a, y, Kind.PLATING);
	}

	/**
	 * Three swept fins (back, port, starboard; the hatch side stays free), each with a landing leg and a light at the
	 * tip.
	 */
	private static void fins(List<Part> parts) {
		int[][] dirs = {{1, 0}, {0, -1}, {0, 1}};
		Kind[] lights = {Kind.LIGHT_RED, Kind.LIGHT_RED, Kind.LIGHT_GREEN};
		for (int f = 0; f < dirs.length; f++) {
			int sd = dirs[f][0];
			int sa = dirs[f][1];
			for (int k = 1; k <= 3; k++) {
				int d = AXIS_D + sd * (AXIS_D + k);
				int a = sa * (AXIS_D + k);
				int top = 7 - 2 * k;
				for (int y = BOTTOM; y <= top; y++) {
					Local back = sd != 0 ? (sd > 0 ? Local.OUT : Local.IN) : (sa > 0 ? Local.BACK : Local.FWD);
					if (y == top) {
						parts.add(new Part(d, a, y, k == 3 ? lights[f] : Kind.STAIRS, back, false));
					} else {
						parts.add(new Part(d, a, y, Kind.PLATING));
					}
				}
				if (k == 3) {
					parts.add(new Part(d, a, BOTTOM - 1, Kind.LEG));
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
			case CANOPY -> ModBlocks.SHIP_WINDOW.defaultBlockState();
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
				|| state.is(ModBlocks.SHIP_VIEWPORT) || state.is(ModBlocks.SHIP_WINDOW);
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
			boolean fuselage = inShape(part.d(), part.a(), part.y()) || part.kind() == Kind.LEG;
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
		for (int a = -14; a <= 14; a++) {
			for (int d = -8; d <= 16; d++) {
				for (int y = BOTTOM - LEG_DEPTH - 1; y <= NOSE_TIP + 2; y++) {
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
		boolean docked = ShipDimensions.ORBIT.identifier().toString().equals(site.dimension())
				|| ShipDimensions.MOTHERSHIP.identifier().toString().equals(site.dimension());
		// The hull is rebuilt every start: older hulls (the box-shaped one) get the current shape.
		remove(level, hatch, out);
		build(level, hatch, out, docked ? Mode.DOCKED : Mode.GROUND);
	}
}
