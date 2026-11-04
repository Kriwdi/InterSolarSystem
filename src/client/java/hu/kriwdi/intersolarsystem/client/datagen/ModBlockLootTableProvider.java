package hu.kriwdi.intersolarsystem.client.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;

import net.minecraft.core.HolderLookup;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.PlanetBlocks;

public class ModBlockLootTableProvider extends FabricBlockLootSubProvider {
	public ModBlockLootTableProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		super(output, lookup);
	}

	@Override
	public void generate() {
		dropSelf(ModBlocks.RUSTED_PANEL);
		dropSelf(ModBlocks.SHIP_HATCH);
		dropSelf(ModBlocks.REPAIR_STATION);
		dropSelf(ModBlocks.DIAGNOSTIC_CONSOLE);
		dropSelf(ModBlocks.NAVIGATION_CONSOLE);
		for (var block : new net.minecraft.world.level.block.Block[] {ModBlocks.STATION_HULL, ModBlocks.STATION_FOIL,
				ModBlocks.STATION_WINDOW, ModBlocks.STATION_LIGHT, ModBlocks.TRUSS, ModBlocks.SOLAR_PANEL, ModBlocks.LUNAR_ROCK,
				ModBlocks.LUNAR_REGOLITH, ModBlocks.MARE_REGOLITH, ModBlocks.FRESH_REGOLITH, ModBlocks.MARE_BASALT,
				ModBlocks.HELIUM3_REGOLITH, ModBlocks.ILMENITE_ORE, ModBlocks.MARS_ROCK, ModBlocks.MARS_REGOLITH,
				ModBlocks.MARS_DUST, ModBlocks.MARS_SANDSTONE, ModBlocks.HEMATITE_ORE, ModBlocks.PERCHLORATE_CRUST,
				ModBlocks.MARS_ICE, PlanetBlocks.VENUS_BASALT, PlanetBlocks.VENUS_REGOLITH, PlanetBlocks.SULFUR_CRUST, PlanetBlocks.MERCURY_ROCK, PlanetBlocks.MERCURY_REGOLITH, PlanetBlocks.HOLLOW_CRUST, PlanetBlocks.CERES_CLAY, PlanetBlocks.CERES_ICE, PlanetBlocks.CARBONATE_SALT, PlanetBlocks.CHONDRITE, PlanetBlocks.CARBONACEOUS_ROCK, PlanetBlocks.NICKEL_IRON_ORE, PlanetBlocks.IRIDIUM_ORE, PlanetBlocks.AMMONIA_CLOUD, PlanetBlocks.SULFIDE_CLOUD, PlanetBlocks.WATER_CLOUD, PlanetBlocks.STORM_CLOUD, PlanetBlocks.METALLIC_HYDROGEN, PlanetBlocks.SULFUR_PLAIN, PlanetBlocks.IO_SILICATE, PlanetBlocks.SO2_FROST, PlanetBlocks.EUROPA_ICE, PlanetBlocks.LINEAE_ICE, PlanetBlocks.EUROPA_SALT, PlanetBlocks.MICROBIAL_MAT}) {
			dropSelf(block);
		}
	}
}
