package hu.kriwdi.intersolarsystem.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;

import hu.kriwdi.intersolarsystem.client.NavigationScreen;
import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.client.repair.PressureScreen;
import hu.kriwdi.intersolarsystem.client.repair.ValveScreen;
import hu.kriwdi.intersolarsystem.client.repair.WiringScreen;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.space.MoonSites;
import hu.kriwdi.intersolarsystem.space.OrbitStation;

/**
 * Not a pass/fail test: it starts a real client, builds the mod's scenes and takes screenshots, so the art, the screens
 * and the sky can be checked without playing. CI publishes the screenshots to the ci-screens branch.
 */
public class SceneScreenshots implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext game = context.worldBuilder().setUseConsistentSettings(false).create()) {
			context.waitTicks(60);
			game.getServer().runCommand("gamemode creative @a");
			game.getServer().runCommand("gamerule doDaylightCycle false");
			game.getServer().runCommand("effect give @a minecraft:night_vision infinite 0 true");

			// The ship's screens.
			context.setScreen(() -> new NavigationScreen(1, 30_000L, "", "earth"));
			context.waitTicks(20);
			context.takeScreenshot("03_navigation");
			context.setScreen(() -> new NavigationScreen(1, 30_000L, "command.intersolarsystem.ship.fly_too_damaged",
					"earth"));
			context.waitTicks(10);
			context.takeScreenshot("03b_navigation_blocked");
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

			// The story: a note, a manual, a terminal, a data tape playing.
			context.runOnClient(client -> hu.kriwdi.intersolarsystem.client.lore.LoreClient.open("notes", "bunker7"));
			context.waitTicks(10);
			context.takeScreenshot("06b_lore_note");
			context.runOnClient(client -> hu.kriwdi.intersolarsystem.client.lore.LoreClient.open("manuals", "launch_checklist"));
			context.waitTicks(10);
			context.takeScreenshot("06c_lore_manual");
			context.runOnClient(client -> hu.kriwdi.intersolarsystem.client.lore.LoreClient.open("terminals", "terminal_9"));
			context.waitTicks(10);
			context.takeScreenshot("06d_lore_terminal");
			context.setScreen(() -> null);
			context.runOnClient(client -> hu.kriwdi.intersolarsystem.client.lore.LoreClient.open("tapes", "mars_last"));
			context.waitTicks(80);
			context.takeScreenshot("06e_lore_tape");

			// Skeletons in a row on a concrete plate (teleport first so the chunks are loaded).
			context.runOnClient(client -> hu.kriwdi.intersolarsystem.client.lore.TapePlayer.stop());
			game.getServer().runCommand("execute in minecraft:overworld run tp @a 15 225 -6 0 35");
			context.waitTicks(60);
			game.getServer().runCommand("fill 0 219 0 30 219 8 minecraft:gray_concrete");
			game.getServer().runCommand("fill 0 220 0 30 224 8 minecraft:air");
			String[] skeletons = {"skeleton_lying", "skeleton_sitting", "skeleton_curled", "skeleton_suited", "dog_skeleton",
					"cow_skeleton", "horse_skeleton"};
			for (int i = 0; i < skeletons.length; i++) {
				game.getServer().runCommand("setblock " + (2 + i * 4) + " 220 3 intersolarsystem:" + skeletons[i] + "[facing=north]");
			}
			context.waitTicks(40);
			context.takeScreenshot("06f_skeletons");

			// Earth orbit: the ISS, the Earth below, and the night sky.
			game.getServer().runOnServer(server -> OrbitStation.build(server.getLevel(ShipDimensions.ORBIT)));
			game.getServer().runCommand("time set noon");
			game.getServer().runCommand("gamemode spectator @a");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a 48 140 -66 facing 0 121 -10");
			context.waitTicks(120);
			context.takeScreenshot("07_orbit_iss");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a 30 112 -70 -20 70");
			context.waitTicks(20);
			context.takeScreenshot("08_orbit_earth_below");
			game.getServer().runCommand("time set midnight");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a 48 140 -66 30 -30");
			context.waitTicks(40);
			context.takeScreenshot("09_orbit_night_sky");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a 0 119 -27 0 5");
			game.getServer().runCommand("time set noon");
			context.waitTicks(40);
			context.takeScreenshot("10_iss_inside");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a -6 116 -9 30 80");
			context.waitTicks(20);
			context.takeScreenshot("10b_iss_cupola");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a -40 135 -60 facing -40 126 -16");
			context.waitTicks(30);
			context.takeScreenshot("10c_iss_solar_wings");
			game.getServer().runCommand("gamemode creative @a");

