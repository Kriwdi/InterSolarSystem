package hu.kriwdi.intersolarsystem.client;

import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import hu.kriwdi.intersolarsystem.client.ui.ShipPanel;
import hu.kriwdi.intersolarsystem.ship.NavigationPayloads;
import hu.kriwdi.intersolarsystem.ship.ShipNavigation;
import hu.kriwdi.intersolarsystem.travel.CelestialBody;
import hu.kriwdi.intersolarsystem.travel.SolarSystem;
import hu.kriwdi.intersolarsystem.travel.TravelMath;

/**
 * The navigation console's star map: the Solar System as an orrery strip, a list of destinations with their travel
 * times for this ship's engine and the current launch window, and the launch button. Times are recomputed every frame
 * from the server's clock, so the window countdown runs live.
 */
public class NavigationScreen extends Screen {
	private static final int WIDTH = 320;
	private static final int HEIGHT = 300;
	private static final int ROW = 12;
	private static final Map<String, Integer> COLOURS = Map.ofEntries(
			Map.entry("earth", 0xFF3D7BD9), Map.entry("earth_orbit", 0xFF9FC4FF), Map.entry("moon", 0xFFC9C9C9), Map.entry("venus", 0xFFE3C27A),
			Map.entry("mercury", 0xFF9E8F80), Map.entry("mars", 0xFFD0603A), Map.entry("ceres", 0xFF8A857C),
			Map.entry("jupiter", 0xFFD9A877), Map.entry("saturn", 0xFFE8D29A), Map.entry("uranus", 0xFF9FE0E6),
			Map.entry("neptune", 0xFF4C6FE0));
	/** Rough orbit radii in AU, only for placing the bodies on the map strip (log scale). */
	private static final Map<String, Double> ORBIT_AU = Map.ofEntries(
			Map.entry("mercury", 0.39), Map.entry("venus", 0.72), Map.entry("earth", 1.0), Map.entry("earth_orbit", 1.0),
			Map.entry("moon", 1.0),
			Map.entry("mars", 1.52), Map.entry("ceres", 2.77), Map.entry("jupiter", 5.2), Map.entry("saturn", 9.5),
			Map.entry("uranus", 19.2), Map.entry("neptune", 30.1));

	private final int engineTier;
	private final long serverTimeAtOpen;
	private final boolean canLaunch;
	private final String here;
	private final List<CelestialBody> bodies = SolarSystem.bodies();
	private int ticks;
	private int selected = 1;
	private boolean launched;

	public NavigationScreen(int engineTier, long gameTime, boolean canLaunch, String here) {
		super(Component.translatable("screen.intersolarsystem.nav.title"));
		this.engineTier = engineTier;
		this.serverTimeAtOpen = gameTime;
		this.canLaunch = canLaunch;
		this.here = here;
	}

	private boolean launchable(CelestialBody b) {
		return canLaunch && !launched && ShipNavigation.reachable(b, here);
	}

	/** Travel time of a leg from where the ship is now. */
	private long legTicks(CelestialBody b) {
		return TravelMath.legTicks(here, b.id(), engineTier, now());
	}

	private int left() {
		return (width - WIDTH) / 2;
	}

	private int top() {
		return Math.max(0, (height - HEIGHT) / 2);
	}

