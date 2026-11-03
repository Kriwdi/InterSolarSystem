package hu.kriwdi.intersolarsystem.client.lore;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

/**
 * A page-turning reader for the lore texts, in three styles: a handwritten note (yellowed paper, ink), a technical
 * manual (white pages with a blue header band and numbered sections) and a terminal (green text on black, a
 * blinking cursor). Pages turn with the arrows on screen, the arrow keys or the mouse wheel.
 */
public class LoreScreen extends Screen {
	public enum Style { NOTE, MANUAL, TERMINAL }

	private static final int W = 220;
	private static final int H = 200;

	private final Style style;
	private final LoreLibrary.Entry entry;
	private final List<List<FormattedCharSequence>> pages = new ArrayList<>();
	private int page;
	private int ticks;

	public LoreScreen(Style style, LoreLibrary.Entry entry) {
		super(Component.literal(entry.title()));
		this.style = style;
		this.entry = entry;
	}

	private int left() {
		return (width - W) / 2;
	}

	private int top() {
		return Math.max(2, (height - H) / 2);
	}

	/** Clickable page arrow. */
	private final class Arrow extends AbstractWidget {
		private final int step;

		Arrow(int x, int y, int step) {
			super(x, y, 18, 12, Component.literal(step < 0 ? "<" : ">"));
			this.step = step;
		}

		@Override
		protected void extractWidgetRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
			boolean enabled = step < 0 ? page > 0 : page < pages.size() - 1;
			int colour = !enabled ? 0x40000000 | (ink() & 0xFFFFFF) : isHovered() ? accent() : ink();
			g.text(font, step < 0 ? "<<" : ">>", getX() + 2, getY() + 2, colour, false);
		}

		@Override
		public void onClick(MouseButtonEvent event, boolean doubleClick) {
			turn(step);
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput output) {
			defaultButtonNarrationText(output);
		}
	}

	@Override
	protected void init() {
		pages.clear();
		int lineWidth = W - 28;
		int linesPerPage = (H - 58) / 10;
		for (String text : entry.pages()) {
			List<FormattedCharSequence> lines = new ArrayList<>();
			for (String paragraph : text.split("\n")) {
				if (paragraph.isEmpty()) {
					lines.add(FormattedCharSequence.EMPTY);
					continue;
				}
				lines.addAll(font.split(Component.literal(paragraph), lineWidth));
			}
			// A long page continues on the next one.
			for (int i = 0; i < lines.size(); i += linesPerPage) {
				pages.add(lines.subList(i, Math.min(lines.size(), i + linesPerPage)));
			}
		}
		if (pages.isEmpty()) {
			pages.add(List.of());
		}
		page = Math.min(page, pages.size() - 1);
		addRenderableWidget(new Arrow(left() + 10, top() + H - 16, -1));
		addRenderableWidget(new Arrow(left() + W - 28, top() + H - 16, 1));
	}

	private void turn(int step) {
		int next = Math.max(0, Math.min(pages.size() - 1, page + step));
		if (next != page) {
			page = next;
			minecraft.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
					net.minecraft.sounds.SoundEvents.BOOK_PAGE_TURN, 1.0F));
		}
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (event.isLeft() || event.isUp()) {
			turn(-1);
			return true;
		}
		if (event.isRight() || event.isDown()) {
			turn(1);
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double dx, double dy) {
		turn(dy > 0 ? -1 : 1);
		return true;
	}

	@Override
	public void tick() {
		super.tick();
		ticks++;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private int ink() {
		return switch (style) {
			case NOTE -> 0xFF3A2A1A;
			case MANUAL -> 0xFF1E2833;
			case TERMINAL -> 0xFF5DFA98;
		};
	}

	private int accent() {
		return switch (style) {
			case NOTE -> 0xFF8A2A1A;
			case MANUAL -> 0xFF2F6FB5;
			case TERMINAL -> 0xFFB8FFD0;
		};
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		int l = left();
		int t = top();
		switch (style) {
			case NOTE -> {
				// Yellowed paper with ruled lines, a torn edge and a coffee ring.
				g.fill(l + 2, t + 3, l + W + 2, t + H + 3, 0x60000000);
				g.fill(l, t, l + W, t + H, 0xFFE8DCB8);
				for (int y = t + 34; y < t + H - 18; y += 10) {
					g.fill(l + 8, y + 9, l + W - 8, y + 10, 0x30406080);
				}
				g.fill(l + 16, t + 4, l + 17, t + H - 4, 0x40B04040);
				for (int x = l; x < l + W; x += 6) {
					g.fill(x, t, x + 3, t + 2, 0xFFD8CCA6);
				}
				for (int i = 0; i < 40; i++) {
					double a = i * Math.PI * 2 / 40;
					int cx = l + W - 40 + (int) (Math.cos(a) * 14);
					int cy = t + H - 46 + (int) (Math.sin(a) * 14);
					g.fill(cx, cy, cx + 2, cy + 2, 0x30805020);
				}
			}
			case MANUAL -> {
				g.fill(l + 2, t + 3, l + W + 2, t + H + 3, 0x60000000);
				g.fill(l, t, l + W, t + H, 0xFFF4F4F0);
				g.fill(l, t, l + W, t + 28, 0xFF2F5F9F);
				g.fill(l, t + 28, l + W, t + 30, 0xFFD9A521);
				int badge = font.width("AURORA") + 8;
				g.fill(l + W - 10 - badge, t + 6, l + W - 10, t + 22, 0xFFE8E8E8);
				g.fill(l + W - 10 - badge, t + 20, l + W - 10, t + 22, 0xFFD9822B);
				g.text(font, "AURORA", l + W - 6 - badge, t + 10, 0xFF2F5F9F, false);
			}
			case TERMINAL -> {
				g.fill(l - 4, t - 4, l + W + 4, t + H + 4, 0xFF2B2F33);
				g.fill(l, t, l + W, t + H, 0xFF050A06);
				for (int y = t; y < t + H; y += 3) {
					g.fill(l, y, l + W, y + 1, 0x18FFFFFF);
				}
			}
		}
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		int titleColour = style == Style.MANUAL ? 0xFFFFFFFF : accent();
		g.text(font, entry.title(), l + 12, t + 8, titleColour, false);
		if (!entry.subtitle().isEmpty()) {
			g.text(font, entry.subtitle(), l + 12, t + 18, style == Style.MANUAL ? 0xFFD8E4F4 : 0xFF7A6A50 | (ink() & 0x00FFFFFF),
					false);
		}
		int y = t + 36;
		List<FormattedCharSequence> lines = pages.get(page);
		for (FormattedCharSequence line : lines) {
			g.text(font, line, l + 14, y, ink(), false);
			y += 10;
		}
		if (style == Style.TERMINAL && (ticks / 10) % 2 == 0) {
			g.fill(l + 14, y + 1, l + 20, y + 9, ink());
		}
		String number = (page + 1) + " / " + pages.size();
		g.text(font, number, l + W / 2 - font.width(number) / 2, t + H - 14, ink(), false);
	}
}
