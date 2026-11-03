package hu.kriwdi.intersolarsystem.space;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The derelict International Space Station at full size (1 block = 1 metre), as block placements around (0, 0, 0).
 * Pure data: the builder turns pieces into blocks.
 *
 * <p>Axes: x along the main truss (port is -x, starboard +x), z along the pressurised modules (forward, the velocity
 * direction, is -z), y up (zenith). The modules are tubes 5 blocks across with a 3 x 3 walkway inside:
 * PMA-2 with the docking port at the front, Harmony (Columbus to port, Kibo with its logistics module and exposed
 * facility to starboard), Destiny, Unity (Tranquility with the Cupola to port, the Quest airlock to starboard), PMA-1,
 * Zarya (Rassvet below) and Zvezda (Poisk above, Nauka below, its own solar wings) at the back. The 109 m truss
 * runs above Destiny with the rotary joints, the radiators and four pairs of 35 m solar wings. Decades of neglect: a
 * few hull plates and solar cells are missing, deterministic per seed.
 */
public final class IssLayout {
	public enum Piece {
		HULL, FOIL_HULL, WINDOW, GLASS, LIGHT, TRUSS, SOLAR, RADIATOR, SUPPLY, DOCK_FLOOR, AIR
	}

	public record Placement(int x, int y, int z, Piece piece) {
	}

	/** Module tube: octagonal cross-section of radius 2 (5 blocks, a 3 x 3 walkway inside). */
	public static final int RADIUS = 2;
	/** The docking port: the front cap of PMA-2, with a two-block doorway. */
	public static final int FRONT_CAP_Z = -32;
	public static final int BACK_CAP_Z = 22;
	public static final int TRUSS_Y = 6;
	public static final int TRUSS_Z = -16;
	public static final int TRUSS_HALF_LENGTH = 56;
	/** Where the ship's hatch stands: just outside the docking port, on the walkway level. */
	public static final int[] DOCK_HATCH = {0, -1, FRONT_CAP_Z - 1};
	/** A cell only the full-size station has (the docking port's floor): tells it from the old, small one. */
	public static final int[] MARKER = {0, -2, FRONT_CAP_Z};
	private static final long DAMAGE_SEED = 1998L;

	private enum Axis { X, Y, Z }

	private IssLayout() {
	}

	static boolean insideTube(int u, int v) {
		return Math.max(Math.abs(u), Math.abs(v)) <= RADIUS && Math.abs(u) + Math.abs(v) <= RADIUS + 1;
	}

	static boolean shell(int u, int v) {
		return insideTube(u, v) && (!insideTube(u + 1, v) || !insideTube(u - 1, v) || !insideTube(u, v + 1)
				|| !insideTube(u, v - 1));
	}

	public static List<Placement> build() {
		Map<String, Placement> cells = new LinkedHashMap<>();
		Random damage = new Random(DAMAGE_SEED);

		// The main row of modules, front to back, with hatch frames where they meet.
		module(cells, damage, Axis.Z, 0, 0, FRONT_CAP_Z, -28, false, true, false); // PMA-2 and the docking adapter
		module(cells, damage, Axis.Z, 0, 0, -27, -21, false, false, false); // Harmony
		module(cells, damage, Axis.Z, 0, 0, -20, -12, false, false, false); // Destiny
		module(cells, damage, Axis.Z, 0, 0, -11, -6, false, false, false); // Unity
		module(cells, damage, Axis.Z, 0, 0, -5, -4, false, false, false); // PMA-1
		module(cells, damage, Axis.Z, 0, 0, -3, 9, true, false, false); // Zarya
		module(cells, damage, Axis.Z, 0, 0, 10, BACK_CAP_Z, true, false, true); // Zvezda
		for (int z : new int[] {-28, -21, -12, -6, -4, 9}) {
			hatchFrame(cells, z);
		}
		// Docking port: the front cap with a two-block doorway.
		put(cells, 0, -1, FRONT_CAP_Z, Piece.AIR);
		put(cells, 0, 0, FRONT_CAP_Z, Piece.AIR);
		put(cells, MARKER[0], MARKER[1], MARKER[2], Piece.DOCK_FLOOR);

		// Side modules (they start in the node's wall, which opens into them).
		module(cells, damage, Axis.X, -24, 0, -9, -2, false, true, false); // Columbus (port of Harmony)
		module(cells, damage, Axis.X, -24, 0, 2, 13, false, false, true); // Kibo pressurised module
		module(cells, damage, Axis.Y, 8, -24, 2, 6, false, false, true); // Kibo logistics module, on top
		kiboExposedFacility(cells);
		module(cells, damage, Axis.X, -9, 0, -9, -2, false, true, false); // Tranquility (port of Unity)
		cupola(cells, -6, -9);
		module(cells, damage, Axis.X, -9, 0, 2, 7, false, false, true); // Quest airlock
		module(cells, damage, Axis.Y, 0, 11, 2, 7, true, false, true); // Poisk, zenith of Zvezda
		module(cells, damage, Axis.Y, 0, 13, -14, -2, true, true, false); // Nauka, nadir of Zvezda
		module(cells, damage, Axis.Y, 0, 3, -7, -2, false, true, false); // Rassvet, nadir of Zarya

		// Supplies along the walkways.
		int[][] supplies = {{-1, -25}, {1, -18}, {-1, -14}, {1, -8}, {-1, 2}, {1, 6}, {-1, 15}, {1, 19}};
		for (int[] s : supplies) {
			put(cells, s[0], -1, s[1], Piece.SUPPLY);
		}
		put(cells, -7, -1, -23, Piece.SUPPLY); // Columbus
		put(cells, 10, -1, -25, Piece.SUPPLY); // Kibo

		truss(cells, damage);
		zvezdaWings(cells, damage);
		return new ArrayList<>(cells.values());
	}

