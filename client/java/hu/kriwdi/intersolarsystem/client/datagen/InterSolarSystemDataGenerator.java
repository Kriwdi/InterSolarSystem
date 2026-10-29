package hu.kriwdi.intersolarsystem.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class InterSolarSystemDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider((output, lookup) -> ModLangProvider.english(output, lookup));
		pack.addProvider((output, lookup) -> ModLangProvider.german(output, lookup));
		pack.addProvider((output, lookup) -> ModLangProvider.hungarian(output, lookup));
		pack.addProvider(ModBlockLootTableProvider::new);
	}
}
