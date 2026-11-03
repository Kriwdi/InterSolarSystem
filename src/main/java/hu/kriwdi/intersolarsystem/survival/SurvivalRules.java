package hu.kriwdi.intersolarsystem.survival;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;

import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.content.ModSounds;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;

/**
 * Rules of survival on the dead worlds:
 * <ul>
 * <li>the soil outside the ships is barren: no tilling, no sowing, no planting (crops and saplings grow only in a
 * ship's farm);</li>
 * <li>a space helmet is sealed: nothing can be eaten or drunk while it is on.</li>
 * </ul>
 */
public final class SurvivalRules {
	private SurvivalRules() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			if (canGrowHere(level) || player.isSpectator()) {
				return InteractionResult.PASS;
			}
			ItemStack stack = player.getItemInHand(hand);
			boolean tilling = stack.getItem() instanceof HoeItem;
			boolean planting = stack.getItem() instanceof BlockItem item && isPlant(item.getBlock());
			if (tilling || planting) {
				player.displayClientMessage(Component.translatable("message.intersolarsystem.barren_soil"), true);
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (helmetOn(player) && stack.has(DataComponents.CONSUMABLE)) {
				player.displayClientMessage(Component.translatable("message.intersolarsystem.helmet_blocks_food"), true);
				level.playSound(player, player.blockPosition(), ModSounds.HELMET_BLOCKED, SoundSource.PLAYERS, 0.8F, 1.0F);
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});
	}

	/** Plants grow only aboard a ship (its interior dimension). */
	public static boolean canGrowHere(Level level) {
		return level.dimension().equals(ShipDimensions.INTERIOR);
	}

	static boolean isPlant(Block block) {
		return block instanceof CropBlock || block instanceof StemBlock || block instanceof SaplingBlock
				|| block instanceof SweetBerryBushBlock || block instanceof SugarCaneBlock || block instanceof CocoaBlock;
	}

	public static boolean helmetOn(Player player) {
		return player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.SPACE_HELMET;
	}
}
