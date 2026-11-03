package hu.kriwdi.intersolarsystem.lore;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * Notes, data tapes and manuals all carry the id of their text in the item's custom data ({@code {lore:"id"}}); the
 * texts themselves live in assets/intersolarsystem/lore/&lt;language&gt;.json and are read by the client.
 */
public final class LoreItems {
	public static final String KEY = "lore";

	private LoreItems() {
	}

	public static String id(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data == null ? "" : data.copyTag().getStringOr(KEY, "");
	}

	public static ItemStack with(ItemStack stack, String id) {
		CompoundTag tag = new CompoundTag();
		tag.putString(KEY, id);
		CustomData.set(DataComponents.CUSTOM_DATA, stack, tag);
		return stack;
	}
}
