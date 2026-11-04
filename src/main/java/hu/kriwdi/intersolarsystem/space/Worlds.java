package hu.kriwdi.intersolarsystem.space;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.worldgen.CloudDeckFeature;

/**
 * The worlds beyond Mars in one table: dimension, size (world border, real size at the Moon's scale, at least 2000
 * blocks unless the body is worth it), gravity, where the small ship comes down (a site is always there to land on)
 * and the hand-made sites (templates from tools/worldgen/space_sites.py). A new body: a row here, its data from
 * tools/worldgen/make_planets.py, its sites, a solar_system.json row.
 */
public final class Worlds {
	/** A site: template, offset from the landing zone's centre (y: blocks above the ground; -1: on the ground). */
	public record Site(String template, int dx, int dz, int height) {
	}

	/**
	 * {@code gravity}: attribute modifier (-1 weightless .. 0 Earth .. positive heavier). {@code zeroG}: floating like in
	 * Earth orbit. {@code landing}: x, z of the landing site; {@code landHeight}: 0 = the ground, else a fixed height
	 * (the asteroid belt's outpost floats).
	 */
	public record World(String body, ResourceKey<Level> dimension, int size, double gravity, boolean zeroG,
			int landX, int landZ, int landHeight, List<Site> sites) {
	}

	private static ResourceKey<Level> dim(String id) {
		return ResourceKey.create(Registries.DIMENSION, ModIds.id(id));
	}

	public static final World VENUS = new World("venus", dim("venus"), 19_200, -0.10, false, 1200, -400, 0, List.of(
			new Site("venus/cloud_city", 0, 0, 200), new Site("venus/lift_foot", 14, 0, -1),
			new Site("venus/venera13", 160, 90, -1), new Site("venus/venera9", -140, 60, -1),
			new Site("venus/descent_capsule", 60, -170, -1)));
	public static final World MERCURY = new World("mercury", dim("mercury"), 7_720, -0.62, false, -600, 900, 0, List.of(
			new Site("mercury/sunshade_base", 0, 0, -1), new Site("mercury/messenger_impact", 180, -120, -1)));
	public static final World CERES = new World("ceres", dim("ceres"), 1_490, -0.97, false, 300, 200, 0, List.of(
			new Site("ceres/occator_station", 0, 0, -1), new Site("ceres/ahuna_cone", 90, -60, -1)));
	public static final World BELT = new World("asteroid_belt", dim("asteroid_belt"), 6_000, -1.0, true, 0, 0, 120, List.of(
			new Site("belt/mining_outpost", 0, 0, 120), new Site("belt/ore_hauler_wreck", 60, 40, 140)));
	public static final World JUPITER = new World("jupiter", dim("jupiter"), 20_000, 1.5, false, 0, 0, 230, List.of(
			new Site("jupiter/aerostat", 0, 0, 230), new Site("jupiter/galileo_probe", 220, 140, 146)));
	public static final World IO = new World("io", dim("io"), 5_770, -0.82, false, 400, -300, 0, List.of(
			new Site("io/volcano_observatory", 0, 0, -1)));
	public static final World EUROPA = new World("europa", dim("europa"), 4_940, -0.87, false, -200, 500, 0, List.of(
			new Site("europa/drill_station", 0, 0, -1)));
	// ---- The outer system (tools/worldgen/make_outer_planets.py, outer_sites.py).
	public static final World GANYMEDE = new World("ganymede", dim("ganymede"), 8_340, -0.85, false, 300, -200, 0, List.of(
			new Site("ganymede/uruk_camp", 0, 0, -1), new Site("ganymede/juice_crash", 170, 110, -1)));
	public static final World CALLISTO = new World("callisto", dim("callisto"), 7_630, -0.87, false, -400, 300, 0, List.of(
			new Site("callisto/hope_base", 0, 0, -1), new Site("callisto/fuel_depot", -60, 40, -1)));
	public static final World SATURN = new World("saturn", dim("saturn"), 20_000, 0.07, false, 0, 0, 230, List.of(
			new Site("saturn/kronos_aerostat", 0, 0, 230), new Site("saturn/storm_buoy", 0, -1900, 190)));
	public static final World RINGS = new World("saturn_rings", dim("saturn_rings"), 6_000, -1.0, true, 0, 0, 122, List.of(
			new Site("rings/daphnis_depot", 0, 0, 120), new Site("rings/ice_hauler_wreck", -70, 50, 128)));
	public static final World TITAN = new World("titan", dim("titan"), 8_150, -0.86, false, 500, 400, 0, List.of(
			new Site("titan/selk_habitat", 0, 0, -1), new Site("titan/huygens", 150, -90, -1),
			new Site("titan/dragonfly", -110, -60, -1), new Site("titan/kraken_harbour", 0, 260, -1),
			new Site("titan/sotra_vent", -300, 180, -1)));
	public static final World ENCELADUS = new World("enceladus", dim("enceladus"), 2_000, -0.99, false, 0, -300, 0, List.of(
			new Site("enceladus/baghdad_lab", 0, 0, -1), new Site("enceladus/ocean_borehole", 50, 90, -1)));
	public static final World URANUS = new World("uranus", dim("uranus"), 20_000, -0.11, false, 0, 0, 230, List.of(
			new Site("uranus/meridian_aerostat", 0, 0, 230)));
	public static final World MIRANDA = new World("miranda", dim("miranda"), 2_000, -0.99, false, 160, 0, 0, List.of(
			new Site("miranda/rupes_camp", 0, 0, -1), new Site("miranda/fallen_lander", -150, 30, -1)));
	public static final World TITANIA = new World("titania", dim("titania"), 2_500, -0.96, false, 200, -120, 0, List.of(
			new Site("titania/messina_station", 0, 0, -1)));
	public static final World NEPTUNE = new World("neptune", dim("neptune"), 20_000, 0.14, false, 0, 0, 230, List.of(
			new Site("neptune/le_verrier_aerostat", 0, 0, 230)));
	public static final World TRITON = new World("triton", dim("triton"), 4_290, -0.92, false, -250, 350, 0, List.of(
			new Site("triton/hili_outpost", 0, 0, -1), new Site("triton/geyser_field_camp", 120, -80, -1)));
	/** Never named on the console: only a log at Triton's outpost gives its orbit (see travel.Discoveries). */
	public static final World NAMELESS = new World("nameless", dim("nameless"), 2_000, -0.99, false, 0, 0, 0, List.of(
			new Site("nameless/survey_rig", 0, 0, -1)));
	/** Gas giants: decks at the same heights as Jupiter's ({@link CloudDeckFeature}). */
	public static final List<World> GAS_GIANTS = List.of(JUPITER, SATURN, URANUS, NEPTUNE);

