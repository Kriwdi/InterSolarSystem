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
			BlockBehaviour.Properties.of().strength(3.0F, 3600000.0F).sound(SoundType.METAL).noOcclusion()
					.pushReaction(PushReaction.IMMOVEABLE));

	public static final Block DIAGNOSTIC_CONSOLE = register("diagnostic_console", DiagnosticConsoleBlock::new,
			BlockBehaviour.Properties.of().strength(3.0F, 3600000.0F).sound(SoundType.METAL).lightLevel(state -> 7).noOcclusion()
					.pushReaction(PushReaction.IMMOVEABLE));

	public static final Block NAVIGATION_CONSOLE = register("navigation_console", NavigationConsoleBlock::new,
			BlockBehaviour.Properties.of().strength(3.0F, 3600000.0F).sound(SoundType.METAL).lightLevel(state -> 9)
					.noOcclusion().pushReaction(PushReaction.IMMOVEABLE));

	// The derelict ISS (phase 5): breakable, so the station itself can be salvaged.
	public static final Block STATION_HULL = register("station_hull", Block::new,
			BlockBehaviour.Properties.of().strength(4.0F, 8.0F).sound(SoundType.METAL));

	public static final Block STATION_FOIL = register("station_foil", Block::new,
			BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL));

	public static final Block STATION_WINDOW = register("station_window", Block::new,
			BlockBehaviour.Properties.of().strength(4.0F, 8.0F).sound(SoundType.GLASS));

	public static final Block STATION_LIGHT = register("station_light", Block::new,
			BlockBehaviour.Properties.of().strength(2.0F, 4.0F).sound(SoundType.GLASS).lightLevel(state -> 13));

	public static final Block TRUSS = register("truss",
			p -> new ShapedBlock(p, new double[] {0, 0, 0, 16, 2, 16}, new double[] {0, 14, 0, 16, 16, 16},
					new double[] {0, 2, 0, 2, 14, 2}, new double[] {14, 2, 0, 16, 14, 2},
					new double[] {0, 2, 14, 2, 14, 16}, new double[] {14, 2, 14, 16, 14, 16}),
			BlockBehaviour.Properties.of().strength(4.0F, 8.0F).sound(SoundType.METAL).noOcclusion());

	public static final Block SOLAR_PANEL = register("solar_panel",
			p -> new ShapedBlock(p, new double[] {0, 7, 0, 16, 9, 16}),
			BlockBehaviour.Properties.of().strength(1.5F, 3.0F).sound(SoundType.GLASS).noOcclusion());

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
					tab.accept(NAVIGATION_CONSOLE);
					tab.accept(STATION_HULL);
					tab.accept(STATION_FOIL);
					tab.accept(STATION_WINDOW);
					tab.accept(STATION_LIGHT);
					tab.accept(TRUSS);
					tab.accept(SOLAR_PANEL);
				});
	}
}
