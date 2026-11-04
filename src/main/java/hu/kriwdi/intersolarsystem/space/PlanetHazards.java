package hu.kriwdi.intersolarsystem.space;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.worldgen.CloudDeckFeature;

/**
 * What each world beyond Mars does to a suited player, on top of the vacuum (every world brings its own danger, and
 * the suit's modules answer them):
 * <ul>
 * <li>Venus: the surface (below {@link #VENUS_CLOUD_BASE}) is 460 °C at 92 bar: without the heat shield and the
 * pressure shell it kills in seconds; with both, the cooling eats the oxygen (a dive has a time limit). The cloud
 * layer above is breathable-temperature but sulphuric: the acid wears the suit under the open sky.</li>
 * <li>Mercury: the sunlit side burns, the night side freezes (the heat shield answers both, at a cost); solar flares
 * come on a schedule everybody can read (a warning first): without radiation shielding, get under a roof.</li>
 * <li>Io: Jupiter's radiation belts, under the open sky, all the time.</li>
 * <li>Europa: the same, weaker, on the ice; under it (the ocean) there is shelter.</li>
 * <li>Jupiter: the deeper, the richer and the deadlier. Below the middle deck the pressure crushes, below the water
 * deck it burns too, and under {@link #JUPITER_DEATH} nothing survives. The zonal winds push everyone in the open,
 * east in the bright zones, west in the dark belts.</li>
 * <li>The outer system: Ganymede's own magnetic field shields its equator, the polar regions lie open to Jupiter's
 * belts; Saturn, Uranus and Neptune have Jupiter's deck rules and winds (Neptune's the wildest); Saturn's rings have
 * particle storms on a schedule (a warning first, the pressure shell or a roof answers them); the icy moons are
 * deadly cold (the heat shield answers it, and on Triton and Nereid, the coldest places of all, it works hard); Titan's
 * thick air slows every fall.</li>
 * </ul>
 * The rules are pure functions of place and time ({@link #zone}), so the visor display shows the same as the server.
 */
public final class PlanetHazards {
	public static final ResourceKey<DamageType> HEAT = ResourceKey.create(Registries.DAMAGE_TYPE, ModIds.id("heat"));
	public static final ResourceKey<DamageType> CRUSHING = ResourceKey.create(Registries.DAMAGE_TYPE, ModIds.id("crushing"));
	public static final ResourceKey<DamageType> RADIATION = ResourceKey.create(Registries.DAMAGE_TYPE, ModIds.id("radiation"));
	public static final ResourceKey<DamageType> FROSTBITE = ResourceKey.create(Registries.DAMAGE_TYPE, ModIds.id("frostbite"));
	/** Ganymede: its magnetic field shields the band within this distance of the equator (z = 0). */
	public static final int GANYMEDE_SHIELD = 1_000;
	/** Saturn's rings: particle storms, like Mercury's flares but on their own schedule. */
	private static final long RING_SALT = 0x5A7_0B17L;

	public static final int VENUS_CLOUD_BASE = 150;
	public static final int JUPITER_CRUSH = CloudDeckFeature.SULFIDE_Y - 20;
	public static final int JUPITER_HEAT = CloudDeckFeature.WATER_Y - 10;
	public static final int JUPITER_DEATH = 20;
	/** Mercury's flares: one window in four of this length, announced {@link #FLARE_WARNING} ticks before. */
	public static final long FLARE_WINDOW = 12_000L;
	public static final long FLARE_WARNING = 400L;
	public static final long FLARE_LENGTH = 900L;

	/** The danger at a spot, for the server and the visor display alike. */
	public enum Zone {
		NONE, ACID, VENUS_SURFACE, SUN_HEAT, NIGHT_COLD, FLARE_WARNING, FLARE, RADIATION, JUPITER_PRESSURE,
		JUPITER_HEAT, JUPITER_DEATH, CRYO_COLD, EXTREME_COLD, RING_STORM_WARNING, RING_STORM;

