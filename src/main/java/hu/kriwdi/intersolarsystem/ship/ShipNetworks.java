package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ShipMachineBlock;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Cell;

/**
 * The ship's conduits and supplies, checked before every launch: the coolant pipes must join the reactor to both
 * engine ports and to the coolant tank, the power cable must run from the reactor to the bridge, and the reactor,
 * the coolant tank and the life support unit must each hold enough for one more flight. Pipes and cables can be
 * broken and laid again, anywhere.
 */
public final class ShipNetworks {
	private static final int MAX_CELLS = 4096;

	private ShipNetworks() {
	}

	/** What stops the launch (empty when the systems are ready). */
	public static List<Component> problems(ServerLevel interior) {
		List<Component> out = new ArrayList<>();
		ShipInteriorLayout layout = ShipInteriorLayout.capsule();
		BlockPos reactor = ShipInteriorBuilder.at(layout.reactor());
		Set<BlockPos> pipes = network(interior, ModBlocks.PIPE, List.of(reactor));
		boolean engines = true;
		for (Cell port : ShipInteriorLayout.ENGINE_PORTS) {
			engines &= touches(pipes, ShipInteriorBuilder.at(port));
		}
		if (!engines) {
			out.add(Component.translatable("message.intersolarsystem.checklist.pipes_engine"));
		}
		if (!touches(pipes, ShipInteriorBuilder.at(layout.coolantTank()))) {
			out.add(Component.translatable("message.intersolarsystem.checklist.pipes_coolant"));
		}
		Set<BlockPos> cables = network(interior, ModBlocks.CABLE, List.of(reactor, reactor.above(), reactor.above(2)));
		boolean bridge = cables.stream().anyMatch(p -> p.getZ() - ShipDimensions.ANCHOR.getZ() >= 15);
		if (!bridge) {
			out.add(Component.translatable("message.intersolarsystem.checklist.cable_bridge"));
		}
		supply(interior, layout.reactor(), "reactor", out);
		supply(interior, layout.coolantTank(), "coolant_tank", out);
		supply(interior, layout.lifeSupport(), "life_support_unit", out);
		return out;
	}

	private static void supply(ServerLevel interior, Cell cell, String key, List<Component> out) {
		BlockState state = interior.getBlockState(ShipInteriorBuilder.at(cell));
		if (state.getBlock() instanceof ShipMachineBlock && state.getValue(ShipMachineBlock.LEVEL) <= 0) {
			out.add(Component.translatable("message.intersolarsystem.checklist.empty." + key));
		}
	}

	/** One flight's worth from each machine (normal flights; test hops are free). */
	public static void consume(ServerLevel interior) {
		ShipInteriorLayout layout = ShipInteriorLayout.capsule();
		for (Cell cell : new Cell[] {layout.reactor(), layout.coolantTank(), layout.lifeSupport()}) {
			BlockPos pos = ShipInteriorBuilder.at(cell);
			BlockState state = interior.getBlockState(pos);
			if (state.getBlock() instanceof ShipMachineBlock) {
				int level = state.getValue(ShipMachineBlock.LEVEL);
				interior.setBlock(pos, state.setValue(ShipMachineBlock.LEVEL, Math.max(0, level - 1)), Block.UPDATE_ALL);
			}
		}
	}

	/** All conduit cells of one kind reachable from the cells next to the sources. */
	static Set<BlockPos> network(ServerLevel level, Block kind, List<BlockPos> sources) {
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		for (BlockPos source : sources) {
			for (Direction d : Direction.values()) {
				BlockPos n = source.relative(d);
				if (level.getBlockState(n).is(kind) && seen.add(n)) {
					queue.add(n);
				}
			}
		}
		while (!queue.isEmpty() && seen.size() < MAX_CELLS) {
			BlockPos p = queue.poll();
			for (Direction d : Direction.values()) {
				BlockPos n = p.relative(d);
				if (level.getBlockState(n).is(kind) && seen.add(n)) {
					queue.add(n);
				}
			}
		}
		return seen;
	}

	private static boolean touches(Set<BlockPos> network, BlockPos machine) {
		for (Direction d : Direction.values()) {
			if (network.contains(machine.relative(d))) {
				return true;
			}
		}
		return false;
	}
}
