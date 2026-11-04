package hu.kriwdi.intersolarsystem.space;

import java.util.List;

import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import hu.kriwdi.intersolarsystem.content.ModItems;

/**
 * Modules fitted to the space suit (one suit, upgraded as the worlds get harsher): the heat shield (Venus's surface,
 * Mercury's day and night, Jupiter's depths), the pressure shell (Venus's surface, Jupiter below the clouds), the
 * radiation shielding (Io, Europa, Mercury's solar flares) and the extended tank (oxygen lasts half again as long).
 * Used while wearing the suit, a module is fitted for good; the visor display lists them. Stored in the suit's custom
 * data, so they stay with the suit.
 */
public final class SuitModules {
	public static final String THERMAL = "thermal";
	public static final String PRESSURE = "pressure";
	public static final String RADIATION = "radiation";
	public static final String TANK = "tank";
	public static final List<String> ALL = List.of(THERMAL, PRESSURE, RADIATION, TANK);
	private static final String KEY = "modules";

	private SuitModules() {
	}

	public static void register() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack held = player.getItemInHand(hand);
			String module = moduleOf(held.getItem());
			if (module == null) {
				return InteractionResult.PASS;
			}
			ItemStack suit = player.getItemBySlot(EquipmentSlot.CHEST);
			if (suit.getItem() != ModItems.SPACE_SUIT) {
				if (player instanceof ServerPlayer sp) {
					sp.sendOverlayMessage(Component.translatable("suit.intersolarsystem.module.wear_suit"));
				}
				return InteractionResult.FAIL;
			}
			if (has(suit, module)) {
				if (player instanceof ServerPlayer sp) {
					sp.sendOverlayMessage(Component.translatable("suit.intersolarsystem.module.already",
							Component.translatable("suit.intersolarsystem.module." + module)));
				}
				return InteractionResult.FAIL;
			}
			if (!level.isClientSide()) {
				add(suit, module);
				if (!player.isCreative()) {
					held.shrink(1);
				}
				level.playSound(null, player.blockPosition(), SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS,
						1.0F, 1.0F);
				if (player instanceof ServerPlayer sp) {
					sp.sendOverlayMessage(Component.translatable("suit.intersolarsystem.module.fitted",
							Component.translatable("suit.intersolarsystem.module." + module)).withStyle(ChatFormatting.GREEN));
				}
			}
			return InteractionResult.SUCCESS;
		});
	}

	static String moduleOf(Item item) {
		if (item == ModItems.THERMAL_MODULE) {
			return THERMAL;
		}
		if (item == ModItems.PRESSURE_MODULE) {
			return PRESSURE;
		}
		if (item == ModItems.RADIATION_MODULE) {
			return RADIATION;
		}
		if (item == ModItems.TANK_MODULE) {
			return TANK;
		}
		return null;
	}

	public static boolean has(ItemStack suit, String module) {
		CustomData data = suit.get(DataComponents.CUSTOM_DATA);
		return data != null && data.copyTag().getStringOr(KEY, "").contains(module);
	}

	/** Whether the player wears the full suit with this module fitted. */
	public static boolean protects(Player player, String module) {
		return hu.kriwdi.intersolarsystem.content.ModArmor.fullSuit(player)
				&& has(player.getItemBySlot(EquipmentSlot.CHEST), module);
	}

	private static void add(ItemStack suit, String module) {
		CustomData data = suit.get(DataComponents.CUSTOM_DATA);
		CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
		String now = tag.getStringOr(KEY, "");
		tag.putString(KEY, now.isEmpty() ? module : now + "," + module);
		CustomData.set(DataComponents.CUSTOM_DATA, suit, tag);
	}
}