		public String key() {
			return "hazard.intersolarsystem." + name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	private PlanetHazards() {
	}

	/** The flare state of Mercury at a time: 0 quiet, 1 warning, 2 flare. Deterministic, so the client can show it. */
	public static int flare(long time) {
		return scheduled(time, 0L);
	}

	/** Saturn's ring storms: 0 quiet, 1 warning, 2 storm. */
	public static int ringStorm(long time) {
		return scheduled(time, RING_SALT);
	}

	private static int scheduled(long time, long salt) {
		long window = Math.floorDiv(time, FLARE_WINDOW) ^ salt;
		long into = Math.floorMod(time, FLARE_WINDOW);
		long h = window * 0x9E3779B97F4A7C15L;
		h ^= h >>> 29;
		if (Math.floorMod(h, 4L) != 0) {
			return 0;
		}
		long start = 2_000L + Math.floorMod(h >>> 7, 6_000L);
		if (into >= start - FLARE_WARNING && into < start) {
			return 1;
		}
		return into >= start && into < start + FLARE_LENGTH ? 2 : 0;
	}

	/**
	 * @param body    the world's body id
	 * @param y       the player's height
	 * @param open    under the open sky
	 * @param sunlit  the sun is up (Mercury)
	 * @param time    game time (Mercury's flares)
	 */
	public static Zone zone(String body, int y, boolean open, boolean sunlit, long time) {
		return switch (body) {
			case "venus" -> y < VENUS_CLOUD_BASE ? Zone.VENUS_SURFACE : open ? Zone.ACID : Zone.NONE;
			case "mercury" -> {
				int f = flare(time);
				if (f == 2 && open) {
					yield Zone.FLARE;
				}
				if (open && sunlit) {
					yield Zone.SUN_HEAT;
				}
				if (open) {
					yield f == 1 ? Zone.FLARE_WARNING : Zone.NIGHT_COLD;
				}
				yield f == 0 ? Zone.NONE : Zone.FLARE_WARNING;
			}
			case "io", "europa" -> open ? Zone.RADIATION : Zone.NONE;
			case "jupiter", "saturn", "uranus", "neptune" -> y < JUPITER_DEATH ? Zone.JUPITER_DEATH
					: y < JUPITER_HEAT ? Zone.JUPITER_HEAT : y < JUPITER_CRUSH ? Zone.JUPITER_PRESSURE : Zone.NONE;
			case "titan", "enceladus", "miranda", "titania" -> open ? Zone.CRYO_COLD : Zone.NONE;
			case "triton", "nameless" -> open ? Zone.EXTREME_COLD : Zone.NONE;
			case "saturn_rings" -> {
				int s = ringStorm(time);
				yield s == 2 && open ? Zone.RING_STORM : s == 0 ? Zone.NONE : Zone.RING_STORM_WARNING;
			}
			default -> Zone.NONE;
		};
	}

	/** Like {@link #zone(String, int, boolean, boolean, long)}, for worlds where the spot on the map matters. */
	public static Zone zone(String body, int y, int z, boolean open, boolean sunlit, long time) {
		if ("ganymede".equals(body)) {
			return open && Math.abs(z) > GANYMEDE_SHIELD ? Zone.RADIATION : Zone.NONE;
		}
		return zone(body, y, open, sunlit, time);
	}

	/** The module that answers a zone ("" none does, null nothing needed). */
	public static String answer(Zone zone) {
		return switch (zone) {
			case VENUS_SURFACE, JUPITER_HEAT -> SuitModules.THERMAL + "+" + SuitModules.PRESSURE;
			case SUN_HEAT, NIGHT_COLD, CRYO_COLD, EXTREME_COLD -> SuitModules.THERMAL;
			case FLARE, RADIATION -> SuitModules.RADIATION;
			case RING_STORM -> SuitModules.PRESSURE;
			case JUPITER_PRESSURE -> SuitModules.PRESSURE;
			case JUPITER_DEATH -> "";
			default -> null;
		};
	}

	/** Whether the worn suit answers a zone. */
	public static boolean answered(ItemStack suit, boolean fullSuit, Zone zone) {
		String need = answer(zone);
		if (need == null) {
			return true;
		}
		if (need.isEmpty() || !fullSuit) {
			return false;
		}
		for (String m : need.split("\\+")) {
			if (!SuitModules.has(suit, m)) {
				return false;
			}
		}
		return true;
	}

	/** One server tick for a player in one of the worlds beyond Mars (survival only; see SpaceEnvironment). */
	static void tick(ServerPlayer player, Worlds.World world, long now) {
		ServerLevel level = (ServerLevel) player.level();
		BlockPos head = player.blockPosition().above();
		boolean open = level.canSeeSky(head);
		Zone zone = zone(world.body(), player.blockPosition().getY(), player.blockPosition().getZ(), open,
				level.isBrightOutside(), now);
		if (open && now % 10 == 0) {
			switch (world.body()) {
				case "jupiter" -> wind(player, 0.12, 90.0, now);
				case "saturn" -> wind(player, 0.15, 120.0, now);
				case "uranus" -> wind(player, 0.10, 70.0, now);
				// Neptune: the fastest winds in the solar system, in gusts.
				case "neptune" -> wind(player, 0.18 + (Math.floorMod(now / 60, 5) == 0 ? 0.22 : 0.0), 150.0, now);
				default -> {
				}
			}
		}
		if ("titan".equals(world.body())) {
			denseAir(player);
		}
		if (zone == Zone.NONE || zone == Zone.FLARE_WARNING || zone == Zone.RING_STORM_WARNING) {
			if (zone == Zone.FLARE_WARNING && now % 100 == 0) {
				player.sendOverlayMessage(Component.translatable("hazard.intersolarsystem.flare_warning"));
			}
			if (zone == Zone.RING_STORM_WARNING && now % 100 == 0) {
				player.sendOverlayMessage(Component.translatable("hazard.intersolarsystem.ring_storm_warning"));
			}
			return;
		}
		ItemStack suit = player.getItemBySlot(EquipmentSlot.CHEST);
		boolean full = hu.kriwdi.intersolarsystem.content.ModArmor.fullSuit(player);
		if (zone == Zone.ACID && !full) {
			// Bare skin in sulphuric acid clouds.
			if (now % 20 == 0) {
				player.hurtServer(level, VacuumBreathing.source(level, HEAT), 1.0F);
			}
			return;
		}
		if (answered(suit, full, zone)) {
			// Protected, at a price: the cooling and the shielding draw on the suit (shorter dives).
			int every = switch (zone) {
				case VENUS_SURFACE, JUPITER_HEAT, EXTREME_COLD -> 10;
				case SUN_HEAT, JUPITER_PRESSURE, CRYO_COLD, RING_STORM -> 20;
				case ACID -> 40;
				default -> 0;
			};
			if (every > 0 && now % every == 0 && suit.getItem() == ModItems.SPACE_SUIT) {
				suit.setDamageValue(Math.min(suit.getMaxDamage(), suit.getDamageValue() + 1));
			}
			return;
		}
		// Io's belts kill in under a minute, Europa's in two; the rest strike every second.
		int interval = zone == Zone.RADIATION ? ("io".equals(world.body()) ? 40 : 80) : 20;
		if (now % interval != 0) {
			return;
		}
		ResourceKey<DamageType> kind = switch (zone) {
			case FLARE, RADIATION -> RADIATION;
			case CRYO_COLD, EXTREME_COLD -> FROSTBITE;
			case VENUS_SURFACE, JUPITER_PRESSURE, JUPITER_DEATH, RING_STORM -> CRUSHING;
			default -> HEAT;
		};
		float amount = switch (zone) {
			case JUPITER_DEATH -> 40.0F;
			case VENUS_SURFACE, JUPITER_HEAT -> 4.0F;
			case JUPITER_PRESSURE, SUN_HEAT, FLARE, EXTREME_COLD, RING_STORM -> 3.0F;
			case CRYO_COLD -> 2.0F;
			default -> 1.0F;
		};
		player.hurtServer(level, VacuumBreathing.source(level, kind), amount);
		if (now % 60 == 0) {
			player.sendOverlayMessage(Component.translatable(zone.key()));
			level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.4F, 0.6F);
		}
	}

	/**
	 * The zonal winds of a gas giant: bright zones blow east, dark belts west (bands {@code bandWidth} blocks apart,
	 * like the cloud decks). Crouching on solid footing holds you.
	 */
	private static void wind(ServerPlayer player, double strength, double bandWidth, long now) {
		if (player.isShiftKeyDown() && player.onGround()) {
			return;
		}
		double band = Math.sin(player.getZ() / bandWidth);
		Vec3 v = player.getDeltaMovement().add(band >= 0 ? strength : -strength, 0.0, 0.0);
		player.setDeltaMovement(v);
		player.needsSync = true;
		player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
	}

	/** Titan: air four times as dense as Earth's at a seventh of the gravity. A falling body drifts down. */
	private static void denseAir(ServerPlayer player) {
		if (!player.onGround() && player.getDeltaMovement().y < -0.25 && !player.isInWater()) {
			player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
					net.minecraft.world.effect.MobEffects.SLOW_FALLING, 10, 0, true, false, false));
		}
	}
}
