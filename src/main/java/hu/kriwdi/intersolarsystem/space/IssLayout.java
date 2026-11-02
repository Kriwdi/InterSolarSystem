package hu.kriwdi.intersolarsystem.space;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The derelict International Space Station, as a list of block placements around (0, 0, 0). Pure data: the builder turns
 * pieces into blocks. The pressurised modules run along the z axis (Harmony at the front with the docking port, then
 * Destiny, Unity, Zarya and Zvezda at the back); the main truss runs along the x axis above them and carries four
 * solar wings. Decades of neglect: a few hull plates and solar cells are missing, deterministic per seed.
 */
public final class IssLayout {
	public enum Piece {
		HULL, FOIL_HULL, WINDOW, LIGHT, TRUSS, SOLAR, SUPPLY, DOCK_FLOOR, AIR
	}

	public record Placement(int x, int y, int z, Piece piece) {
	}

	/** Module tube: octagonal cross-section of radius 3 around the axis at y = 0, x = 0. */
	public static final int RADIUS = 3;
	public static final int FRONT_CAP_Z = -15;
	public static final int BACK_CAP_Z = 18;
	public static final int TRUSS_Y = 6;
	public static final int TRUSS_HALF_LENGTH = 32;
	/** Where the ship's hatch stands: on the docking platform in front of Harmony. */
	public static final int[] DOCK_HATCH = {0, -2, -20};
	private static final long DAMAGE_SEED = 1998L;

	private IssLayout() {
	}

	private static boolean insideTube(int dx, int dy) {
		return Math.max(Math.abs(dx), Math.abs(dy)) <= RADIUS && Math.abs(dx) + Math.abs(dy) <= RADIUS + 1;
	}

	private static boolean shell(int dx, int dy) {
		return insideTube(dx, dy) && (!insideTube(dx + 1, dy) || !insideTube(dx - 1, dy) || !insideTube(dx, dy + 1)
				|| !insideTube(dx, dy - 1));
	}

	/** Russian segment (Zarya, Zvezda) in gold insulation foil, the rest in white panels. */
	private static boolean russian(int z) {
		return z >= 3;
	}

	public static List<Placement> build() {
		Map<String, Placement> cells = new LinkedHashMap<>();
		Random damage = new Random(DAMAGE_SEED);

		// Pressurised tube with end caps.
		for (int z = FRONT_CAP_Z; z <= BACK_CAP_Z; z++) {
			boolean cap = z == FRONT_CAP_Z || z == BACK_CAP_Z;
			for (int dx = -RADIUS; dx <= RADIUS; dx++) {
				for (int dy = -RADIUS; dy <= RADIUS; dy++) {
					if (!insideTube(dx, dy)) {
						continue;
					}
					Piece piece;
					if (cap || shell(dx, dy)) {
						boolean window = !cap && dy == 0 && Math.abs(dx) == RADIUS && Math.floorMod(z, 4) == 1;
						piece = window ? Piece.WINDOW : russian(z) ? Piece.FOIL_HULL : Piece.HULL;
						// Breaches: a few plates are gone (never in the floor or the front cap).
						if (!cap && dy > -2 && piece != Piece.WINDOW && damage.nextDouble() < 0.04) {
							piece = Piece.AIR;
						}
					} else {
						boolean ceilingLight = dy == RADIUS - 1 && dx == 0 && Math.floorMod(z, 3) == 0;
						piece = ceilingLight ? Piece.LIGHT : Piece.AIR;
					}
					put(cells, dx, dy, z, piece);
				}
			}
		}
		// Doorway in the front cap, towards the docking platform.
		put(cells, 0, -2, FRONT_CAP_Z, Piece.AIR);
		put(cells, 0, -1, FRONT_CAP_Z, Piece.AIR);
		// Supply containers along the modules (on the floor, against the walls).
		for (int[] s : new int[][] {{-2, -9}, {2, -6}, {-2, -1}, {2, 5}, {-2, 12}, {2, 15}}) {
			put(cells, s[0], -2, s[1], Piece.SUPPLY);
		}
		// Docking platform and the walkway to the doorway.
		for (int z = -22; z <= FRONT_CAP_Z - 1; z++) {
			for (int x = -2; x <= 2; x++) {
				boolean platform = z <= -18;
				if (platform || Math.abs(x) <= 1) {
					put(cells, x, -3, z, Piece.DOCK_FLOOR);
				}
			}
		}
		// Main truss along x, a column down to the modules, and the solar wing masts along z.
		for (int x = -TRUSS_HALF_LENGTH; x <= TRUSS_HALF_LENGTH; x++) {
			put(cells, x, TRUSS_Y, 0, Piece.TRUSS);
		}
		for (int y = RADIUS + 1; y < TRUSS_Y; y++) {
			put(cells, 0, y, 0, Piece.TRUSS);
		}
		for (int mast : new int[] {-26, 26}) {
			for (int z = -13; z <= 13; z++) {
				put(cells, mast, TRUSS_Y, z, Piece.TRUSS);
			}
			// Four wings per side of the truss: arrays of cells either side of the mast.
			for (int z = -13; z <= 13; z++) {
				if (Math.abs(z) < 2) {
					continue;
				}
				for (int dx = -5; dx <= 5; dx++) {
					if (dx == 0) {
						continue;
					}
					boolean missing = damage.nextDouble() < 0.08;
					put(cells, mast + dx, TRUSS_Y, z, missing ? Piece.AIR : Piece.SOLAR);
				}
			}
		}
		return new ArrayList<>(cells.values());
	}

	private static void put(Map<String, Placement> cells, int x, int y, int z, Piece piece) {
		cells.put(x + "," + y + "," + z, new Placement(x, y, z, piece));
	}
}