	/** Venus: the cloud city's deck and where its lift comes down (the foot shelter's spot). */
	public static final int VENUS_CITY_Y = 200;
	public static final int VENUS_LIFT_DX = 14;

	public static final List<World> ALL = List.of(VENUS, MERCURY, CERES, BELT, JUPITER, IO, EUROPA, GANYMEDE, CALLISTO,
			SATURN, RINGS, TITAN, ENCELADUS, URANUS, MIRANDA, TITANIA, NEPTUNE, TRITON, NAMELESS);
	private static final Map<String, World> BY_BODY = ALL.stream()
			.collect(java.util.stream.Collectors.toUnmodifiableMap(World::body, w -> w));

	private Worlds() {
	}

	public static Optional<World> byBody(String body) {
		return Optional.ofNullable(BY_BODY.get(body));
	}

	public static Optional<World> byDimension(ResourceKey<Level> key) {
		return ALL.stream().filter(w -> w.dimension().equals(key)).findFirst();
	}

	public static Optional<World> byDimension(String id) {
		return ALL.stream().filter(w -> w.dimension().identifier().toString().equals(id)).findFirst();
	}

	/** The world the mothership's lift goes down to from a station ("jupiter_orbit": Jupiter's aerostat). */
	public static Optional<World> byStation(String station) {
		return station.endsWith("_orbit") ? byBody(station.substring(0, station.length() - "_orbit".length()))
				: Optional.empty();
	}

	/**
	 * Nowhere to stand (the landing site was mined away over the void of the belt or of Jupiter): a small plated pad
	 * at the landing spot, so neither the ship nor a player falls into nothing.
	 */
	public static BlockPos emergencyPad(ServerLevel level, World w) {
		int y = w.landHeight() > 0 ? w.landHeight() : MoonSites.surface(level, w.landX(), w.landZ()).getY();
		if (y <= level.getMinY() + 1) {
			y = 120;
		}
		for (int dx = -3; dx <= 3; dx++) {
			for (int dz = -3; dz <= 3; dz++) {
				BlockPos p = new BlockPos(w.landX() + dx, y - 1, w.landZ() + dz);
				if (level.getBlockState(p).isAir()) {
					level.setBlock(p, hu.kriwdi.intersolarsystem.content.ModBlocks.STATION_HULL.defaultBlockState(), 3);
				}
			}
		}
		return new BlockPos(w.landX(), y, w.landZ());
	}

