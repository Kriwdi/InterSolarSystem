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
		hu.kriwdi.intersolarsystem.content.ModCreativeTab.initialize();
		ModLoot.initialize();
		hu.kriwdi.intersolarsystem.worldgen.ModWorldgen.initialize();
		ShipNetworking.register();
		ShipCommands.register();
		ShipFlightController.register();
		SpaceEnvironment.register();
		hu.kriwdi.intersolarsystem.worldgen.EarthEnvironment.register();
		hu.kriwdi.intersolarsystem.worldgen.StartSite.register();
		SmokeTest.register();
		LOGGER.info("InterSolarSystem initialized");
	}
}
