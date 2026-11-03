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
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ShipFacingBlock;

/**
 * The ship as it stands where it landed: a hull of indestructible plating around the hatch, with portholes, a cockpit
 * windshield on the nose, thrusters at the stern and four landing legs. It is built when the ship lands and removed
 * when it takes off; only the hull's own blocks are ever removed.
 *
 * <p>Local coordinates: {@code d} goes into the ship from the hatch wall (0..8), {@code a} runs along the ship
 * (stern -10 to nose +12, the hatch at 0), {@code y} is up from the hatch (the floor is at -1).
 */
public final class ShipShell {
	public static final int WIDTH = 9;
	public static final int STERN = -10;
	public static final int NOSE = 12;
	public static final int BOTTOM = -1;
	public static final int TOP = 5;

	public enum Kind { PLATING, PORTHOLE, THRUSTER, FILL, LEG }

	public record Part(int d, int a, int y, Kind kind) {
	}

	private static final List<Part> PARTS = layout();

	private ShipShell() {
	}

	public static void register() {
		// Worlds from before the hull existed: a landed ship gets its hull the next time the server starts.
		ServerLifecycleEvents.SERVER_STARTED.register(ShipShell::ensureBuilt);
	}

	/** Inset of the cross-section at a point along the ship (the nose tapers). */
	static int inset(int a) {
		return a > 10 ? a - 10 : 0;
	}

	static boolean inShape(int d, int a, int y) {
		if (a < STERN || a > NOSE) {
			return false;
		}
		int k = inset(a);
		if (d < k || d > WIDTH - 1 - k || y < BOTTOM || y > TOP - k) {
			return false;
		}
		boolean sideEdge = d == k || d == WIDTH - 1 - k;
		boolean corner = sideEdge && (y == BOTTOM || y == TOP - k);
		return !corner;
	}

	static List<Part> layout() {
		List<Part> parts = new ArrayList<>();
		for (int a = STERN; a <= NOSE; a++) {
			for (int d = 0; d < WIDTH; d++) {
				for (int y = BOTTOM; y <= TOP; y++) {
					if (!inShape(d, a, y) || (d == 0 && a == 0 && y == 0)) {
						continue;
					}
					boolean surface = !inShape(d - 1, a, y) || !inShape(d + 1, a, y) || !inShape(d, a - 1, y)
							|| !inShape(d, a + 1, y) || !inShape(d, a, y - 1) || !inShape(d, a, y + 1);
					if (!surface) {
						parts.add(new Part(d, a, y, Kind.FILL));
						continue;
					}
					parts.add(new Part(d, a, y, kindAt(d, a, y)));
				}
			}
		}
		// Landing legs under the four corners; their length is found when building.
		for (int d : new int[] {1, WIDTH - 2}) {
			for (int a : new int[] {-8, 8}) {
				parts.add(new Part(d, a, BOTTOM - 1, Kind.LEG));
			}
		}
		return List.copyOf(parts);
	}

	private static Kind kindAt(int d, int a, int y) {
		boolean side = d == inset(a) || d == WIDTH - 1 - inset(a);
		if (side && y == 2 && a != 0 && (a == -6 || a == -3 || a == 3 || a == 6 || a == 9)) {
			return Kind.PORTHOLE;
		}
		if (a >= 11 && y >= 2 && y < TOP - inset(a)) {
			return Kind.PORTHOLE; // the cockpit windshield
		}
		if (a == STERN && ((y == 1 || y == 3) && (d == 2 || d == 6) || y == 2 && d == 4)) {
			return Kind.THRUSTER;
		}
		return Kind.PLATING;
	}

	public static List<Part> parts() {
		return PARTS;
	}

	static BlockPos at(BlockPos hatch, Direction out, int d, int a, int y) {
		Direction in = out.getOpposite();
		Direction along = out.getClockWise();
		return hatch.relative(in, d).relative(along, a).above(y);
	}

	private static BlockState stateOf(Kind kind) {
		return switch (kind) {
			case PORTHOLE -> ModBlocks.SHIP_PORTHOLE.defaultBlockState();
			case THRUSTER -> ModBlocks.SHIP_THRUSTER.defaultBlockState();
			default -> ModBlocks.SHIP_PLATING.defaultBlockState();
		};
	}

	static boolean isHull(BlockState state) {
		return state.is(ModBlocks.SHIP_PLATING) || state.is(ModBlocks.SHIP_PORTHOLE) || state.is(ModBlocks.SHIP_THRUSTER);
	}

	/** Builds the hull around a hatch that faces {@code out}. Blocks with contents (chests...) are never replaced. */
	public static void build(ServerLevel level, BlockPos hatch, Direction out) {
		for (Part part : PARTS) {
			BlockPos pos = at(hatch, out, part.d(), part.a(), part.y());
			level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
			if (part.kind() == Kind.LEG) {
				// Down to the ground, at most 12 blocks.
				for (int i = 0; i < 12; i++) {
					BlockPos p = pos.below(i);
					BlockState st = level.getBlockState(p);
					if (!st.canBeReplaced() && !isHull(st)) {
						break;
					}
					level.setBlock(p, ModBlocks.SHIP_PLATING.defaultBlockState(), Block.UPDATE_CLIENTS);
				}
				continue;
			}
			BlockState current = level.getBlockState(pos);
			if (level.getBlockEntity(pos) != null || current.is(ModBlocks.SHIP_HATCH)) {
				continue;
			}
			if (part.kind() == Kind.FILL && !current.canBeReplaced()) {
				continue; // terrain inside the hull stays as it is
			}
			level.setBlock(pos, stateOf(part.kind()), Block.UPDATE_CLIENTS);
		}
	}

	/** Takes the hull away (takeoff): only the hull's own blocks, including the legs. */
	public static void remove(ServerLevel level, BlockPos hatch, Direction out) {
		for (Part part : PARTS) {
			BlockPos pos = at(hatch, out, part.d(), part.a(), part.y());
			int depth = part.kind() == Kind.LEG ? 12 : 1;
			for (int i = 0; i < depth; i++) {
				BlockPos p = pos.below(i);
				if (isHull(level.getBlockState(p))) {
					level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
				} else if (part.kind() == Kind.LEG) {
					break;
				}
			}
		}
	}

	/** Whether the hull stands around this hatch (checked on a wall block next to it). */
	public static boolean isBuilt(ServerLevel level, BlockPos hatch, Direction out) {
		return isHull(level.getBlockState(at(hatch, out, 0, 0, 1)));
	}

	static void ensureBuilt(MinecraftServer server) {
		ShipState state = ShipStateAccess.get(server);
		if (state.isReadOnly() || state.phase() != ShipFlightPhase.LANDED || state.landingSite().isEmpty()) {
			return;
		}
		ShipState.LandingSite site = state.landingSite().get();
		if (ShipDimensions.ORBIT.identifier().toString().equals(site.dimension())) {
			return; // docked at the ISS
		}
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
		if (!isBuilt(level, hatch, out)) {
			build(level, hatch, out);
		}
	}
}
