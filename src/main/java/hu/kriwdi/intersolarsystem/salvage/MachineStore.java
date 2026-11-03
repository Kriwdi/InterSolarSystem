package hu.kriwdi.intersolarsystem.salvage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * The state of the machines of the salvage chain (saved with the world): fuel in generators, charge in battery banks,
 * the queue and progress of recyclers, and how far props have been taken apart. Kept by position, so the machine
 * blocks themselves need no block entities; an entry whose block is gone is dropped on the next tick.
 */
public class MachineStore extends SavedData {
	/** One machine. {@code fuel}: generator burn ticks; {@code charge}: battery energy; {@code queue}: recycler input. */
	public static final class Machine {
		public int fuel;
		public int charge;
		public int progress;
		public float lastSupply;
		public List<ItemStack> queue = new ArrayList<>();

		static final Codec<Machine> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.INT.optionalFieldOf("fuel", 0).forGetter(m -> m.fuel),
				Codec.INT.optionalFieldOf("charge", 0).forGetter(m -> m.charge),
				Codec.INT.optionalFieldOf("progress", 0).forGetter(m -> m.progress),
				ItemStack.CODEC.listOf().optionalFieldOf("queue", List.of()).forGetter(m -> m.queue))
				.apply(i, (fuel, charge, progress, queue) -> {
					Machine m = new Machine();
					m.fuel = fuel;
					m.charge = charge;
					m.progress = progress;
					m.queue = new ArrayList<>(queue);
					return m;
				}));
	}

	public static final Codec<MachineStore> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(Codec.STRING, Machine.CODEC).optionalFieldOf("machines", Map.of()).forGetter(s -> s.machines),
			Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("disassembly", Map.of()).forGetter(s -> s.disassembly))
			.apply(i, (machines, disassembly) -> {
				MachineStore s = new MachineStore();
				s.machines.putAll(machines);
				s.disassembly.putAll(disassembly);
				return s;
			}));
	public static final SavedDataType<MachineStore> TYPE = new SavedDataType<>(ModIds.id("machines"), MachineStore::new,
			CODEC, null);

	private final Map<String, Machine> machines = new HashMap<>();
	private final Map<String, Integer> disassembly = new HashMap<>();

	public static MachineStore get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public static String key(ResourceKey<Level> dimension, BlockPos pos) {
		return dimension.identifier() + "|" + pos.getX() + "|" + pos.getY() + "|" + pos.getZ();
	}

	public static BlockPos pos(String key) {
		String[] p = key.split("\\|");
		return new BlockPos(Integer.parseInt(p[1]), Integer.parseInt(p[2]), Integer.parseInt(p[3]));
	}

	public static String dimension(String key) {
		return key.substring(0, key.indexOf('|'));
	}

	public Machine machine(ResourceKey<Level> dimension, BlockPos pos) {
		setDirty();
		return machines.computeIfAbsent(key(dimension, pos), k -> new Machine());
	}

	public Machine peek(ResourceKey<Level> dimension, BlockPos pos) {
		return machines.get(key(dimension, pos));
	}

	public Map<String, Machine> machines() {
		return machines;
	}

	public void remove(String key) {
		machines.remove(key);
		disassembly.remove(key);
		setDirty();
	}

	public int disassemblyStep(ResourceKey<Level> dimension, BlockPos pos) {
		return disassembly.getOrDefault(key(dimension, pos), 0);
	}

	public void setDisassemblyStep(ResourceKey<Level> dimension, BlockPos pos, int step) {
		if (step <= 0) {
			disassembly.remove(key(dimension, pos));
		} else {
			disassembly.put(key(dimension, pos), step);
		}
		setDirty();
	}

	public void markDirty() {
		setDirty();
	}
}
