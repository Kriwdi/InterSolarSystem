package hu.kriwdi.intersolarsystem.client.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;

import net.minecraft.core.HolderLookup;

import hu.kriwdi.intersolarsystem.content.ModBlocks;

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
				ModBlocks.STATION_WINDOW, ModBlocks.STATION_LIGHT, ModBlocks.TRUSS, ModBlocks.SOLAR_PANEL}) {
			dropSelf(block);
		}
	}
}
