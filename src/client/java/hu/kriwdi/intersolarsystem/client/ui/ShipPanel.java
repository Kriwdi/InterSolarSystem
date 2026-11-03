package hu.kriwdi.intersolarsystem.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Drawing helpers shared by the ship's screens: riveted metal panels, hazard-striped headers, bevelled buttons. */
public final class ShipPanel {
	public static final int TEXT = 0xFFE6E2D6;
	public static final int HINT = 0xFF9BA3A8;
	public static final int GOOD = 0xFF5DFA98;
	public static final int BAD = 0xFFFF5A3C;
	public static final int WARN = 0xFFF2B233;
	private static final int PANEL = 0xF0262A2E;
	private static final int EDGE_LIGHT = 0xFF59616A;
	private static final int EDGE_DARK = 0xFF121417;
	private static final int STRIPE_YELLOW = 0xFFD9A521;
	private static final int STRIPE_BLACK = 0xFF1E1F21;
	private static final int RIVET = 0xFF8D959C;

	/**
	 * The left mouse button. Minecraft 26.x numbers mouse buttons from 1 (SDL), not from 0 like GLFW did: checking for 0
	 * made every click in the ship's screens do nothing.
	 */
	public static final int LEFT_BUTTON = 1;

	private ShipPanel() {
	}

	/** Panel with header stripes, corner rivets, a centred title and a hint line under it. */
	public static void panel(GuiGraphicsExtractor g, Font font, int l, int t, int w, int h, Component title,
			Component hint) {
		int r = l + w;
		int b = t + h;
		g.fill(l - 2, t - 2, r + 2, b + 2, EDGE_DARK);
		g.fill(l - 1, t - 1, r + 1, t, EDGE_LIGHT);
		g.fill(l - 1, t - 1, l, b + 1, EDGE_LIGHT);
		g.fill(l, t, r, b, PANEL);
		for (int x = l; x < r; x++) {
			g.fill(x, t, x + 1, t + 4, ((x - l) / 4) % 2 == 0 ? STRIPE_YELLOW : STRIPE_BLACK);
		}
		for (int[] p : new int[][] {{l + 3, t + 7}, {r - 5, t + 7}, {l + 3, b - 5}, {r - 5, b - 5}}) {
			g.fill(p[0], p[1], p[0] + 2, p[1] + 2, RIVET);
			g.fill(p[0] + 1, p[1] + 1, p[0] + 2, p[1] + 2, EDGE_DARK);
		}
		g.centeredText(font, title, l + w / 2, t + 10, TEXT);
		if (hint != null) {
			g.centeredText(font, hint, l + w / 2, t + 24, HINT);
		}
	}

	/** Sunken dark display area (for maps and readouts). */
	public static void display(GuiGraphicsExtractor g, int x, int y, int w, int h) {
		g.fill(x - 1, y - 1, x + w + 1, y + h + 1, EDGE_LIGHT);
		g.fill(x - 1, y - 1, x + w, y + h, EDGE_DARK);
		g.fill(x, y, x + w, y + h, 0xFF070B10);
	}

	public static void button(GuiGraphicsExtractor g, Font font, int x, int y, int w, int h, Component label,
			boolean hover, int face) {
		g.fill(x, y, x + w, y + h, EDGE_DARK);
		g.fill(x, y, x + w - 1, y + h - 1, EDGE_LIGHT);
		g.fill(x + 1, y + 1, x + w - 1, y + h - 1, hover ? brighten(face) : face);
		g.centeredText(font, label, x + w / 2, y + (h - 8) / 2, TEXT);
	}

	public static int reachableColour(boolean reachable) {
		return reachable ? GOOD : HINT;
	}

	public static int brighten(int argb) {
		int r = Math.min(255, ((argb >> 16) & 0xFF) + 30);
		int gr = Math.min(255, ((argb >> 8) & 0xFF) + 30);
		int b = Math.min(255, (argb & 0xFF) + 30);
		return (argb & 0xFF000000) | (r << 16) | (gr << 8) | b;
	}

	public static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}
}
