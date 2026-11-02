package hu.kriwdi.intersolarsystem.ship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class RepairPuzzlesTest {
	/** Clicks each round at the first tick after the previous click that hits the zone. */
	private static int[] solveValve(ValvePuzzle p) {
		int[] clicks = new int[p.rounds()];
		int t = 0;
		for (int i = 0; i < p.rounds(); i++) {
			t++;
			while (!p.hit(i, t)) {
				t++;
			}
			clicks[i] = t;
		}
		return clicks;
	}

	@Test
	void valveNeedleStaysOnTheScale() {
		ValvePuzzle p = ValvePuzzle.of(5L, 3);
		for (int t = 0; t < 2000; t++) {
			double n = p.needle(1, t);
			assertTrue(n >= 0 && n <= 100);
		}
	}

	@Test
	void valveTimedClicksSolveAndBadOnesFail() {
		for (long seed = 0; seed < 40; seed++) {
			ValvePuzzle p = ValvePuzzle.of(seed, 3);
			int[] good = solveValve(p);
			assertTrue(p.isSolvedBy(good, 100000), "seed " + seed);
			assertFalse(p.isSolvedBy(good, good[2] - 1), "too late for the server clock");
			int[] reordered = good.clone();
			reordered[1] = reordered[0];
			assertFalse(p.isSolvedBy(reordered, 100000));
			assertFalse(p.isSolvedBy(new int[] {good[0]}, 100000));
		}
	}

	@Test
	void valveMissIsRejected() {
		ValvePuzzle p = ValvePuzzle.of(9L, 1);
		int t = 1;
		while (p.hit(0, t)) {
			t++;
		}
		assertFalse(p.isSolvedBy(new int[] {t}, 100000));
	}

	/** Greedy: move a unit from the fuller to the emptier side across the first unbalanced boundary. */
	private static int[] solvePressure(PressurePuzzle p) {
		int[] v = p.start();
		int target = p.target();
		List<Integer> moves = new ArrayList<>();
		while (!PressurePuzzle.isLevel(v)) {
			int surplus = 0;
			for (int i = 0; i < v.length - 1; i++) {
				surplus += v[i] - target;
				if (surplus != 0) {
					int move = surplus > 0 ? 2 * i : 2 * i + 1;
					if (PressurePuzzle.apply(v, move)) {
						moves.add(move);
						break;
					}
				}
			}
		}
		return moves.stream().mapToInt(Integer::intValue).toArray();
	}

	@Test
	void pressureBoardsAreSolvableWithinTheLimit() {
		for (long seed = 0; seed < 60; seed++) {
			PressurePuzzle p = PressurePuzzle.of(seed, 4);
			assertFalse(PressurePuzzle.isLevel(p.start()));
			int[] moves = solvePressure(p);
			assertEquals(p.minimumMoves(), moves.length, "seed " + seed);
			assertTrue(p.isSolvedBy(moves));
		}
	}

	@Test
	void pressureRejectsIllegalOrTooManyMoves() {
		PressurePuzzle p = PressurePuzzle.of(11L, 3);
		assertFalse(p.isSolvedBy(new int[] {}));
		assertFalse(p.isSolvedBy(new int[] {99}));
		int[] good = solvePressure(p);
		int[] padded = new int[good.length + PressurePuzzle.SPARE_MOVES + 2];
		System.arraycopy(good, 0, padded, 0, good.length);
		for (int i = good.length; i < padded.length; i += 2) {
			padded[i] = 0;
			if (i + 1 < padded.length) {
				padded[i + 1] = 1;
			}
		}
		assertFalse(p.isSolvedBy(padded));
	}

	@Test
	void sameSeedSameBoards() {
		assertTrue(java.util.Arrays.equals(PressurePuzzle.of(3L, 4).start(), PressurePuzzle.of(3L, 4).start()));
		assertEquals(ValvePuzzle.of(3L, 2).zoneLow(1), ValvePuzzle.of(3L, 2).zoneLow(1));
	}
}
