package hu.kriwdi.intersolarsystem.client.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import net.minecraft.core.HolderLookup;

/** Writes one language file from {@link ModLangTable}. */
public class ModLangProvider extends FabricLanguageProvider {
	private final ModLangTable.Language language;

	private ModLangProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
			ModLangTable.Language language) {
		super(output, language.code(), lookup);
		this.language = language;
	}

	static ModLangProvider english(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		return new ModLangProvider(output, lookup, ModLangTable.Language.ENGLISH);
	}

	static ModLangProvider german(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		return new ModLangProvider(output, lookup, ModLangTable.Language.GERMAN);
	}

	static ModLangProvider hungarian(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
		return new ModLangProvider(output, lookup, ModLangTable.Language.HUNGARIAN);
	}

	@Override
	public void generateTranslations(HolderLookup.Provider holderLookup, TranslationBuilder builder) {
		for (ModLangTable.Row row : ModLangTable.rows()) {
			builder.add(row.key(), row.text(language));
		}
	}
}
