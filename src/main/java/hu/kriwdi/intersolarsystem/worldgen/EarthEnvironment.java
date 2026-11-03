package hu.kriwdi.intersolarsystem.worldgen;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.gamerules.GameRules;

import hu.kriwdi.intersolarsystem.content.ModArmor;

/**
 * The dead Earth's rules: nothing from the old world wanders in (no patrols, traders or phantoms; the biomes spawn no
 * animals or monsters), the Nether stays shut, the acid swamp's water burns and the fallout zone irradiates anyone
 * without a sealed space suit.
 */
public final class EarthEnvironment {
	private static final long WARNING_INTERVAL = 200;
	private static final Map<UUID, Long> LAST_WARNING = new HashMap<>();

	private EarthEnvironment() {
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(EarthEnvironment::applyRules);
		ServerTickEvents.END_SERVER_TICK.register(EarthEnvironment::tick);
	}

	private static void applyRules(MinecraftServer server) {
		GameRules rules = server.overworld().getGameRules();
		rules.set(GameRules.SPAWN_PATROLS, false, server);
		rules.set(GameRules.SPAWN_WANDERING_TRADERS, false, server);
		rules.set(GameRules.SPAWN_PHANTOMS, false, server);
		rules.set(GameRules.ALLOW_ENTERING_NETHER_USING_PORTALS, false, server);
	}

	private static void tick(MinecraftServer server) {
		long now = server.overworld().getGameTime();
		if (now % 20 != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (!player.level().dimension().equals(Level.OVERWORLD) || player.isCreative() || player.isSpectator()
					|| ModArmor.fullSuit(player)) {
				continue;
			}
			ServerLevel level = (ServerLevel) player.level();
			Holder<Biome> biome = level.getBiome(player.blockPosition());
			if (biome.is(DeadEarth.ACID_SWAMP) && player.isInWater()) {
				player.hurtServer(level, player.damageSources().generic(), 2.0F);
				player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
				warn(player, now, "message.intersolarsystem.hazard.acid");
			} else if (biome.is(DeadEarth.IRRADIATED_CRATER) && now % 60 == 0) {
				player.hurtServer(level, player.damageSources().generic(), 1.0F);
				player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));
				warn(player, now, "message.intersolarsystem.hazard.radiation");
			}
		}
	}

	private static void warn(ServerPlayer player, long now, String key) {
		Long last = LAST_WARNING.get(player.getUUID());
		if (last == null || now - last >= WARNING_INTERVAL) {
			LAST_WARNING.put(player.getUUID(), now);
			player.sendSystemMessage(Component.translatable(key));
		}
	}
}
