package hu.kriwdi.intersolarsystem.client.repair;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import hu.kriwdi.intersolarsystem.ship.RepairMinigame;
import hu.kriwdi.intersolarsystem.ship.ValvePuzzle;

/**
 * Overheating: a temperature needle sweeps across the gauge; close the valve (click the button or press space) while it
 * is in the green band, once per round. A miss ends the repair at once.
 */
public class ValveScreen extends RepairBoardScreen {
	private static final int GAUGE_WIDTH = 200;
	private static final int GAUGE_HEIGHT = 18;
	private static final int KEY_SPACE = 32;

	private final ValvePuzzle puzzle;
	private final int[] clicks;
	private int round;

	public ValveScreen(long seed, int size) {
		super(RepairMinigame.VALVE, seed, Component.translatable("screen.intersolarsystem.valve.title"),
				Component.translatable("screen.intersolarsystem.valve.hint"));
		puzzle = ValvePuzzle.of(seed, size);
		clicks = new int[puzzle.rounds()];
	}

	@Override
	protected int panelWidth() {
		return 240;
	}

	@Override
	protected int panelHeight() {
		return 130;
	}

	private int gaugeX() {
		return left() + (panelWidth() - GAUGE_WIDTH) / 2;
	}

	private int gaugeY() {
		return contentTop() + 6;
	}

	private int buttonX() {
		return left() + panelWidth() / 2 - 50;
	}

	private int buttonY() {
		return gaugeY() + GAUGE_HEIGHT + 30;
	}

	@Override
	protected void drawBoard(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		int gx = gaugeX();
		int gy = gaugeY();
		// Gauge: blue (cold) to red (hot) scale with tick marks, the green band, and the needle.
		graphics.fill(gx - 2, gy - 2, gx + GAUGE_WIDTH + 2, gy + GAUGE_HEIGHT + 2, 0xFF0B0C0E);
		for (int x = 0; x < GAUGE_WIDTH; x++) {
			float t = x / (float) GAUGE_WIDTH;
			int r = (int) (60 + 170 * t);
			int b = (int) (200 - 170 * t);
			graphics.fill(gx + x, gy, gx + x + 1, gy + GAUGE_HEIGHT, 0xFF000000 | (r << 16) | (40 << 8) | b);
		}
		int r0 = Math.min(round, puzzle.rounds() - 1);
		int zx0 = gx + puzzle.zoneLow(r0) * GAUGE_WIDTH / 100;
		int zx1 = gx + puzzle.zoneHigh(r0) * GAUGE_WIDTH / 100;
		graphics.fill(zx0, gy - 1, zx1, gy + GAUGE_HEIGHT + 1, 0xC02E9E5B);
		graphics.fill(zx0, gy - 3, zx1, gy - 1, GOOD);
		for (int i = 0; i <= 10; i++) {
			int x = gx + i * GAUGE_WIDTH / 10;
			graphics.fill(x, gy + GAUGE_HEIGHT, x + 1, gy + GAUGE_HEIGHT + (i % 5 == 0 ? 5 : 3), 0xFFB9C0C6);
		}
		if (round < puzzle.rounds()) {
			double needle = puzzle.needle(round, ticks + partialTick);
			int nx = gx + (int) Math.round(needle * GAUGE_WIDTH / 100.0);
			graphics.fill(nx - 1, gy - 5, nx + 2, gy + GAUGE_HEIGHT + 5, 0xFF0B0C0E);
			graphics.fill(nx, gy - 4, nx + 1, gy + GAUGE_HEIGHT + 4, 0xFFFFFFFF);
		}
		// Round lamps.
		for (int i = 0; i < puzzle.rounds(); i++) {
			int lx = left() + panelWidth() / 2 - puzzle.rounds() * 8 + i * 16 + 3;
			int ly = gy + GAUGE_HEIGHT + 12;
			graphics.fill(lx - 1, ly - 1, lx + 9, ly + 9, 0xFF0B0C0E);
			graphics.fill(lx, ly, lx + 8, ly + 8, i < round ? GOOD : i == round ? 0xFFF2B233 : 0xFF3A3F44);
		}
		boolean hover = inside(mouseX, mouseY, buttonX(), buttonY(), 100, 20);
		button(graphics, buttonX(), buttonY(), 100, 20, Component.translatable("screen.intersolarsystem.valve.close"),
				hover, 0xFF8E2219);
	}

	private void closeValve() {
		if (sent() || round >= puzzle.rounds()) {
			return;
		}
		clicks[round] = ticks;
		boolean hit = puzzle.hit(round, ticks) && (round == 0 || ticks > clicks[round - 1]);
		round++;
		if (!hit) {
			// Fill the rest so the server sees a complete (failing) answer.
			for (int i = round; i < clicks.length; i++) {
				clicks[i] = ticks + i;
			}
			send(clicks.clone());
		} else if (round == puzzle.rounds()) {
			send(clicks.clone());
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0 && inside(event.x(), event.y(), buttonX(), buttonY(), 100, 20)) {
			closeValve();
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.key() == KEY_SPACE) {
			closeValve();
			return true;
		}
		return super.keyPressed(event);
	}
}
