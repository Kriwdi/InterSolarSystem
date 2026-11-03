package hu.kriwdi.intersolarsystem.content;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
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

	/** Clear window glass (the Cupola). */
	public static final Block STATION_GLASS = register("station_glass", Block::new,
			BlockBehaviour.Properties.of().strength(1.5F, 6.0F).sound(SoundType.GLASS).noOcclusion());

	/** The station's heat radiator: a thin white ribbed panel (like a solar panel, without cells). */
	public static final Block STATION_RADIATOR = register("station_radiator",
			p -> new ShapedBlock(p, new double[] {0, 7, 0, 16, 9, 16}),
			BlockBehaviour.Properties.of().strength(1.5F, 3.0F).sound(SoundType.METAL).noOcclusion());

	// The Moon (phase 6).
	public static final Block LUNAR_ROCK = register("lunar_rock", Block::new,
			BlockBehaviour.Properties.of().strength(1.8F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

	public static final Block LUNAR_REGOLITH = register("lunar_regolith", Block::new,
			BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.SAND));

	public static final Block MARE_REGOLITH = register("mare_regolith", Block::new,
			BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.SAND));

	public static final Block FRESH_REGOLITH = register("fresh_regolith", Block::new,
			BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.SAND));

	public static final Block MARE_BASALT = register("mare_basalt", Block::new,
			BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.BASALT).requiresCorrectToolForDrops());

	public static final Block HELIUM3_REGOLITH = register("helium3_regolith", Block::new,
			BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.SAND).lightLevel(state -> 2));

	public static final Block ILMENITE_ORE = register("ilmenite_ore", Block::new,
			BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.BASALT).requiresCorrectToolForDrops());

	// Relics on the Moon.
	public static final Block LANDER_ENGINE = register("lander_engine", Block::new,
			BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL));

	public static final Block LUNAR_FLAG = register("lunar_flag",
			p -> new ShipFacingBlock(p, new double[] {7, 0, 7, 9, 16, 9}),
			BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.WOOL).noOcclusion().noCollision());

	public static final Block ROVER_WHEEL = register("rover_wheel",
			p -> new ShipFacingBlock(p, new double[] {4, 0, 1, 12, 14, 15}),
			BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.CHAIN).noOcclusion());

	public static final Block ANTENNA_DISH = register("antenna_dish",
			p -> new ShapedBlock(p, new double[] {1, 6, 1, 15, 10, 15}, new double[] {7, 0, 7, 9, 6, 9}),
			BlockBehaviour.Properties.of().strength(2.0F).sound(SoundType.METAL).noOcclusion());

	public static final Block DATA_TERMINAL = register("data_terminal", DataTerminalBlock::new,
			BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL).lightLevel(state -> 5)
					.noOcclusion());

	// ---- The dead Earth (phase 7): ground of the twelve biomes.
	public static final Block ASH = register("ash", Block::new,
			BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.SAND));

	public static final Block CINDER_ROCK = register("cinder_rock", Block::new,
			BlockBehaviour.Properties.of().strength(1.5F, 6.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops());

	public static final Block DRIED_SILT = register("dried_silt", Block::new,
			BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.MUD_BRICKS));

	public static final Block SALT_CRUST = register("salt_crust", Block::new,
			BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.CALCITE));

	public static final Block SALT_BLOCK = register("salt_block", Block::new,
			BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.CALCITE).requiresCorrectToolForDrops());

	public static final Block LITHIUM_BRINE_CRUST = register("lithium_brine_crust", Block::new,
			BlockBehaviour.Properties.of().strength(0.8F).sound(SoundType.CALCITE));

	public static final Block IRRADIATED_SOIL = register("irradiated_soil", Block::new,
			BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.GRAVEL).lightLevel(state -> 1));

	public static final Block TRINITITE = register("trinitite", Block::new,
			BlockBehaviour.Properties.of().strength(1.2F, 4.0F).sound(SoundType.GLASS).lightLevel(state -> 6)
					.requiresCorrectToolForDrops());

	public static final Block ACIDIC_MUD = register("acidic_mud", Block::new,
			BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.MUD));

	public static final Block COMPACTED_JUNK = register("compacted_junk", Block::new,
			BlockBehaviour.Properties.of().strength(1.8F, 5.0F).sound(SoundType.METAL));

	public static final Block TAR_SAND = register("tar_sand", Block::new,
			BlockBehaviour.Properties.of().strength(0.6F).sound(SoundType.SOUL_SOIL).speedFactor(0.6F));

	public static final Block OIL_SHALE = register("oil_shale", Block::new,
			BlockBehaviour.Properties.of().strength(1.5F, 6.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());

	public static final Block SILICA_SAND = register("silica_sand", Block::new,
			BlockBehaviour.Properties.of().strength(0.5F).sound(SoundType.SAND));

	public static final Block PETRIFIED_LOG = register("petrified_log", RotatedPillarBlock::new,
			BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

	public static final Block PETRIFIED_WOOD = register("petrified_wood", Block::new,
			BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());

	public static final Block DEAD_LOG = register("dead_log", RotatedPillarBlock::new,
			BlockBehaviour.Properties.of().strength(1.6F).sound(SoundType.WOOD));

	// ---- Ores for the space programme (no vanilla ores generate on the dead Earth).
	public static final Block BAUXITE_ORE = ore("bauxite_ore", 2.5F, SoundType.STONE, 0);
	public static final Block DEEPSLATE_BAUXITE_ORE = ore("deepslate_bauxite_ore", 4.0F, SoundType.DEEPSLATE, 0);
	public static final Block TITANIUM_ORE = ore("titanium_ore", 3.0F, SoundType.STONE, 0);
	public static final Block DEEPSLATE_TITANIUM_ORE = ore("deepslate_titanium_ore", 4.5F, SoundType.DEEPSLATE, 0);
	public static final Block URANINITE_ORE = ore("uraninite_ore", 3.0F, SoundType.STONE, 3);
	public static final Block DEEPSLATE_URANINITE_ORE = ore("deepslate_uraninite_ore", 4.5F, SoundType.DEEPSLATE, 3);
	public static final Block SPODUMENE_ORE = ore("spodumene_ore", 2.5F, SoundType.STONE, 0);
	public static final Block DEEPSLATE_SPODUMENE_ORE = ore("deepslate_spodumene_ore", 4.0F, SoundType.DEEPSLATE, 0);
	public static final Block MONAZITE_ORE = ore("monazite_ore", 3.0F, SoundType.STONE, 0);
	public static final Block DEEPSLATE_MONAZITE_ORE = ore("deepslate_monazite_ore", 4.5F, SoundType.DEEPSLATE, 0);

	// ---- Shaped debris and props of the dead Earth (models built from boxes; outlines follow the models).
	public static final Block SCRAP_PILE = register("scrap_pile",
			p -> new ShapedBlock(p, new double[] {0, 0, 0, 16, 4, 16}, new double[] {2, 4, 3, 13, 8, 12},
					new double[] {5, 8, 5, 10, 11, 10}),
			BlockBehaviour.Properties.of().strength(1.2F).sound(SoundType.METAL).noOcclusion());

	public static final Block RUBBLE = register("rubble",
			p -> new ShapedBlock(p, new double[] {0, 0, 0, 16, 3, 16}, new double[] {3, 3, 2, 12, 6, 11}),
			BlockBehaviour.Properties.of().strength(1.0F).sound(SoundType.STONE).noOcclusion());

	public static final Block TOXIC_BARREL = register("toxic_barrel",
			p -> new ShapedBlock(p, new double[] {2, 0, 2, 14, 15, 14}),
			BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.METAL).noOcclusion().lightLevel(state -> 4));

	public static final Block MINING_DRONE = register("mining_drone",
			p -> new ShipFacingBlock(p, new double[] {1, 0, 3, 15, 9, 15}, new double[] {5, 2, 0, 11, 7, 3}),
			BlockBehaviour.Properties.of().strength(2.5F).sound(SoundType.METAL).noOcclusion());

	public static final Block TAR_PUDDLE = register("tar_puddle",
			p -> new ShapedBlock(p, new double[] {0, 0, 0, 16, 1, 16}),
			BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.HONEY_BLOCK).noOcclusion().speedFactor(0.4F)
					.jumpFactor(0.5F));

	// ---- The ship seen from outside (built around the hatch where it lands) and its viewports. Indestructible: the
	// hull leaves with the ship at takeoff.
	public static final Block SHIP_PLATING = register("ship_plating", Block::new,
			BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noLootTable()
					.pushReaction(PushReaction.IMMOVEABLE));

	public static final Block SHIP_PORTHOLE = register("ship_porthole", Block::new,
			BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.GLASS).noLootTable()
					.pushReaction(PushReaction.IMMOVEABLE).lightLevel(state -> 6));

	public static final Block SHIP_THRUSTER = register("ship_thruster", Block::new,
			BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.METAL).noLootTable()
					.pushReaction(PushReaction.IMMOVEABLE));

	/** See-through viewport of the ship's interior: the window frame with clear glass. */
	public static final Block SHIP_VIEWPORT = register("ship_viewport", Block::new,
			BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).sound(SoundType.GLASS).noLootTable()
					.noOcclusion().pushReaction(PushReaction.IMMOVEABLE));

	// ---- The research ship's outer hull: shaped plating, livery band, navigation lights, the hatch's upper half.
	public static final Block SHIP_PLATING_STAIRS = register("ship_plating_stairs",
			p -> new StairBlock(SHIP_PLATING.defaultBlockState(), p), hull(SoundType.METAL).noOcclusion());

	public static final Block SHIP_PLATING_SLAB = register("ship_plating_slab", SlabBlock::new, hull(SoundType.METAL));

	public static final Block SHIP_STRIPE = register("ship_stripe", Block::new, hull(SoundType.METAL));

	public static final Block SHIP_LIGHT_RED = register("ship_light_red", Block::new,
			hull(SoundType.GLASS).lightLevel(state -> 10));

	public static final Block SHIP_LIGHT_GREEN = register("ship_light_green", Block::new,
			hull(SoundType.GLASS).lightLevel(state -> 10));

	/** Upper half of the two-block hatch: drawn by the lower half's model, a click on it opens the hatch below. */
	public static final Block SHIP_HATCH_TOP = register("ship_hatch_top", ShipHatchTopBlock::new,
			hull(SoundType.METAL).noOcclusion());

	private static BlockBehaviour.Properties hull(SoundType sound) {
		return BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).sound(sound).noLootTable()
				.pushReaction(PushReaction.IMMOVEABLE);
	}

	/** Padded flight seat to sit in (the pilot's and the crew's seats on the bridge). */
	public static final Block ASTRONAUT_SEAT = register("astronaut_seat", AstronautSeatBlock::new,
			BlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.METAL).noOcclusion());

	// ---- Dead wood: planks sawn from the bleached trunks, and what is built from them.
	public static final Block DEAD_PLANKS = register("dead_planks", Block::new,
			BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());

	public static final Block DEAD_SLAB = register("dead_slab", SlabBlock::new,
			BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());

	public static final Block DEAD_STAIRS = register("dead_stairs",
			p -> new StairBlock(DEAD_PLANKS.defaultBlockState(), p),
			BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());

	public static final Block DEAD_FENCE = register("dead_fence", FenceBlock::new,
			BlockBehaviour.Properties.of().strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());

	private ModBlocks() {
	}

	private static Block ore(String name, float hardness, SoundType sound, int light) {
		return register(name, Block::new, BlockBehaviour.Properties.of().strength(hardness, 3.0F).sound(sound)
				.requiresCorrectToolForDrops().lightLevel(state -> light));
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

	/** Loads the class, which registers everything above. The items are listed in {@link ModCreativeTab}. */
	public static void initialize() {
	}
}