			// The space suit, seen from the front.
			game.getServer().runCommand("item replace entity @a armor.head with intersolarsystem:space_helmet");
			game.getServer().runCommand("item replace entity @a armor.chest with intersolarsystem:space_suit");
			game.getServer().runCommand("item replace entity @a armor.legs with intersolarsystem:space_pants");
			game.getServer().runCommand("item replace entity @a armor.feet with intersolarsystem:space_boots");
			game.getServer().runCommand("execute in intersolarsystem:orbit run tp @a 0 119 -17 0 10");
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
			game.getServer().runCommand("execute in intersolarsystem:moon run tp @a " + a11.x() + " " + (y + 18) + " "
					+ (a11.z() - 30) + " 0 25");
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

			// The dead Earth: every biome and the main structures, at noon.
			game.getServer().runCommand("time set 6000");
			game.getServer().runCommand("effect give @a minecraft:night_vision infinite 0 true");
			// Spectators fly: the camera stays where it is put instead of falling to the ground.
			game.getServer().runCommand("gamemode spectator @a");
			context.runOnClient(client -> client.options.renderDistance().set(10));
			for (var biome : hu.kriwdi.intersolarsystem.worldgen.DeadEarth.ALL) {
				BlockPos at = game.getServer().computeOnServer(server -> {
					ServerLevel earth = server.overworld();
					var hit = earth.findClosestBiome3d(h -> h.is(biome), BlockPos.ZERO, 4000, 32, 64);
					if (hit == null) {
						return null;
					}
					// Step 40 blocks further in, away from the biome's edge nearest to the origin.
					BlockPos p = hit.getFirst();
					double len = Math.max(1.0, Math.sqrt((double) p.getX() * p.getX() + (double) p.getZ() * p.getZ()));
					int x = p.getX() + (int) Math.round(p.getX() / len * 40);
					int z = p.getZ() + (int) Math.round(p.getZ() / len * 40);
					return MoonSites.surface(earth, x, z);
				});
				if (at == null) {
					continue;
				}
				game.getServer().runCommand("execute in minecraft:overworld run tp @a " + at.getX() + " " + (at.getY() + 16)
						+ " " + at.getZ() + " -45 30");
				context.waitTicks(160);
				context.takeScreenshot("20_earth_" + biome.identifier().getPath());
			}
			for (String name : new String[] {"dead_city", "space_center", "hospital", "seed_bank", "colony_overgrown",
					"oil_pumpjack", "dry_shipwreck", "oasis_refuge", "terraformer", "farmhouse", "radio_tower", "airport", "barracks"}) {
				BlockPos at = game.getServer().computeOnServer(server -> {
					ServerLevel earth = server.overworld();
					var structure = server.registryAccess().lookupOrThrow(Registries.STRUCTURE)
							.getOrThrow(ResourceKey.create(Registries.STRUCTURE, ModIds.id(name)));
					BlockPos found = earth.findNearestMapStructure(HolderSet.direct(structure), BlockPos.ZERO, 120, false);
					return found == null ? null : MoonSites.surface(earth, found.getX() - 14, found.getZ() - 14);
				});
				if (at == null) {
					continue;
				}
				game.getServer().runCommand("execute in minecraft:overworld run tp @a " + at.getX() + " " + (at.getY() + 18)
						+ " " + at.getZ() + " -45 32");
				context.waitTicks(160);
				context.takeScreenshot("30_structure_" + name);
			}
		}
	}

	/** Surface height of a Moon column. Level#getHeight does not load chunks (it returns the world bottom), so load first. */
	private static int surfaceY(TestSingleplayerContext game, int x, int z) {
		return game.getServer().computeOnServer(server -> {
			return MoonSites.surface(server.getLevel(ShipDimensions.MOON), x, z).getY();
		});
	}
}
