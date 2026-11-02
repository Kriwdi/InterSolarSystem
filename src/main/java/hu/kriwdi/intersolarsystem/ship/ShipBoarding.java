package hu.kriwdi.intersolarsystem.ship;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Cell;

/** Server-side boarding and leaving. All ship state changes go through {@link ShipState}. */
public final class ShipBoarding {
	private ShipBoarding() {
	}

	/** Called when a player uses a ship hatch: boards from the world, leaves from the interior. */
	public static void use(ServerLevel level, BlockPos hatchPos, ServerPlayer player) {
		if (level.dimension().equals(ShipDimensions.INTERIOR)) {
			leave(level.getServer(), player);
		} else {
			board(level, hatchPos, player);
		}
	}

	private static void board(ServerLevel level, BlockPos hatchPos, ServerPlayer player) {
		MinecraftServer server = level.getServer();
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior == null) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.interior_missing"));
			return;
		}
		ShipState state = ShipStateAccess.get(server);
		Optional<ShipState.LandingSite> site = state.landingSite();
		String dimension = level.dimension().identifier().toString();
		boolean matches = site.map(s -> s.dimension().equals(dimension) && s.x() == hatchPos.getX()
				&& s.y() == hatchPos.getY() && s.z() == hatchPos.getZ()).orElse(false);
		boolean positionHatchPresent = site.map(s -> isHatchAt(server, s)).orElse(false);
		HatchAction action = ShipHatchRules.decide(state.phase(), site.isPresent(), matches, positionHatchPresent);
		switch (action) {
			case BUSY -> {
				player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.busy"));
				return;
			}
			case NOT_CONNECTED -> {
				player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.not_connected"));
				return;
			}
			case REGISTER, REDOCK -> {
				state.setLandingSite(new ShipState.LandingSite(dimension, hatchPos.getX(), hatchPos.getY(), hatchPos.getZ()));
				if (action == HatchAction.REDOCK) {
					player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.redocked"));
				}
			}
			case BOARD -> {
			}
		}
		ShipInteriorLayout layout = ShipInteriorLayout.capsule();
		Cell hatchCell = layout.hatch();
		BlockPos interiorHatch = ShipDimensions.ANCHOR.offset(hatchCell.dx(), hatchCell.dy(), hatchCell.dz());
		if (interior.getBlockState(interiorHatch).getBlock() != ModBlocks.SHIP_HATCH) {
			// First build clears the inside; a repair (hatch lost) keeps whatever players put inside.
			ShipInteriorBuilder.build(interior, !state.interiorBuilt());
			state.setInteriorBuilt(true);
		}
		ShipInteriorBuilder.ensureStation(interior);
		Cell entry = layout.entry();
		Vec3 target = Vec3.atBottomCenterOf(ShipDimensions.ANCHOR.offset(entry.dx(), entry.dy(), entry.dz()));
		player.teleport(new TeleportTransition(interior, target, Vec3.ZERO, player.getYRot(), 0.0F,
				TeleportTransition.DO_NOTHING));
		player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.boarded"));
	}

	private static void leave(MinecraftServer server, ServerPlayer player) {
		ShipState state = ShipStateAccess.get(server);
		if (!ShipHatchRules.canLeave(state.phase())) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.cannot_leave"));
			return;
		}
		Optional<TeleportTransition> atSite = state.landingSite().flatMap(site -> transitionBesideHatch(server, site, player));
		if (atSite.isPresent()) {
			player.teleport(atSite.get());
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.exited"));
			return;
		}
		LevelData.RespawnData spawn = server.getRespawnData();
		ServerLevel spawnLevel = server.getLevel(spawn.dimension());
		if (spawnLevel == null) {
			spawnLevel = server.overworld();
		}
		player.teleport(new TeleportTransition(spawnLevel, Vec3.atBottomCenterOf(spawn.pos()), Vec3.ZERO,
				player.getYRot(), 0.0F, TeleportTransition.DO_NOTHING));
		player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.exit_fallback"));
	}

	/** The loaded level of a landing site, or null if its dimension is unknown or not loaded. */
	static ServerLevel levelOf(MinecraftServer server, ShipState.LandingSite site) {
		Identifier dimensionId = Identifier.tryParse(site.dimension());
		return dimensionId == null ? null : server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
	}

	static boolean isHatchAt(MinecraftServer server, ShipState.LandingSite site) {
		ServerLevel target = levelOf(server, site);
		return target != null
				&& target.getBlockState(new BlockPos(site.x(), site.y(), site.z())).getBlock() == ModBlocks.SHIP_HATCH;
	}

	private static Optional<TeleportTransition> transitionBesideHatch(MinecraftServer server, ShipState.LandingSite site,
			ServerPlayer player) {
		ServerLevel target = levelOf(server, site);
		if (target == null || !isHatchAt(server, site)) {
			return Optional.empty();
		}
		return ShipBoardingRules.findStandingSpot(new Cell(site.x(), site.y(), site.z()),
						cell -> isSturdyFloor(target, cell), cell -> isFree(target, cell))
				.map(feet -> new TeleportTransition(target, Vec3.atBottomCenterOf(new BlockPos(feet.dx(), feet.dy(), feet.dz())),
						Vec3.ZERO, player.getYRot(), 0.0F, TeleportTransition.DO_NOTHING));
	}

	static boolean isSturdyFloor(Level level, Cell cell) {
		BlockPos pos = new BlockPos(cell.dx(), cell.dy(), cell.dz());
		return level.getBlockState(pos).isFaceSturdy(level, pos, Direction.UP);
	}

	static boolean isFree(Level level, Cell cell) {
		BlockPos pos = new BlockPos(cell.dx(), cell.dy(), cell.dz());
		BlockState state = level.getBlockState(pos);
		return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty();
	}
}
