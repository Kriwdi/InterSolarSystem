package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Block layout of the ship's interior, relative to the room's lowest corner. Pure data: the server-side builder turns
 * {@link Piece}s into blocks.
 *
 * <p>The standard ship is 9 wide, 7 high and 21 long. The nose (bridge) is at the high z end, behind a panoramic
 * viewport and a skylight; the consoles face the pilot. Through a doorway: the living section (bunks on the left,
 * cargo and the repair station on the right, portholes on both sides, the hatch in the left wall). Through a second
 * doorway at the back: the engine room with the reactor column and the drive ports in the rear wall.
 */
public final class ShipInteriorLayout {
	public static final int SIZE_X = 9;
	public static final int SIZE_Y = 7;
	public static final int SIZE_Z = 21;

	public enum Piece {
		HULL_PANEL, FLOOR, VIEWPORT, LIGHT, HATCH, HATCH_TOP, REPAIR_STATION, DIAGNOSTIC_CONSOLE, NAVIGATION_CONSOLE, FLIGHT_PANEL, THROTTLE, SEAT,
		BED_HEAD, BED_FOOT, BARREL, CHEST, ENGINE_PORT, REACTOR_CORE, REACTOR_CAGE, PIPE, AIR;

		/** The ship itself: these cannot be broken from inside, even in creative. */
		public boolean structural() {
			return switch (this) {
				case HULL_PANEL, FLOOR, VIEWPORT, LIGHT, HATCH, HATCH_TOP, REPAIR_STATION, DIAGNOSTIC_CONSOLE, NAVIGATION_CONSOLE,
						FLIGHT_PANEL, THROTTLE,
						ENGINE_PORT, REACTOR_CORE, REACTOR_CAGE, PIPE -> true;
				default -> false;
			};
		}
	}

	public record Cell(int dx, int dy, int dz) {
	}

	public record Placement(Cell cell, Piece piece) {
	}

	private static final Cell HATCH = new Cell(0, 1, 9);
	private static final Cell ENTRY = new Cell(2, 1, 9);
	private static final Cell STATION = new Cell(7, 1, 10);
	private static final Cell CONSOLE = new Cell(2, 1, 18);
	private static final Cell NAVIGATION = new Cell(4, 1, 18);
	/** The pilot's seat (in front of the navigation console), with the switch panel and the throttle beside it. */
	private static final Cell PILOT_SEAT = new Cell(4, 1, 16);
	private static final Cell FLIGHT_PANEL = new Cell(3, 1, 17);
	private static final Cell THROTTLE = new Cell(5, 1, 17);
	/** A cell of the panoramic viewport: if it is not a viewport, the room is an old capsule and is rebuilt. */
	private static final Cell MARKER = new Cell(4, 3, SIZE_Z - 1);

	private final List<Placement> placements;

	private ShipInteriorLayout(List<Placement> placements) {
		this.placements = placements;
	}

	/** The standard ship (the name stays from the first, one-room capsule). */
	public static ShipInteriorLayout capsule() {
		Map<Cell, Piece> special = specials();
		List<Placement> list = new ArrayList<>(SIZE_X * SIZE_Y * SIZE_Z);
		for (int y = 0; y < SIZE_Y; y++) {
			for (int z = 0; z < SIZE_Z; z++) {
				for (int x = 0; x < SIZE_X; x++) {
					Cell cell = new Cell(x, y, z);
					list.add(new Placement(cell, special.getOrDefault(cell, base(cell))));
				}
			}
		}
		return new ShipInteriorLayout(List.copyOf(list));
	}

