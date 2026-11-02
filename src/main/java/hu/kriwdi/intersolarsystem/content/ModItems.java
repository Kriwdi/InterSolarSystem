package hu.kriwdi.intersolarsystem.content;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

import hu.kriwdi.intersolarsystem.core.ModIds;

public final class ModItems {
	public static final Item SCRAP_SENSOR = register("scrap_sensor", Item::new, new Item.Properties());

	public static final Item REPAIR_KIT = register("repair_kit", Item::new, new Item.Properties().stacksTo(16));

	/** Salvaged technology (phase 3a): found in ruins, crafted into repair kits and ship parts. */
	public static final Item SCRAP_METAL = register("scrap_metal", Item::new, new Item.Properties());

	public static final Item SALVAGED_WIRING = register("salvaged_wiring", Item::new, new Item.Properties());

	public static final Item SALVAGED_CIRCUIT = register("salvaged_circuit", Item::new, new Item.Properties());

	/** Held in the off hand during repairs: light faults need no minigame, harder ones get a smaller board. */
	public static final Item MULTITOOL = register("multitool", Item::new, new Item.Properties().durability(48));

	public static final Item SPACE_HELMET = register("space_helmet", Item::new,
			new Item.Properties().humanoidArmor(ModArmor.SPACE_SUIT, ArmorType.HELMET));

	public static final Item SPACE_SUIT = register("space_suit", Item::new,
			new Item.Properties().humanoidArmor(ModArmor.SPACE_SUIT, ArmorType.CHESTPLATE));

	public static final Item SPACE_PANTS = register("space_pants", Item::new,
			new Item.Properties().humanoidArmor(ModArmor.SPACE_SUIT, ArmorType.LEGGINGS));

	public static final Item SPACE_BOOTS = register("space_boots", Item::new,
			new Item.Properties().humanoidArmor(ModArmor.SPACE_SUIT, ArmorType.BOOTS));

	private ModItems() {
	}

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ModIds.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	/** Loads the class, which registers everything above. The items are listed in {@link ModCreativeTab}. */
	public static void initialize() {
	}
}
