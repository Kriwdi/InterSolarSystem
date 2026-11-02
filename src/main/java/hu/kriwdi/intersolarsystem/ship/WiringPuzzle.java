package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The wiring repair minigame (short circuit): coloured wire ends on the left must be connected to the matching colour
 * on the right. Both sides are shuffled from a seed, so server and client build the same board and the server can check
 * the answer without trusting the client.
 */
public record WiringPuzzle(long seed, int[] left, int[] right) {
	public static final int MIN_WIRES = 3;
	public static final int MAX_WIRES = 6;

	public static WiringPuzzle of(long seed, int wires) {
		int count = Math.max(MIN_WIRES, Math.min(MAX_WIRES, wires));
		Random random = new Random(seed);
		List<Integer> colours = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			colours.add(i);
		}
		Collections.shuffle(colours, random);
		int[] left = colours.stream().mapToInt(Integer::intValue).toArray();
		Collections.shuffle(colours, random);
		int[] right = colours.stream().mapToInt(Integer::intValue).toArray();
		return new WiringPuzzle(seed, left, right);
	}

	public int size() {
		return left.length;
	}

	/** {@code connections[i]} is the right slot the player joined to left slot {@code i}. */
	public boolean isSolvedBy(int[] connections) {
		if (connections == null || connections.length != left.length) {
			return false;
		}
		boolean[] used = new boolean[right.length];
		for (int i = 0; i < connections.length; i++) {
			int j = connections[i];
			if (j < 0 || j >= right.length || used[j] || right[j] != left[i]) {
				return false;
			}
			used[j] = true;
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof WiringPuzzle p && p.seed == seed && Arrays.equals(p.left, left) && Arrays.equals(p.right, right);
	}

	@Override
	public int hashCode() {
		return Long.hashCode(seed) * 31 + Arrays.hashCode(left) * 17 + Arrays.hashCode(right);
	}

	@Override
	public String toString() {
		return "WiringPuzzle[" + seed + ", " + Arrays.toString(left) + " -> " + Arrays.toString(right) + "]";
	}
}
