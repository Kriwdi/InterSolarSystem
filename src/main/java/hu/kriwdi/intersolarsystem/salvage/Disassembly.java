package hu.kriwdi.intersolarsystem.salvage;

import java.util.List;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import hu.kriwdi.intersolarsystem.content.ModItems;

/**
 * Taking props, wrecks and machines apart where they stand: right click with the multitool (bolts, panels, boards)
 * or the cutting torch (metal), one step per click, each step giving its parts. The step list comes from
 * {@link DisassemblyTable}; the block is gone after the last step. Sneak-clicking a machine still uses it.
 */
public final class Disassembly {
	private Disassembly() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack tool = player.getItemInHand(hand);
			if (tool.getItem() != ModItems.MULTITOOL && tool.getItem() != ModItems.CUTTING_TORCH) {
				return InteractionResult.PASS;
			}
			BlockPos pos = hit.getBlockPos();
			String blockId = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
			List<DisassemblyTable.Step> steps = DisassemblyTable.get(blockId);
			if (steps == null) {
				return InteractionResult.PASS;
			}
			if (!(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
				return InteractionResult.SUCCESS;
			}
			MachineStore store = MachineStore.get(serverLevel.getServer());
			int done = Math.min(store.disassemblyStep(level.dimension(), pos), steps.size() - 1);
			DisassemblyTable.Step step = steps.get(done);
			String toolId = BuiltInRegistries.ITEM.getKey(tool.getItem()).toString();
			Component stepText = Component.translatable("salvage.intersolarsystem.step." + step.key());
			if (!step.tool().equals(toolId)) {
				Item needed = BuiltInRegistries.ITEM.getValue(Identifier.parse(step.tool()));
				serverPlayer.sendOverlayMessage(Component.translatable("salvage.intersolarsystem.needs_tool",
						done + 1, steps.size(), stepText, needed.getDefaultInstance().getHoverName()));
				return InteractionResult.SUCCESS_SERVER;
			}
			for (SalvageTable.Output part : step.parts()) {
				Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(part.item()));
				if (item != Items.AIR) {
					serverLevel.addFreshEntity(new ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
							new ItemStack(item, part.count())));
				}
			}
			tool.hurtAndBreak(1, serverPlayer, hand == net.minecraft.world.InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND
					: EquipmentSlot.OFFHAND);
			boolean cutter = tool.getItem() == ModItems.CUTTING_TORCH;
			serverLevel.playSound(null, pos, cutter ? SoundEvents.FIRECHARGE_USE : SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS,
					0.7F, cutter ? 1.4F : 1.0F);
			serverLevel.sendParticles(cutter ? ParticleTypes.LAVA : ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 0.6,
					pos.getZ() + 0.5, 6, 0.3, 0.2, 0.3, 0.05);
			if (done + 1 >= steps.size()) {
				store.setDisassemblyStep(level.dimension(), pos, 0);
				serverLevel.destroyBlock(pos, false);
				serverPlayer.sendOverlayMessage(Component.translatable("salvage.intersolarsystem.done"));
			} else {
				store.setDisassemblyStep(level.dimension(), pos, done + 1);
				DisassemblyTable.Step next = steps.get(done + 1);
				Item nextTool = BuiltInRegistries.ITEM.getValue(Identifier.parse(next.tool()));
				serverPlayer.sendOverlayMessage(Component.translatable("salvage.intersolarsystem.progress", done + 1,
						steps.size(), stepText, Component.translatable("salvage.intersolarsystem.step." + next.key()),
						nextTool.getDefaultInstance().getHoverName()));
			}
			return InteractionResult.SUCCESS_SERVER;
		});
	}
}
