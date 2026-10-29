package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Candidate landing columns around a target, nearest first. */
public final class ShipLandingSearch {
	public record Column(int x, int z) {
	}

	private ShipLandingSearch() {
	}

	public static List<Column> candidates(int centerX, int centerZ, int radius) {
		List<int[]> offsets = new ArrayList<>();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz <= radius * radius) {
					offsets.add(new int[] {dx, dz});
				}
			}
		}
		offsets.sort(Comparator.<int[]>comparingInt(o -> o[0] * o[0] + o[1] * o[1])
				.thenComparingInt(o -> o[0]).thenComparingInt(o -> o[1]));
		return offsets.stream().map(o -> new Column(centerX + o[0], centerZ + o[1])).toList();
	}
}
