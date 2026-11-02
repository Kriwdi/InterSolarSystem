package hu.kriwdi.intersolarsystem.content;

import java.util.function.Function;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import hu.kriwdi.intersolarsystem.core.ModIds;

public final class ModBlocks {
	public static final Block RUSTED_PANEL = register("rusted_panel", ShipHullBlock::new,
			BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.METAL));

	public static final Block SHIP_HATCH = register("ship_hatch", ShipHatchBlock::new,
			BlockBehaviour.Properties.of().strength(3.0F, 3600000.0F).sound(SoundType.METAL)
					.pushReaction(PushReaction.IMMOVEABLE));

	public static final Block REPAIR_STATION = register("repair_station", RepairStationBlock::new,
			BlockBehaviour.Properties.of().strength(3.0F, 3600000.0F).sound(SoundType.METAL)
					.pushReaction(PushReaction.IMMOVEABLE));

	public static final Block DIAGNOSTIC_CONSOLE = register("diagnostic_console", DiagnosticConsoleBlock::new,
			BlockBehaviour.Properties.of().strength(3.0F, 3600000.0F).sound(SoundType.METAL).lightLevel(state -> 7)
					.pushReaction(PushReaction.IMMOVEABLE));

	private ModBlocks() {
	}

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory,
			BlockBehaviour.Properties properties) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, ModIds.id(name));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, ModIds.id(name));
		Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(properties.setId(blockKey)));
		Registry.register(BuiltInRegistries.ITEM, itemKey,
				new BlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)));
		return block;
	}

	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
				.register(tab -> {
					tab.accept(RUSTED_PANEL);
					tab.accept(SHIP_HATCH);
					tab.accept(REPAIR_STATION);
					tab.accept(DIAGNOSTIC_CONSOLE);
				});
	}
}
