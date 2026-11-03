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
import hu.kriwdi.intersolarsystem.ship.ShipBoarding;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipFlightPhase;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorBuilder;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout;
import hu.kriwdi.intersolarsystem.ship.ShipNavigation;
import hu.kriwdi.intersolarsystem.ship.ShipState;
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
			tp(game, "minecraft:overworld", hatch.getX() - 16, hatch.getY() + 5, hatch.getZ() - 16, hatch.getX() + 4,
					hatch.getY() + 2, hatch.getZ());
			context.waitTicks(140);
			context.takeScreenshot("40_ship_outside");
			tp(game, "minecraft:overworld", hatch.getX() + 20, hatch.getY() + 7, hatch.getZ() + 18, hatch.getX() + 4,
					hatch.getY() + 2, hatch.getZ());
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
			String phase = game.getServer().computeOnServer(server -> ShipStateAccess.get(server).phase().name());
			LOGGER.info("[JOURNEY] after clicking LAUNCH the ship is {}", phase);
			if (!"TAKEOFF".equals(phase)) {
				LOGGER.error("[JOURNEY] the mouse clicks did not launch the ship; launching directly");
				context.setScreen(() -> null);
				game.getServer().runOnServer(server -> ShipNavigation.launch(server, player(server), "moon"));
			}
			context.waitTicks(120);
			context.takeScreenshot("48_takeoff_countdown");
			waitForPhase(context, game, ShipFlightPhase.FLIGHT);
			context.waitTicks(25);
			context.takeScreenshot("49_liftoff");
			context.waitTicks(50);
			context.takeScreenshot("50_climbing");
			context.waitTicks(80);
			context.takeScreenshot("51_cruise");
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
			tp(game, "intersolarsystem:moon", moonHatch.getX() + 6, moonHatch.getY() + 5, moonHatch.getZ() - 18,
					moonHatch.getX() - 4, moonHatch.getY() + 2, moonHatch.getZ() + 6);
			context.waitTicks(120);
			context.takeScreenshot("54_ship_on_the_moon");
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
