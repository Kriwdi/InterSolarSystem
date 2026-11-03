package hu.kriwdi.intersolarsystem.salvage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.UrbanBlocks;

/**
 * An electric grid: generators, battery banks and consumers joined by cables (or standing right next to each other).
 *
 * <p>Power is in kW. Each grid is solved every {@link Machines#INTERVAL} ticks: solar panels first (daylight, open sky),
 * then battery banks, then diesel generators (which only burn fuel when their power is needed). Surplus charges the
 * batteries. Consumers get {@link #satisfaction()} of what they ask for and slow down accordingly.
 *
 * <p>New generators, stores and consumers (a reactor, an RTG, the spaceport's machines, other worlds' power sources)
 * join by being listed in {@link Role}.
 */
public final class PowerGrid {
	/** What a block is to the grid. */
	public enum Role { CABLE, SOLAR, DIESEL, BATTERY, RECYCLER, MODULE, NONE }

	public static final float SOLAR_KW = 2.0F;
	public static final float DIESEL_KW = 8.0F;
	public static final int BATTERY_CAPACITY = 4000;
	private static final int MAX_NODES = 1024;

	public final List<BlockPos> solar = new ArrayList<>();
	public final List<BlockPos> diesel = new ArrayList<>();
	public final List<BlockPos> batteries = new ArrayList<>();
	public final List<BlockPos> consumers = new ArrayList<>();
	public final Set<BlockPos> nodes = new HashSet<>();
	private float satisfaction;
	private float supply;
	private float demand;

	public static Role role(BlockState state) {
		Block b = state.getBlock();
		if (b == ModBlocks.CABLE) {
			return Role.CABLE;
		}
		if (b == ModBlocks.SOLAR_PANEL) {
			return Role.SOLAR;
		}
		if (b == UrbanBlocks.DIESEL_GENERATOR) {
			return Role.DIESEL;
		}
		if (b == UrbanBlocks.BATTERY_BANK) {
			return Role.BATTERY;
		}
		if (b == UrbanBlocks.RECYCLER) {
			return Role.RECYCLER;
		}
		if (b == UrbanBlocks.SHREDDER_MODULE || b == UrbanBlocks.SMELTER_MODULE || b == UrbanBlocks.SORTER_MODULE) {
			return Role.MODULE;
		}
		return Role.NONE;
	}

	/** Whether a cable joins up with this block. */
	public static boolean connects(BlockState state) {
		Role r = role(state);
		return r != Role.NONE && r != Role.MODULE;
	}

	/** Everything joined to {@code start}. */
	public static PowerGrid trace(ServerLevel level, BlockPos start) {
		PowerGrid grid = new PowerGrid();
		ArrayDeque<BlockPos> open = new ArrayDeque<>();
		open.add(start.immutable());
		grid.nodes.add(start.immutable());
		while (!open.isEmpty() && grid.nodes.size() < MAX_NODES) {
			BlockPos pos = open.poll();
			if (!level.isLoaded(pos)) {
				continue;
			}
			Role role = role(level.getBlockState(pos));
			switch (role) {
				case SOLAR -> grid.solar.add(pos);
				case DIESEL -> grid.diesel.add(pos);
				case BATTERY -> grid.batteries.add(pos);
				case RECYCLER -> grid.consumers.add(pos);
				default -> {
				}
			}
			if (role == Role.NONE || role == Role.MODULE) {
				continue;
			}
			for (Direction d : Direction.values()) {
				BlockPos next = pos.relative(d);
				if (!grid.nodes.contains(next) && level.isLoaded(next) && connects(level.getBlockState(next))) {
					grid.nodes.add(next);
					open.add(next);
				}
			}
		}
		return grid;
	}

	/**
	 * Balances one interval: {@code demand} kW wanted by the consumers. Burns diesel and moves battery charge as needed.
	 */
	public void solve(ServerLevel level, MachineStore store, float wanted) {
		demand = wanted;
		boolean day = level.dimensionType().hasSkyLight() && level.isBrightOutside();
		float sun = 0;
		for (BlockPos p : solar) {
			if (day && level.canSeeSky(p.above())) {
				sun += SOLAR_KW;
			}
		}
		float need = demand - sun;
		float fromBattery = 0;
		float fromDiesel = 0;
		if (need > 0) {
			for (BlockPos p : batteries) {
				MachineStore.Machine m = store.machine(level.dimension(), p);
				float take = Math.min(need - fromBattery, m.charge);
				m.charge -= (int) take;
				fromBattery += take;
			}
			for (BlockPos p : diesel) {
				if (fromBattery + fromDiesel >= need) {
					break;
				}
				MachineStore.Machine m = store.machine(level.dimension(), p);
				if (m.fuel > 0) {
					m.fuel = Math.max(0, m.fuel - Machines.INTERVAL);
					fromDiesel += DIESEL_KW;
				}
			}
		} else {
			// Surplus sunlight charges the batteries.
			float spare = -need;
			for (BlockPos p : batteries) {
				MachineStore.Machine m = store.machine(level.dimension(), p);
				int add = (int) Math.min(spare, BATTERY_CAPACITY - m.charge);
				m.charge += add;
				spare -= add;
			}
		}
		supply = sun + fromBattery + fromDiesel;
		satisfaction = demand <= 0 ? 1.0F : Math.min(1.0F, supply / demand);
		store.markDirty();
	}

	public float satisfaction() {
		return satisfaction;
	}

	public float supply() {
		return supply;
	}

	public float demand() {
		return demand;
	}

	/** Generation the grid could give right now (for the status line). */
	public float capacity(ServerLevel level, MachineStore store) {
		boolean day = level.dimensionType().hasSkyLight() && level.isBrightOutside();
		float c = 0;
		for (BlockPos p : solar) {
			if (day && level.canSeeSky(p.above())) {
				c += SOLAR_KW;
			}
		}
		for (BlockPos p : diesel) {
			MachineStore.Machine m = store.peek(level.dimension(), p);
			if (m != null && m.fuel > 0) {
				c += DIESEL_KW;
			}
		}
		for (BlockPos p : batteries) {
			MachineStore.Machine m = store.peek(level.dimension(), p);
			if (m != null && m.charge > 0) {
				c += 10.0F;
			}
		}
		return c;
	}
}
