package hu.kriwdi.intersolarsystem.space;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import hu.kriwdi.intersolarsystem.content.ModArmor;
import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.content.ModSounds;
import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Breathing in a vacuum, without the vanilla drowning (no bubbles, no drowning damage).
 *
 * <ul>
 * <li><b>Sealed suit with oxygen:</b> fine; a punctured suit hisses and loses its oxygen four times as fast.</li>
 * <li><b>Hypoxia</b> (full suit, empty tank): the breath in the lungs runs out over ~15 s, then the player grows
 * slow and weak and takes {@code hypoxia} damage; the screen darkens at the edges, the heart pounds.</li>
 * <li><b>Decompression</b> (no full suit): the breath is gone in ~4 s and the damage is heavier; the screen
 * frosts over and the ears ring.</li>
 * </ul>
 * Hits that get through the suit can puncture it; sealant foam (right click) closes the hole.
 */
public final class VacuumBreathing {
	public enum Mode { SAFE, LEAKING, HYPOXIA, DECOMPRESSION }

	public static final ResourceKey<DamageType> HYPOXIA = ResourceKey.create(Registries.DAMAGE_TYPE, ModIds.id("hypoxia"));
	public static final ResourceKey<DamageType> DECOMPRESSION = ResourceKey.create(Registries.DAMAGE_TYPE,
			ModIds.id("decompression"));
	private static final String LEAK = "leak";

	private static final Map<UUID, Float> BREATH = new HashMap<>();

	private VacuumBreathing() {
	}

