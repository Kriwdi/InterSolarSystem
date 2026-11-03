package hu.kriwdi.intersolarsystem.worldgen;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import hu.kriwdi.intersolarsystem.core.ModIds;

/** Registers the mod's world generation pieces (used by the JSON in data/intersolarsystem/worldgen). */
public final class ModWorldgen {
	private ModWorldgen() {
	}

	public static void initialize() {
		FlatSiteStructure.register();
		Registry.register(BuiltInRegistries.FEATURE_TYPE, ModIds.id("lunar_surface"), LunarSurfaceFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, ModIds.id("crater"), CraterFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, ModIds.id("dead_tree"), DeadTreeFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, ModIds.id("strata"), StrataFeature.CODEC);
		Registry.register(BuiltInRegistries.FEATURE_TYPE, ModIds.id("fallout_crater"), FalloutCraterFeature.CODEC);
	}
}
