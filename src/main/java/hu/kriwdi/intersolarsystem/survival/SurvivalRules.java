package hu.kriwdi.intersolarsystem.survival;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.core.ModIds;
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
			if (player.isSpectator()) {
				return InteractionResult.PASS;
			}
			ItemStack stack = player.getItemInHand(hand);
			if (canGrowHere(level)) {
				return shipFarm(player, level, stack, hit);
			}
			boolean tilling = stack.is(ItemTags.HOES);
			boolean planting = stack.getItem() instanceof BlockItem item && isPlant(item.getBlock());
			if (tilling || planting) {
				tell(player, Component.translatable("message.intersolarsystem.barren_soil"));
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (stack.is(ModItems.WATER_CANISTER)) {
				return fillCanister(player, level, stack);
			}
			if (helmetOn(player) && stack.has(DataComponents.CONSUMABLE)) {
				tell(player, Component.translatable("message.intersolarsystem.helmet_blocks_food"));
				level.playSound(player, player.blockPosition(), ModSounds.HELMET_BLOCKED, SoundSource.PLAYERS, 0.8F, 1.0F);
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});
	}

	/**
	 * In the ship's farm only living soil (from an oasis) can be tilled; ordinary dirt brought aboard is as dead as the
	 * ground it came from.
	 */
	private static InteractionResult shipFarm(Player player, Level level, ItemStack stack, BlockHitResult hit) {
		if (!(stack.is(ItemTags.HOES))) {
			return InteractionResult.PASS;
		}
		BlockPos pos = hit.getBlockPos();
		BlockState state = level.getBlockState(pos);
		if (state.is(ModBlocks.LIVING_SOIL) || state.is(ModBlocks.OASIS_GRASS)) {
			if (!level.getBlockState(pos.above()).isAir()) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide()) {
				level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
				level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
			}
			return InteractionResult.SUCCESS;
		}
		if (state.is(BlockTags.DIRT)) {
			tell(player, Component.translatable("message.intersolarsystem.dead_soil"));
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	/** An empty canister dipped into water: clean only at an oasis. */
	private static InteractionResult fillCanister(Player player, Level level, ItemStack stack) {
		if (!(player.pick(5.0, 0.0F, true) instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK
				|| !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
			return InteractionResult.PASS;
		}
		boolean oasis = level.getBiome(hit.getBlockPos()).is(ModIds.id("oasis"));
		if (!oasis) {
			tell(player, Component.translatable("message.intersolarsystem.water_polluted"));
			return InteractionResult.FAIL;
		}
		if (!level.isClientSide()) {
			stack.shrink(1);
			ItemStack full = new ItemStack(ModItems.CLEAN_WATER_CANISTER);
			if (!player.getInventory().add(full)) {
				player.drop(full, false);
			}
			tell(player, Component.translatable("message.intersolarsystem.water_filled"));
			level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
		return InteractionResult.SUCCESS;
	}

	private static void tell(Player player, Component message) {
		if (!player.level().isClientSide()) {
			player.sendOverlayMessage(message);
		}
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
