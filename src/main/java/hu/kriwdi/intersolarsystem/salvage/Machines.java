package hu.kriwdi.intersolarsystem.salvage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.content.UrbanBlocks;

/**
 * The salvage machines at work: recyclers turn what they are fed into materials, powered by the grid
 * ({@link PowerGrid}); diesel generators take fuel; battery banks show their charge.
 *
 * <p><b>Recycler.</b> Right click with an item: the whole stack goes into the queue (up to {@link #QUEUE} stacks).
 * Empty hand: status (power, queue, yield, modules). Sneak with an empty hand: the queue comes back out. Finished
 * materials go into a container next to the recycler (chest, barrel…) or pop out on top.
 * <p><b>Modules</b> touching the recycler: a shredder doubles the speed, a smelter adds 15% yield (and smelts scrap
 * metal into iron straight away), a sorter adds 10% yield and lets rare materials out. Yield of a bare recycler is
 * 55-75% (random per batch), up to 100% with modules.
 */
public final class Machines {
	public static final int INTERVAL = 10;
	public static final int QUEUE = 4;
	/** Work per item at full power (in ticks); halved by a shredder. */
	private static final int WORK = 160;
	private static final float RECYCLER_KW = 4.0F;
	private static final float MODULE_KW = 2.0F;

	private Machines() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			if (server.getTickCount() % INTERVAL == 0) {
				tick(server);
			}
		});
	}

	// ---------------------------------------------------------------- ticking
	private static void tick(MinecraftServer server) {
		MachineStore store = MachineStore.get(server);
		Set<BlockPos> solved = new HashSet<>();
		List<String> gone = new ArrayList<>();
		for (ServerLevel level : server.getAllLevels()) {
			String dim = level.dimension().identifier().toString();
			solved.clear();
			for (Map.Entry<String, MachineStore.Machine> e : new ArrayList<>(store.machines().entrySet())) {
				if (!MachineStore.dimension(e.getKey()).equals(dim)) {
					continue;
				}
				BlockPos pos = MachineStore.pos(e.getKey());
				if (!level.isLoaded(pos)) {
					continue;
				}
				BlockState state = level.getBlockState(pos);
				PowerGrid.Role role = PowerGrid.role(state);
				if (role == PowerGrid.Role.NONE || role == PowerGrid.Role.CABLE) {
					// The machine is gone: whatever was queued falls out.
					for (ItemStack stack : e.getValue().queue) {
						level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
					}
					gone.add(e.getKey());
					continue;
				}
				if (role == PowerGrid.Role.RECYCLER && !e.getValue().queue.isEmpty() && !solved.contains(pos)) {
					PowerGrid grid = PowerGrid.trace(level, pos);
					float wanted = 0;
					for (BlockPos c : grid.consumers) {
						MachineStore.Machine m = store.peek(level.dimension(), c);
						if (m != null && !m.queue.isEmpty()) {
							wanted += RECYCLER_KW + MODULE_KW * modules(level, c).count();
						}
					}
					grid.solve(level, store, wanted);
					for (BlockPos c : grid.consumers) {
						solved.add(c);
						MachineStore.Machine m = store.peek(level.dimension(), c);
						if (m != null && !m.queue.isEmpty()) {
							m.lastSupply = grid.satisfaction();
							work(level, c, m, grid.satisfaction());
						}
					}
					for (BlockPos d : grid.diesel) {
						MachineStore.Machine m = store.peek(level.dimension(), d);
						if (m != null && m.fuel > 0 && grid.supply() > 0 && level.random.nextInt(3) == 0) {
							level.sendParticles(ParticleTypes.LARGE_SMOKE, d.getX() + 0.75, d.getY() + 1.7, d.getZ() + 0.6, 1,
									0.02, 0.05, 0.02, 0.01);
							level.playSound(null, d, SoundEvents.MINECART_RIDING, SoundSource.BLOCKS, 0.15F, 0.6F);
						}
					}
				}
			}
		}
		gone.forEach(store::remove);
	}

	private record Modules(boolean shredder, boolean smelter, boolean sorter) {
		int count() {
			return (shredder ? 1 : 0) + (smelter ? 1 : 0) + (sorter ? 1 : 0);
		}
	}

	private static Modules modules(ServerLevel level, BlockPos recycler) {
		boolean shredder = false;
		boolean smelter = false;
		boolean sorter = false;
		for (Direction d : Direction.values()) {
			Block b = level.getBlockState(recycler.relative(d)).getBlock();
			shredder |= b == UrbanBlocks.SHREDDER_MODULE;
			smelter |= b == UrbanBlocks.SMELTER_MODULE;
			sorter |= b == UrbanBlocks.SORTER_MODULE;
		}
		return new Modules(shredder, smelter, sorter);
	}

	private static void work(ServerLevel level, BlockPos pos, MachineStore.Machine m, float power) {
		if (power <= 0.01F) {
			return;
		}
		Modules mods = modules(level, pos);
		m.progress += (int) (INTERVAL * power * (mods.shredder() ? 2 : 1));
		if (level.random.nextInt(2) == 0) {
			level.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 3, 0.25, 0.1, 0.25, 0.05);
			level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.35F, 0.7F + level.random.nextFloat() * 0.3F);
		}
		if (m.progress < WORK) {
			return;
		}
		m.progress = 0;
		ItemStack head = m.queue.get(0);
		List<SalvageTable.Output> outputs = SalvageTable.get(BuiltInRegistries.ITEM.getKey(head.getItem()).toString());
		ItemStack one = head.split(1);
		if (head.isEmpty()) {
			m.queue.remove(0);
		}
		if (outputs == null) {
			emit(level, pos, one);
			return;
		}
		float yield = 0.55F + level.random.nextFloat() * 0.2F + (mods.smelter() ? 0.15F : 0) + (mods.sorter() ? 0.1F : 0);
		yield = Math.min(1.0F, yield);
		for (SalvageTable.Output out : outputs) {
			if (out.rare() && !mods.sorter()) {
				continue;
			}
			Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(out.item()));
			if (item == Items.AIR) {
				continue;
			}
			float amount = out.count() * yield;
			int count = (int) amount + (level.random.nextFloat() < amount - (int) amount ? 1 : 0);
			if (out.rare() && level.random.nextFloat() > 0.35F) {
				count = 0;
			}
			if (mods.smelter() && item == ModItems.SCRAP_METAL) {
				// The smelter melts scrap straight into iron.
				item = Items.IRON_NUGGET;
				count *= 6;
			}
			while (count > 0) {
				int n = Math.min(count, item.getDefaultMaxStackSize());
				emit(level, pos, new ItemStack(item, n));
				count -= n;
			}
		}
		level.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.25F, 1.6F);
	}

	/** Into a container next to the machine, or out on top. */
	private static void emit(ServerLevel level, BlockPos pos, ItemStack stack) {
		for (Direction d : Direction.values()) {
			BlockEntity be = level.getBlockEntity(pos.relative(d));
			if (be instanceof Container container) {
				stack = insert(container, stack);
				if (stack.isEmpty()) {
					return;
				}
			}
		}
		level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, stack));
	}

	private static ItemStack insert(Container container, ItemStack stack) {
		for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
			ItemStack slot = container.getItem(i);
			if (slot.isEmpty()) {
				container.setItem(i, stack.copy());
				stack = ItemStack.EMPTY;
			} else if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
				int move = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
				slot.grow(move);
				stack.shrink(move);
			}
		}
		container.setChanged();
		return stack;
	}

	// ---------------------------------------------------------------- interaction
	/** Right click on a machine of the salvage chain. Returns true if handled. */
	public static boolean interact(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack held) {
		MachineStore store = MachineStore.get(level.getServer());
		PowerGrid.Role role = PowerGrid.role(level.getBlockState(pos));
		switch (role) {
			case RECYCLER -> recycler(level, pos, player, held, store);
			case DIESEL -> generator(level, pos, player, held, store);
			case BATTERY -> {
				MachineStore.Machine m = store.machine(level.dimension(), pos);
				player.sendOverlayMessage(Component.translatable("machine.intersolarsystem.battery.status",
						Math.round(100.0F * m.charge / PowerGrid.BATTERY_CAPACITY)));
			}
			case MODULE -> player.sendOverlayMessage(Component.translatable(
					"machine.intersolarsystem.module." + BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).getPath()));
			default -> {
				return false;
			}
		}
		return true;
	}

	private static void recycler(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack held, MachineStore store) {
		MachineStore.Machine m = store.machine(level.dimension(), pos);
		if (held.isEmpty()) {
			if (player.isShiftKeyDown()) {
				for (ItemStack stack : m.queue) {
					player.getInventory().placeItemBackInInventory(stack);
				}
				m.queue.clear();
				m.progress = 0;
				store.markDirty();
				return;
			}
			PowerGrid grid = PowerGrid.trace(level, pos);
			Modules mods = modules(level, pos);
			float need = RECYCLER_KW + MODULE_KW * mods.count();
			float have = grid.capacity(level, store);
			int yieldLow = Math.round(100 * Math.min(1.0F, 0.55F + (mods.smelter() ? 0.15F : 0) + (mods.sorter() ? 0.1F : 0)));
			int yieldHigh = Math.round(100 * Math.min(1.0F, 0.75F + (mods.smelter() ? 0.15F : 0) + (mods.sorter() ? 0.1F : 0)));
			int items = m.queue.stream().mapToInt(ItemStack::getCount).sum();
			player.sendSystemMessage(Component.translatable("machine.intersolarsystem.recycler.status",
					String.format("%.0f", have), String.format("%.0f", need), items, yieldLow, yieldHigh,
					(mods.shredder() ? "S" : "-") + (mods.smelter() ? "M" : "-") + (mods.sorter() ? "R" : "-")));
			if (have <= 0) {
				player.sendSystemMessage(Component.translatable("machine.intersolarsystem.recycler.no_power"));
			}
			return;
		}
		String id = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
		if (SalvageTable.get(id) == null) {
			player.sendOverlayMessage(Component.translatable("machine.intersolarsystem.recycler.cannot", held.getHoverName()));
			return;
		}
		if (m.queue.size() >= QUEUE) {
			player.sendOverlayMessage(Component.translatable("machine.intersolarsystem.recycler.full"));
			return;
		}
		m.queue.add(held.copy());
		held.setCount(0);
		store.markDirty();
		level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.8F, 0.6F);
		player.sendOverlayMessage(Component.translatable("machine.intersolarsystem.recycler.loaded", m.queue.size(), QUEUE));
	}

	private static int burnTime(ItemStack stack) {
		if (stack.is(Items.COAL) || stack.is(Items.CHARCOAL)) {
			return 1600;
		}
		if (stack.is(Items.COAL_BLOCK)) {
			return 16000;
		}
		if (stack.getItem() == ModItems.BITUMEN) {
			return 2400;
		}
		if (stack.is(net.minecraft.tags.ItemTags.LOGS) || stack.is(net.minecraft.tags.ItemTags.PLANKS)) {
			return 300;
		}
		return 0;
	}

	private static void generator(ServerLevel level, BlockPos pos, ServerPlayer player, ItemStack held, MachineStore store) {
		MachineStore.Machine m = store.machine(level.dimension(), pos);
		int burn = burnTime(held);
		if (burn > 0) {
			int room = (64000 - m.fuel) / burn;
			int take = Math.min(room, held.getCount());
			if (take <= 0) {
				player.sendOverlayMessage(Component.translatable("machine.intersolarsystem.generator.full"));
				return;
			}
			m.fuel += take * burn;
			held.shrink(take);
			store.markDirty();
			level.playSound(null, pos, SoundEvents.BUCKET_EMPTY_LAVA, SoundSource.BLOCKS, 0.6F, 1.4F);
		}
		player.sendOverlayMessage(Component.translatable("machine.intersolarsystem.generator.status",
				m.fuel / 1200, (int) PowerGrid.DIESEL_KW));
	}
}
