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
	}
}
