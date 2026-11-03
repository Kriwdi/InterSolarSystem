package hu.kriwdi.intersolarsystem.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.kriwdi.intersolarsystem.client.NavigationScreen;
import hu.kriwdi.intersolarsystem.content.FlightPanelBlock;
import hu.kriwdi.intersolarsystem.ship.FlightControls;
import hu.kriwdi.intersolarsystem.ship.ShipBoarding;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipFlightPhase;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorBuilder;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout;
import hu.kriwdi.intersolarsystem.ship.ShipNavigation;
import hu.kriwdi.intersolarsystem.ship.ShipIncident;
import hu.kriwdi.intersolarsystem.ship.ShipIncidentType;
import hu.kriwdi.intersolarsystem.ship.ShipState;
import hu.kriwdi.intersolarsystem.ship.ShipSubsystem;
import hu.kriwdi.intersolarsystem.ship.ShipStateAccess;

/**
 * Plays the ship like a player would, in a new dead Earth world: looks at the landed ship, boards it, walks the
 * rooms, opens the navigation console and clicks the Moon and LAUNCH with the mouse, rides the takeoff, flight and
 * landing (screenshots of the view through the windows), and leaves the ship on the Moon. The log says whether the
 * mouse clicks really launched the ship.
 */
public class ShipJourney implements FabricClientGameTest {
	private static final Logger LOGGER = LoggerFactory.getLogger("intersolarsystem-journey");

	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext game = context.worldBuilder().setUseConsistentSettings(false).create()) {
			context.waitTicks(60);
			game.getServer().runCommand("gamemode creative @a");
			game.getServer().runCommand("time set 6000");
			context.runOnClient(client -> client.options.renderDistance().set(8));
			context.getInput().resizeWindow(1280, 720);
			BlockPos hatch = game.getServer().computeOnServer(server -> ShipStateAccess.get(server).landingSite()
					.map(s -> new BlockPos(s.x(), s.y(), s.z())).orElse(null));
			if (hatch == null) {
				LOGGER.error("[JOURNEY] the new world has no landed ship");
				return;
			}
			LOGGER.info("[JOURNEY] ship hatch at {}", hatch.toShortString());

			// The ship from outside (the hatch faces west, the hull runs north-south east of it).
			tp(game, "minecraft:overworld", hatch.getX() - 22, hatch.getY() + 7, hatch.getZ() - 20, hatch.getX() + 3,
					hatch.getY() + 10, hatch.getZ());
			context.waitTicks(140);
			context.takeScreenshot("40_ship_outside");
			tp(game, "minecraft:overworld", hatch.getX() + 24, hatch.getY() + 4, hatch.getZ() + 20, hatch.getX() + 3,
					hatch.getY() + 10, hatch.getZ());
			context.waitTicks(60);
			context.takeScreenshot("41_ship_outside_stern");

			// Board through the hatch and look around.
			game.getServer().runOnServer(server -> ShipBoarding.use(server.overworld(), hatch, player(server)));
			context.waitTicks(60);
			context.takeScreenshot("42_ship_boarded");
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 15.5, 4.5, 67, 20.5);
			context.waitTicks(40);
			context.takeScreenshot("43_bridge_view_earth");
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 17.5, 4.5, 63.5, 21.5);
			context.waitTicks(20);
			context.takeScreenshot("43b_bridge_looking_down");
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 9.5, 4.5, 65.5, 1.5);
			context.waitTicks(30);
			context.takeScreenshot("44_toward_engine_room");
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 8.5, 4.5, 65.5, 20.5);
			context.waitTicks(30);
			context.takeScreenshot("45_toward_bridge");

			// Incidents show where they happen: one of each kind, seen from the engine room's doorway.
			game.getServer().runOnServer(server -> {
				ShipState st = ShipStateAccess.get(server);
				long t = server.overworld().getGameTime() + 6000;
				st.addIncident(new ShipIncident(ShipSubsystem.ENGINE, ShipIncidentType.OVERHEAT, t));
				st.addIncident(new ShipIncident(ShipSubsystem.LANDING_LAUNCH, ShipIncidentType.FIRE, t));
			});
			context.waitTicks(30);
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 7.5, 0.5, 66, 3.5);
			context.waitTicks(30);
			context.takeScreenshot("44b_fault_steam_vent");
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 7.5, 7.5, 65.2, 3.5);
			context.waitTicks(15);
			context.takeScreenshot("44c_fault_fire");
			// Put the fire out with the extinguisher (use key), then clear the rest.
			game.getServer().runCommand("item replace entity @a weapon.mainhand with intersolarsystem:fire_extinguisher");
			tp(game, "intersolarsystem:ship_interior", 5.5, 65, 5.5, 7.5, 65.3, 3.5);
			context.waitTicks(5);
			context.getInput().lookAt(new BlockPos(7, 65, 3));
			context.waitTicks(5);
			context.getInput().pressKey(options -> options.keyUse);
			context.waitTicks(10);
			int left = game.getServer().computeOnServer(server -> ShipStateAccess.get(server).hazards().incidents().size());
			LOGGER.info("[JOURNEY] incidents after the extinguisher: {} (was 2)", left);
			game.getServer().runOnServer(server -> {
				ShipState st = ShipStateAccess.get(server);
				for (ShipIncident i : java.util.List.copyOf(st.hazards().incidents())) {
					st.resolveIncident(i);
				}
			});
			game.getServer().runCommand("item replace entity @a weapon.mainhand with minecraft:air");
			context.waitTicks(20);

			// Sit down in the pilot's seat with the use key, look at the consoles, get up again with sneak.
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 14.5, 4.5, 65.4, 16.5);
			context.waitTicks(5);
			context.getInput().lookAt(new BlockPos(4, 65, 16));
			context.waitTicks(5);
			context.getInput().pressKey(options -> options.keyUse);
			context.waitTicks(20);
			boolean sitting = game.getServer().computeOnServer(server -> player(server).isPassenger());
			LOGGER.info("[JOURNEY] sitting in the seat: {}", sitting);
			context.takeScreenshot("45c_sitting_in_the_seat");
			context.getInput().holdKeyFor(options -> options.keyShift, 10);
			context.waitTicks(10);
			boolean stillSitting = game.getServer().computeOnServer(server -> player(server).isPassenger());
			LOGGER.info("[JOURNEY] after sneaking still sitting: {}", stillSitting);

			// The navigation console, clicked with the mouse: select the Moon, then LAUNCH.
			game.getServer().runCommand("iss ship engine 4");
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 16.5, 4.5, 66, 20.5);
			context.waitTicks(10);
			// Opened like a player does: looking at the console and pressing the use key (right click).
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 17.3, 4.5, 65.6, 18.5);
			context.waitTicks(10);
			context.getInput().lookAt(new BlockPos(4, 65, 18));
			context.waitTicks(5);
			context.takeScreenshot("45b_aim_at_console");
			String aim = context.computeOnClient(client -> client.hitResult == null ? "nothing"
					: client.hitResult.getType() + " " + (client.hitResult instanceof net.minecraft.world.phys.BlockHitResult b
							? b.getBlockPos() + " " + client.level.getBlockState(b.getBlockPos()) : ""));
			LOGGER.info("[JOURNEY] aiming at {}", aim);
			context.getInput().pressKey(options -> options.keyUse);
			try {
				context.waitForScreen(NavigationScreen.class);
				LOGGER.info("[JOURNEY] the use key opened the navigation console");
			} catch (AssertionError e) {
				LOGGER.error("[JOURNEY] the use key did not open the console; opening it directly");
				game.getServer().runOnServer(server -> ShipNavigation.open(server, player(server)));
				context.waitForScreen(NavigationScreen.class);
			}
			context.waitTicks(10);
			context.takeScreenshot("46_nav_open");
			click(context, true);
			int selectedRow = context.computeOnClient(client -> NavigationScreen.current.selectedIndex());
			LOGGER.info("[JOURNEY] after clicking the Moon row the selected row is {}", selectedRow);
			context.waitTicks(5);
			context.takeScreenshot("47_nav_moon_selected");
			click(context, false);
			context.waitTicks(10);
			String course = game.getServer().computeOnServer(server -> FlightControls.course());
			LOGGER.info("[JOURNEY] after clicking SET COURSE the course is '{}'", course);
			// The checklist: all switches on, sit in the pilot's seat, push the throttle three notches.
			game.getServer().runOnServer(server -> {
				var interior = server.getLevel(ShipDimensions.INTERIOR);
				var pos = ShipInteriorBuilder.at(ShipInteriorLayout.capsule().flightPanel());
				var panel = interior.getBlockState(pos);
				for (var sw : FlightPanelBlock.SWITCHES) {
					panel = panel.setValue(sw, true);
				}
				interior.setBlock(pos, panel, 3);
			});
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 14.5, 4.5, 65.4, 16.5);
			context.waitTicks(5);
			context.getInput().lookAt(new BlockPos(4, 65, 16));
			context.waitTicks(5);
			context.getInput().pressKey(options -> options.keyUse);
			context.waitTicks(15);
			context.getInput().lookAt(new BlockPos(5, 65, 17));
			context.waitTicks(5);
			context.takeScreenshot("47b_pilot_controls");
			String missing = game.getServer().computeOnServer(server -> String.valueOf(FlightControls.missing(server,
					player(server), true).stream().map(c -> c.getString()).toList()));
			LOGGER.info("[JOURNEY] checklist before the throttle: {}", missing);
			for (int i = 0; i < 3; i++) {
				context.getInput().pressKey(options -> options.keyUse);
				context.waitTicks(6);
			}
			context.waitTicks(10);
			String phase = game.getServer().computeOnServer(server -> ShipStateAccess.get(server).phase().name());
			LOGGER.info("[JOURNEY] after the throttle the ship is {}", phase);
			if (!"TAKEOFF".equals(phase)) {
				LOGGER.error("[JOURNEY] the checklist did not launch the ship; launching directly");
				game.getServer().runOnServer(server -> ShipNavigation.launch(server, player(server), "moon"));
			}
			context.waitTicks(120);
			context.takeScreenshot("48_takeoff_countdown");
			waitForPhase(context, game, ShipFlightPhase.FLIGHT);
			context.waitTicks(25);
			context.takeScreenshot("49_liftoff");
			// A meteor shower on the way: the pilot holds D (steer right) for a while.
			game.getServer().runCommand("iss ship meteors");
			context.getInput().lookAt(new BlockPos(4, 66, 21));
			context.getInput().holdKeyFor(options -> options.keyRight, 60);
			context.waitTicks(100);
			context.takeScreenshot("50_meteor_shower");
			context.waitTicks(20);
			context.takeScreenshot("51_meteor_shower_close");
			waitForPhase(context, game, ShipFlightPhase.LANDING);
			context.waitTicks(30);
			context.takeScreenshot("52_landing");
			waitForPhase(context, game, ShipFlightPhase.LANDED);
			context.waitTicks(40);
			context.takeScreenshot("53_moon_through_window");
			tp(game, "intersolarsystem:ship_interior", 4.5, 65, 17.5, 4.5, 63.5, 21.5);
			context.waitTicks(20);
			context.takeScreenshot("53b_moon_ground_through_window");
			String landed = game.getServer().computeOnServer(
					server -> String.valueOf(ShipStateAccess.get(server).landingSite()));
			LOGGER.info("[JOURNEY] landed: {}", landed);

			// Leave the ship on the Moon and look back at it.
			game.getServer().runOnServer(server -> ShipBoarding.use(server.getLevel(ShipDimensions.INTERIOR),
					ShipInteriorBuilder.at(ShipInteriorLayout.capsule().hatch()), player(server)));
			context.waitTicks(40);
			BlockPos moonHatch = game.getServer().computeOnServer(server -> ShipStateAccess.get(server).landingSite()
					.map(s -> new BlockPos(s.x(), s.y(), s.z())).orElse(BlockPos.ZERO));
			tp(game, "intersolarsystem:moon", moonHatch.getX() + 14, moonHatch.getY() + 6, moonHatch.getZ() - 22,
					moonHatch.getX(), moonHatch.getY() + 10, moonHatch.getZ() + 3);
			context.waitTicks(120);
			context.takeScreenshot("54_ship_on_the_moon");
			tp(game, "intersolarsystem:moon", moonHatch.getX() - 22, moonHatch.getY() + 9, moonHatch.getZ() + 14,
					moonHatch.getX(), moonHatch.getY() + 10, moonHatch.getZ() + 3);
			context.waitTicks(40);
			context.takeScreenshot("54b_ship_on_the_moon_side");

			// Back aboard and up to the ISS: the ship docks at the station's front port.
			game.getServer().runOnServer(server -> ShipBoarding.use(server.getLevel(ShipDimensions.MOON), moonHatch,
					player(server)));
			context.waitTicks(20);
			game.getServer().runOnServer(server -> ShipNavigation.launch(server, player(server), "earth_orbit"));
			waitForPhase(context, game, ShipFlightPhase.FLIGHT);
			waitForPhase(context, game, ShipFlightPhase.LANDED);
			context.waitTicks(20);
			BlockPos dock = game.getServer().computeOnServer(server -> ShipStateAccess.get(server).landingSite()
					.map(s -> new BlockPos(s.x(), s.y(), s.z())).orElse(BlockPos.ZERO));
			LOGGER.info("[JOURNEY] docked at {}", dock.toShortString());
			game.getServer().runOnServer(server -> ShipBoarding.use(server.getLevel(ShipDimensions.INTERIOR),
					ShipInteriorBuilder.at(ShipInteriorLayout.capsule().hatch()), player(server)));
			context.waitTicks(20);
			game.getServer().runCommand("gamemode spectator @a");
			tp(game, "intersolarsystem:orbit", dock.getX() + 26, dock.getY() + 10, dock.getZ() - 22, dock.getX(),
					dock.getY() + 10, dock.getZ() - 3);
			context.waitTicks(100);
			context.takeScreenshot("55_docked_at_the_iss");
			tp(game, "intersolarsystem:orbit", dock.getX() - 30, dock.getY() + 24, dock.getZ() - 40, dock.getX(),
					dock.getY(), dock.getZ() + 20);
			context.waitTicks(40);
			context.takeScreenshot("56_iss_and_ship");
		}
	}

	private static ServerPlayer player(MinecraftServer server) {
		return server.getPlayerList().getPlayers().get(0);
	}

	private static void tp(TestSingleplayerContext game, String dimension, double x, double y, double z, double fx,
			double fy, double fz) {
		game.getServer().runCommand("execute in " + dimension + " run tp @a " + x + " " + y + " " + z + " facing " + fx
				+ " " + fy + " " + fz);
	}

	/** Clicks the Moon row or the launch button of the open star map with the mouse. */
	private static void click(ClientGameTestContext context, boolean moonRow) {
		double[] at = context.computeOnClient(client -> {
			NavigationScreen screen = NavigationScreen.current;
			int[] p = moonRow ? screen.rowCentre(2) : screen.launchCentre();
			double scale = (double) client.getWindow().getWidth() / client.getWindow().getGuiScaledWidth();
			return new double[] {p[0] * scale, p[1] * scale};
		});
		context.getInput().setCursorPos(at[0], at[1]);
		context.waitTicks(2);
		// Left button: 26.x numbers mouse buttons from 1.
		context.getInput().pressMouse(1);
	}

	private static void waitForPhase(ClientGameTestContext context, TestSingleplayerContext game, ShipFlightPhase phase) {
		for (int i = 0; i < 120; i++) {
			ShipFlightPhase now = game.getServer().computeOnServer(server -> ShipStateAccess.get(server).phase());
			if (now == phase) {
				return;
			}
			context.waitTicks(10);
		}
		LOGGER.error("[JOURNEY] the ship never reached {}", phase);
	}

	@SuppressWarnings("unused")
	private static ShipState state(MinecraftServer server) {
		return ShipStateAccess.get(server);
	}
}
