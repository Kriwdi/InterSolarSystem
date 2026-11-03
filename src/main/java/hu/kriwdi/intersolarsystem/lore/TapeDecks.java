package hu.kriwdi.intersolarsystem.lore;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.content.ModSounds;

/**
 * Data tapes play only in a deck: a data terminal or a radio set. Everybody within earshot hears the recording (the
 * clicks, the hiss and the garbled voices) and reads its captions.
 */
public final class TapeDecks {
	public static final double HEARING = 24.0;

	private TapeDecks() {
	}

	/** Plays the tape in the player's hand at the deck; false if it is not a tape. */
	public static boolean play(ServerLevel level, BlockPos deck, ServerPlayer player, ItemStack stack) {
		if (stack.getItem() != ModItems.DATA_TAPE) {
			return false;
		}
		String id = LoreItems.id(stack);
		if (id.isEmpty()) {
			player.sendOverlayMessage(Component.translatable("message.intersolarsystem.tape_blank"));
			return true;
		}
		level.playSound(null, deck, ModSounds.TAPE_START, SoundSource.BLOCKS, 0.9F, 1.0F);
		TapePlayPayload payload = new TapePlayPayload(id, deck.getX(), deck.getY(), deck.getZ());
		for (ServerPlayer p : level.players()) {
			if (p.distanceToSqr(deck.getX() + 0.5, deck.getY() + 0.5, deck.getZ() + 0.5) <= HEARING * HEARING) {
				ServerPlayNetworking.send(p, payload);
			}
		}
		return true;
	}
}
