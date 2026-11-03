package hu.kriwdi.intersolarsystem.ship;

import java.util.Set;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import hu.kriwdi.intersolarsystem.content.ModBlocks;

/**
 * Keeps the ship whole: its walls, viewports, consoles and hatch cannot be broken from inside (not even in creative,
 * where block hardness does not count), its outer hull cannot be taken apart where it landed (only in creative while
 * sneaking, to clean up by hand), and anyone who still ends up outside the ship's room in the interior dimension is
 * put back inside.
 */
public final class ShipInteriorGuard {
	private static final Set<ShipInteriorLayout.Cell> PROTECTED = ShipInteriorLayout.capsule().protectedCells();

	private ShipInteriorGuard() {
	}

	public static void register() {
		PlayerBlockBreakEvents.BEFORE.register(ShipInteriorGuard::allowBreak);
		ServerTickEvents.END_SERVER_TICK.register(ShipInteriorGuard::tick);
	}

	private static boolean allowBreak(Level level, Player player, BlockPos pos, BlockState state,
			net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
		if (level.dimension().equals(ShipDimensions.INTERIOR)) {
			BlockPos rel = pos.subtract(ShipDimensions.ANCHOR);
			return !PROTECTED.contains(new ShipInteriorLayout.Cell(rel.getX(), rel.getY(), rel.getZ()));
		}
		if (state.is(ModBlocks.SHIP_PLATING) || state.is(ModBlocks.SHIP_PORTHOLE) || state.is(ModBlocks.SHIP_THRUSTER)) {
			return player.isCreative() && player.isShiftKeyDown();
		}
		return true;
	}

	private static void tick(MinecraftServer server) {
		if (server.overworld().getGameTime() % 10 != 0) {
			return;
		}
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior == null) {
			return;
		}
		for (ServerPlayer player : java.util.List.copyOf(interior.players())) {
			if (player.isSpectator()) {
				continue;
			}
			BlockPos rel = player.blockPosition().subtract(ShipDimensions.ANCHOR);
			if (!ShipInteriorLayout.inside(rel.getX(), rel.getY(), rel.getZ())) {
				ShipInteriorLayout.Cell entry = ShipInteriorLayout.capsule().entry();
				Vec3 target = Vec3.atBottomCenterOf(ShipInteriorBuilder.at(entry));
				player.teleport(new TeleportTransition(interior, target, Vec3.ZERO, player.getYRot(), 0.0F,
						TeleportTransition.DO_NOTHING));
			}
		}
	}
}