	/**
	 * A tube along {@code axis} from {@code from} to {@code to}, its centre line at ({@code c1}, {@code c2}) in the other
	 * two axes (for Z: x, y; for X: z, y; for Y: x, z). Caps close the low or high end.
	 */
	private static void module(Map<String, Placement> cells, Random damage, Axis axis, int c1, int c2, int from,
			int to, boolean foil, boolean capLow, boolean capHigh) {
		for (int t = from; t <= to; t++) {
			boolean cap = capLow && t == from || capHigh && t == to;
			for (int u = -RADIUS; u <= RADIUS; u++) {
				for (int v = -RADIUS; v <= RADIUS; v++) {
					if (!insideTube(u, v)) {
						continue;
					}
					Piece piece;
					if (cap || shell(u, v)) {
						boolean floor = axis != Axis.Y && v == -RADIUS;
						boolean window = !cap && !floor && Math.abs(u) == RADIUS && v == 0 && Math.floorMod(t, 4) == 1;
						piece = window ? Piece.WINDOW : foil ? Piece.FOIL_HULL : Piece.HULL;
						if (!cap && !floor && !window && damage.nextDouble() < 0.02) {
							piece = Piece.AIR; // a breach
						}
					} else {
						boolean ceiling = axis != Axis.Y && v == RADIUS - 1 && u == 0 && Math.floorMod(t, 3) == 0;
						piece = ceiling ? Piece.LIGHT : Piece.AIR;
					}
					int[] p = switch (axis) {
						case Z -> new int[] {c1 + u, c2 + v, t};
						case X -> new int[] {t, c2 + v, c1 + u};
						case Y -> new int[] {c1 + u, t, c2 + v};
					};
					put(cells, p[0], p[1], p[2], piece);
				}
			}
		}
	}

	/** The hatch frame between two modules: the upper corners of the walkway close in, a plus-shaped opening stays. */
	private static void hatchFrame(Map<String, Placement> cells, int z) {
		put(cells, -1, 1, z, Piece.HULL);
		put(cells, 1, 1, z, Piece.HULL);
	}

