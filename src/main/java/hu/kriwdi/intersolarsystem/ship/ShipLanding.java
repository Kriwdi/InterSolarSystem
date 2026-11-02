package hu.kriwdi.intersolarsystem.ship;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.space.MoonSites;
import hu.kriwdi.intersolarsystem.space.OrbitStation;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Cell;
import hu.kriwdi.intersolarsystem.ship.ShipLandingSearch.Column;

/** Finds a landing spot in the Overworld and puts the ship's hatch there. */
public final class ShipLanding {
	public static final int SEARCH_RADIUS = 32;

	private ShipLanding() {
	}

	/** First suitable hatch cell around (x, z), nearest first. */
	public static Optional<BlockPos> findHatchCell(ServerLevel level, int x, int z, int radius) {
		for (Column column : ShipLandingSearch.candidates(x, z, radius)) {
			Optional<BlockPos> cell = suitableCell(level, column.x(), column.z());
			if (cell.isPresent()) {
				return cell;
			}
		}
		return Optional.empty();
	}

	/**
	 * Surface height of a column. {@code Level#getHeight} does NOT load chunks: for an unloaded chunk it returns the
	 * world's minimum height, so the chunk is loaded first (the ship lands while everyone is inside the ship).
	 */
	private static int surfaceHeight(ServerLevel level, int x, int z) {
		level.getChunk(x >> 4, z >> 4);
		return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
	}

	private static Optional<BlockPos> suitableCell(ServerLevel level, int x, int z) {
		int y = surfaceHeight(level, x, z);
		if (y <= level.getMinY() + 1) {
			return Optional.empty();
		}
		BlockPos cell = new BlockPos(x, y, z);
		BlockPos floor = cell.below();
		BlockState here = level.getBlockState(cell);
		BlockState below = level.getBlockState(floor);
		if (!here.canBeReplaced() || !here.getFluidState().isEmpty()) {
			return Optional.empty();
		}
		if (!below.isFaceSturdy(level, floor, Direction.UP) || !below.getFluidState().isEmpty()) {
			return Optional.empty();
		}
		Optional<Cell> standing = ShipBoardingRules.findStandingSpot(new Cell(x, y, z),
				c -> ShipBoarding.isSturdyFloor(level, c), c -> ShipBoarding.isFree(level, c));
		return standing.isPresent() ? Optional.of(cell) : Optional.empty();
	}

	/** Where the ship landed, and whether it had to fall back to the world spawn. */
	public record Landing(BlockPos hatch, boolean usedSpawn, boolean docked, boolean moon) {
		public Landing(BlockPos hatch, boolean usedSpawn) {
			this(hatch, usedSpawn, false, false);
		}

		public Landing(BlockPos hatch, boolean usedSpawn, boolean docked) {
			this(hatch, usedSpawn, docked, false);
		}
	}

	/**
	 * Lands the ship at the flight destination (or near the world spawn if no dry spot is nearby), places the hatch
	 * and moves the ship's position there. This is the critical part of a landing; announcements come from
	 * {@link #announce} after the phase has advanced.
	 */
	public static Landing place(MinecraftServer server, ShipState state) {
		if (ShipDimensions.ORBIT.identifier().toString().equals(state.travel().landIn())) {
			ServerLevel orbit = server.getLevel(ShipDimensions.ORBIT);
			if (orbit != null) {
				BlockPos hatch = OrbitStation.dock(orbit);
				state.setLandingSite(new ShipState.LandingSite(orbit.dimension().identifier().toString(),
						hatch.getX(), hatch.getY(), hatch.getZ()));
				return new Landing(hatch, false, true);
			}
		}
		if (ShipDimensions.MOON.identifier().toString().equals(state.travel().landIn())) {
			ServerLevel moon = server.getLevel(ShipDimensions.MOON);
			if (moon != null) {
				MoonSites.ensureBuilt(moon);
				ShipState.Destination target = state.destination()
						.orElse(new ShipState.Destination(MoonSites.LANDING_ZONE[0], MoonSites.LANDING_ZONE[1]));
				BlockPos hatch = findHatchCell(moon, target.x(), target.z(), SEARCH_RADIUS)
						.orElseGet(() -> new BlockPos(target.x(), surfaceHeight(moon, target.x(), target.z()), target.z()));
				moon.setBlock(hatch, ModBlocks.SHIP_HATCH.defaultBlockState(), Block.UPDATE_ALL);
				state.setLandingSite(new ShipState.LandingSite(moon.dimension().identifier().toString(),
						hatch.getX(), hatch.getY(), hatch.getZ()));
				return new Landing(hatch, false, false, true);
			}
		}
		ServerLevel overworld = server.overworld();
		LevelData.RespawnData spawn = server.getRespawnData();
		BlockPos spawnPos = spawn.pos();
		ShipState.Destination destination = state.destination()
				.orElse(new ShipState.Destination(spawnPos.getX(), spawnPos.getZ()));
		boolean usedSpawn = false;
		Optional<BlockPos> cell = findHatchCell(overworld, destination.x(), destination.z(), SEARCH_RADIUS);
		if (cell.isEmpty()) {
			usedSpawn = true;
			cell = findHatchCell(overworld, spawnPos.getX(), spawnPos.getZ(), SEARCH_RADIUS);
		}
		BlockPos hatch = cell.orElseGet(() -> new BlockPos(spawnPos.getX(),
				surfaceHeight(overworld, spawnPos.getX(), spawnPos.getZ()),
				spawnPos.getZ()));
		overworld.setBlock(hatch, ModBlocks.SHIP_HATCH.defaultBlockState(), Block.UPDATE_ALL);
		state.setLandingSite(new ShipState.LandingSite(overworld.dimension().identifier().toString(),
				hatch.getX(), hatch.getY(), hatch.getZ()));
		return new Landing(hatch, usedSpawn);
	}

	public static void announce(MinecraftServer server, Landing landing) {
		String key = landing.moon() ? "message.intersolarsystem.moon.landed"
				: landing.docked() ? "message.intersolarsystem.orbit.docked"
				: landing.usedSpawn() ? "message.intersolarsystem.ship.landed_spawn" : "message.intersolarsystem.ship.landed";
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior != null) {
			interior.players().forEach(p -> p.sendSystemMessage(
					Component.translatable(key, landing.hatch().getX(), landing.hatch().getZ())));
		}
	}
}
