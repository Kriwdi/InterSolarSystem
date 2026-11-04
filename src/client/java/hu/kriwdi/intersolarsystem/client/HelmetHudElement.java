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
import hu.kriwdi.intersolarsystem.space.VacuumBreathing;
import hu.kriwdi.intersolarsystem.space.VacuumStatusPayload;

/**
 * The helmet's visor display and what the vacuum does to your sight.
 *
 * <p>With the helmet on: an oxygen arc on the left, the suit's seal and pressure arc on the
 * right, warnings at the bottom (leak, low oxygen, hypoxia). Without air: hypoxia darkens and blurs the edges of the
 * view in time with the heartbeat; decompression frosts it over and makes it flicker.
 */
public class HelmetHudElement implements HudElement {
	private static VacuumStatusPayload status;
	private static long receivedAt;

	public static void update(VacuumStatusPayload payload, long gameTime) {
		status = payload;
		receivedAt = gameTime;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.level == null) {
			return;
		}
		long time = mc.level.getGameTime();
		VacuumStatusPayload s = status != null && time - receivedAt < 30 ? status : null;
		boolean helmet = player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.SPACE_HELMET;
		int w = g.guiWidth();
		int h = g.guiHeight();
		if (s != null && s.vacuum()) {
			float strain = 1.0F - s.breath();
			if (s.mode() == VacuumBreathing.Mode.HYPOXIA.ordinal() && strain > 0.02F) {
				// The heartbeat pulses the darkness.
				float pulse = (float) (0.85 + 0.15 * Math.sin(time * 0.9));
				vignette(g, w, h, Math.min(1.0F, 0.25F + strain) * pulse, 0x000000);
			} else if (s.mode() == VacuumBreathing.Mode.DECOMPRESSION.ordinal()) {
				float flicker = (time / 3) % 5 == 0 ? 0.8F : 1.0F;
				vignette(g, w, h, Math.min(1.0F, 0.35F + strain) * flicker, 0xCFE6F2);
				frost(g, w, h, Math.min(1.0F, 0.3F + strain), time);
			}
		}
		if (!helmet) {
			if (s != null && s.vacuum() && s.mode() == VacuumBreathing.Mode.DECOMPRESSION.ordinal() && (time / 8) % 2 == 0) {
				centred(g, mc, Component.translatable("hud.intersolarsystem.helmet.decompression"), w / 2, h - 70, 0xFFFF5A3C);
			}
			return;
		}
		double oxygen = s != null ? s.oxygen() : ModArmor.oxygen(player);
		boolean leak = s != null && s.leak();
		boolean sealed = ModArmor.fullSuit(player);
		int cx = w / 2;
		int cy = h / 2;
		int radius = (int) (Math.min(w, h) * 0.44);
		int oxygenColour = oxygen > 0.5 ? 0xFF4FC3F7 : oxygen > 0.2 ? 0xFFF2B233 : 0xFFFF5A3C;
		boolean blinkLow = oxygen <= 0.2 && (time / 6) % 2 == 0;
		arc(g, cx, cy, radius, 150, 210, oxygen, blinkLow ? 0xFF5A1A12 : oxygenColour);
		int seal = !sealed ? 0xFFFF5A3C : leak ? ((time / 5) % 2 == 0 ? 0xFFFF5A3C : 0xFF5A1A12) : 0xFF6CE07A;
		arc(g, cx, cy, radius, -30, 30, sealed && !leak ? 1.0 : 0.35, seal);
		int lx = cx - (int) (radius * Math.cos(Math.toRadians(30))) - 4;
		int rx = cx + (int) (radius * Math.cos(Math.toRadians(30))) + 4;
		int ty = cy - (int) (radius * Math.sin(Math.toRadians(30))) - 12;
		g.text(mc.font, Component.translatable("hud.intersolarsystem.helmet.oxygen").getString() + " "
				+ Math.round(oxygen * 100) + "%", lx - 30, ty, oxygenColour, true);
		String pressure = Component.translatable("hud.intersolarsystem.helmet.pressure").getString() + " "
				+ (!sealed || leak && (time / 10) % 2 == 0 ? (sealed ? "21" : "0") : "34") + " kPa";
		g.text(mc.font, pressure, rx - 10, ty, seal, true);
		Component warning = null;
		int colour = 0xFFFF5A3C;
		if (s != null && s.vacuum() && s.mode() == VacuumBreathing.Mode.HYPOXIA.ordinal()) {
			warning = Component.translatable("hud.intersolarsystem.helmet.hypoxia");
		} else if (leak) {
			warning = Component.translatable("hud.intersolarsystem.helmet.leak");
		} else if (oxygen <= 0.2) {
			warning = Component.translatable("hud.intersolarsystem.helmet.low");
			colour = 0xFFF2B233;
		}
		if (warning != null && (time / 8) % 2 == 0) {
			centred(g, mc, warning, cx, h - 70, colour);
		}
		eva(g, mc, player, cx, cy + radius / 2 + 8, time);
	}

	/** Spacewalk readouts under the visor: tether, drift, the manoeuvring unit, debris streams. */
	private static void eva(GuiGraphicsExtractor g, Minecraft mc, LocalPlayer player, int cx, int y, long time) {
		if (!mc.level.dimension().equals(hu.kriwdi.intersolarsystem.ship.ShipDimensions.ORBIT)) {
			return;
		}
		var tether = ZeroGravity.tether();
		java.util.List<Component> lines = new java.util.ArrayList<>();
		java.util.List<Integer> colours = new java.util.ArrayList<>();
		if (tether.on()) {
			double d = Math.sqrt(player.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(tether.anchor())));
			lines.add(Component.translatable("hud.intersolarsystem.eva.tether", (int) Math.round(d), tether.length()));
			colours.add(d > tether.length() - 2 ? 0xFFF2B233 : 0xFFF2C744);
		} else if (ZeroGravity.drifting() && (time / 10) % 2 == 0) {
			lines.add(Component.translatable("hud.intersolarsystem.eva.drift"));
			colours.add(0xFFFF5A3C);
		}
		net.minecraft.world.item.ItemStack mmu = hu.kriwdi.intersolarsystem.space.OrbitalEva.findMmu(player);
		if (!mmu.isEmpty()) {
			int fuel = Math.round(100F * (mmu.getMaxDamage() - mmu.getDamageValue()) / mmu.getMaxDamage());
			lines.add(Component.translatable(ZeroGravity.thrusting() ? "hud.intersolarsystem.eva.mmu_firing"
					: "hud.intersolarsystem.eva.mmu", fuel));
			colours.add(fuel > 20 ? 0xFF4FC3F7 : 0xFFF2B233);
		}
		int phase = ZeroGravity.debrisPhase(time);
		if (phase == 1) {
			lines.add(Component.translatable("hud.intersolarsystem.eva.debris_in", ZeroGravity.debrisLeft(time) / 20 + 1));
			colours.add(0xFFF2B233);
		} else if (phase == 2 && (time / 6) % 2 == 0) {
			lines.add(Component.translatable("hud.intersolarsystem.eva.debris_now"));
			colours.add(0xFFFF5A3C);
		}
		for (int i = 0; i < lines.size(); i++) {
			var line = lines.get(i);
			g.text(mc.font, line, cx - mc.font.width(line) / 2, y + i * 10, colours.get(i), true);
		}
	}

	/** Dots along a circular arc (degrees, 0 = right, counter-clockwise up); the first {@code fill} part is lit. */
	private static void arc(GuiGraphicsExtractor g, int cx, int cy, int r, int from, int to, double fill, int colour) {
		int steps = 28;
		for (int i = 0; i <= steps; i++) {
			double t = (double) i / steps;
			// Oxygen and seal both fill from the bottom up.
			double angle = Math.toRadians(from < 90 ? from + (to - from) * t : to - (to - from) * t);
			int x = cx + (int) Math.round(Math.cos(angle) * r);
			int y = cy + (int) Math.round(Math.sin(angle) * r);
			boolean lit = t <= fill + 1e-6;
			g.fill(x - 2, y - 1, x + 2, y + 2, 0x90000000);
			g.fill(x - 1, y - 1, x + 1, y + 1, lit ? colour : 0x50FFFFFF);
		}
	}

	/** Bands of colour from the screen edge inwards, fading out. */
	private static void vignette(GuiGraphicsExtractor g, int w, int h, float strength, int rgb) {
		int bands = 14;
		int depth = (int) (Math.min(w, h) * (0.12 + 0.3 * strength));
		int step = Math.max(1, depth / bands);
		for (int i = 0; i < bands; i++) {
			int alpha = (int) (strength * 200 * Math.pow(1.0 - (double) i / bands, 1.6));
			int c = (Math.min(255, alpha) << 24) | rgb;
			int o = i * step;
			g.fill(o, o, w - o, o + step, c);
			g.fill(o, h - o - step, w - o, h - o, c);
			g.fill(o, o + step, o + step, h - o - step, c);
			g.fill(w - o - step, o + step, w - o, h - o - step, c);
		}
	}

	/** Ice crystals creeping in from the edges. */
	private static void frost(GuiGraphicsExtractor g, int w, int h, float strength, long time) {
		long seed = 0x9E3779B97F4A7C15L;
		int n = (int) (120 * strength);
		for (int i = 0; i < n; i++) {
			seed = seed * 6364136223846793005L + 1442695040888963407L;
			int edge = (int) ((seed >>> 33) % 4);
			double along = ((seed >>> 13) & 0xFFFF) / 65535.0;
			double in = Math.pow(((seed >>> 40) & 0xFF) / 255.0, 2) * Math.min(w, h) * 0.25 * strength;
			int x = edge < 2 ? (int) (along * w) : edge == 2 ? (int) in : w - 1 - (int) in;
			int y = edge >= 2 ? (int) (along * h) : edge == 0 ? (int) in : h - 1 - (int) in;
			int size = 1 + (int) ((seed >>> 50) % 3);
			g.fill(x - size, y, x + size + 1, y + 1, 0xB0F2FAFF);
			g.fill(x, y - size, x + 1, y + size + 1, 0xB0F2FAFF);
		}
	}

	private static void centred(GuiGraphicsExtractor g, Minecraft mc, Component text, int x, int y, int colour) {
		int tw = mc.font.width(text);
		g.fill(x - tw / 2 - 4, y - 3, x + tw / 2 + 4, y + 11, 0x90000000);
		g.text(mc.font, text, x - tw / 2, y, colour, true);
	}
}
