package hu.kriwdi.intersolarsystem.content;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
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

	// ---- Space-programme materials from the dead Earth's ores (phase 7).
	public static final Item RAW_BAUXITE = simple("raw_bauxite");
	public static final Item ALUMINIUM_INGOT = simple("aluminium_ingot");
	public static final Item RAW_TITANIUM = simple("raw_titanium");
	public static final Item TITANIUM_INGOT = simple("titanium_ingot");
	public static final Item RAW_URANIUM = simple("raw_uranium");
	public static final Item URANIUM_INGOT = simple("uranium_ingot");
	public static final Item LITHIUM_DUST = simple("lithium_dust");
	public static final Item RARE_EARTH_DUST = simple("rare_earth_dust");
	public static final Item SILICON = simple("silicon");
	public static final Item BITUMEN = simple("bitumen");

	// ---- Components made from them.
	public static final Item TITANIUM_PLATE = simple("titanium_plate");

	// ---- Salvage: what things are taken apart into, and the torch that cuts them.
	public static final Item PLASTIC_SCRAP = simple("plastic_scrap");
	public static final Item RUBBER_SCRAP = simple("rubber_scrap");
	public static final Item GLASS_SHARDS = simple("glass_shards");
	public static final Item CUTTING_TORCH = register("cutting_torch", Item::new, new Item.Properties().durability(160));
	public static final Item SOLAR_CELL = simple("solar_cell");
	public static final Item BATTERY_CELL = simple("battery_cell");
	public static final Item RARE_EARTH_MAGNET = simple("rare_earth_magnet");
	public static final Item URANIUM_FUEL_ROD = register("uranium_fuel_rod", Item::new,
			new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));

	/** Engine upgrades, installed on the navigation console: the ion drive (tier 2) and the fusion drive (tier 3). */
	public static final Item ION_DRIVE_KIT = register("ion_drive_kit", Item::new,
			new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));

	public static final Item FUSION_DRIVE_KIT = register("fusion_drive_kit", Item::new,
			new Item.Properties().stacksTo(1).rarity(Rarity.RARE));

	// ---- Titanium tools: a little better than iron, much tougher.
	public static final TagKey<Item> TITANIUM_TOOL_MATERIALS = TagKey.create(Registries.ITEM,
			ModIds.id("titanium_tool_materials"));

	public static final ToolMaterial TITANIUM = new ToolMaterial(
			TagKey.create(Registries.BLOCK, ModIds.id("incorrect_for_titanium_tool")), 900, 7.0F, 2.5F, 12,
			TITANIUM_TOOL_MATERIALS);

	public static final Item TITANIUM_PICKAXE = register("titanium_pickaxe", Item::new,
			new Item.Properties().pickaxe(TITANIUM, 1.0F, -2.8F));
	public static final Item TITANIUM_AXE = register("titanium_axe", Item::new,
			new Item.Properties().axe(TITANIUM, 6.0F, -3.1F));
	public static final Item TITANIUM_SHOVEL = register("titanium_shovel", Item::new,
			new Item.Properties().shovel(TITANIUM, 1.5F, -3.0F));
	public static final Item TITANIUM_HOE = register("titanium_hoe", Item::new,
			new Item.Properties().hoe(TITANIUM, -2.5F, -0.5F));
	public static final Item TITANIUM_SWORD = register("titanium_sword", Item::new,
			new Item.Properties().sword(TITANIUM, 3.0F, -2.4F));

	private ModItems() {
	}

	private static Item simple(String name) {
		return register(name, Item::new, new Item.Properties());
	}

	/** Empty aluminium canister; filled at an oasis pond it holds clean water. */
	public static final Item WATER_CANISTER = register("water_canister", Item::new, new Item.Properties().stacksTo(16));

	public static final Item CLEAN_WATER_CANISTER = register("clean_water_canister", Item::new,
			new Item.Properties().stacksTo(16));

	/** Tools for fixing incidents on the spot: soldering iron (short circuits), sealant (leaks), extinguisher (fires). */
	public static final Item SOLDERING_IRON = register("soldering_iron", Item::new, new Item.Properties().durability(32));

	public static final Item SEALANT = register("sealant", Item::new, new Item.Properties().stacksTo(16));

	public static final Item FIRE_EXTINGUISHER = register("fire_extinguisher", Item::new,
			new Item.Properties().durability(12));

	/** CO2 scrubber cartridge for the life support unit (four flights). */
	public static final Item AIR_FILTER = register("air_filter", Item::new, new Item.Properties().stacksTo(16));

	/** Lore: a handwritten note, a data tape (audio log) and a technical manual; the text id is in the custom data. */
	public static final Item NOTE = register("note", Item::new, new Item.Properties().stacksTo(1));

	public static final Item DATA_TAPE = register("data_tape", Item::new, new Item.Properties().stacksTo(1));

	public static final Item MANUAL = register("manual", Item::new, new Item.Properties().stacksTo(1));

	private static Item register(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, ModIds.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
	}

	/** Loads the class, which registers everything above. The items are listed in {@link ModCreativeTab}. */
	public static void initialize() {
	}
}
