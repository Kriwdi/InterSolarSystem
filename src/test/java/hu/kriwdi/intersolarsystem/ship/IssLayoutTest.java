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
	void dockHatchStandsOnTheFloorWithRoomBeside() {
		Map<String, IssLayout.Piece> g = grid();
		int[] h = IssLayout.DOCK_HATCH;
		assertEquals(IssLayout.Piece.DOCK_FLOOR, g.get(h[0] + "," + (h[1] - 1) + "," + h[2]));
		assertEquals(IssLayout.Piece.DOCK_FLOOR, g.get((h[0] + 1) + "," + (h[1] - 1) + "," + h[2]));
		assertTrue(g.get(h[0] + "," + h[1] + "," + h[2]) == null);
	}

	@Test
	void doorwayLeadsFromThePlatformIntoTheModules() {
		Map<String, IssLayout.Piece> g = grid();
		assertEquals(IssLayout.Piece.AIR, g.get("0,-2," + IssLayout.FRONT_CAP_Z));
		assertEquals(IssLayout.Piece.AIR, g.get("0,-1," + IssLayout.FRONT_CAP_Z));
		assertEquals(IssLayout.Piece.DOCK_FLOOR, g.get("0,-3," + (IssLayout.FRONT_CAP_Z - 1)));
		assertEquals(IssLayout.Piece.HULL, g.get("0,-3,0"));
	}

	@Test
	void stationHasSuppliesLightsWindowsAndSolarWings() {
		Map<IssLayout.Piece, Integer> counts = new HashMap<>();
		IssLayout.build().forEach(p -> counts.merge(p.piece(), 1, Integer::sum));
		assertEquals(6, counts.get(IssLayout.Piece.SUPPLY));
		assertTrue(counts.get(IssLayout.Piece.LIGHT) >= 10);
		assertTrue(counts.get(IssLayout.Piece.WINDOW) >= 10);
		assertTrue(counts.get(IssLayout.Piece.SOLAR) > 300);
		assertTrue(counts.get(IssLayout.Piece.FOIL_HULL) > 0);
	}
}
