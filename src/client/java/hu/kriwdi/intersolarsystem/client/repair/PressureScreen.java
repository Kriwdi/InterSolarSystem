package hu.kriwdi.intersolarsystem.client.repair;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import hu.kriwdi.intersolarsystem.ship.PressurePuzzle;
import hu.kriwdi.intersolarsystem.ship.RepairMinigame;

/**
 * Leak / pressure loss: equalise the tanks. The arrow valves between two tanks move one unit across; the moves are
 * limited. The answer is sent once the tanks are level or the moves run out.
 */
public class PressureScreen extends RepairBoardScreen {
	private static final int TANK_WIDTH = 26;
	private static final int TANK_HEIGHT = 74;
	private static final int GAP = 22;
	private static final int UNIT = TANK_HEIGHT / PressurePuzzle.MAX_PRESSURE;

	private final PressurePuzzle puzzle;
	private final int[] values;
	private final List<Integer> moves = new ArrayList<>();

	public PressureScreen(long seed, int size) {
		super(RepairMinigame.PRESSURE, seed, Component.translatable("screen.intersolarsystem.pressure.title"),
				Component.translatable("screen.intersolarsystem.pressure.hint"));
		puzzle = PressurePuzzle.of(seed, size);
		values = puzzle.start();
	}

	@Override
	protected int panelWidth() {
		return Math.max(220, puzzle.chambers() * (TANK_WIDTH + GAP) + 30);
	}

	@Override
	protected int panelHeight() {
		return TANK_HEIGHT + 82;
	}

	private int tankX(int i) {
		int total = puzzle.chambers() * TANK_WIDTH + (puzzle.chambers() - 1) * GAP;
		return left() + (panelWidth() - total) / 2 + i * (TANK_WIDTH + GAP);
	}

	private int tankY() {
		return contentTop() + 4;
	}

	/** Valve arrow between tank i and i+1: upper one moves right, lower one moves left. */
	private int arrowX(int i) {
		return tankX(i) + TANK_WIDTH + 3;
	}

	private int arrowY(boolean right) {
		return tankY() + TANK_HEIGHT / 2 + (right ? -14 : 4);
	}

	@Override
	protected void drawBoard(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int target = puzzle.target();
		for (int i = 0; i < values.length; i++) {
			int x = tankX(i);
			int y = tankY();
			// Glass tank with a steel rim, the fluid, and level marks.
			graphics.fill(x - 2, y - 2, x + TANK_WIDTH + 2, y + TANK_HEIGHT + 2, 0xFF5D646B);
			graphics.fill(x, y, x + TANK_WIDTH, y + TANK_HEIGHT, 0xFF0F1A1E);
			int fill = values[i] * UNIT;
			boolean level = values[i] == target;
			int fluid = level ? 0xFF2FAF6A : values[i] > target ? 0xFFE0703A : 0xFF3A8FD0;
			graphics.fill(x + 1, y + TANK_HEIGHT - fill, x + TANK_WIDTH - 1, y + TANK_HEIGHT, fluid);
			graphics.fill(x + 1, y + TANK_HEIGHT - fill, x + TANK_WIDTH - 1, y + TANK_HEIGHT - fill + 1,
					brighten(brighten(fluid)));
			graphics.fill(x + 3, y + 2, x + 5, y + TANK_HEIGHT - 2, 0x40FFFFFF);
			for (int u = 1; u < PressurePuzzle.MAX_PRESSURE; u++) {
				graphics.fill(x + TANK_WIDTH - 5, y + TANK_HEIGHT - u * UNIT, x + TANK_WIDTH - 1,
						y + TANK_HEIGHT - u * UNIT + 1, 0x80B9C0C6);
			}
			int ty = y + TANK_HEIGHT - target * UNIT;
			graphics.fill(x - 4, ty, x + TANK_WIDTH + 4, ty + 1, 0xFFF2B233);
			graphics.centeredText(font, Component.literal(String.valueOf(values[i])), x + TANK_WIDTH / 2,
					y + TANK_HEIGHT + 6, level ? GOOD : TEXT);
		}
		for (int i = 0; i < values.length - 1; i++) {
			for (boolean right : new boolean[] {true, false}) {
				int ax = arrowX(i);
				int ay = arrowY(right);
				boolean hover = inside(mouseX, mouseY, ax, ay, GAP - 6, 10);
				button(graphics, ax, ay, GAP - 6, 10, Component.literal(right ? ">" : "<"), hover, 0xFF3A3F44);
			}
		}
		int left = puzzle.moveLimit() - moves.size();
		graphics.centeredText(font, Component.translatable("screen.intersolarsystem.pressure.moves", left),
				left() + panelWidth() / 2, tankY() + TANK_HEIGHT + 20, left <= 1 ? BAD : HINT);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0 || sent()) {
			return super.mouseClicked(event, doubleClick);
		}
		for (int i = 0; i < values.length - 1; i++) {
			for (boolean right : new boolean[] {true, false}) {
				if (inside(event.x(), event.y(), arrowX(i), arrowY(right), GAP - 6, 10)) {
					int move = right ? 2 * i : 2 * i + 1;
					if (PressurePuzzle.apply(values, move)) {
						moves.add(move);
						if (PressurePuzzle.isLevel(values) || moves.size() >= puzzle.moveLimit()) {
							send(moves.stream().mapToInt(Integer::intValue).toArray());
						}
					}
					return true;
				}
			}
		}
		return super.mouseClicked(event, doubleClick);
	}
}
