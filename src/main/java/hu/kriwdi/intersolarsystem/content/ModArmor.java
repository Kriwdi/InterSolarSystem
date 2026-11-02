package hu.kriwdi.intersolarsystem.content;

import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * The space suit. The suit's (chest piece's) durability is its oxygen: one point per second in vacuum, refilled by the
 * ship's life support. Hits wear it like any armour, which is a breach in the suit.
 */
public final class ModArmor {
	public static final ResourceKey<EquipmentAsset> SPACE_SUIT_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID,
			ModIds.id("space_suit"));
	public static final TagKey<Item> REPAIRS_SPACE_SUIT = TagKey.create(Registries.ITEM, ModIds.id("repairs_space_suit"));

	/** Base durability 37: the suit gets 16 x 37 = 592 points, about ten minutes of oxygen. */
	public static final ArmorMaterial SPACE_SUIT = new ArmorMaterial(37,
			Map.of(ArmorType.HELMET, 1, ArmorType.CHESTPLATE, 3, ArmorType.LEGGINGS, 2, ArmorType.BOOTS, 1,
					ArmorType.BODY, 3),
			9, SoundEvents.ARMOR_EQUIP_IRON, 0.0F, 0.0F, REPAIRS_SPACE_SUIT, SPACE_SUIT_ASSET);

	private ModArmor() {
	}

	public static boolean isSuitPiece(ItemStack stack) {
		Item item = stack.getItem();
		return item == ModItems.SPACE_HELMET || item == ModItems.SPACE_SUIT || item == ModItems.SPACE_PANTS
				|| item == ModItems.SPACE_BOOTS;
	}

	/** All four pieces worn. */
	public static boolean fullSuit(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.SPACE_HELMET
				&& entity.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.SPACE_SUIT
				&& entity.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.SPACE_PANTS
				&& entity.getItemBySlot(EquipmentSlot.FEET).getItem() == ModItems.SPACE_BOOTS;
	}

	/** Oxygen left in the worn suit, 0..1 (0 when no suit is worn). */
	public static double oxygen(LivingEntity entity) {
		ItemStack suit = entity.getItemBySlot(EquipmentSlot.CHEST);
		if (suit.getItem() != ModItems.SPACE_SUIT || suit.getMaxDamage() <= 0) {
			return 0.0;
		}
		return 1.0 - (double) suit.getDamageValue() / suit.getMaxDamage();
	}
}
