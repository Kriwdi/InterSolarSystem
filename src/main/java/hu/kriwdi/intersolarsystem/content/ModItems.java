package hu.kriwdi.intersolarsystem.content;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import hu.kriwdi.intersolarsystem.core.ModIds;

public final class ModItems {
	public static final Item SCRAP_SENSOR = register("scrap_sensor", Item::new, new Item.Properties());

	public static final Item REPAIR_KIT = register("repair_kit", Item::new, new Item.Properties().stacksTo(16));

	/** Salvaged technology (phase 3a): found in ruins, crafted into repair kits and ship parts. */
	public static final Item SCRAP_METAL = register("scrap_metal", Item::new, new Item.Properties());

	public static final Item SALVAGED_WIRING = register("salvaged_wiring", Item::new, new Item.Properties());

	public static final Item SALVAGED_CIRCUIT = register("salvaged_circuit", Item::new, new Item.Properties());

	private ModItems() {
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ModIds.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
				.register(tab -> {
					tab.accept(SCRAP_SENSOR);
					tab.accept(REPAIR_KIT);
					tab.accept(SCRAP_METAL);
					tab.accept(SALVAGED_WIRING);
					tab.accept(SALVAGED_CIRCUIT);
				});
	}
}