	/** The Cupola under Tranquility: a seven-window dome to look down at the Earth from. */
	private static void cupola(Map<String, Placement> cells, int x, int z) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				boolean rim = Math.max(Math.abs(dx), Math.abs(dz)) == 2;
				put(cells, x + dx, -2, z + dz, rim ? Piece.HULL : Piece.AIR);
			}
		}
		for (int y = -4; y <= -3; y++) {
			for (int dx = -2; dx <= 2; dx++) {
				for (int dz = -2; dz <= 2; dz++) {
					boolean edge = Math.max(Math.abs(dx), Math.abs(dz)) == 2;
					boolean corner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
					put(cells, x + dx, y, z + dz, corner ? Piece.HULL : edge ? Piece.GLASS : Piece.AIR);
				}
			}
		}
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				boolean rim = Math.max(Math.abs(dx), Math.abs(dz)) == 2;
				put(cells, x + dx, -5, z + dz, rim ? Piece.HULL : Piece.GLASS);
			}
		}
	}

	/** Kibo's exposed facility: an open deck at the end of the module with experiment boxes on it. */
	private static void kiboExposedFacility(Map<String, Placement> cells) {
		for (int x = 14; x <= 19; x++) {
			for (int z = -26; z <= -22; z++) {
				put(cells, x, -1, z, Piece.HULL);
			}
		}
		for (int[] e : new int[][] {{15, -26}, {17, -26}, {19, -24}, {15, -22}, {18, -22}}) {
			put(cells, e[0], 0, e[1], Piece.RADIATOR);
			put(cells, e[0], 1, e[1], Piece.LIGHT);
		}
	}

	/** The integrated truss above Destiny, the rotary joints, the radiators and the eight solar wings. */
	private static void truss(Map<String, Placement> cells, Random damage) {
		for (int x = -TRUSS_HALF_LENGTH; x <= TRUSS_HALF_LENGTH; x++) {
			for (int y = TRUSS_Y - 1; y <= TRUSS_Y + 1; y++) {
				for (int z = TRUSS_Z - 1; z <= TRUSS_Z + 1; z++) {
					put(cells, x, y, z, Piece.TRUSS);
				}
			}
		}
		// Mount from Destiny's roof up to the truss.
		for (int y = RADIUS + 1; y < TRUSS_Y - 1; y++) {
			put(cells, 0, y, TRUSS_Z, Piece.TRUSS);
		}
		// Solar alpha rotary joints: heavy rings around the truss.
		for (int jx : new int[] {-32, -31, 31, 32}) {
			for (int y = TRUSS_Y - 2; y <= TRUSS_Y + 2; y++) {
				for (int z = TRUSS_Z - 2; z <= TRUSS_Z + 2; z++) {
					if (Math.abs(y - TRUSS_Y) == 2 || Math.abs(z - TRUSS_Z) == 2) {
						put(cells, jx, y, z, Piece.HULL);
					}
				}
			}
		}
		// Heat rejection radiators: two white panels each side, reaching back over the modules.
		for (int sign : new int[] {-1, 1}) {
			for (int[] panel : new int[][] {{11, 14}, {17, 20}, {23, 26}}) {
				for (int x = panel[0]; x <= panel[1]; x++) {
					for (int z = TRUSS_Z + 2; z <= TRUSS_Z + 22; z++) {
						put(cells, sign * x, TRUSS_Y, z, damage.nextDouble() < 0.03 ? Piece.AIR : Piece.RADIATOR);
					}
				}
			}
		}
		// Solar wings: at each of the four beta gimbals a mast reaches 35 m forward and aft, with a blanket of cells on
		// each side of it.
		for (int mast : new int[] {-54, -40, 40, 54}) {
			for (int dir : new int[] {-1, 1}) {
				for (int i = 2; i <= 36; i++) {
					int z = TRUSS_Z + dir * i;
					put(cells, mast, TRUSS_Y, z, Piece.TRUSS);
					if (i < 3) {
						continue;
					}
					for (int dx = 2; dx <= 6; dx++) {
						for (int s : new int[] {-1, 1}) {
							put(cells, mast + s * dx, TRUSS_Y, z, damage.nextDouble() < 0.06 ? Piece.AIR : Piece.SOLAR);
						}
					}
				}
			}
		}
	}

	/** Zvezda's own pair of solar wings, out to the sides near its back. */
	private static void zvezdaWings(Map<String, Placement> cells, Random damage) {
		for (int s : new int[] {-1, 1}) {
			for (int x = 3; x <= 13; x++) {
				for (int z = 17; z <= 19; z++) {
					put(cells, s * x, 0, z, x == 3 ? Piece.TRUSS : damage.nextDouble() < 0.06 ? Piece.AIR : Piece.SOLAR);
				}
			}
		}
	}

	private static void put(Map<String, Placement> cells, int x, int y, int z, Piece piece) {
		cells.put(x + "," + y + "," + z, new Placement(x, y, z, piece));
	}
}
