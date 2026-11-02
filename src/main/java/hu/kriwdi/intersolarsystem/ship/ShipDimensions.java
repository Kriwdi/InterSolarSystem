package hu.kriwdi.intersolarsystem.ship;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import hu.kriwdi.intersolarsystem.core.ModIds;

public final class ShipDimensions {
	/** The ship's interior dimension (defined by datapack JSON). */
	public static final ResourceKey<Level> INTERIOR = ResourceKey.create(Registries.DIMENSION, ModIds.id("ship_interior"));

	/** Earth orbit: the shared space dimension with the ISS (defined by datapack JSON). */
	public static final ResourceKey<Level> ORBIT = ResourceKey.create(Registries.DIMENSION, ModIds.id("orbit"));

	/** Lowest corner of the interior room inside {@link #INTERIOR}. */
	public static final BlockPos ANCHOR = new BlockPos(0, 64, 0);

	private ShipDimensions() {
	}
}
