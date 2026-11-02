package hu.kriwdi.intersolarsystem.client.repair;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import hu.kriwdi.intersolarsystem.client.ui.ShipPanel;
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
		ShipPanel.panel(graphics, font, left(), top(), panelWidth(), panelHeight(), title, hint);
		drawBoard(graphics, mouseX, mouseY, partialTick);
	}

	protected abstract void drawBoard(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick);

	protected void button(GuiGraphicsExtractor graphics, int x, int y, int w, int h, Component label, boolean hover,
			int face) {
		ShipPanel.button(graphics, font, x, y, w, h, label, hover, face);
	}

	protected static int brighten(int argb) {
		return ShipPanel.brighten(argb);
	}

	protected static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}
}