	private static Piece base(Cell c) {
		int x = c.dx();
		int y = c.dy();
		int z = c.dz();
		boolean floor = y == 0;
		boolean ceiling = y == SIZE_Y - 1;
		boolean side = x == 0 || x == SIZE_X - 1;
		boolean end = z == 0 || z == SIZE_Z - 1;
		if (floor) {
			return x >= 3 && x <= 5 ? Piece.FLOOR : Piece.HULL_PANEL;
		}
		if (ceiling) {
			boolean skylight = x >= 3 && x <= 5 && z >= 16 && z <= 19;
			boolean light = (x == 2 || x == 6) && (z == 3 || z == 8 || z == 12 || z == 16);
			return skylight ? Piece.VIEWPORT : light ? Piece.LIGHT : Piece.HULL_PANEL;
		}
		if (end && z == SIZE_Z - 1) {
			// Panoramic viewport across the nose, down to the floor: the ground shows when you look down.
			return x >= 1 && x <= SIZE_X - 2 ? Piece.VIEWPORT : Piece.HULL_PANEL;
		}
		if (end) {
			return y == 2 && (x == 2 || x == 6) ? Piece.ENGINE_PORT : Piece.HULL_PANEL;
		}
		if (side) {
			boolean bridgeWindow = z >= 16 && z <= 19 && y >= 1 && y <= 4;
			boolean porthole = y == 3 && (z == 7 || z == 11 || z == 13 && x == SIZE_X - 1 || z == 2);
			return bridgeWindow || porthole ? Piece.VIEWPORT : Piece.HULL_PANEL;
		}
		// Bulkheads with doorways (3 wide, 3 high) between engine room, living section and bridge.
		if ((z == 5 || z == 14) && !(x >= 3 && x <= 5 && y <= 3)) {
			return Piece.HULL_PANEL;
		}
		// Pipes along the ceiling corners.
		if (y == SIZE_Y - 2 && (x == 1 || x == SIZE_X - 2) && z != 5 && z != 14) {
			return Piece.PIPE;
		}
		return Piece.AIR;
	}

	private static Map<Cell, Piece> specials() {
		Map<Cell, Piece> m = new HashMap<>();
		m.put(HATCH, Piece.HATCH);
		m.put(new Cell(HATCH.dx(), HATCH.dy() + 1, HATCH.dz()), Piece.HATCH_TOP);
		m.put(STATION, Piece.REPAIR_STATION);
		m.put(CONSOLE, Piece.DIAGNOSTIC_CONSOLE);
		m.put(NAVIGATION, Piece.NAVIGATION_CONSOLE);
		m.put(new Cell(2, 1, 16), Piece.SEAT);
		m.put(PILOT_SEAT, Piece.SEAT);
		m.put(FLIGHT_PANEL, Piece.FLIGHT_PANEL);
		m.put(THROTTLE, Piece.THROTTLE);
		m.put(new Cell(6, 1, 18), Piece.DIAGNOSTIC_CONSOLE);
		// Bunks on the left (head towards the bow).
		m.put(new Cell(1, 1, 6), Piece.BED_FOOT);
		m.put(new Cell(1, 1, 7), Piece.BED_HEAD);
		m.put(new Cell(1, 1, 11), Piece.BED_FOOT);
		m.put(new Cell(1, 1, 12), Piece.BED_HEAD);
		// Cargo on the right.
		for (int z = 6; z <= 8; z++) {
			m.put(new Cell(7, 1, z), Piece.BARREL);
		}
		m.put(new Cell(7, 2, 6), Piece.BARREL);
		m.put(new Cell(7, 1, 12), Piece.CHEST);
		m.put(new Cell(7, 1, 13), Piece.CHEST);
		// Reactor column in the engine room, caged.
		m.put(new Cell(4, 1, 2), Piece.REACTOR_CORE);
		m.put(new Cell(4, 2, 2), Piece.LIGHT);
		m.put(new Cell(4, 3, 2), Piece.REACTOR_CORE);
		for (int y = 1; y <= 3; y++) {
			m.put(new Cell(3, y, 2), Piece.REACTOR_CAGE);
			m.put(new Cell(5, y, 2), Piece.REACTOR_CAGE);
		}
		return m;
	}

	public List<Placement> placements() {
		return placements;
	}

	/** Everything except air: the part that can be re-placed without clearing what players put inside. */
	public List<Placement> structure() {
		return placements.stream().filter(p -> p.piece() != Piece.AIR).toList();
	}

	/** Cells that must not be broken from inside. */
	public Set<Cell> protectedCells() {
		java.util.HashSet<Cell> set = new java.util.HashSet<>();
		for (Placement p : placements) {
			if (p.piece().structural()) {
				set.add(p.cell());
			}
		}
		return set;
	}

	public static boolean inside(int dx, int dy, int dz) {
		return dx >= -1 && dx <= SIZE_X && dy >= -1 && dy <= SIZE_Y && dz >= -1 && dz <= SIZE_Z;
	}

	public Cell hatch() {
		return HATCH;
	}

	public Cell station() {
		return STATION;
	}

	public Cell navigation() {
		return NAVIGATION;
	}

	public Cell pilotSeat() {
		return PILOT_SEAT;
	}

	public Cell flightPanel() {
		return FLIGHT_PANEL;
	}

	public Cell throttle() {
		return THROTTLE;
	}

	public Cell console() {
		return CONSOLE;
	}

	public Cell entry() {
		return ENTRY;
	}

	public Cell marker() {
		return MARKER;
	}
}
