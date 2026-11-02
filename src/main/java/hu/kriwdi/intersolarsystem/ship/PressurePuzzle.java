package hu.kriwdi.intersolarsystem.ship;

import java.util.Arrays;
import java.util.Random;

/**
 * The pressure minigame (leak, pressure loss): a row of chambers with uneven pressure. A move opens the valve between
 * two neighbours and moves one unit across. The goal is equal pressure everywhere within a limited number of moves.
 * The server replays the moves.
 */
public final class PressurePuzzle {
	public static final int MIN_CHAMBERS = 3;
	public static final int MAX_CHAMBERS = 5;
	public static final int MAX_PRESSURE = 9;
	/** Moves allowed on top of the shortest solution. */
	public static final int SPARE_MOVES = 2;

	private final long seed;
	private final int[] start;

	private PressurePuzzle(long seed, int[] start) {
		this.seed = seed;
		this.start = start;
	}

	public static PressurePuzzle of(long seed, int chambers) {
		int n = Math.max(MIN_CHAMBERS, Math.min(MAX_CHAMBERS, chambers));
		Random random = new Random(seed);
		int target = 3 + random.nextInt(4);
		int[] values = new int[n];
		Arrays.fill(values, target);
		// Scramble with random legal moves so the board is always solvable and never already solved.
		while (isLevel(values)) {
			for (int k = 0; k < n * 3; k++) {
				apply(values, random.nextInt(2 * (n - 1)));
			}
		}
		return new PressurePuzzle(seed, values);
	}

	public long seed() {
		return seed;
	}

	public int[] start() {
		return start.clone();
	}

	public int chambers() {
		return start.length;
	}

	public int target() {
		return Arrays.stream(start).sum() / start.length;
	}

	/** Shortest number of moves: every unit has to cross each boundary where the left part is out of balance. */
	public int minimumMoves() {
		int moves = 0;
		int surplus = 0;
		for (int i = 0; i < start.length - 1; i++) {
			surplus += start[i] - target();
			moves += Math.abs(surplus);
		}
		return moves;
	}

	public int moveLimit() {
		return minimumMoves() + SPARE_MOVES;
	}

	/**
	 * Applies one move in place: {@code 2*i} moves a unit from chamber i to i+1, {@code 2*i+1} from i+1 to i. Returns
	 * false (and changes nothing) for an unknown move or one that would empty or overfill a chamber.
	 */
	public static boolean apply(int[] values, int move) {
		int i = move / 2;
		if (move < 0 || i >= values.length - 1) {
			return false;
		}
		int from = move % 2 == 0 ? i : i + 1;
		int to = move % 2 == 0 ? i + 1 : i;
		if (values[from] <= 0 || values[to] >= MAX_PRESSURE) {
			return false;
		}
		values[from]--;
		values[to]++;
		return true;
	}

	public static boolean isLevel(int[] values) {
		return Arrays.stream(values).allMatch(v -> v == values[0]);
	}

	public boolean isSolvedBy(int[] moves) {
		if (moves == null || moves.length > moveLimit()) {
			return false;
		}
		int[] values = start();
		for (int move : moves) {
			if (!apply(values, move)) {
				return false;
			}
		}
		return isLevel(values);
	}
}
