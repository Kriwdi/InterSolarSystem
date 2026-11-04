package hu.kriwdi.intersolarsystem.space;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ShipFacingBlock;
import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipShell;
import hu.kriwdi.intersolarsystem.ship.ShipState;
import hu.kriwdi.intersolarsystem.ship.ShipStateAccess;

/**
 * Builds the full-size ISS in the orbit dimension the first time a ship arrives (or replaces the small one of older
 * worlds), and docks the ship at its front port: the ship's hull hangs in space in front of the station, its hatch
 * against the docking port.
 */
public final class OrbitStation {
	/** The station's origin (the module axis under the truss). */
	public static final BlockPos CENTRE = new BlockPos(0, 120, 0);
	public static final ResourceKey<LootTable> SUPPLY_LOOT = ResourceKey.create(Registries.LOOT_TABLE,
			ModIds.id("chests/iss_supply"));
	/** The ship's hatch faces the station (south, +z); the ship's body lies forward of the port. */
	public static final Direction DOCK_FACING = Direction.SOUTH;

	private OrbitStation() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(OrbitStation::ensureCurrent);
	}

	public static BlockPos dockHatch() {
		int[] d = IssLayout.DOCK_HATCH;
		return CENTRE.offset(d[0], d[1], d[2]);
	}

	/** Whether the full-size station stands (the wreck field is placed with it). */
	public static boolean present(ServerLevel orbit) {
		return isCurrent(orbit);
	}

	private static boolean isCurrent(ServerLevel orbit) {
		int[] m = IssLayout.MARKER;
		BlockPos marker = CENTRE.offset(m[0], m[1], m[2]);
		orbit.getChunk(marker.getX() >> 4, marker.getZ() >> 4);
		return orbit.getBlockState(marker).is(ModBlocks.STATION_HULL);
	}

	/** The ship docks here; the station is built first if it is not there yet. Returns the hatch position. */
	public static BlockPos dock(ServerLevel orbit) {
		if (!isCurrent(orbit)) {
			build(orbit);
		}
		BlockPos hatch = dockHatch();
		orbit.setBlock(hatch, ModBlocks.SHIP_HATCH.defaultBlockState().setValue(ShipFacingBlock.FACING, DOCK_FACING),
				Block.UPDATE_ALL);
		ShipShell.build(orbit, hatch, DOCK_FACING, ShipShell.Mode.DOCKED);
		return hatch;
	}

	/** Builds the station; the small station of older worlds is cleared away first. */
	public static void build(ServerLevel orbit) {
		clearOldStation(orbit);
		for (IssLayout.Placement p : IssLayout.build()) {
			BlockPos pos = CENTRE.offset(p.x(), p.y(), p.z());
			orbit.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
			orbit.setBlock(pos, stateFor(p.piece()), Block.UPDATE_CLIENTS);
			if (p.piece() == IssLayout.Piece.SUPPLY) {
				if (orbit.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity container) {
					container.setLootTable(SUPPLY_LOOT);
					container.setLootTableSeed(orbit.getRandom().nextLong());
				}
			}
		}
		SpaceSites.ensureOrbit(orbit);
	}

	/** Removes the blocks of the first, small station (only station and ship blocks; nothing else is touched). */
	private static void clearOldStation(ServerLevel orbit) {
		for (int x = -38; x <= 38; x++) {
			for (int z = -23; z <= 19; z++) {
				orbit.getChunk((CENTRE.getX() + x) >> 4, (CENTRE.getZ() + z) >> 4);
				for (int y = -4; y <= 8; y++) {
					BlockPos pos = CENTRE.offset(x, y, z);
					BlockState st = orbit.getBlockState(pos);
					if (st.is(ModBlocks.STATION_HULL) || st.is(ModBlocks.STATION_FOIL) || st.is(ModBlocks.STATION_WINDOW)
							|| st.is(ModBlocks.STATION_LIGHT) || st.is(ModBlocks.TRUSS) || st.is(ModBlocks.SOLAR_PANEL)
							|| st.is(Blocks.BARREL) || st.is(ModBlocks.SHIP_HATCH) || ShipShell.isHull(st)) {
						orbit.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
					}
				}
			}
		}
	}

	/**
	 * Older worlds: a small station is replaced by the full-size one when the server starts, and a ship docked at the
	 * old port moves to the new one.
	 */
	static void ensureCurrent(MinecraftServer server) {
		ServerLevel orbit = server.getLevel(ShipDimensions.ORBIT);
		ShipState state = ShipStateAccess.get(server);
		if (orbit == null || state.isReadOnly()) {
			return;
		}
		BlockPos oldPort = CENTRE.offset(0, -2, -20);
		orbit.getChunk(oldPort.getX() >> 4, oldPort.getZ() >> 4);
		boolean oldStation = orbit.getBlockState(oldPort).is(ModBlocks.SHIP_HATCH)
				|| orbit.getBlockState(oldPort.below()).is(ModBlocks.STATION_HULL);
		if (!oldStation || isCurrent(orbit)) {
			return;
		}
		build(orbit);
		boolean docked = state.landingSite().map(s -> ShipDimensions.ORBIT.identifier().toString().equals(s.dimension()))
				.orElse(false);
		if (docked) {
			BlockPos hatch = dock(orbit);
			state.setLandingSite(new ShipState.LandingSite(orbit.dimension().identifier().toString(), hatch.getX(),
					hatch.getY(), hatch.getZ()));
		}
	}

	private static BlockState stateFor(IssLayout.Piece piece) {
		return switch (piece) {
			case HULL, DOCK_FLOOR -> ModBlocks.STATION_HULL.defaultBlockState();
			case FOIL_HULL -> ModBlocks.STATION_FOIL.defaultBlockState();
			case WINDOW -> ModBlocks.STATION_WINDOW.defaultBlockState();
			case GLASS -> ModBlocks.STATION_GLASS.defaultBlockState();
			case LIGHT -> ModBlocks.STATION_LIGHT.defaultBlockState();
			case TRUSS -> ModBlocks.TRUSS.defaultBlockState();
			case SOLAR -> ModBlocks.SOLAR_PANEL.defaultBlockState();
			case RADIATOR -> ModBlocks.STATION_RADIATOR.defaultBlockState();
			case SUPPLY -> Blocks.BARREL.defaultBlockState();
			case AIR -> Blocks.AIR.defaultBlockState();
		};
	}
}
