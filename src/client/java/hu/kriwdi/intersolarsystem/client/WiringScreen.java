package hu.kriwdi.intersolarsystem.client;

import java.util.Arrays;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import hu.kriwdi.intersolarsystem.ship.WiringPayloads;
import hu.kriwdi.intersolarsystem.ship.WiringPuzzle;

/**
 * The wiring minigame: click a wire end on the left, then the end of the same colour on the right. When every wire is
 * connected the answer goes to the server, which decides whether the short circuit is fixed. The game does not pause:
 * the incident's clock keeps running.
 */
public class WiringScreen extends Screen {
	private static final int[] COLOURS = {0xFFE53935, 0xFF1E88E5, 0xFFFDD835, 0xFF43A047, 0xFFFFFFFF, 0xFF8E24AA};
	private static final int ROW = 26;
	private static final int NODE = 10;
	private static final int PANEL_WIDTH = 220;
	private static final int PANEL_BACKGROUND = 0xE0181C1A;
	private static final int PANEL_BORDER = 0xFF6B5B4B;
	private static final int TEXT = 0xFFE0E0E0;
	private static final int HINT = 0xFF9E9E9E;
	private static final int SELECTED = 0xFFFFFFFF;

	private final WiringPuzzle puzzle;
	private final int[] connections;
	private int selectedLeft = -1;
	private boolean sent;

	public WiringScreen(long seed, int wires) {
		super(Component.translatable("screen.intersolarsystem.wiring.title"));
		this.puzzle = WiringPuzzle.of(seed, wires);
		this.connections = new int[puzzle.size()];
		Arrays.fill(connections, -1);
	}

	private int panelLeft() {
		return (width - PANEL_WIDTH) / 2;
	}

	private int panelTop() {
		return (height - panelHeight()) / 2;
	}

	private int panelHeight() {
		return 44 + puzzle.size() * ROW + 16;
	}

	private int leftX() {
		return panelLeft() + 30;
	}

	private int rightX() {
		return panelLeft() + PANEL_WIDTH - 30 - NODE;
	}

	private int rowY(int slot) {
		return panelTop() + 40 + slot * ROW;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int left = panelLeft();
		int top = panelTop();
		graphics.fill(left - 1, top - 1, left + PANEL_WIDTH + 1, top + panelHeight() + 1, PANEL_BORDER);
		graphics.fill(left, top, left + PANEL_WIDTH, top + panelHeight(), PANEL_BACKGROUND);
		graphics.centeredText(font, title, left + PANEL_WIDTH / 2, top + 8, TEXT);
		graphics.centeredText(font, Component.translatable("screen.intersolarsystem.wiring.hint"), left + PANEL_WIDTH / 2,
				top + 22, HINT);

		for (int i = 0; i < puzzle.size(); i++) {
			if (connections[i] >= 0) {
				drawWire(graphics, rowY(i), rowY(connections[i]), COLOURS[puzzle.left()[i]]);
			}
		}
		if (selectedLeft >= 0) {
			int y = rowY(selectedLeft) + NODE / 2;
			int x = leftX() + NODE;
			graphics.fill(x, y - 1, Math.max(x + 1, mouseX), y + 1, COLOURS[puzzle.left()[selectedLeft]]);
		}
		for (int i = 0; i < puzzle.size(); i++) {
			drawNode(graphics, leftX(), rowY(i), COLOURS[puzzle.left()[i]], i == selectedLeft);
			drawNode(graphics, rightX(), rowY(i), COLOURS[puzzle.right()[i]], false);
		}
	}

	private static void drawNode(GuiGraphicsExtractor graphics, int x, int y, int colour, boolean selected) {
		graphics.fill(x - 1, y - 1, x + NODE + 1, y + NODE + 1, selected ? SELECTED : 0xFF000000);
		graphics.fill(x, y, x + NODE, y + NODE, colour);
	}

	/** A wire as three straight segments: out, across, in. */
	private void drawWire(GuiGraphicsExtractor graphics, int fromRowY, int toRowY, int colour) {
		int y1 = fromRowY + NODE / 2;
		int y2 = toRowY + NODE / 2;
		int x1 = leftX() + NODE;
		int x2 = rightX();
		int mid = (x1 + x2) / 2;
		graphics.fill(x1, y1 - 1, mid + 1, y1 + 1, colour);
		graphics.fill(mid - 1, Math.min(y1, y2) - 1, mid + 1, Math.max(y1, y2) + 1, colour);
		graphics.fill(mid - 1, y2 - 1, x2, y2 + 1, colour);
	}

	private static boolean inside(double mx, double my, int x, int y) {
		return mx >= x - 3 && mx <= x + NODE + 3 && my >= y - 3 && my <= y + NODE + 3;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() != 0 || sent) {
			return super.mouseClicked(event, doubleClick);
		}
		for (int i = 0; i < puzzle.size(); i++) {
			if (inside(event.x(), event.y(), leftX(), rowY(i))) {
				selectedLeft = i;
				connections[i] = -1;
				return true;
			}
			if (selectedLeft >= 0 && inside(event.x(), event.y(), rightX(), rowY(i))) {
				for (int k = 0; k < connections.length; k++) {
					if (connections[k] == i) {
						connections[k] = -1;
					}
				}
				connections[selectedLeft] = i;
				selectedLeft = -1;
				if (Arrays.stream(connections).allMatch(c -> c >= 0)) {
					sent = true;
					ClientPlayNetworking.send(new WiringPayloads.Result(puzzle.seed(), connections.clone()));
					onClose();
				}
				return true;
			}
		}
		selectedLeft = -1;
		return super.mouseClicked(event, doubleClick);
	}
}