	/** Weightless places: Earth orbit and the asteroid belt (floating, handholds, tether, the manoeuvring unit). */
	public static boolean weightless(ResourceKey<Level> key) {
		return key.equals(hu.kriwdi.intersolarsystem.ship.ShipDimensions.ORBIT) || key.equals(BELT.dimension())
				|| key.equals(RINGS.dimension());
	}

	/** Airless places where the stars show by day (the client's sky). */
	public static boolean starry(ResourceKey<Level> key) {
		return byDimension(key).map(w -> w != VENUS && w != TITAN && !GAS_GIANTS.contains(w)).orElse(false);
	}

	/**
	 * The ground of a world at a column, under the cloud city on Venus (scanning from below its deck), like
	 * {@link MoonSites#surface} elsewhere.
	 */
	public static BlockPos surfaceBelow(ServerLevel level, int x, int z, int fromY) {
		level.getChunk(x >> 4, z >> 4);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, Math.min(fromY, level.getMaxY()), z);
		for (; pos.getY() > level.getMinY(); pos.move(0, -1, 0)) {
			var state = level.getBlockState(pos);
			if (!state.isAir() && !state.getCollisionShape(level, pos).isEmpty()) {
				return pos.above().immutable();
			}
		}
		return new BlockPos(x, level.getMinY(), z);
	}

	/** Where ground sites look for the ground from: below the cloud base on Venus, the top elsewhere. */
	public static int groundScanFrom(World w) {
		return w == VENUS ? PlanetHazards.VENUS_CLOUD_BASE : Integer.MAX_VALUE;
	}

	/** Aphrodite's lift: down to the foot shelter on the surface, or back up to the deck. */
	public static void venusLift(ServerLevel level, net.minecraft.server.level.ServerPlayer player) {
		ensureSites(level, VENUS);
		int x = VENUS.landX() + VENUS_LIFT_DX;
		int z = VENUS.landZ();
		BlockPos to;
		if (player.getY() >= PlanetHazards.VENUS_CLOUD_BASE) {
			// Into the foot shelter, beside its lift: the lift block found under the shelter's roof.
			to = surfaceBelow(level, x, z + 2, PlanetHazards.VENUS_CLOUD_BASE);
			for (int y = PlanetHazards.VENUS_CLOUD_BASE; y > level.getMinY(); y--) {
				if (level.getBlockState(new BlockPos(x, y, z)).is(hu.kriwdi.intersolarsystem.content.UrbanBlocks.HANGAR_LIFT)) {
					to = new BlockPos(x, y, z + 2);
					break;
				}
			}
			player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("venus.intersolarsystem.lift_down"));
		} else {
			to = new BlockPos(x - 2, VENUS_CITY_Y + 1, z);
			player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("venus.intersolarsystem.lift_up"));
		}
		player.teleport(new net.minecraft.world.level.portal.TeleportTransition(level,
				net.minecraft.world.phys.Vec3.atBottomCenterOf(to), net.minecraft.world.phys.Vec3.ZERO, player.getYRot(),
				0.0F, net.minecraft.world.level.portal.TeleportTransition.DO_NOTHING));
		level.playSound(null, to, net.minecraft.sounds.SoundEvents.PISTON_EXTEND, net.minecraft.sounds.SoundSource.BLOCKS,
				0.8F, 0.6F);
	}

	/** World borders, at server start. */
	public static void setBorders(MinecraftServer server) {
		for (World w : ALL) {
			ServerLevel level = server.getLevel(w.dimension());
			if (level != null) {
				level.getWorldBorder().setSize(w.size());
			}
		}
	}

	/** The block the ship's hatch should stand on, or the spot above which to search. */
	public static BlockPos landingSearchStart(World w) {
		return new BlockPos(w.landX(), w.landHeight(), w.landZ());
	}

	/** Places the world's sites once (the first landing). */
	public static void ensureSites(ServerLevel level, World w) {
		SpaceSites.ensureWorld(level, w);
	}
}
