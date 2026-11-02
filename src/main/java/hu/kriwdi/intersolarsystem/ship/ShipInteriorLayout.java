package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Block layout of the rescue capsule interior, relative to the room's lowest corner.
 * Pure data: the server-side builder turns {@link Piece}s into blocks.
 */
public final class ShipInteriorLayout {
	public static final int SIZE_X = 9;
	public static final int SIZE_Y = 6;
	public static final int SIZE_Z = 7;

	public enum Piece { HULL_PANEL, LIGHT, HATCH, REPAIR_STATION, DIAGNOSTIC_CONSOLE, NAVIGATION_CONSOLE, AIR }

	public record Cell(int dx, int dy, int dz) {
	}

	public record Placement(Cell cell, Piece piece) {
	}

	private static final Cell HATCH = new Cell(4, 2, 0);
	private static final Cell ENTRY = new Cell(4, 1, 3);
	private static final Cell STATION = new Cell(7, 1, 5);
	private static final Cell CONSOLE = new Cell(1, 1, 5);
	private static final Cell NAVIGATION = new Cell(4, 1, 5);
	private static final Set<Cell> LIGHTS = Set.of(
			new Cell(2, 4, 2), new Cell(6, 4, 2), new Cell(2, 4, 4), new Cell(6, 4, 4));

	private final List<Placement> placements;

	private ShipInteriorLayout(List<Placement> placements) {
		this.placements = placements;
	}

	public static ShipInteriorLayout capsule() {
		List<Placement> list = new ArrayList<>(SIZE_X * SIZE_Y * SIZE_Z);
		for (int y = 0; y < SIZE_Y; y++) {
			for (int z = 0; z < SIZE_Z; z++) {
				for (int x = 0; x < SIZE_X; x++) {
					Cell cell = new Cell(x, y, z);
					list.add(new Placement(cell, pieceAt(cell)));
				}
			}
		}
		return new ShipInteriorLayout(List.copyOf(list));
	}

	private static Piece pieceAt(Cell c) {
		if (c.equals(HATCH)) {
			return Piece.HATCH;
		}
		if (c.equals(STATION)) {
			return Piece.REPAIR_STATION;
		}
		if (c.equals(CONSOLE)) {
			return Piece.DIAGNOSTIC_CONSOLE;
		}
		if (c.equals(NAVIGATION)) {
			return Piece.NAVIGATION_CONSOLE;
		}
		boolean shell = c.dx() == 0 || c.dx() == SIZE_X - 1 || c.dy() == 0 || c.dy() == SIZE_Y - 1
				|| c.dz() == 0 || c.dz() == SIZE_Z - 1;
		if (shell) {
			return Piece.HULL_PANEL;
		}
		return LIGHTS.contains(c) ? Piece.LIGHT : Piece.AIR;
	}

	public List<Placement> placements() {
		return placements;
	}

	/** Everything except air: the part that can be re-placed without clearing what players put inside. */
	public List<Placement> structure() {
		return placements.stream().filter(p -> p.piece() != Piece.AIR).toList();
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

	public Cell console() {
		return CONSOLE;
	}

	public Cell entry() {
		return ENTRY;
	}
}
