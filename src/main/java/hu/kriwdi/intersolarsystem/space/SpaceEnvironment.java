package hu.kriwdi.intersolarsystem.space;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import hu.kriwdi.intersolarsystem.content.ModSounds;

import hu.kriwdi.intersolarsystem.content.ModArmor;
import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.ship.ShipState;
import hu.kriwdi.intersolarsystem.ship.ShipStateAccess;
import hu.kriwdi.intersolarsystem.ship.ShipSubsystem;

/**
 * What space does to a player, every server tick: microgravity in orbit (and in the ship when its stabiliser fails),
 * vacuum without a sealed, filled suit, oxygen use, and refilling inside the ship.
 */
public final class SpaceEnvironment {
	public static final double ORBIT_GRAVITY_FACTOR = -0.85;
	/** One sixth of Earth's gravity. */
	public static final double MOON_GRAVITY_FACTOR = -0.83;
	public static final int MOON_SIZE = 5500;
	public static final double FAILED_STABILISER_GRAVITY_FACTOR = -0.6;
	public static final int STABILISER_FAILURE = 20;
	public static final int LIFE_SUPPORT_MINIMUM = 20;
	/** Width of the Earth (the overworld border) in blocks. */
	public static final double EARTH_SIZE = 20_000.0;
	/** Net air lost per tick in vacuum after vanilla's own refill of 4 (15 s from full to empty). */
	private static final int AIR_LOSS = 5;
	private static final Identifier GRAVITY_ID = ModIds.id("microgravity");
	private static final Identifier FALL_ID = ModIds.id("microgravity_fall");

	private SpaceEnvironment() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(SpaceEnvironment::tick);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			// The Moon is 5500 blocks across (scaled against the 20 000-block Earth).
			ServerLevel moon = server.getLevel(ShipDimensions.MOON);
			if (moon != null) {
				moon.getWorldBorder().setSize(MOON_SIZE);
			}
			// The Earth is 20 000 blocks across. Only a border still at the vanilla default is changed, so a size an
			// admin set on purpose stays.
			var earthBorder = server.overworld().getWorldBorder();
			if (earthBorder.getSize() > EARTH_SIZE * 2) {
				earthBorder.setCenter(0.0, 0.0);
				earthBorder.setSize(EARTH_SIZE);
			}
		});
	}

	private static void tick(MinecraftServer server) {
		ShipState ship = ShipStateAccess.get(server);
		long now = server.overworld().getGameTime();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			boolean orbit = player.level().dimension().equals(ShipDimensions.ORBIT);
			boolean moon = player.level().dimension().equals(ShipDimensions.MOON);
			boolean interior = player.level().dimension().equals(ShipDimensions.INTERIOR);
			double gravity = orbit ? ORBIT_GRAVITY_FACTOR
					: moon ? MOON_GRAVITY_FACTOR
					: interior && ship.condition(ShipSubsystem.STABILIZER) < STABILISER_FAILURE
							? FAILED_STABILISER_GRAVITY_FACTOR : 0.0;
			setModifier(player, Attributes.GRAVITY, GRAVITY_ID, gravity, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			setModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_ID, gravity != 0.0 ? 64.0 : 0.0,
					AttributeModifier.Operation.ADD_VALUE);
			if (player.isCreative() || player.isSpectator()) {
				continue;
			}
			if (orbit || moon) {
				breathe(player, now);
			} else if (interior && ship.condition(ShipSubsystem.LIFE_SUPPORT) >= LIFE_SUPPORT_MINIMUM && now % 5 == 0) {
				refill(player);
			}
		}
	}

	private static void breathe(ServerPlayer player, long now) {
		ItemStack suit = player.getItemBySlot(EquipmentSlot.CHEST);
		boolean sealed = ModArmor.fullSuit(player) && ModArmor.oxygen(player) > 0.0;
		if (sealed) {
			player.setAirSupply(player.getMaxAirSupply());
			// The suit's own sounds: slow breathing, and a beeping alarm when the tank runs low.
			if (now % 70 == 0) {
				player.level().playSound(null, player.blockPosition(), ModSounds.SUIT_BREATH, SoundSource.PLAYERS, 0.35F,
						0.95F + player.getRandom().nextFloat() * 0.1F);
			}
			if (ModArmor.oxygen(player) < 0.2 && now % 40 == 20) {
				player.level().playSound(null, player.blockPosition(), ModSounds.OXYGEN_ALARM, SoundSource.PLAYERS, 0.6F,
						1.0F);
			}
			if (now % 20 == 0) {
				suit.setDamageValue(Math.min(suit.getMaxDamage() - 1, suit.getDamageValue() + 1));
				if (suit.getDamageValue() >= suit.getMaxDamage() - 1) {
					// The last point stands for an empty tank: the suit is never destroyed by breathing.
					suit.setDamageValue(suit.getMaxDamage());
				}
			}
			return;
		}
		if (now % 25 == 0) {
			player.level().playSound(null, player.blockPosition(), ModSounds.SUFFOCATE, SoundSource.PLAYERS, 0.8F,
					0.9F + player.getRandom().nextFloat() * 0.2F);
		}
		int air = player.getAirSupply() - AIR_LOSS;
		if (air <= -20) {
			air = 0;
			player.hurtServer((ServerLevel) player.level(), player.damageSources().drown(), 2.0F);
		}
		player.setAirSupply(air);
	}

	private static void refill(ServerPlayer player) {
		ItemStack suit = player.getItemBySlot(EquipmentSlot.CHEST);
		if (suit.getItem() == ModItems.SPACE_SUIT && suit.getDamageValue() > 0) {
			suit.setDamageValue(Math.max(0, suit.getDamageValue() - 6));
		}
	}

	private static void setModifier(ServerPlayer player, Holder<Attribute> attribute, Identifier id, double amount,
			AttributeModifier.Operation operation) {
		AttributeInstance instance = player.getAttribute(attribute);
		if (instance == null) {
			return;
		}
		if (amount == 0.0) {
			if (instance.hasModifier(id)) {
				instance.removeModifier(id);
			}
		} else {
			AttributeModifier current = instance.getModifier(id);
			if (current == null || current.amount() != amount) {
				instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
			}
		}
	}
}