	private long now() {
		return serverTimeAtOpen + ticks;
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

	private int mapY() {
		return top() + 38;
	}

	private int listY() {
		return mapY() + 44;
	}

	private int launchX() {
		return left() + WIDTH - 104;
	}

	private int launchY() {
		return top() + HEIGHT - 26;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(g, mouseX, mouseY, partialTick);
		int l = left();
		int t = top();
		ShipPanel.panel(g, font, l, t, WIDTH, HEIGHT, title, Component.translatable("screen.intersolarsystem.nav.engine",
				engineTier, TravelMath.multiplier(engineTier)));
		drawMap(g, l + 10, mapY(), WIDTH - 20, 36, partialTick);
		drawList(g, l + 10, listY(), mouseX, mouseY);
		drawDetails(g, l + 10, listY() + bodies.size() * ROW + 10);
		boolean launchable = launchable(bodies.get(selected));
		ShipPanel.button(g, font, launchX(), launchY(), 94, 18,
				Component.translatable("screen.intersolarsystem.nav.launch"),
				launchable && ShipPanel.inside(mouseX, mouseY, launchX(), launchY(), 94, 18),
				launchable ? 0xFF2E6B47 : 0xFF3A3F44);
	}

	/** Orrery strip: the Sun on the left, bodies on a log scale, slowly drifting markers for life. */
	private void drawMap(GuiGraphicsExtractor g, int x, int y, int w, int h, float partialTick) {
		ShipPanel.display(g, x, y, w, h);
		for (int i = 0; i < 40; i++) {
			int sx = x + (int) ((i * 73 + 11) % w);
			int sy = y + (int) ((i * 37 + 5) % h);
			g.fill(sx, sy, sx + 1, sy + 1, (i % 3 == 0) ? 0xFFFFFFFF : 0xFF6C7A8A);
		}
		int sunX = x + 8;
		int midY = y + h / 2;
		g.fill(sunX - 4, midY - 4, sunX + 5, midY + 5, 0xFFFFB02E);
		g.fill(sunX - 3, midY - 5, sunX + 4, midY + 6, 0xFFFFC85A);
		g.fill(sunX - 2, midY - 2, sunX + 3, midY + 3, 0xFFFFF2B0);
		double min = Math.log(0.3);
		double max = Math.log(35);
		for (int i = 0; i < bodies.size(); i++) {
			CelestialBody b = bodies.get(i);
			double au = ORBIT_AU.getOrDefault(b.id(), 1.0);
			int bx = sunX + 14 + (int) ((Math.log(au) - min) / (max - min) * (w - 30));
			int by = midY;
			if (b.kind() == CelestialBody.Kind.MOON) {
				double a = (now() + partialTick) * 0.05;
				bx += (int) Math.round(Math.cos(a) * 6);
				by += (int) Math.round(Math.sin(a) * 6);
			}
			int colour = COLOURS.getOrDefault(b.id(), 0xFFFFFFFF);
			if (b.kind() == CelestialBody.Kind.ORBIT) {
				// Drawn as a thin ring around the Earth.
				int r = 4;
				int ring = i == selected ? 0xFF00FFAA : (colour & 0x00FFFFFF) | 0x90000000;
				g.fill(bx - r, by - r, bx + r + 1, by - r + 1, ring);
				g.fill(bx - r, by + r, bx + r + 1, by + r + 1, ring);
				g.fill(bx - r, by - r, bx - r + 1, by + r + 1, ring);
				g.fill(bx + r, by - r, bx + r + 1, by + r + 1, ring);
				continue;
			}
			int size = b.kind() == CelestialBody.Kind.GAS_GIANT ? 3 : b.kind() == CelestialBody.Kind.MOON ? 1 : 2;
			if (i == selected) {
				g.fill(bx - size - 2, by - size - 2, bx + size + 3, by + size + 3, 0x8000FFAA);
			}
			g.fill(bx - size, by - size, bx + size + 1, by + size + 1, colour);
			if ("saturn".equals(b.id())) {
				g.fill(bx - 5, by, bx + 6, by + 1, 0xFFC9B27A);
			}
		}
	}

	private void drawList(GuiGraphicsExtractor g, int x, int y, int mouseX, int mouseY) {
		int w = WIDTH - 20;
		ShipPanel.display(g, x, y, w, bodies.size() * ROW + 4);
		for (int i = 0; i < bodies.size(); i++) {
			CelestialBody b = bodies.get(i);
			int ry = y + 2 + i * ROW;
			boolean hover = ShipPanel.inside(mouseX, mouseY, x, ry, w, ROW);
			if (i == selected) {
				g.fill(x + 1, ry, x + w - 1, ry + ROW, 0xFF1F3A2C);
			} else if (hover) {
				g.fill(x + 1, ry, x + w - 1, ry + ROW, 0xFF161D24);
			}
			g.fill(x + 4, ry + 4, x + 9, ry + 9, COLOURS.getOrDefault(b.id(), 0xFFFFFFFF));
			g.text(font, Component.translatable(b.translationKey()), x + 13, ry + 3, ShipPanel.TEXT);
			Component status = Component.translatable("screen.intersolarsystem.nav.status." + b.status().name()
					.toLowerCase(java.util.Locale.ROOT));
			int statusColour = switch (b.status()) {
				case SURFACE, ORBIT, LANDING -> b.id().equals(here) ? ShipPanel.HINT : ShipPanel.GOOD;
				case FLYBY -> ShipPanel.reachableColour(ShipNavigation.reachable(b, here));
				case LOCKED -> 0xFF6B737A;
			};
			g.text(font, status, x + 118, ry + 3, statusColour);
			if (!b.id().equals(here) && b.status() != CelestialBody.Status.LOCKED) {
				String time = TravelMath.formatDuration(legTicks(b));
				g.text(font, time, x + w - 4 - font.width(time), ry + 3, ShipPanel.TEXT);
			} else if (b.transferDays() > 0 && b.status() == CelestialBody.Status.LOCKED) {
				String time = TravelMath.formatDuration(TravelMath.oneWayTicks(b, engineTier, now()));
				g.text(font, time, x + w - 4 - font.width(time), ry + 3, ShipPanel.TEXT);
			}
		}
	}

	private void drawDetails(GuiGraphicsExtractor g, int x, int y) {
		CelestialBody b = bodies.get(selected);
		boolean isHere = b.id().equals(here);
		if (b.status() == CelestialBody.Status.SURFACE) {
			g.text(font, Component.translatable(isHere ? "screen.intersolarsystem.nav.surface_hint"
					: "screen.intersolarsystem.nav.deorbit", TravelMath.formatDuration(legTicks(b))), x, y,
					isHere ? ShipPanel.HINT : ShipPanel.GOOD);
			return;
		}
		if (b.status() == CelestialBody.Status.ORBIT) {
			g.text(font, Component.translatable(isHere ? "screen.intersolarsystem.nav.in_orbit"
					: "screen.intersolarsystem.nav.to_orbit", TravelMath.formatDuration(legTicks(b))), x, y,
					isHere ? ShipPanel.HINT : ShipPanel.GOOD);
			return;
		}
		if (b.status() == CelestialBody.Status.LANDING) {
			g.text(font, Component.translatable(isHere ? "screen.intersolarsystem.nav.landed_here"
					: "screen.intersolarsystem.nav.landing", Component.translatable(b.translationKey()),
					TravelMath.formatDuration(legTicks(b))), x, y, isHere ? ShipPanel.HINT : ShipPanel.GOOD);
			double factor = TravelMath.windowFactor(b, now());
			int penalty = (int) Math.round((factor - 1.0) * 100);
			if (!isHere && penalty > 2) {
				g.text(font, Component.translatable("screen.intersolarsystem.nav.window_next", penalty,
						TravelMath.formatDuration(TravelMath.ticksToNextWindow(b, now()))), x, y + 12, ShipPanel.WARN);
			}
			return;
		}
		double factor = TravelMath.windowFactor(b, now());
		int penalty = (int) Math.round((factor - 1.0) * 100);
		long next = TravelMath.ticksToNextWindow(b, now());
		Component window = penalty <= 2
				? Component.translatable("screen.intersolarsystem.nav.window_open")
				: Component.translatable("screen.intersolarsystem.nav.window_next", penalty,
						TravelMath.formatDuration(next));
		g.text(font, window, x, y, penalty <= 2 ? ShipPanel.GOOD : ShipPanel.WARN);
		if (b.status() == CelestialBody.Status.FLYBY) {
			g.text(font, Component.translatable("screen.intersolarsystem.nav.flyby",
					TravelMath.formatDuration(TravelMath.flybyTicks(b, engineTier, now()))), x, y + 12, ShipPanel.TEXT);
		} else {
			Component line = b.assist().isPresent()
					? Component.translatable("screen.intersolarsystem.nav.locked_assist",
							Component.translatable("body.intersolarsystem." + b.assist().get()))
					: Component.translatable("screen.intersolarsystem.nav.locked");
			g.text(font, line, x, y + 12, ShipPanel.HINT);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			int x = left() + 10;
			for (int i = 0; i < bodies.size(); i++) {
				if (ShipPanel.inside(event.x(), event.y(), x, listY() + 2 + i * ROW, WIDTH - 20, ROW)) {
					selected = i;
					return true;
				}
			}
			CelestialBody b = bodies.get(selected);
			if (ShipPanel.inside(event.x(), event.y(), launchX(), launchY(), 94, 18) && launchable(b)) {
				launched = true;
				ClientPlayNetworking.send(new NavigationPayloads.Launch(b.id()));
				onClose();
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}
}
