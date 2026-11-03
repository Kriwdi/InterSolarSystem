package hu.kriwdi.intersolarsystem.client.lore;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.lore.LoreItems;
import hu.kriwdi.intersolarsystem.lore.LoreOpenPayload;

/** Reading notes and manuals, playing tapes, terminal screens and the titles in the item tooltips. */
public final class LoreClient {
	private LoreClient() {
	}

	public static void register() {
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack stack = player.getItemInHand(hand);
			String kind = kindOf(stack);
			if (kind == null) {
				return InteractionResult.PASS;
			}
			if (level.isClientSide()) {
				open(kind, LoreItems.id(stack));
			}
			return InteractionResult.SUCCESS;
		});
		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			String kind = kindOf(stack);
			if (kind == null) {
				return;
			}
			LoreLibrary.Entry entry = LoreLibrary.get(kind, LoreItems.id(stack));
			if (entry != null) {
				lines.add(1, Component.literal(entry.title()).withStyle(ChatFormatting.GOLD));
				if (!entry.subtitle().isEmpty()) {
					lines.add(2, Component.literal(entry.subtitle()).withStyle(ChatFormatting.GRAY));
				}
			}
			lines.add(Component.translatable(kind.equals("tapes") ? "lore.intersolarsystem.play_hint" : "lore.intersolarsystem.use_hint")
					.withStyle(ChatFormatting.DARK_GRAY));
		});
		ClientPlayNetworking.registerGlobalReceiver(LoreOpenPayload.TYPE,
				(payload, context) -> context.client().execute(() -> open(payload.kind() + "s", payload.id())));
	}

	private static String kindOf(ItemStack stack) {
		if (stack.is(ModItems.NOTE)) {
			return "notes";
		}
		if (stack.is(ModItems.MANUAL)) {
			return "manuals";
		}
		if (stack.is(ModItems.DATA_TAPE)) {
			return "tapes";
		}
		return null;
	}

	/** Opens a text: a reader for notes, manuals and terminals, the tape player for tapes. */
	public static void open(String kind, String id) {
		LoreLibrary.Entry entry = LoreLibrary.get(kind, id);
		Minecraft mc = Minecraft.getInstance();
		if (entry == null) {
			if (mc.player != null) {
				mc.player.sendOverlayMessage(Component.literal("[" + kind + ": " + id + "]"));
			}
			return;
		}
		switch (kind) {
			case "tapes" -> TapePlayer.play(entry);
			case "manuals" -> mc.setScreenAndShow(new LoreScreen(LoreScreen.Style.MANUAL, entry));
			case "terminals" -> mc.setScreenAndShow(new LoreScreen(LoreScreen.Style.TERMINAL, entry));
			default -> mc.setScreenAndShow(new LoreScreen(LoreScreen.Style.NOTE, entry));
		}
	}
}
