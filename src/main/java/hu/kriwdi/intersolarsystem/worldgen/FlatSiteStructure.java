package hu.kriwdi.intersolarsystem.worldgen;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

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
			Codec.intRange(1, 64).fieldOf("max_variation").forGetter(s -> s.maxVariation))
			.apply(i, FlatSiteStructure::new));

	public static final StructureType<FlatSiteStructure> TYPE = () -> CODEC;

	private static final int STEP = 24;

	private final Holder<Structure> layout;
	private final int checkRadius;
	private final int maxVariation;

	public FlatSiteStructure(StructureSettings settings, Holder<Structure> layout, int checkRadius, int maxVariation) {
		super(settings);
		this.layout = layout;
		this.checkRadius = checkRadius;
		this.maxVariation = maxVariation;
	}

	public static void register() {
		Registry.register(BuiltInRegistries.STRUCTURE_TYPE, ModIds.id("flat_site"), TYPE);
	}

	@Override
	protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		int cx = context.chunkPos().getMinBlockX() + 8;
		int cz = context.chunkPos().getMinBlockZ() + 8;
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (int dx = -checkRadius; dx <= checkRadius; dx += STEP) {
			for (int dz = -checkRadius; dz <= checkRadius; dz += STEP) {
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

	@Override
	public StructureType<?> type() {
		return TYPE;
	}
}
