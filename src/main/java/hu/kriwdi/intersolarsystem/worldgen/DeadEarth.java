package hu.kriwdi.intersolarsystem.worldgen;

import java.util.List;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import hu.kriwdi.intersolarsystem.core.ModIds;

/** The biomes of the dead Earth (phase 7). Their JSON is written by tools/worldgen/make_worldgen.py. */
public final class DeadEarth {
	public static final ResourceKey<Biome> ASH_DESERT = key("ash_desert");
	public static final ResourceKey<Biome> DRIED_SEABED = key("dried_seabed");
	public static final ResourceKey<Biome> PETRIFIED_FOREST = key("petrified_forest");
	public static final ResourceKey<Biome> IRRADIATED_CRATER = key("irradiated_crater");
	public static final ResourceKey<Biome> FROZEN_MEGALOPOLIS = key("frozen_megalopolis");
	public static final ResourceKey<Biome> ACID_SWAMP = key("acid_swamp");
	public static final ResourceKey<Biome> JUNK_MOUNTAINS = key("junk_mountains");
	public static final ResourceKey<Biome> SALT_FLATS = key("salt_flats");
	public static final ResourceKey<Biome> DEEP_CANYONS = key("deep_canyons");
	public static final ResourceKey<Biome> OASIS = key("oasis");
	public static final ResourceKey<Biome> ABANDONED_FARMLAND = key("abandoned_farmland");
	public static final ResourceKey<Biome> OIL_FIELD = key("oil_field");

	public static final List<ResourceKey<Biome>> ALL = List.of(ASH_DESERT, DRIED_SEABED, PETRIFIED_FOREST,
			IRRADIATED_CRATER, FROZEN_MEGALOPOLIS, ACID_SWAMP, JUNK_MOUNTAINS, SALT_FLATS, DEEP_CANYONS, OASIS,
			ABANDONED_FARMLAND, OIL_FIELD);

	private DeadEarth() {
	}

	private static ResourceKey<Biome> key(String name) {
		return ResourceKey.create(Registries.BIOME, ModIds.id(name));
	}
}
