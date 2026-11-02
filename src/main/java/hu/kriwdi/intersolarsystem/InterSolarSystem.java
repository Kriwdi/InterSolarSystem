package hu.kriwdi.intersolarsystem;

import net.fabricmc.api.ModInitializer;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.ship.ShipCommands;
import hu.kriwdi.intersolarsystem.ship.ShipFlightController;
import hu.kriwdi.intersolarsystem.ship.ShipNetworking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterSolarSystem implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(ModIds.MOD_ID);

	@Override
	public void onInitialize() {
		ModItems.initialize();
		ModBlocks.initialize();
		ShipNetworking.register();
		ShipCommands.register();
		ShipFlightController.register();
		LOGGER.info("InterSolarSystem initialized");
	}
}
