package hu.kriwdi.intersolarsystem.ship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import hu.kriwdi.intersolarsystem.space.IssLayout;

class IssLayoutTest {
	private static Map<String, IssLayout.Piece> grid() {
		Map<String, IssLayout.Piece> map = new HashMap<>();
		for (IssLayout.Placement p : IssLayout.build()) {
			map.put(p.x() + "," + p.y() + "," + p.z(), p.piece());
		}
		return map;
	}

	@Test
	void layoutIsDeterministicAndHasNoDuplicates() {
		List<IssLayout.Placement> a = IssLayout.build();
		assertEquals(a, IssLayout.build());
		assertEquals(a.size(), grid().size());
	}

	@Test
	void dockingPortOpensIntoTheModules() {
		Map<String, IssLayout.Piece> g = grid();
		int[] h = IssLayout.DOCK_HATCH;
		// The hatch stands in space just in front of the port; the doorway behind it is two blocks tall.
		assertTrue(g.get(h[0] + "," + h[1] + "," + h[2]) == null);
		assertEquals(IssLayout.Piece.AIR, g.get(h[0] + "," + h[1] + "," + (h[2] + 1)));
		assertEquals(IssLayout.Piece.AIR, g.get(h[0] + "," + (h[1] + 1) + "," + (h[2] + 1)));
		assertEquals(IssLayout.Piece.DOCK_FLOOR, g.get(h[0] + "," + (h[1] - 1) + "," + (h[2] + 1)));
		// A walkway with a floor all the way to Zvezda's back cap.
		for (int z = IssLayout.FRONT_CAP_Z + 1; z < IssLayout.BACK_CAP_Z; z++) {
			assertEquals(IssLayout.Piece.AIR, g.get("0,-1," + z), "walkway at z " + z);
			IssLayout.Piece floor = g.get("0,-2," + z);
			assertTrue(floor != null, "floor at z " + z);
		}
	}

	@Test
	void stationIsFullSize() {
		int minX = 0, maxX = 0, minZ = 0, maxZ = 0;
		for (IssLayout.Placement p : IssLayout.build()) {
			minX = Math.min(minX, p.x());
			maxX = Math.max(maxX, p.x());
			minZ = Math.min(minZ, p.z());
			maxZ = Math.max(maxZ, p.z());
		}
		assertTrue(maxX - minX >= 108, "truss span " + (maxX - minX));
		assertTrue(maxZ - minZ >= 72, "length " + (maxZ - minZ));
	}

	@Test
	void stationHasSuppliesLightsWindowsSolarWingsAndRadiators() {
		Map<IssLayout.Piece, Integer> counts = new HashMap<>();
		IssLayout.build().forEach(p -> counts.merge(p.piece(), 1, Integer::sum));
		assertTrue(counts.get(IssLayout.Piece.SUPPLY) >= 8);
		assertTrue(counts.get(IssLayout.Piece.LIGHT) >= 20);
		assertTrue(counts.get(IssLayout.Piece.WINDOW) >= 30);
		assertTrue(counts.get(IssLayout.Piece.SOLAR) > 2000);
		assertTrue(counts.get(IssLayout.Piece.RADIATOR) > 300);
		assertTrue(counts.get(IssLayout.Piece.FOIL_HULL) > 0);
	}
}
