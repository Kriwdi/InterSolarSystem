package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.InterSolarSystem;
import hu.kriwdi.intersolarsystem.content.FlightPanelBlock;
import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ThrottleBlock;
import hu.kriwdi.intersolarsystem.travel.CelestialBody;
import hu.kriwdi.intersolarsystem.travel.SolarSystem;

/**
 * The launch checklist: a course chosen at the navigation console, the four switches of the panel on, the pilot in
 * the pilot's seat, then the throttle pushed to full thrust. After every landing the switches go off and the lever
 * goes back, so each flight starts with the checklist again.
 */
public final class FlightControls {
	private static String course = "";

	private FlightControls() {
	}

	public static void reset() {
		course = "";
	}

	public static String course() {
		return course;
	}

	/** The navigation console sets the course (it does not launch any more). */
	public static void plotCourse(MinecraftServer server, ServerPlayer player, String bodyId) {
		ShipState state = ShipStateAccess.get(server);
		Optional<CelestialBody> body = SolarSystem.byId(bodyId);
		if (body.isEmpty() || !ShipNavigation.reachable(body.get(), ShipNavigation.here(state))) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.nav.unreachable")
					.withStyle(ChatFormatting.RED));
			return;
		}
		course = bodyId;
		InterSolarSystem.LOGGER.info("[nav] {} set the course to {}", player.getName().getString(), bodyId);
		ShipFlightController.broadcast(server, Component.translatable("message.intersolarsystem.nav.course_set",
				Component.translatable(body.get().translationKey())));
		List<Component> missing = missing(server, player, false);
		if (!missing.isEmpty()) {
			ShipFlightController.broadcast(server, checklist(missing));
		}
	}

	/** The throttle reached full thrust: launch if everything is ready. Returns whether the takeoff started. */
	public static boolean throttleUp(MinecraftServer server, ServerPlayer player) {
		List<Component> missing = missing(server, player, true);
		if (!missing.isEmpty()) {
			player.sendSystemMessage(checklist(missing));
			return false;
		}
		String target = course;
		boolean started = ShipNavigation.launch(server, player, target);
		if (started) {
			course = "";
		}
		return started;
	}

	/** The steps still missing before launch (empty when ready). */
	public static List<Component> missing(MinecraftServer server, ServerPlayer player, boolean needPilot) {
		List<Component> missing = new ArrayList<>();
		if (course.isEmpty()) {
			missing.add(Component.translatable("message.intersolarsystem.checklist.course"));
		}
		BlockState panel = interiorState(server, ShipInteriorLayout.capsule().flightPanel());
		if (panel != null && panel.is(ModBlocks.FLIGHT_PANEL)) {
			for (int i = 0; i < FlightPanelBlock.SWITCHES.length; i++) {
				if (!panel.getValue(FlightPanelBlock.SWITCHES[i])) {
					missing.add(Component.translatable("message.intersolarsystem.checklist.switch",
							Component.translatable("message.intersolarsystem.switch.name." + FlightPanelBlock.NAMES[i])));
				}
			}
		}
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior != null) {
			missing.addAll(ShipNetworks.problems(interior));
		}
		if (needPilot && ShipManeuver.pilot(server) != player) {
			missing.add(Component.translatable("message.intersolarsystem.checklist.seat"));
		}
		return missing;
	}

	private static Component checklist(List<Component> missing) {
		MutableComponent text = Component.translatable("message.intersolarsystem.checklist.header")
				.withStyle(ChatFormatting.GOLD);
		for (Component line : missing) {
			text.append(Component.literal("\n - ")).append(line);
		}
		return text;
	}

	private static BlockState interiorState(MinecraftServer server, ShipInteriorLayout.Cell cell) {
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		return interior == null ? null : interior.getBlockState(ShipInteriorBuilder.at(cell));
	}

	/** After a landing: switches off, throttle back, course cleared. */
	public static void powerDown(MinecraftServer server) {
		course = "";
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior == null) {
			return;
		}
		BlockPos panel = ShipInteriorBuilder.at(ShipInteriorLayout.capsule().flightPanel());
		BlockState p = interior.getBlockState(panel);
		if (p.is(ModBlocks.FLIGHT_PANEL)) {
			for (var s : FlightPanelBlock.SWITCHES) {
				p = p.setValue(s, false);
			}
			interior.setBlock(panel, p, Block.UPDATE_ALL);
		}
		BlockPos throttle = ShipInteriorBuilder.at(ShipInteriorLayout.capsule().throttle());
		BlockState t = interior.getBlockState(throttle);
		if (t.is(ModBlocks.THROTTLE)) {
			interior.setBlock(throttle, t.setValue(ThrottleBlock.LEVEL, 0), Block.UPDATE_ALL);
		}
	}
}
