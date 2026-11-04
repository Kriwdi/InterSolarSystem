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
				ModBlocks.MARS_ICE, PlanetBlocks.VENUS_BASALT, PlanetBlocks.VENUS_REGOLITH, PlanetBlocks.SULFUR_CRUST, PlanetBlocks.MERCURY_ROCK, PlanetBlocks.MERCURY_REGOLITH, PlanetBlocks.HOLLOW_CRUST, PlanetBlocks.CERES_CLAY, PlanetBlocks.CERES_ICE, PlanetBlocks.CARBONATE_SALT, PlanetBlocks.CHONDRITE, PlanetBlocks.CARBONACEOUS_ROCK, PlanetBlocks.NICKEL_IRON_ORE, PlanetBlocks.IRIDIUM_ORE, PlanetBlocks.AMMONIA_CLOUD, PlanetBlocks.SULFIDE_CLOUD, PlanetBlocks.WATER_CLOUD, PlanetBlocks.STORM_CLOUD, PlanetBlocks.METALLIC_HYDROGEN, PlanetBlocks.SULFUR_PLAIN, PlanetBlocks.IO_SILICATE, PlanetBlocks.SO2_FROST, PlanetBlocks.EUROPA_ICE, PlanetBlocks.LINEAE_ICE, PlanetBlocks.EUROPA_SALT, PlanetBlocks.MICROBIAL_MAT, hu.kriwdi.intersolarsystem.content.OuterBlocks.GANYMEDE_DARK_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.GROOVED_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.GANYMEDE_FROST, hu.kriwdi.intersolarsystem.content.OuterBlocks.CALLISTO_REGOLITH, hu.kriwdi.intersolarsystem.content.OuterBlocks.CALLISTO_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.PINNACLE_FROST, hu.kriwdi.intersolarsystem.content.OuterBlocks.SATURN_HAZE, hu.kriwdi.intersolarsystem.content.OuterBlocks.SATURN_BELT_CLOUD, hu.kriwdi.intersolarsystem.content.OuterBlocks.HEXAGON_STORM_CLOUD, hu.kriwdi.intersolarsystem.content.OuterBlocks.RING_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.DUSTY_RING_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.DEUTERIUM_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.THOLIN_SAND, hu.kriwdi.intersolarsystem.content.OuterBlocks.TITAN_BEDROCK_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.HYDROCARBON_SLUDGE, hu.kriwdi.intersolarsystem.content.OuterBlocks.CRYOLAVA_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.ENCELADUS_SNOW, hu.kriwdi.intersolarsystem.content.OuterBlocks.ENCELADUS_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.FISSURE_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.URANUS_METHANE_CLOUD, hu.kriwdi.intersolarsystem.content.OuterBlocks.DEEP_SULFIDE_CLOUD, hu.kriwdi.intersolarsystem.content.OuterBlocks.MIRANDA_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.CORONA_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.SCARP_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.TITANIA_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.TITANIA_FROST, hu.kriwdi.intersolarsystem.content.OuterBlocks.NEPTUNE_METHANE_CLOUD, hu.kriwdi.intersolarsystem.content.OuterBlocks.DARK_SPOT_CLOUD, hu.kriwdi.intersolarsystem.content.OuterBlocks.NITROGEN_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.CANTALOUPE_ICE, hu.kriwdi.intersolarsystem.content.OuterBlocks.GEYSER_SOOT, hu.kriwdi.intersolarsystem.content.OuterBlocks.NAMELESS_ROCK, hu.kriwdi.intersolarsystem.content.OuterBlocks.SILENT_CRYSTAL, hu.kriwdi.intersolarsystem.content.OuterBlocks.GEYSER_VENT, hu.kriwdi.intersolarsystem.content.OuterBlocks.NITROGEN_GEYSER, hu.kriwdi.intersolarsystem.content.OuterBlocks.DRAGONFLY_FLIGHT_COMPUTER}) {
			// vent_colony (biosamples) and diamond_hail (diamonds) have hand-written tables (tools/art/make_outer_assets.py).
			dropSelf(block);
		}
	}
}
