package hu.kriwdi.intersolarsystem.client.repair;

import java.util.Arrays;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import hu.kriwdi.intersolarsystem.ship.RepairMinigame;
import hu.kriwdi.intersolarsystem.ship.WiringPuzzle;

/** Short circuit: join each coloured terminal on the left to the same colour on the right. */
public class WiringScreen extends RepairBoardScreen {
	private static final int[] COLOURS = {0xFFE53935, 0xFF1E88E5, 0xFFFDD835, 0xFF43A047, 0xFFF5F5F5, 0xFF8E24AA};
	private static final int ROW = 22;
	private static final int NODE = 10;

	private final WiringPuzzle puzzle;
	private final int[] connections;
	private int selectedLeft = -1;

	public WiringScreen(long seed, int size) {
		super(RepairMinigame.WIRING, seed, Component.translatable("screen.intersolarsystem.wiring.title"),
				Component.translatable("screen.intersolarsystem.wiring.hint"));
		puzzle = WiringPuzzle.of(seed, size);
		connections = new int[puzzle.size()];
		Arrays.fill(connections, -1);
	}

	@Override
	protected int panelWidth() {
		return 240;
	}

	@Override
	protected int panelHeight() {
		return 52 + puzzle.size() * ROW;
	}

	private int leftX() {
		return left() + 34;
	}

	private int rightX() {
		return left() + panelWidth() - 34 - NODE;
	}

	private int rowY(int slot) {
		return contentTop() + slot * ROW;
	}

	@Override
	protected void drawBoard(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		// Terminal strips behind the nodes.
		graphics.fill(leftX() - 6, rowY(0) - 4, leftX() + NODE + 4, rowY(puzzle.size() - 1) + NODE + 4, 0xFF15181B);
		graphics.fill(rightX() - 4, rowY(0) - 4, rightX() + NODE + 6, rowY(puzzle.size() - 1) + NODE + 4, 0xFF15181B);
		for (int i = 0; i < puzzle.size(); i++) {
			if (connections[i] >= 0) {
				drawWire(graphics, rowY(i), rowY(connections[i]), COLOURS[puzzle.left()[i]]);
			}
		}
		if (selectedLeft >= 0) {
			int y = rowY(selectedLeft) + NODE / 2;
			int x = leftX() + NODE;
			int colour = COLOURS[puzzle.left()[selectedLeft]];
			// A loose wire end follows the cursor (two-pixel line with a darker underside).
			graphics.fill(x, y - 1, Math.max(x + 1, mouseX), y + 1, colour);
			graphics.fill(Math.max(x, mouseX - 1), Math.min(y, mouseY), Math.max(x + 1, mouseX + 1),
					Math.max(y, mouseY), colour);
		}
		for (int i = 0; i < puzzle.size(); i++) {
			drawTerminal(graphics, leftX(), rowY(i), COLOURS[puzzle.left()[i]], i == selectedLeft);
			drawTerminal(graphics, rightX(), rowY(i), COLOURS[puzzle.right()[i]], false);
		}
	}

	private static void drawTerminal(GuiGraphicsExtractor graphics, int x, int y, int colour, boolean selected) {
		graphics.fill(x - 1, y - 1, x + NODE + 1, y + NODE + 1, selected ? 0xFFFFFFFF : 0xFF000000);
		graphics.fill(x, y, x + NODE, y + NODE, colour);
		graphics.fill(x + 3, y + 3, x + NODE - 3, y + NODE - 3, 0xFFB9C0C6);
		graphics.fill(x + 4, y + 4, x + NODE - 4, y + NODE - 4, 0xFF5D646B);
	}

	/** A wire as three straight segments (out, across, in) with a dark shadow line for depth. */
	private void drawWire(GuiGraphicsExtractor graphics, int fromRowY, int toRowY, int colour) {
		int y1 = fromRowY + NODE / 2;
		int y2 = toRowY + NODE / 2;
		int x1 = leftX() + NODE;
		int x2 = rightX();
		int mid = (x1 + x2) / 2 + (fromRowY - toRowY) / 6;
		int shadow = 0xFF0B0C0E;
		graphics.fill(x1, y1 + 1, mid + 2, y1 + 2, shadow);
		graphics.fill(mid + 1, Math.min(y1, y2), mid + 2, Math.max(y1, y2) + 2, shadow);
		graphics.fill(mid, y2 + 1, x2, y2 + 2, shadow);
		graphics.fill(x1, y1 - 1, mid + 1, y1 + 1, colour);
		graphics.fill(mid - 1, Math.min(y1, y2) - 1, mid + 1, Math.max(y1, y2) + 1, colour);
		graphics.fill(mid - 1, y2 - 1, x2, y2 + 1, colour);
	}

	private static boolean near(double mx, double my, int x, int y) {
		return mx >= x - 3 && mx <= x + NODE + 3 && my >= y - 3 && my <= y + NODE + 3;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0 || sent()) {
			return super.mouseClicked(event, doubleClick);
		}
		for (int i = 0; i < puzzle.size(); i++) {
			if (near(event.x(), event.y(), leftX(), rowY(i))) {
				selectedLeft = i;
				connections[i] = -1;
				return true;
			}
			if (selectedLeft >= 0 && near(event.x(), event.y(), rightX(), rowY(i))) {
				for (int k = 0; k < connections.length; k++) {
					if (connections[k] == i) {
						connections[k] = -1;
					}
				}
				connections[selectedLeft] = i;
				selectedLeft = -1;
				if (Arrays.stream(connections).allMatch(c -> c >= 0)) {
					send(connections.clone());
				}
				return true;
			}
		}
		selectedLeft = -1;
		return super.mouseClicked(event, doubleClick);
	}
}
