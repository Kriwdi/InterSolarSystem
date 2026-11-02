package hu.kriwdi.intersolarsystem.client;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

import hu.kriwdi.intersolarsystem.content.ModArmor;
import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;

/**
 * Suit oxygen gauge above the hotbar, shown while wearing the space suit or while in orbit. In orbit without a sealed
 * suit it flashes a vacuum warning instead.
 */
public class OxygenHudElement implements HudElement {
	private static final int WIDTH = 82;
	private static final int TEXT = 0xFFE6E2D6;

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null || minecraft.level == null) {
			return;
		}
		boolean orbit = minecraft.level.dimension().equals(ShipDimensions.ORBIT)
				|| minecraft.level.dimension().equals(ShipDimensions.MOON);
		boolean wearing = player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.SPACE_SUIT;
		if (!orbit && !wearing) {
			return;
		}
		int x = g.guiWidth() / 2 + 96;
		int y = g.guiHeight() - 22;
		long time = minecraft.level.getGameTime();
		if (orbit && !ModArmor.fullSuit(player)) {
			if ((time / 8) % 2 == 0) {
				g.fill(x - 2, y - 2, x + WIDTH + 2, y + 12, 0xC0400000);
				g.text(minecraft.font, Component.translatable("hud.intersolarsystem.vacuum"), x + 2, y + 1, 0xFFFF5A3C);
			}
			return;
		}
		double oxygen = ModArmor.oxygen(player);
		int colour = oxygen > 0.5 ? 0xFF4FC3F7 : oxygen > 0.2 ? 0xFFF2B233 : 0xFFFF5A3C;
		g.fill(x - 2, y - 2, x + WIDTH + 2, y + 12, 0xA0000000);
		g.text(minecraft.font, "O₂", x, y + 1, TEXT);
		int barX = x + 16;
		int barW = WIDTH - 18;
		g.fill(barX, y + 1, barX + barW, y + 8, 0xFF1B2228);
		int filled = (int) Math.round(barW * oxygen);
		boolean low = oxygen <= 0.2 && (time / 6) % 2 == 0;
		if (!low) {
			g.fill(barX, y + 1, barX + filled, y + 8, colour);
			g.fill(barX, y + 1, barX + filled, y + 2, 0x60FFFFFF);
		}
		for (int i = 1; i < 4; i++) {
			int tick = barX + barW * i / 4;
			g.fill(tick, y + 1, tick + 1, y + 8, 0x80000000);
		}
	}
}
