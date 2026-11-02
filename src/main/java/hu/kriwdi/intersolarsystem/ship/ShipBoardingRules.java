package hu.kriwdi.intersolarsystem.ship;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Cell;

public final class ShipBoardingRules {
	private static final List<int[]> NEIGHBOURS = List.of(new int[] {1, 0}, new int[] {0, 1}, new int[] {-1, 0}, new int[] {0, -1});

	private ShipBoardingRules() {
	}

	/**
	 * First horizontal neighbour of the hatch (+x, +z, -x, -z) with a solid floor and two free cells above it.
	 * Cells here are absolute world coordinates.
	 */
	public static Optional<Cell> findStandingSpot(Cell hatch, Predicate<Cell> isSolid, Predicate<Cell> isFree) {
		for (int[] n : NEIGHBOURS) {
			Cell feet = new Cell(hatch.dx() + n[0], hatch.dy(), hatch.dz() + n[1]);
			Cell head = new Cell(feet.dx(), feet.dy() + 1, feet.dz());
			Cell floor = new Cell(feet.dx(), feet.dy() - 1, feet.dz());
			if (isFree.test(feet) && isFree.test(head) && isSolid.test(floor)) {
				return Optional.of(feet);
			}
		}
		return Optional.empty();
	}
}
