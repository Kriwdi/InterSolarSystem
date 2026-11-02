package hu.kriwdi.intersolarsystem.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.client.CameraType;
import net.minecraft.server.level.ServerLevel;

import hu.kriwdi.intersolarsystem.client.NavigationScreen;
import hu.kriwdi.intersolarsystem.client.repair.PressureScreen;
import hu.kriwdi.intersolarsystem.client.repair.ValveScreen;
import hu.kriwdi.intersolarsystem.client.repair.WiringScreen;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorBuilder;
import hu.kriwdi.intersolarsystem.space.MoonSites;
import hu.kriwdi.intersolarsystem.space.OrbitStation;

/**
 * Not a pass/fail test: it starts a real client, builds the mod's scenes and takes screenshots, so the art, the screens
 * and the sky can be checked without playing. CI publishes the screenshots to the ci-screens branch.
 */
public class SceneScreenshots implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext game = context.worldBuilder().create()) {
			context.waitTicks(60);
			game.getServer().runCommand("gamemode creative @a");
			game.getServer().runCommand("gamerule doDaylightCycle false");
			game.getServer().runCommand("effect give @a minecraft:night_vision infinite 0 true");

			// Ship interior with the consoles, hatch and HUD.
			game.getServer().runOnServer(server -> {
				ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
				ShipInteriorBuilder.build(interior, true);
				ShipInteriorBuilder.ensureStation(interior);
			});
			game.getServer().runCommand("execute in intersolarsystem:ship_interior run tp @a 4.5 65 1.6 0 22");
			context.waitTicks(80);
			context.takeScreenshot("01_ship_interior");
			game.getServer().runCommand("execute in intersolarsystem:ship_interior run tp @a 4.5 65 4.2 180 18");
			context.waitTicks(40);
			context.takeScreenshot("02_ship_hatch");

			// The ship's screens.
			context.setScreen(() -> new NavigationScreen(1, 30_000L, true, "earth"));
			context.waitTicks(20);
			context.takeScreenshot("03_navigation");
			context.setScreen(() -> new WiringScreen(42L, 5));
			context.waitTicks(5);
			context.takeScreenshot("04_wiring");
			context.setScreen(() -> new ValveScreen(42L, 3));
			context.waitTicks(30);
			context.takeScreenshot("05_valve");
			context.setScreen(() -> new PressureScreen(42L, 4));
			context.waitTicks(5);
			context.takeScreenshot("06_pressure");
			context.setScreen(() -> null);

			// Earth orbit: the ISS, the Earth below, and the night sky.
			game.getServer().runOnServer(server -> OrbitStation.build(server.getLevel(ShipDimensions.ORBIT)));
			game.getServer().runCommand("time set noon");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a 14 128 -38 25 18");
			context.waitTicks(120);
			context.takeScreenshot("07_orbit_iss");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a 14 128 -38 25 80");
			context.waitTicks(20);
			context.takeScreenshot("08_orbit_earth_below");
			game.getServer().runCommand("time set midnight");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a 14 128 -38 25 -30");
			context.waitTicks(40);
			context.takeScreenshot("09_orbit_night_sky");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a 0 118 -10 180 0");
			game.getServer().runCommand("time set noon");
			context.waitTicks(40);
			context.takeScreenshot("10_iss_inside");

			// The space suit, seen from the front.
			game.getServer().runCommand("item replace entity @a armor.head with intersolarsystem:space_helmet");
			game.getServer().runCommand("item replace entity @a armor.chest with intersolarsystem:space_suit");
			game.getServer().runCommand("item replace entity @a armor.legs with intersolarsystem:space_pants");
			game.getServer().runCommand("item replace entity @a armor.feet with intersolarsystem:space_boots");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a 0 118 -20 0 10");
			context.runOnClient(client -> client.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
			context.waitTicks(30);
			context.takeScreenshot("11_space_suit");
			context.runOnClient(client -> client.options.setCameraType(CameraType.FIRST_PERSON));

			// The Moon: Apollo 11, Tranquility Base, the landscape and the Earth in the sky (lunar noon, then night).
			game.getServer().runOnServer(server -> MoonSites.ensureBuilt(server.getLevel(ShipDimensions.MOON)));
			game.getServer().runCommand("time set 17700");
			game.getServer().runCommand("effect clear @a minecraft:night_vision"); // real lunar lighting
			MoonSites.Apollo a11 = MoonSites.APOLLO.get(0);
			int y = surfaceY(game, a11.x(), a11.z());
			game.getServer().runCommand("execute in intersolarsystem:moon run tp @a " + (a11.x() + 7) + " " + (y + 2)
					+ " " + (a11.z() + 6) + " 135 20");
			context.waitTicks(160);
			context.takeScreenshot("12_moon_apollo11");
			game.getServer().runCommand("execute in intersolarsystem:moon run tp @a " + (MoonSites.BASE[0] - 16) + " "
					+ (surfaceY(game, MoonSites.BASE[0] - 16, MoonSites.BASE[1] - 14) + 6) + " " + (MoonSites.BASE[1] - 14) + " -45 25");
			context.waitTicks(80);
			context.takeScreenshot("13_moon_tranquility_base");
			game.getServer().runCommand("execute in intersolarsystem:moon run tp @a " + a11.x() + " " + (y + 40) + " "
					+ a11.z() + " 0 35");
			context.waitTicks(120);
			context.takeScreenshot("14_moon_landscape");
			for (int yaw : new int[] {0, 90, 180, 270}) {
				game.getServer().runCommand("execute in intersolarsystem:moon run tp @a " + a11.x() + " " + (y + 4) + " "
						+ a11.z() + " " + yaw + " -45");
				context.waitTicks(20);
				context.takeScreenshot("15_moon_sky_yaw" + yaw);
			}
			game.getServer().runCommand("time set 53100");
			context.waitTicks(40);
			context.takeScreenshot("16_moon_night_sky");
		}
	}

	/** Surface height of a Moon column. Level#getHeight does not load chunks (it returns the world bottom), so load first. */
	private static int surfaceY(TestSingleplayerContext game, int x, int z) {
		return game.getServer().computeOnServer(server -> {
			return MoonSites.surface(server.getLevel(ShipDimensions.MOON), x, z).getY();
		});
	}
}
