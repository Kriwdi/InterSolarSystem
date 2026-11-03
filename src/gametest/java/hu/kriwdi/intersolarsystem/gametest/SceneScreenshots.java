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
			context.runOnClient(client -> hu.kriwdi.intersolarsystem.client.lore.TapePlayer.play(
					hu.kriwdi.intersolarsystem.client.lore.LoreLibrary.get("tapes", "mars_last"), client.player.getX(),
					client.player.getY(), client.player.getZ()));
			context.waitTicks(80);
			context.takeScreenshot("06e_lore_tape");

			// Skeletons in a row on a concrete plate (teleport first so the chunks are loaded).
			context.runOnClient(client -> hu.kriwdi.intersolarsystem.client.lore.TapePlayer.stop());
			game.getServer().runCommand("execute in minecraft:overworld run tp @a 15 221 -5 0 30");
			context.waitTicks(60);
			game.getServer().runCommand("fill 0 219 -8 30 219 8 minecraft:gray_concrete");
			game.getServer().runCommand("fill 0 220 -8 30 224 8 minecraft:air");
			String[] skeletons = {"skeleton_lying", "skeleton_sitting", "skeleton_curled", "skeleton_suited", "dog_skeleton",
					"cow_skeleton", "horse_skeleton"};
			for (int i = 0; i < skeletons.length; i++) {
				game.getServer().runCommand("setblock " + (2 + i * 4) + " 220 3 intersolarsystem:" + skeletons[i] + "[facing=north]");
			}
			game.getServer().runCommand("execute in minecraft:overworld run tp @a 15 221 -5 0 30");
			context.waitTicks(40);
			context.takeScreenshot("06f_skeletons");
			// The salvage chain: diesel generator -> cable -> recycler (with a shredder) -> chest; four scrap metal in,
			// iron nuggets out.
			game.getServer().runCommand("setblock 2 220 -6 intersolarsystem:diesel_generator[facing=north]");
			game.getServer().runCommand("setblock 3 220 -6 intersolarsystem:cable");
			game.getServer().runCommand("setblock 4 220 -6 intersolarsystem:recycler[facing=north]");
			game.getServer().runCommand("setblock 4 220 -5 intersolarsystem:shredder_module[facing=north]");
			game.getServer().runCommand("setblock 5 220 -6 minecraft:chest[facing=north]");
			game.getServer().runOnServer(server -> {
				net.minecraft.server.level.ServerLevel earth = server.overworld();
				net.minecraft.server.level.ServerPlayer player = server.getPlayerList().getPlayers().get(0);
				hu.kriwdi.intersolarsystem.salvage.Machines.interact(earth, new BlockPos(2, 220, -6), player,
						new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.COAL, 4));
				hu.kriwdi.intersolarsystem.salvage.Machines.interact(earth, new BlockPos(4, 220, -6), player,
						new net.minecraft.world.item.ItemStack(hu.kriwdi.intersolarsystem.content.ModItems.SCRAP_METAL, 4));
			});
			game.getServer().runCommand("execute in minecraft:overworld run tp @a 4 221 -10 0 25");
			context.waitTicks(420);
			context.takeScreenshot("06g_recycler");
			game.getServer().runOnServer(server -> {
				var be = server.overworld().getBlockEntity(new BlockPos(5, 220, -6));
				int nuggets = 0;
				if (be instanceof net.minecraft.world.Container c) {
					for (int i = 0; i < c.getContainerSize(); i++) {
						if (c.getItem(i).is(net.minecraft.world.item.Items.IRON_NUGGET)) {
							nuggets += c.getItem(i).getCount();
						}
					}
				}
				org.slf4j.LoggerFactory.getLogger("salvage-test").info("[SALVAGE] chest holds {} iron nuggets", nuggets);
			});

			// The spaceport's crane builds the mothership's keel (facilities restored by hand here), then the finished
			// ship launches into its own dimension.
			game.getServer().runCommand("gamemode spectator @a");
			game.getServer().runCommand("setblock 60 170 60 intersolarsystem:crane_console[facing=north]");
			game.getServer().runCommand("execute in minecraft:overworld run tp @a 60 215 95 180 35");
			context.waitTicks(80);
			game.getServer().runOnServer(server -> {
				net.minecraft.server.level.ServerLevel earth = server.overworld();
				var site = hu.kriwdi.intersolarsystem.spaceport.SpaceportState.get(server).site(
						earth.dimension().identifier().toString(), new BlockPos(60, 170, 60));
				for (hu.kriwdi.intersolarsystem.spaceport.Facility f : hu.kriwdi.intersolarsystem.spaceport.Facility.values()) {
					site.facility(f).calibrated = true;
				}
				site.reactorFuel = 1_000_000L;
				site.reactorFault = false;
				site.nextFaultCheck = Long.MAX_VALUE;
				net.minecraft.server.level.ServerPlayer player = server.getPlayerList().getPlayers().get(0);
				hu.kriwdi.intersolarsystem.spaceport.Spaceports.crane(earth, new BlockPos(60, 170, 60), player,
						new net.minecraft.world.item.ItemStack(hu.kriwdi.intersolarsystem.content.ModItems.KEEL_KIT));
			});
			context.waitTicks(300);
			context.takeScreenshot("06h_crane_building_keel");
			context.waitTicks(700);
			context.takeScreenshot("06i_keel_built");
			game.getServer().runOnServer(server -> {
				net.minecraft.server.level.ServerLevel earth = server.overworld();
				var site = hu.kriwdi.intersolarsystem.spaceport.SpaceportState.get(server).site(
						earth.dimension().identifier().toString(), new BlockPos(60, 170, 60));
				org.slf4j.LoggerFactory.getLogger("spaceport-test").info("[SPACEPORT] sections done {}", site.sectionsDone);
				site.sectionsDone = hu.kriwdi.intersolarsystem.spaceport.Section.mothershipSections();
				net.minecraft.server.level.ServerPlayer player = server.getPlayerList().getPlayers().get(0);
				boolean ok = hu.kriwdi.intersolarsystem.spaceport.Mothership.launch(earth, new BlockPos(60, 170, 60), player);
				org.slf4j.LoggerFactory.getLogger("spaceport-test").info("[SPACEPORT] mothership launched {}", ok);
			});
			context.waitTicks(60);
			context.takeScreenshot("06j_mothership_hangar");
			game.getServer().runCommand("execute in intersolarsystem:mothership run tp @a 120 104 -5 90 10");
			context.waitTicks(60);
			context.takeScreenshot("06k_mothership_bridge");
			game.getServer().runCommand("execute in intersolarsystem:mothership run tp @a -80 130 90 225 25");
			context.waitTicks(80);
			context.takeScreenshot("06l_mothership_outside");
			game.getServer().runCommand("execute in intersolarsystem:mothership run tp @a 0 101 0 90 0");
			context.waitTicks(40);
			context.takeScreenshot("06m_mothership_inside");
			game.getServer().runCommand("gamemode creative @a");

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
			// The helmet display in survival, then a punctured suit, then an empty tank (hypoxia), then no helmet
			// (decompression).
			game.getServer().runCommand("gamemode survival @a");
			game.getServer().runCommand("effect give @a minecraft:resistance infinite 4 true");
			context.waitTicks(30);
			context.takeScreenshot("11b_helmet_hud");
			game.getServer().runCommand("item replace entity @a armor.chest with intersolarsystem:space_suit[custom_data={leak:1b}]");
			context.waitTicks(30);
			context.takeScreenshot("11c_suit_leak");
			game.getServer().runCommand("item replace entity @a armor.chest with intersolarsystem:space_suit[damage=592]");
			context.waitTicks(200);
			context.takeScreenshot("11d_hypoxia");
			game.getServer().runCommand("item replace entity @a armor.head with minecraft:air");
			game.getServer().runCommand("item replace entity @a armor.chest with intersolarsystem:space_suit");
			context.waitTicks(60);
			context.takeScreenshot("11e_decompression");
			game.getServer().runCommand("effect clear @a");
			game.getServer().runCommand("gamemode creative @a");
			game.getServer().runCommand("item replace entity @a armor.head with intersolarsystem:space_helmet");
			game.getServer().runCommand("item replace entity @a armor.chest with intersolarsystem:space_suit");

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
			game.getServer().runCommand("item replace entity @a armor.head with minecraft:air");
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
					"oil_pumpjack", "dry_shipwreck", "oasis_refuge", "terraformer", "farmhouse", "radio_tower", "airport", "barracks",
					"verdant_lab", "bunker_network", "karman_spaceport"}) {
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
				if (name.equals("dead_city") || name.equals("airport") || name.equals("hospital") || name.equals("verdant_lab") || name.equals("karman_spaceport")) {
					// The big ones also from high above, looking both ways.
					game.getServer().runCommand("execute in minecraft:overworld run tp @a " + (at.getX() - 30) + " "
							+ (at.getY() + 70) + " " + (at.getZ() - 30) + " -45 40");
					context.waitTicks(200);
					context.takeScreenshot("31_aerial_" + name + "_a");
					game.getServer().runCommand("execute in minecraft:overworld run tp @a " + (at.getX() + 90) + " "
							+ (at.getY() + 70) + " " + (at.getZ() + 90) + " 135 40");
					context.waitTicks(200);
					context.takeScreenshot("31_aerial_" + name + "_b");
					game.getServer().runCommand("execute in minecraft:overworld run tp @a " + (at.getX() + 14) + " "
							+ (at.getY() + 150) + " " + (at.getZ() + 14) + " 0 90");
					context.waitTicks(200);
					context.takeScreenshot("31_aerial_" + name + "_top");
				}
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
