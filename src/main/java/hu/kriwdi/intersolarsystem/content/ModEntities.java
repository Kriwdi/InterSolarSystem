package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import hu.kriwdi.intersolarsystem.core.ModIds;

public final class ModEntities {
	/**
	 * What a sitting player rides (see {@link AstronautSeatBlock}). It must stay serialisable (no noSave()): the server
	 * refuses to let anyone ride an entity type that cannot be saved. An empty seat removes itself on its next tick.
	 */
	public static final EntityType<SeatEntity> SEAT = register("seat",
			EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC).sized(0.4F, 0.2F).noSummon()
					.clientTrackingRange(8));

	private ModEntities() {
	}

	private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name,
			EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, ModIds.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	/** Loads the class, which registers everything above. */
	public static void initialize() {
	}
}
