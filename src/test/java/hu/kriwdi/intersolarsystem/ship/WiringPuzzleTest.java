package hu.kriwdi.intersolarsystem.ship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class WiringPuzzleTest {
	private static int[] solve(WiringPuzzle p) {
		int[] answer = new int[p.size()];
		for (int i = 0; i < p.size(); i++) {
			for (int j = 0; j < p.size(); j++) {
				if (p.right()[j] == p.left()[i]) {
					answer[i] = j;
				}
			}
		}
		return answer;
	}

	@Test
	void sameSeedSameBoard() {
		assertEquals(WiringPuzzle.of(42L, 4), WiringPuzzle.of(42L, 4));
	}

	@Test
	void bothSidesHoldEveryColourOnce() {
		WiringPuzzle p = WiringPuzzle.of(7L, 5);
		int[] l = p.left().clone();
		int[] r = p.right().clone();
		Arrays.sort(l);
		Arrays.sort(r);
		assertTrue(Arrays.equals(new int[] {0, 1, 2, 3, 4}, l));
		assertTrue(Arrays.equals(l, r));
	}

	@Test
	void correctAnswerSolves() {
		for (long seed = 0; seed < 50; seed++) {
			WiringPuzzle p = WiringPuzzle.of(seed, 4);
			assertTrue(p.isSolvedBy(solve(p)));
		}
	}

	@Test
	void wrongOrMalformedAnswersFail() {
		WiringPuzzle p = WiringPuzzle.of(3L, 4);
		int[] good = solve(p);
		int[] swapped = good.clone();
		swapped[0] = good[1];
		swapped[1] = good[0];
		assertFalse(p.isSolvedBy(swapped));
		assertFalse(p.isSolvedBy(new int[] {0, 0, 0, 0}));
		assertFalse(p.isSolvedBy(new int[] {0, 1, 2}));
		assertFalse(p.isSolvedBy(new int[] {0, 1, 2, 9}));
		assertFalse(p.isSolvedBy(null));
	}

	@Test
	void sizeIsClampedAndFollowsNavigation() {
		assertEquals(WiringPuzzle.MIN_WIRES, WiringPuzzle.of(1L, 0).size());
		assertEquals(WiringPuzzle.MAX_WIRES, WiringPuzzle.of(1L, 99).size());
		assertEquals(3, WiringPuzzle.wiresFor(100));
		assertEquals(4, WiringPuzzle.wiresFor(50));
		assertEquals(5, WiringPuzzle.wiresFor(10));
	}
}
