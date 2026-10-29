package hu.kriwdi.intersolarsystem.client;

import java.util.Optional;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;

import hu.kriwdi.intersolarsystem.ship.ShipCommands;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipHudModel;
import hu.kriwdi.intersolarsystem.ship.ShipStatusSnapshot;

/** Draws the ship status panel while the local player is inside the ship. */
public class ShipHudElement implements HudElement {
	private static final int X = 4;
	private static final int Y = 4;
	private static final int PADDING = 3;
	private static final int BAR_WIDTH = 50;
	private static final int GAP = 4;
	private static final int BACKGROUND = 0x80000000;
	private static final int TEXT = 0xFFFFFFFF;
	private static final int BAR_BACKGROUND = 0xFF333333;
	private static final int ALERT = 0xFFFF5555;

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft minecraft = Minecraft.getInstance();
		ClientLevel level = minecraft.level;
		if (level == null || !level.dimension().equals(ShipDimensions.INTERIOR)) {
			return;
		}
		Optional<ShipStatusSnapshot> snapshot = ClientShipStatus.snapshot(level);
		if (snapshot.isEmpty()) {
			return;
		}
		long now = level.getGameTime();
		ShipHudModel model = ShipHudModel.from(snapshot.get(), ClientShipStatus.receivedAtTick(), now);
		Font font = minecraft.font;
		int lineHeight = font.lineHeight + 2;

		Component phase = model.phase().clock()
				.map(clock -> Component.translatable(model.phase().translationKey(), clock))
				.orElseGet(() -> Component.translatable(model.phase().translationKey()));

		int nameWidth = 0;
		int percentWidth = 0;
		for (ShipHudModel.SubsystemRow row : model.subsystems()) {
			nameWidth = Math.max(nameWidth, font.width(Component.translatable(ShipCommands.subsystemKey(row.subsystem()))));
			percentWidth = Math.max(percentWidth, font.width(row.percent() + "%"));
		}
		int widest = Math.max(font.width(phase), nameWidth + GAP + BAR_WIDTH + GAP + percentWidth);
		Component[] alerts = new Component[model.incidents().size()];
		for (int i = 0; i < alerts.length; i++) {
			ShipHudModel.IncidentRow row = model.incidents().get(i);
			alerts[i] = Component.translatable("message.intersolarsystem.ship.alert_line",
					Component.translatable(ShipCommands.incidentKey(row.type())),
					Component.translatable(ShipCommands.subsystemKey(row.subsystem())), row.clock());
			widest = Math.max(widest, font.width(alerts[i]));
		}

		int rows = 1 + model.subsystems().size() + alerts.length;
		graphics.fill(X, Y, X + widest + 2 * PADDING, Y + rows * lineHeight + 2 * PADDING, BACKGROUND);

		int y = Y + PADDING;
		graphics.text(font, phase, X + PADDING, y, TEXT);
		y += lineHeight;
		int barX = X + PADDING + nameWidth + GAP;
		for (ShipHudModel.SubsystemRow row : model.subsystems()) {
			graphics.text(font, Component.translatable(ShipCommands.subsystemKey(row.subsystem())), X + PADDING, y, TEXT);
			graphics.fill(barX, y + 1, barX + BAR_WIDTH, y + font.lineHeight, BAR_BACKGROUND);
			int filled = BAR_WIDTH * row.percent() / 100;
			graphics.fill(barX, y + 1, barX + filled, y + font.lineHeight, severityColor(row.severity()));
			graphics.text(font, row.percent() + "%", barX + BAR_WIDTH + GAP, y, TEXT);
			y += lineHeight;
		}
		for (int i = 0; i < alerts.length; i++) {
			boolean hidden = model.incidents().get(i).blinking() && (now / 5) % 2 == 1;
			if (!hidden) {
				graphics.text(font, alerts[i], X + PADDING, y, ALERT);
			}
			y += lineHeight;
		}
	}

	private static int severityColor(ShipHudModel.Severity severity) {
		return switch (severity) {
			case SAFE -> 0xFF55FF55;
			case WARN -> 0xFFFFAA00;
			case CRITICAL -> 0xFFFF5555;
		};
	}
}
