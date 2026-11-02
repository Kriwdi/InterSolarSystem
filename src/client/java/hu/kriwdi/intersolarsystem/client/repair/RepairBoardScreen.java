package hu.kriwdi.intersolarsystem.client.repair;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import hu.kriwdi.intersolarsystem.ship.RepairBoardPayloads;
import hu.kriwdi.intersolarsystem.ship.RepairMinigame;

/**
 * Common frame of the repair minigames: a riveted metal panel with a hazard-striped header, the title and a hint line.
 * The game does not pause (the incident's clock keeps running). The answer goes to the server exactly once.
 */
public abstract class RepairBoardScreen extends Screen {
	protected static final int TEXT = 0xFFE6E2D6;
	protected static final int HINT = 0xFF9BA3A8;
	protected static final int GOOD = 0xFF5DFA98;
	protected static final int BAD = 0xFFFF5A3C;
	private static final int PANEL = 0xF0262A2E;
	private static final int PANEL_EDGE_LIGHT = 0xFF59616A;
	private static final int PANEL_EDGE_DARK = 0xFF121417;
	private static final int STRIPE_YELLOW = 0xFFD9A521;
	private static final int STRIPE_BLACK = 0xFF1E1F21;
	private static final int RIVET = 0xFF8D959C;

	private final RepairMinigame kind;
	private final long seed;
	private final Component hint;
	private boolean sent;
	/** Client ticks since the board opened; the valve answer is measured in these. */
	protected int ticks;

	protected RepairBoardScreen(RepairMinigame kind, long seed, Component title, Component hint) {
		super(title);
		this.kind = kind;
		this.seed = seed;
		this.hint = hint;
	}

	protected abstract int panelWidth();

	protected abstract int panelHeight();

	protected int left() {
		return (width - panelWidth()) / 2;
	}

	protected int top() {
		return (height - panelHeight()) / 2;
	}

	/** Content area starts below the header and the hint. */
	protected int contentTop() {
		return top() + 40;
	}

	protected boolean sent() {
		return sent;
	}

	protected void send(int[] answer) {
		if (sent) {
			return;
		}
		sent = true;
		ClientPlayNetworking.send(new RepairBoardPayloads.Result(kind, seed, answer));
		onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		ticks++;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int l = left();
		int t = top();
		int r = l + panelWidth();
		int b = t + panelHeight();
		graphics.fill(l - 2, t - 2, r + 2, b + 2, PANEL_EDGE_DARK);
		graphics.fill(l - 1, t - 1, r + 1, t, PANEL_EDGE_LIGHT);
		graphics.fill(l - 1, t - 1, l, b + 1, PANEL_EDGE_LIGHT);
		graphics.fill(l, t, r, b, PANEL);
		// Hazard-striped header strip.
		for (int x = l; x < r; x++) {
			boolean yellow = ((x - l) / 4) % 2 == 0;
			graphics.fill(x, t, x + 1, t + 4, yellow ? STRIPE_YELLOW : STRIPE_BLACK);
		}
		for (int[] p : new int[][] {{l + 3, t + 7}, {r - 5, t + 7}, {l + 3, b - 5}, {r - 5, b - 5}}) {
			graphics.fill(p[0], p[1], p[0] + 2, p[1] + 2, RIVET);
			graphics.fill(p[0] + 1, p[1] + 1, p[0] + 2, p[1] + 2, PANEL_EDGE_DARK);
		}
		graphics.centeredText(font, title, l + panelWidth() / 2, t + 10, TEXT);
		graphics.centeredText(font, hint, l + panelWidth() / 2, t + 24, HINT);
		drawBoard(graphics, mouseX, mouseY, partialTick);
	}

	protected abstract void drawBoard(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick);

	/** A small bevelled button. */
	protected void button(GuiGraphicsExtractor graphics, int x, int y, int w, int h, Component label, boolean hover,
			int face) {
		graphics.fill(x, y, x + w, y + h, PANEL_EDGE_DARK);
		graphics.fill(x, y, x + w - 1, y + h - 1, PANEL_EDGE_LIGHT);
		graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, hover ? brighten(face) : face);
		graphics.centeredText(font, label, x + w / 2, y + (h - 8) / 2, TEXT);
	}

	protected static int brighten(int argb) {
		int r = Math.min(255, ((argb >> 16) & 0xFF) + 30);
		int g = Math.min(255, ((argb >> 8) & 0xFF) + 30);
		int b = Math.min(255, (argb & 0xFF) + 30);
		return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
	}

	protected static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}
}
