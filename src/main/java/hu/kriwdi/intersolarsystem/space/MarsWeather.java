package hu.kriwdi.intersolarsystem.space;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import hu.kriwdi.intersolarsystem.content.ModSounds;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;

/**
 * Martian dust storms: every two to four days a storm rises for a few minutes. Out under the sky the dust streams past
 * on the wind, you lean into it (slowed), and solar panels give a quarter of their power. Announced on the way in
 * and out.
 */
public final class MarsWeather {
	private static final int WARNING = 400;
	private static long next = -1L;
	private static long until = -1L;
	private static Vec3 wind = new Vec3(1, 0, 0);
	private static final DustParticleOptions DUST = new DustParticleOptions(0xC97A45, 1.6F);
	private static final DustParticleOptions FINE = new DustParticleOptions(0xE0A070, 0.9F);

	private MarsWeather() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(MarsWeather::tick);
	}

	/** How much of the sun reaches the ground (1 normally, a quarter in a storm). */
	public static float sunlight(Level level) {
		return level.dimension().equals(ShipDimensions.MARS) && storming(level.getGameTime()) ? 0.25F : 1.0F;
	}

	public static boolean storming(long now) {
		return until > 0 && now < until;
	}

	private static void tick(MinecraftServer server) {
		ServerLevel mars = server.getLevel(ShipDimensions.MARS);
		if (mars == null || mars.players().isEmpty()) {
			return;
		}
		long now = mars.getGameTime();
		if (next < 0 || next + 12000 < now) {
			next = now + 48000 + mars.getRandom().nextInt(48000);
		}
		if (now == next - WARNING) {
			for (ServerPlayer p : mars.players()) {
				p.sendSystemMessage(Component.translatable("weather.intersolarsystem.mars.storm_coming", WARNING / 20)
						.withStyle(ChatFormatting.GOLD));
			}
		}
		if (now == next) {
			double a = mars.getRandom().nextDouble() * Math.PI * 2;
			wind = new Vec3(Math.cos(a), 0.05, Math.sin(a));
			until = now + 3600 + mars.getRandom().nextInt(3600);
			for (ServerPlayer p : mars.players()) {
				p.sendSystemMessage(Component.translatable("weather.intersolarsystem.mars.storm").withStyle(ChatFormatting.RED));
				mars.playSound(null, p.blockPosition(), ModSounds.OXYGEN_ALARM, SoundSource.PLAYERS, 0.5F, 0.5F);
			}
		}
		if (storming(now)) {
			for (ServerPlayer p : mars.players()) {
				if (p.isSpectator() || !mars.canSeeSky(p.blockPosition().above())) {
					continue;
				}
				if (now % 20 == 0) {
					p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 0, true, false, true));
				}
				for (int i = 0; i < 10; i++) {
					Vec3 at = p.position().add((mars.getRandom().nextDouble() - 0.5) * 24, mars.getRandom().nextDouble() * 6,
							(mars.getRandom().nextDouble() - 0.5) * 24).subtract(wind.scale(8));
					mars.sendParticles(p, i % 3 == 0 ? FINE : DUST, false, false, at.x, at.y, at.z, 0, wind.x, wind.y,
							wind.z, 0.9);
				}
			}
			if (now == until - 1) {
				for (ServerPlayer p : mars.players()) {
					p.sendSystemMessage(Component.translatable("weather.intersolarsystem.mars.storm_over")
							.withStyle(ChatFormatting.GREEN));
				}
				next = now + 48000 + mars.getRandom().nextInt(48000);
			}
		}
	}

	/** Test hook: a storm now. */
	public static void startNow(ServerLevel mars) {
		next = mars.getGameTime() + 1;
	}
}