	public static void register() {
		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamage, damageTaken, blocked) -> {
			if (entity instanceof ServerPlayer player && damageTaken >= 3.0F && !blocked
					&& player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.SPACE_SUIT
					&& !source.is(DamageTypeTags.BYPASSES_ARMOR) && !source.is(HYPOXIA) && !source.is(DECOMPRESSION)
					&& player.getRandom().nextFloat() < 0.3F) {
				setLeak(player.getItemBySlot(EquipmentSlot.CHEST), true);
				player.sendOverlayMessage(Component.translatable("message.intersolarsystem.suit_punctured"));
				player.level().playSound(null, player.blockPosition(), ModSounds.SUIT_LEAK, SoundSource.PLAYERS, 0.9F, 1.0F);
			}
		});
		UseItemCallback.EVENT.register((player, level, hand) -> {
			// An oxygen canister tops up the suit's tank (a third of it).
			ItemStack held = player.getItemInHand(hand);
			ItemStack suit = player.getItemBySlot(EquipmentSlot.CHEST);
			if (held.getItem() != ModItems.OXYGEN_CANISTER || suit.getItem() != ModItems.SPACE_SUIT
					|| suit.getDamageValue() == 0) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide()) {
				suit.setDamageValue(Math.max(0, suit.getDamageValue() - suit.getMaxDamage() / 3));
				if (!player.isCreative()) {
					held.shrink(1);
				}
				level.playSound(null, player.blockPosition(), ModSounds.SUIT_BREATH, SoundSource.PLAYERS, 1.0F, 1.3F);
				if (player instanceof ServerPlayer sp) {
					sp.sendOverlayMessage(Component.translatable("message.intersolarsystem.oxygen_refilled",
							Math.round(100 * ModArmor.oxygen(sp))));
				}
			}
			return InteractionResult.SUCCESS;
		});
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack held = player.getItemInHand(hand);
			ItemStack suit = player.getItemBySlot(EquipmentSlot.CHEST);
			if (held.getItem() != ModItems.SEALANT || suit.getItem() != ModItems.SPACE_SUIT || !leaking(suit)) {
				return InteractionResult.PASS;
			}
			if (!level.isClientSide()) {
				setLeak(suit, false);
				if (!player.getAbilities().instabuild) {
					held.shrink(1);
				}
				level.playSound(null, player.blockPosition(), SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 0.8F, 0.8F);
				player.sendOverlayMessage(Component.translatable("message.intersolarsystem.suit_sealed"));
			}
			return InteractionResult.SUCCESS;
		});
	}

	public static boolean leaking(ItemStack suit) {
		CustomData data = suit.get(DataComponents.CUSTOM_DATA);
		return data != null && data.copyTag().getBooleanOr(LEAK, false);
	}

	private static void setLeak(ItemStack suit, boolean leak) {
		CustomData data = suit.get(DataComponents.CUSTOM_DATA);
		CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
		if (leak) {
			tag.putBoolean(LEAK, true);
		} else {
			tag.remove(LEAK);
		}
		CustomData.set(DataComponents.CUSTOM_DATA, suit, tag);
	}

	/** One server tick for a player in a place without air. */
	static void breathe(ServerPlayer player, long now) {
		ServerLevel level = (ServerLevel) player.level();
		ItemStack suit = player.getItemBySlot(EquipmentSlot.CHEST);
		boolean full = ModArmor.fullSuit(player);
		boolean leak = suit.getItem() == ModItems.SPACE_SUIT && leaking(suit);
		double oxygen = ModArmor.oxygen(player);
		player.setAirSupply(player.getMaxAirSupply());
		float breath = BREATH.getOrDefault(player.getUUID(), 1.0F);
		Mode mode;
		if (full && oxygen > 0.0) {
			mode = leak ? Mode.LEAKING : Mode.SAFE;
			breath = Math.min(1.0F, breath + 0.02F);
			if (now % 70 == 0) {
				level.playSound(null, player.blockPosition(), ModSounds.SUIT_BREATH, SoundSource.PLAYERS, 0.35F,
						0.95F + player.getRandom().nextFloat() * 0.1F);
			}
			if (oxygen < 0.2 && now % 40 == 20) {
				level.playSound(null, player.blockPosition(), ModSounds.OXYGEN_ALARM, SoundSource.PLAYERS, 0.6F, 1.0F);
			}
			if (leak && now % 30 == 0) {
				level.playSound(null, player.blockPosition(), ModSounds.SUIT_LEAK, SoundSource.PLAYERS, 0.45F,
						0.9F + player.getRandom().nextFloat() * 0.2F);
			}
			int every = leak ? 5 : 20;
			if (now % every == 0) {
				suit.setDamageValue(Math.min(suit.getMaxDamage() - 1, suit.getDamageValue() + 1));
				if (suit.getDamageValue() >= suit.getMaxDamage() - 1) {
					// The last point stands for an empty tank: the suit is never destroyed by breathing.
					suit.setDamageValue(suit.getMaxDamage());
				}
			}
		} else if (full) {
			mode = Mode.HYPOXIA;
			breath = Math.max(0.0F, breath - 1.0F / 300.0F);
			if (now % 22 == 0) {
				level.playSound(null, player.blockPosition(), ModSounds.HEARTBEAT, SoundSource.PLAYERS,
						0.5F + 0.5F * (1.0F - breath), 0.9F + 0.3F * (1.0F - breath));
			}
			if (now % 60 == 0) {
				level.playSound(null, player.blockPosition(), ModSounds.SUFFOCATE, SoundSource.PLAYERS, 0.5F, 0.85F);
			}
			if (breath < 0.5F && now % 20 == 0) {
				player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, breath < 0.2F ? 2 : 0, false, false, false));
				player.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 40, 1, false, false, false));
			}
			if (breath <= 0.0F && now % 20 == 0) {
				player.hurtServer(level, source(level, HYPOXIA), 1.5F);
			}
		} else {
			mode = Mode.DECOMPRESSION;
			breath = Math.max(0.0F, breath - 1.0F / 80.0F);
			if (now % 40 == 0) {
				level.playSound(null, player.blockPosition(), ModSounds.EAR_RING, SoundSource.PLAYERS, 0.7F, 1.0F);
			}
			if (now % 20 == 0) {
				player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 1, false, false, false));
			}
			if (breath <= 0.0F && now % 10 == 0) {
				player.hurtServer(level, source(level, DECOMPRESSION), 2.0F);
			}
		}
		BREATH.put(player.getUUID(), breath);
		if (now % 4 == 0) {
			ServerPlayNetworking.send(player, new VacuumStatusPayload((float) oxygen, breath, (byte) mode.ordinal(), leak, true));
		}
	}

	/** Somewhere with air: the lungs recover; the helmet display still shows the tank while the suit is worn. */
	static void breatheAir(ServerPlayer player, long now) {
		UUID id = player.getUUID();
		Float breath = BREATH.get(id);
		if (breath != null) {
			breath = Math.min(1.0F, breath + 0.05F);
			if (breath >= 1.0F) {
				BREATH.remove(id);
			} else {
				BREATH.put(id, breath);
			}
		}
		ItemStack suit = player.getItemBySlot(EquipmentSlot.CHEST);
		if (now % 10 == 0 && suit.getItem() == ModItems.SPACE_SUIT) {
			ServerPlayNetworking.send(player, new VacuumStatusPayload((float) ModArmor.oxygen(player),
					breath == null ? 1.0F : breath, (byte) Mode.SAFE.ordinal(), leaking(suit), false));
		}
	}

	/** A hit that tore the suit (orbital debris): it leaks until sealed. */
	public static void puncture(ServerPlayer player) {
		ItemStack suit = player.getItemBySlot(EquipmentSlot.CHEST);
		if (suit.getItem() == ModItems.SPACE_SUIT && !leaking(suit)) {
			setLeak(suit, true);
			player.sendOverlayMessage(Component.translatable("message.intersolarsystem.suit_punctured"));
			player.level().playSound(null, player.blockPosition(), ModSounds.SUIT_LEAK, SoundSource.PLAYERS, 0.9F, 1.0F);
		}
	}

	static DamageSource source(ServerLevel level, ResourceKey<DamageType> key) {
		return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key));
	}
}
