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
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Every surface ruin of the dead Earth is wrapped in this: the layout (an ordinary jigsaw structure) does the placing,
 * this makes it sit in the landscape.
 *
 * <ul>
 * <li><b>Site choice</b> (optional, {@code max_variation} &gt; 0): the flattest spot within 8 chunks is used; a site too
 * hilly everywhere is skipped unless the structure is {@code guaranteed}.</li>
 * <li><b>Clearing:</b> natural terrain left above the ruin's ground level inside its pieces is removed (up to
 * {@code clear_height}), so nothing is buried by a hill the terrain adaptation could not shave off.</li>
 * <li><b>Foundations:</b> wherever a piece's ground layer hangs over a dip, its own ground block is continued down to
 * the terrain, so no ruin floats.</li>
 * <li><b>Blending:</b> within {@code blend} blocks of the ruin the terrain slopes up or down to its ground level with
 * the column's own natural blocks, instead of ending in a cliff or a trench.</li>
 * </ul>
 */
public final class FlatSiteStructure extends Structure {
	public static final MapCodec<FlatSiteStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			settingsCodec(i),
			Structure.CODEC.fieldOf("layout").forGetter(s -> s.layout),
			Codec.intRange(0, 256).optionalFieldOf("check_radius", 0).forGetter(s -> s.checkRadius),
			Codec.intRange(0, 64).optionalFieldOf("max_variation", 0).forGetter(s -> s.maxVariation),
			Codec.intRange(0, 64).optionalFieldOf("ground_offset", 0).forGetter(s -> s.groundOffset),
			Codec.intRange(0, 64).optionalFieldOf("clear_height", 24).forGetter(s -> s.clearHeight),
			Codec.BOOL.optionalFieldOf("guaranteed", false).forGetter(s -> s.guaranteed),
			Codec.BOOL.optionalFieldOf("foundation", true).forGetter(s -> s.foundation),
			Codec.intRange(0, 12).optionalFieldOf("blend", 8).forGetter(s -> s.blend))
			.apply(i, FlatSiteStructure::new));

	public static final StructureType<FlatSiteStructure> TYPE = () -> CODEC;

	/** Chunk offsets tried around a candidate (5 x 5 spots, up to 8 chunks away), the candidate itself first. */
	private static final int[] OFFSETS = {0, -4, 4, -8, 8};
	private static final int SEARCH_GRID = OFFSETS.length;
	/** How deep a foundation may reach before it gives up (a ruin over a canyon keeps its overhang). */
	private static final int MAX_FOUNDATION = 40;

	private final Holder<Structure> layout;
	private final int checkRadius;
	private final int maxVariation;
	private final int groundOffset;
	private final int clearHeight;
	private final boolean guaranteed;
	private final boolean foundation;
	private final int blend;

	public FlatSiteStructure(StructureSettings settings, Holder<Structure> layout, int checkRadius, int maxVariation,
			int groundOffset, int clearHeight, boolean guaranteed, boolean foundation, int blend) {
		super(settings);
		this.layout = layout;
		this.checkRadius = checkRadius;
		this.maxVariation = maxVariation;
		this.groundOffset = groundOffset;
		this.clearHeight = clearHeight;
		this.guaranteed = guaranteed;
		this.foundation = foundation;
		this.blend = blend;
	}

	public static void register() {
		Registry.register(BuiltInRegistries.STRUCTURE_TYPE, ModIds.id("flat_site"), TYPE);
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		if (maxVariation <= 0) {
			return layout.value().findValidGenerationPoint(context);
		}
		if (!context.couldValidBiomeExistOnTopOfChunkCenter()) {
			return Optional.empty();
		}
		int chunkX = context.chunkPos().getMinBlockX() >> 4;
		int chunkZ = context.chunkPos().getMinBlockZ() >> 4;
		int bestVariation = Integer.MAX_VALUE;
		int bestX = chunkX;
		int bestZ = chunkZ;
		for (int i = 0; i < SEARCH_GRID * SEARCH_GRID && bestVariation > 2; i++) {
			// Start at the candidate itself, then spread outwards.
			int ox = OFFSETS[i % SEARCH_GRID];
			int oz = OFFSETS[i / SEARCH_GRID];
			int variation = variation(context, ((chunkX + ox) << 4) + 8, ((chunkZ + oz) << 4) + 8, bestVariation);
			if (variation < bestVariation) {
				bestVariation = variation;
				bestX = chunkX + ox;
				bestZ = chunkZ + oz;
			}
		}
		if (bestVariation > maxVariation && !guaranteed) {
			return Optional.empty();
		}
		GenerationContext moved = new GenerationContext(context.registryAccess(), context.chunkGenerator(),
				context.biomeSource(), context.climateSampler(), context.randomState(), context.structureTemplateManager(),
				context.seed(), new ChunkPos(bestX, bestZ), context.heightAccessor(), context.validBiome());
		return layout.value().findValidGenerationPoint(moved);
	}

	/** Height difference over a 3 x 3 grid spanning the check radius (stops early once it exceeds {@code limit}). */
	private int variation(GenerationContext context, int cx, int cz, int limit) {
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < 9; i++) {
			int dx = (i % 3 - 1) * checkRadius;
			int dz = (i / 3 - 1) * checkRadius;
			int y = context.chunkGenerator().getFirstOccupiedHeight(cx + dx, cz + dz, Heightmap.Types.WORLD_SURFACE_WG,
					context.heightAccessor(), context.randomState());
			min = Math.min(min, y);
			max = Math.max(max, y);
			if (max - min >= limit) {
				return max - min;
			}
		}
		return max - min;
	}

	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
			RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, PiecesContainer pieces) {
		BoundingBox all = pieces.calculateBoundingBox();
		int ground = all.minY() + groundOffset;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockState air = Blocks.AIR.defaultBlockState();
		for (int x = chunkBox.minX(); x <= chunkBox.maxX(); x++) {
			for (int z = chunkBox.minZ(); z <= chunkBox.maxZ(); z++) {
				boolean inside = false;
				for (StructurePiece piece : pieces.pieces()) {
					BoundingBox b = piece.getBoundingBox();
					if (x >= b.minX() && x <= b.maxX() && z >= b.minZ() && z <= b.maxZ()) {
						inside = true;
						break;
					}
				}
				if (inside) {
					// Clear leftover terrain above the ground, then underpin the ground layer.
					int top = Math.min(ground + clearHeight, level.getMaxY());
					for (int y = ground + 1; y <= top; y++) {
						pos.set(x, y, z);
						if (natural(level.getBlockState(pos))) {
							level.setBlock(pos, air, 2);
						}
					}
					if (foundation) {
						underpin(level, pos, x, ground, z);
					}
				} else if (blend > 0) {
					int d = distanceOutside(all, x, z);
					if (d > 0 && d <= blend) {
						blendColumn(level, pos, x, z, ground, d);
					}
				}
			}
		}
	}

	/** Continues the ruin's ground block (or, under air, the nearest natural block) down to solid terrain. */
	private static void underpin(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int ground, int z) {
		pos.set(x, ground, z);
		BlockState top = level.getBlockState(pos);
		if (top.isAir() || !top.isSolidRender()) {
			return;
		}
		BlockState fill = top;
		for (int y = ground - 1; y > ground - MAX_FOUNDATION && y > level.getMinY(); y--) {
			pos.set(x, y, z);
			BlockState below = level.getBlockState(pos);
			if (!below.isAir() && !below.canBeReplaced() && below.getFluidState().isEmpty()) {
				return;
			}
			level.setBlock(pos, fill, 2);
		}
	}

	/** Slopes the natural terrain of one column towards the ruin's ground level ({@code d} blocks away from it). */
	private void blendColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int ground, int d) {
		int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
		double t = (double) d / (blend + 1);
		t = t * t * (3 - 2 * t);
		int target = (int) Math.round(ground + (surface - ground) * t);
		if (surface > target) {
			for (int y = surface; y > target; y--) {
				pos.set(x, y, z);
				if (natural(level.getBlockState(pos))) {
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
				}
			}
			// The cut keeps its surface: the block that was on top goes onto the new top.
			pos.set(x, target, z);
		} else if (surface < target) {
			pos.set(x, surface, z);
			BlockState top = level.getBlockState(pos);
			if (!natural(top)) {
				return;
			}
			BlockState under = top.is(BlockTags.DIRT) || top.is(BlockTags.SAND) ? top : Blocks.STONE.defaultBlockState();
			for (int y = surface + 1; y <= target; y++) {
				pos.set(x, y, z);
				if (level.getBlockState(pos).isAir()) {
					level.setBlock(pos, y == target ? top : under, 2);
				}
			}
		}
	}

	private static int distanceOutside(BoundingBox b, int x, int z) {
		int dx = Math.max(0, Math.max(b.minX() - x, x - b.maxX()));
		int dz = Math.max(0, Math.max(b.minZ() - z, z - b.maxZ()));
		return Math.max(dx, dz);
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
