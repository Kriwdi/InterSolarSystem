package hu.kriwdi.intersolarsystem.content;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import hu.kriwdi.intersolarsystem.core.ModIds;

/** The mod's own creative tab: every item and block of the mod, in registration order. */
public final class ModCreativeTab {
	public static final ResourceKey<CreativeModeTab> KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
			ModIds.id("main"));

	private ModCreativeTab() {
	}

	public static void initialize() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, KEY, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup." + ModIds.MOD_ID))
				.icon(() -> new ItemStack(ModItems.SPACE_HELMET))
				.displayItems((parameters, output) -> BuiltInRegistries.ITEM.forEach(item -> {
					if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(ModIds.MOD_ID)) {
						output.accept(item);
					}
				}))
				.build());
	}
}
