package hu.kriwdi.intersolarsystem.worldgen;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * A big ruin that needs level ground (the airport, the hospital complex, the megacity). Before the layout (an ordinary
 * jigsaw structure) is placed, the terrain around the chunk is sampled on a grid: if the surface varies by more than
 * {@code max_variation} blocks within {@code check_radius}, the site is skipped. Terrain adaptation can only shave a
 * few blocks off a hill, so a runway on a hillside would end up buried.
 */
public final class FlatSiteStructure extends Structure {
	public static final MapCodec<FlatSiteStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			settingsCodec(i),
			Structure.CODEC.fieldOf("layout").forGetter(s -> s.layout),
			Codec.intRange(16, 256).fieldOf("check_radius").forGetter(s -> s.checkRadius),
			Codec.intRange(1, 64).fieldOf("max_variation").forGetter(s -> s.maxVariation),
			Codec.intRange(0, 64).optionalFieldOf("ground_offset", 0).forGetter(s -> s.groundOffset),
			Codec.intRange(0, 64).optionalFieldOf("clear_height", 24).forGetter(s -> s.clearHeight))
			.apply(i, FlatSiteStructure::new));

	public static final StructureType<FlatSiteStructure> TYPE = () -> CODEC;

	/** Samples per side of the grid (terrain height lookups are expensive: 5 x 5 at most per candidate). */
	private static final int GRID = 5;

	private final Holder<Structure> layout;
	private final int checkRadius;
	private final int maxVariation;
	private final int groundOffset;
	private final int clearHeight;

	public FlatSiteStructure(StructureSettings settings, Holder<Structure> layout, int checkRadius, int maxVariation,
			int groundOffset, int clearHeight) {
		super(settings);
		this.layout = layout;
		this.checkRadius = checkRadius;
		this.maxVariation = maxVariation;
		this.groundOffset = groundOffset;
		this.clearHeight = clearHeight;
	}

	public static void register() {
		Registry.register(BuiltInRegistries.STRUCTURE_TYPE, ModIds.id("flat_site"), TYPE);
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		int cx = context.chunkPos().getMinBlockX() + 8;
		int cz = context.chunkPos().getMinBlockZ() + 8;
		if (!context.couldValidBiomeExistOnTopOfChunkCenter()) {
			return Optional.empty();
		}
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int step = 2 * checkRadius / (GRID - 1);
		for (int i = 0; i < GRID; i++) {
			for (int j = 0; j < GRID; j++) {
				// Centre and corners first: most hilly sites fail within the first few samples.
				int dx = -checkRadius + ((i + 2) % GRID) * step;
				int dz = -checkRadius + ((j + 2) % GRID) * step;
				int y = context.chunkGenerator().getFirstOccupiedHeight(cx + dx, cz + dz, Heightmap.Types.WORLD_SURFACE_WG,
						context.heightAccessor(), context.randomState());
				min = Math.min(min, y);
				max = Math.max(max, y);
				if (max - min > maxVariation) {
					return Optional.empty();
				}
			}
		}
		return layout.value().findValidGenerationPoint(context);
	}

	/**
	 * After the ruin is placed in a chunk: whatever natural terrain is left above its ground level (a hill the
	 * terrain adaptation could not shave off) is removed, so nothing is buried. Only natural blocks are touched
	 * (stone, dirt, sand, gravel, ores, dead trees, plants, the dead Earth's soils), never the ruin itself.
	 */
	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
			RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, PiecesContainer pieces) {
		BoundingBox box = pieces.calculateBoundingBox();
		int ground = box.minY() + groundOffset;
		int top = Math.min(ground + clearHeight, level.getMaxY());
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockState air = Blocks.AIR.defaultBlockState();
		for (int x = Math.max(box.minX(), chunkBox.minX()); x <= Math.min(box.maxX(), chunkBox.maxX()); x++) {
			for (int z = Math.max(box.minZ(), chunkBox.minZ()); z <= Math.min(box.maxZ(), chunkBox.maxZ()); z++) {
				for (int y = ground + 1; y <= top; y++) {
					pos.set(x, y, z);
					if (natural(level.getBlockState(pos))) {
						level.setBlock(pos, air, 2);
					}
				}
			}
		}
	}

	private static boolean natural(BlockState state) {
		if (state.isAir()) {
			return false;
		}
		if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.DIRT) || state.is(BlockTags.SAND)
				|| state.is(Blocks.GRAVEL) || state.is(Blocks.SNOW) || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.ICE)
				|| state.is(Blocks.PACKED_ICE) || state.is(Blocks.CALCITE) || state.is(Blocks.TUFF)
				|| state.is(Blocks.DEAD_BUSH) || state.is(Blocks.SHORT_GRASS)) {
			return true;
		}
		Block block = state.getBlock();
		if (block == ModBlocks.ASH || block == ModBlocks.CINDER_ROCK || block == ModBlocks.DRIED_SILT
				|| block == ModBlocks.SALT_CRUST || block == ModBlocks.SALT_BLOCK || block == ModBlocks.LITHIUM_BRINE_CRUST
				|| block == ModBlocks.IRRADIATED_SOIL || block == ModBlocks.TRINITITE || block == ModBlocks.ACIDIC_MUD
				|| block == ModBlocks.COMPACTED_JUNK || block == ModBlocks.TAR_SAND || block == ModBlocks.OIL_SHALE
				|| block == ModBlocks.SILICA_SAND || block == ModBlocks.PETRIFIED_LOG || block == ModBlocks.PETRIFIED_WOOD
				|| block == ModBlocks.DEAD_LOG || block == ModBlocks.OASIS_GRASS) {
			return true;
		}
		return BuiltInRegistries.BLOCK.getKey(block).getPath().endsWith("_ore");
	}

	@Override
	public StructureType<?> type() {
		return TYPE;
	}
}
