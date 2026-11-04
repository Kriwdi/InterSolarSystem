package hu.kriwdi.intersolarsystem;

import net.fabricmc.api.ModInitializer;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.content.ModLoot;
import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.core.SmokeTest;
import hu.kriwdi.intersolarsystem.ship.ShipCommands;
import hu.kriwdi.intersolarsystem.ship.ShipFlightController;
import hu.kriwdi.intersolarsystem.ship.ShipNetworking;
import hu.kriwdi.intersolarsystem.space.SpaceEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterSolarSystem implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(ModIds.MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.initialize();
		ModBlocks.initialize();
		hu.kriwdi.intersolarsystem.content.ModEntities.initialize();
		hu.kriwdi.intersolarsystem.content.ModSounds.initialize();
		hu.kriwdi.intersolarsystem.survival.SurvivalRules.register();
		hu.kriwdi.intersolarsystem.content.ModCreativeTab.initialize();
		ModLoot.initialize();
		hu.kriwdi.intersolarsystem.worldgen.ModWorldgen.initialize();
		ShipNetworking.register();
		ShipCommands.register();
		ShipFlightController.register();
		hu.kriwdi.intersolarsystem.ship.ShipInteriorGuard.register();
		hu.kriwdi.intersolarsystem.space.OrbitStation.register();
		hu.kriwdi.intersolarsystem.ship.ShipShell.register();
		SpaceEnvironment.register();
		hu.kriwdi.intersolarsystem.space.OrbitalEva.register();
		hu.kriwdi.intersolarsystem.space.SpaceSites.register();
		hu.kriwdi.intersolarsystem.spaceport.Spaceports.register();
		hu.kriwdi.intersolarsystem.spaceport.Mothership.register();
		hu.kriwdi.intersolarsystem.salvage.Machines.register();
		hu.kriwdi.intersolarsystem.salvage.Disassembly.register();
		hu.kriwdi.intersolarsystem.worldgen.EarthEnvironment.register();
		hu.kriwdi.intersolarsystem.worldgen.StartSite.register();
		SmokeTest.register();
		LOGGER.info("InterSolarSystem initialized");
	}
}
