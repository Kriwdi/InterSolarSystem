package hu.kriwdi.intersolarsystem.client.lore;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;

import hu.kriwdi.intersolarsystem.content.ModSounds;

/**
 * Plays a data tape: radio static and clicks, and the recording's lines as captions at the bottom of the screen, one
 * after another at their times. Playing another tape (or the same one again) restarts it.
 */
public final class TapePlayer implements HudElement {
	private static LoreLibrary.Entry playing;
	private static int ticks;

	public static void play(LoreLibrary.Entry entry) {
		playing = entry;
		ticks = 0;
		sound(ModSounds.TAPE_START, 0.8F);
	}

	public static void stop() {
		playing = null;
	}

	private static void sound(net.minecraft.sounds.SoundEvent event, float volume) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null && mc.level != null) {
			mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), event, SoundSource.PLAYERS,
					volume, 1.0F, false);
		}
	}

	public static void tick() {
		if (playing == null) {
			return;
		}
		ticks++;
		for (LoreLibrary.Line line : playing.lines()) {
			if (line.at() == ticks) {
				sound(ModSounds.TAPE_STATIC, 0.35F);
			}
		}
		int end = playing.lines().isEmpty() ? 0 : playing.lines().get(playing.lines().size() - 1).at() + 120;
		if (ticks > end) {
			sound(ModSounds.TAPE_START, 0.5F);
			playing = null;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
		if (playing == null) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		LoreLibrary.Line current = null;
		for (LoreLibrary.Line line : playing.lines()) {
			if (line.at() <= ticks) {
				current = line;
			}
		}
		int w = Math.min(320, g.guiWidth() - 20);
		int x = (g.guiWidth() - w) / 2;
		int y = g.guiHeight() - 92;
		String head = "▶ " + playing.title();
		g.fill(x - 4, y - 14, x + w + 4, y + 30, 0xA0000000);
		g.text(mc.font, head, x, y - 11, 0xFFF2B233, false);
		// A small level meter that jumps while a line is "spoken".
		for (int i = 0; i < 10; i++) {
			int h = current != null && ticks - current.at() < 80 ? (int) (2 + 6 * Math.abs(Math.sin((ticks + i * 3) * 0.6))) : 1;
			g.fill(x + w - 40 + i * 4, y - 3 - h, x + w - 38 + i * 4, y - 3, 0xFF5DFA98);
		}
		if (current != null) {
			String text = (current.who().isEmpty() ? "" : current.who() + ": ") + current.text();
			int ly = y;
			for (FormattedCharSequence line : mc.font.split(Component.literal(text), w)) {
				g.text(mc.font, line, x, ly, 0xFFFFFFFF, false);
				ly += 10;
				if (ly > y + 20) {
					break;
				}
			}
		}
	}
}
