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
	private static double sourceX;
	private static double sourceY;
	private static double sourceZ;

	/** Plays a tape in the deck at (x, y, z): every sound comes from the deck. */
	public static void play(LoreLibrary.Entry entry, double deckX, double deckY, double deckZ) {
		playing = entry;
		ticks = 0;
		sourceX = deckX;
		sourceY = deckY;
		sourceZ = deckZ;
	}

	public static void stop() {
		playing = null;
	}

	private static void sound(net.minecraft.sounds.SoundEvent event, float volume, float pitch) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) {
			mc.level.playLocalSound(sourceX, sourceY, sourceZ, event, SoundSource.RECORDS, volume, pitch, false);
		}
	}

	/** Each speaker keeps one voice: the pitch comes from the name. */
	private static float pitchOf(String who) {
		return 0.8F + (Math.abs(who.hashCode()) % 45) / 100.0F;
	}

	public static void tick() {
		if (playing == null) {
			return;
		}
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.player.distanceToSqr(sourceX, sourceY, sourceZ) > 32 * 32) {
			playing = null;
			return;
		}
		ticks++;
		if (ticks % 50 == 1) {
			sound(ModSounds.TAPE_STATIC, 0.25F, 1.0F);
		}
		LoreLibrary.Line current = null;
		for (LoreLibrary.Line line : playing.lines()) {
			if (line.at() <= ticks) {
				current = line;
			}
		}
		if (current != null) {
			int since = ticks - current.at();
			boolean effect = current.text().startsWith("[");
			int speaking = Math.min(110, 20 + current.text().length() * 2);
			if (since == 0) {
				sound(ModSounds.TAPE_STATIC, 0.6F, 1.0F);
			}
			if (!effect && since < speaking && since % 22 == 2) {
				sound(ModSounds.TAPE_VOICE, 0.9F, pitchOf(current.who()));
			}
			if (effect && since == 2) {
				String t = current.text().toLowerCase(java.util.Locale.ROOT);
				if (t.contains("alarm") || t.contains("siren") || t.contains("riaszt") || t.contains("sziréna")
						|| t.contains("tones") || t.contains("hang]")) {
					sound(ModSounds.OXYGEN_ALARM, 0.8F, 0.7F);
				} else if (t.contains("bird") || t.contains("madár")) {
					sound(net.minecraft.sounds.SoundEvents.PARROT_AMBIENT, 0.8F, 1.3F);
				} else {
					sound(ModSounds.TAPE_STATIC, 0.9F, 0.8F);
				}
			}
		}
		int end = playing.lines().isEmpty() ? 0 : playing.lines().get(playing.lines().size() - 1).at() + 120;
		if (ticks > end) {
			sound(ModSounds.TAPE_START, 0.6F, 1.0F);
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
