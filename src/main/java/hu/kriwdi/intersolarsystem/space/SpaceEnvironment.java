package hu.kriwdi.intersolarsystem.space;

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
import net.minecraft.world.item.ItemStack;

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
	public static final double FAILED_STABILISER_GRAVITY_FACTOR = -0.6;
	public static final int STABILISER_FAILURE = 20;
	public static final int LIFE_SUPPORT_MINIMUM = 20;
	/** Net air lost per tick in vacuum after vanilla's own refill of 4 (15 s from full to empty). */
	private static final int AIR_LOSS = 5;
	private static final Identifier GRAVITY_ID = ModIds.id("microgravity");
	private static final Identifier FALL_ID = ModIds.id("microgravity_fall");

	private SpaceEnvironment() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(SpaceEnvironment::tick);
	}

	private static void tick(MinecraftServer server) {
		ShipState ship = ShipStateAccess.get(server);
		long now = server.overworld().getGameTime();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			boolean orbit = player.level().dimension().equals(ShipDimensions.ORBIT);
			boolean interior = player.level().dimension().equals(ShipDimensions.INTERIOR);
			double gravity = orbit ? ORBIT_GRAVITY_FACTOR
					: interior && ship.condition(ShipSubsystem.STABILIZER) < STABILISER_FAILURE
							? FAILED_STABILISER_GRAVITY_FACTOR : 0.0;
			setModifier(player, Attributes.GRAVITY, GRAVITY_ID, gravity, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
			setModifier(player, Attributes.SAFE_FALL_DISTANCE, FALL_ID, gravity != 0.0 ? 64.0 : 0.0,
					AttributeModifier.Operation.ADD_VALUE);
			if (player.isCreative() || player.isSpectator()) {
				continue;
			}
			if (orbit) {
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
			if (now % 20 == 0) {
				suit.setDamageValue(Math.min(suit.getMaxDamage() - 1, suit.getDamageValue() + 1));
				if (suit.getDamageValue() >= suit.getMaxDamage() - 1) {
					// The last point stands for an empty tank: the suit is never destroyed by breathing.
					suit.setDamageValue(suit.getMaxDamage());
				}
			}
			return;
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
