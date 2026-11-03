package hu.kriwdi.intersolarsystem.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import hu.kriwdi.intersolarsystem.client.sky.ClientManeuver;
import hu.kriwdi.intersolarsystem.client.ui.ShipPanel;
import hu.kriwdi.intersolarsystem.travel.MeteorField;

/**
 * The proximity radar during a meteor shower (top right): the ship in the middle, rocks as dots that grow and turn red
 * as they close in on a collision course, the hit count, and what to do.
 */
public class RadarHudElement implements HudElement {
	private static final int SIZE = 96;

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		if (!ClientManeuver.active() || minecraft.player == null) {
			return;
		}
		float partial = 0.0F;
		int x = g.guiWidth() - SIZE - 8;
		int y = 8;
		ShipPanel.display(g, x, y + 12, SIZE, SIZE);
		g.text(minecraft.font, Component.translatable("hud.intersolarsystem.radar.title"), x, y, ShipPanel.BAD);
		int cx = x + SIZE / 2;
		int cy = y + 12 + SIZE / 2;
		// Range rings and cross hairs.
		for (int i = -SIZE / 2 + 2; i < SIZE / 2 - 1; i += 2) {
			g.fill(cx + i, cy, cx + i + 1, cy + 1, 0xFF1C3A2A);
			g.fill(cx, cy + i, cx + 1, cy + i + 1, 0xFF1C3A2A);
		}
		float elapsed = ClientManeuver.elapsed(partial);
		float sx = ClientManeuver.shipX();
		float sy = ClientManeuver.shipY();
		float scale = SIZE / 2.0F / 1.6F;
		for (MeteorField.Meteor rock : ClientManeuver.field()) {
			float p = (elapsed - rock.appear()) / MeteorField.APPROACH_TICKS;
			if (p < 0.0F || p > 1.0F) {
				continue;
			}
			int px = cx + Math.round((rock.x() - sx) * scale);
			int py = cy - Math.round((rock.y() - sy) * scale);
			if (px < x + 1 || px > x + SIZE - 2 || py < y + 13 || py > y + 12 + SIZE - 2) {
				continue;
			}
			int r = 1 + Math.round(p * p * rock.radius() * scale * 0.9F);
			boolean danger = rock.hits(sx, sy);
			int colour = danger ? (p > 0.6F ? 0xFFFF4A2E : 0xFFF2B233) : 0xFF8A9298;
			g.fill(px - r, py - r, px + r + 1, py + r + 1, colour);
		}
		// The ship.
		g.fill(cx - 1, cy - 3, cx + 2, cy + 3, 0xFF5DFA98);
		g.fill(cx - 3, cy + 1, cx + 4, cy + 3, 0xFF5DFA98);
		int ty = y + SIZE + 16;
		g.text(minecraft.font, Component.translatable("hud.intersolarsystem.radar.hits", ClientManeuver.hits()), x, ty,
				ShipPanel.TEXT);
		boolean blink = (minecraft.level.getGameTime() / 10) % 2 == 0;
		Component hint = ClientManeuver.piloted() ? Component.translatable("hud.intersolarsystem.radar.steer")
				: Component.translatable("hud.intersolarsystem.radar.no_pilot");
		int hx = g.guiWidth() - minecraft.font.width(hint) - 8;
		g.text(minecraft.font, hint, Math.min(x, hx), ty + 11,
				ClientManeuver.piloted() ? ShipPanel.GOOD : blink ? ShipPanel.BAD : ShipPanel.WARN);
	}
}
