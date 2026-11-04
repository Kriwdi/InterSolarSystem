package hu.kriwdi.intersolarsystem.space;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;

/**
 * The hand-made sites of space, placed once per world from templates (see {@link SiteTemplates}):
 * <ul>
 * <li>the wreck field round the ISS: the old HALCYON laboratory, a dead relay satellite, a spent upper stage, a space
 * telescope, a crew capsule adrift, and five clouds of fragments between them and the station (stepping stones for
 * tethered walks);</li>
 * <li>the historic Moon missions at their real coordinates (Luna 9, Surveyor 3 by Apollo 12, Luna 17 / Lunokhod 1,
 * Luna 21 / Lunokhod 2, Chang'e 3 and 4, Chandrayaan-3, SLIM, Luna 24) and the Ark-1 colony on the rim of
 * Shackleton crater at the south pole.</li>
 * </ul>
 * New groups of sites (other bodies, more wrecks) are added to the lists below; a world gets each group once.
 */
public final class SpaceSites extends SavedData {
	public record Site(String template, int x, int y, int z) {
	}

	/** Around the ISS centre ({@link OrbitStation#CENTRE}). */
	public static final List<Site> ORBIT = List.of(
			new Site("orbit/debris_cloud_1", 75, 4, -10), new Site("orbit/debris_cloud_2", -78, -6, 5),
			new Site("orbit/debris_cloud_3", 10, -32, 0), new Site("orbit/debris_cloud_4", -15, 34, -20),
			new Site("orbit/debris_cloud_5", 30, 0, 50),
			new Site("orbit/halcyon", 105, 8, -30), new Site("orbit/dead_satellite", -112, -12, 30),
			new Site("orbit/upper_stage", 20, -58, 20), new Site("orbit/space_telescope", -30, 62, -45),
			new Site("orbit/capsule_adrift", 50, 10, 86));

	/** Moon sites by selenographic latitude / longitude (y unused: they stand on the surface). */
	public record MoonSite(String template, double latitude, double longitude, int dx, int dz) {
		public int x() {
			return (int) Math.round(longitude * MoonSites.BLOCKS_PER_DEGREE) + dx;
		}

		public int z() {
			return (int) Math.round(-latitude * MoonSites.BLOCKS_PER_DEGREE) + dz;
		}
	}

	public static final List<MoonSite> MOON = List.of(
			new MoonSite("moon/luna9", 7.08, -64.37, 0, 0),
			// Surveyor 3 stands 180 m from Apollo 12: a few blocks at this scale; set apart so both are visible.
			new MoonSite("moon/surveyor3", -3.012, -23.422, 14, -6),
			new MoonSite("moon/lunokhod1", 38.28, -35.0, 0, 0),
			new MoonSite("moon/lunokhod2", 25.85, 30.45, 0, 0),
			new MoonSite("moon/change3", 44.12, -19.51, 0, 0),
			new MoonSite("moon/change4", -45.44, 177.59, 0, 0),
			new MoonSite("moon/chandrayaan3", -69.37, 32.32, 0, 0),
			new MoonSite("moon/slim", -13.32, 25.25, 0, 0),
			new MoonSite("moon/luna24", 12.71, 62.21, 0, 0),
			new MoonSite("moon/ark1_colony", -89.2, 0.0, 0, 0));

	public static final Codec<SpaceSites> CODEC = CompoundTag.CODEC.xmap(SpaceSites::load, SpaceSites::save);
	public static final SavedDataType<SpaceSites> TYPE = new SavedDataType<>(ModIds.id("space_sites"), SpaceSites::new,
			CODEC, null);

	private final Set<String> placed = new HashSet<>();
	/** Where each site went in this session (tests look here). */
	public static final java.util.Map<String, BlockPos> PLACED_AT = new java.util.concurrent.ConcurrentHashMap<>();

	public static SpaceSites get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			// Worlds that already have the station or the Moon sites get the new sites on the next start.
			ServerLevel orbit = server.getLevel(ShipDimensions.ORBIT);
			if (orbit != null && OrbitStation.present(orbit)) {
				ensureOrbit(orbit);
			}
			ServerLevel moon = server.getLevel(ShipDimensions.MOON);
			if (moon != null && MoonSites.built(moon)) {
				ensureMoon(moon);
			}
		});
	}

	public static void ensureOrbit(ServerLevel orbit) {
		SpaceSites state = get(orbit.getServer());
		if (state.placed.contains("orbit")) {
			return;
		}
		for (Site s : ORBIT) {
			SiteTemplates.place(orbit, s.template(), OrbitStation.CENTRE.offset(s.x(), s.y(), s.z()));
		}
		state.placed.add("orbit");
		state.setDirty();
	}

	public static void ensureMoon(ServerLevel moon) {
		SpaceSites state = get(moon.getServer());
		if (state.placed.contains("moon")) {
			return;
		}
		for (MoonSite s : MOON) {
			BlockPos at = ground(moon, s.x(), s.z(), s.template().endsWith("colony") ? 40 : 6);
			SiteTemplates.place(moon, s.template(), at);
			PLACED_AT.put(s.template(), at);
		}
		state.placed.add("moon");
		state.setDirty();
	}

	public static void ensureMars(ServerLevel mars) {
		SpaceSites state = get(mars.getServer());
		if (state.placed.contains("mars")) {
			return;
		}
		for (MarsSites.Site s : MarsSites.SITES) {
			BlockPos at = MarsSites.ground(mars, s);
			SiteTemplates.place(mars, s.template(), at);
			PLACED_AT.put(s.template(), at);
		}
		state.placed.add("mars");
		state.setDirty();
	}

	/** The sites of one of the worlds beyond Mars ({@link Worlds}), once. */
	public static void ensureWorld(ServerLevel level, Worlds.World w) {
		SpaceSites state = get(level.getServer());
		if (state.placed.contains(w.body())) {
			return;
		}
		for (Worlds.Site s : w.sites()) {
			int x = w.landX() + s.dx();
			int z = w.landZ() + s.dz();
			BlockPos at = s.height() >= 0 ? new BlockPos(x, s.height(), z) : groundBelow(level, x, z, 8,
					Worlds.groundScanFrom(w));
			if (s.height() < 0 && at.getY() < Worlds.siteFloor(w)) {
				// Titan: never below the methane seas (the site's own deep foundation fills the rest).
				at = new BlockPos(at.getX(), Worlds.siteFloor(w), at.getZ());
			}
			SiteTemplates.place(level, s.template(), at);
			PLACED_AT.put(s.template(), at);
		}
		state.placed.add(w.body());
		state.setDirty();
	}

	/** Whether a group of sites ("moon", "mars", a far world) is in this world yet. */
	public boolean placedGroup(String group) {
		return placed.contains(group);
	}

	/** One-time story events of this world (the Ark-1 wake). */
	public boolean flagged(String key) {
		return placed.contains("flag:" + key);
	}

	public void flag(String key) {
		placed.add("flag:" + key);
		setDirty();
	}

	/** The regolith layer a site stands on: the median surface over a few samples round the centre. */
	public static BlockPos ground(ServerLevel moon, int x, int z, int spread) {
		int[] ys = new int[9];
		int i = 0;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				ys[i++] = MoonSites.surface(moon, x + dx * spread, z + dz * spread).getY();
			}
		}
		java.util.Arrays.sort(ys);
		return new BlockPos(x, ys[4] - 1, z);
	}

	/** Like {@link #ground}, scanning down from {@code fromY} (under Venus's cloud city). */
	static BlockPos groundBelow(ServerLevel level, int x, int z, int spread, int fromY) {
		int[] ys = new int[9];
		int i = 0;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				ys[i++] = Worlds.surfaceBelow(level, x + dx * spread, z + dz * spread, fromY).getY();
			}
		}
		java.util.Arrays.sort(ys);
		return new BlockPos(x, ys[4] - 1, z);
	}

	private static SpaceSites load(CompoundTag t) {
		SpaceSites s = new SpaceSites();
		for (var e : t.getListOrEmpty("placed")) {
			e.asString().ifPresent(s.placed::add);
		}
		return s;
	}

	private CompoundTag save() {
		CompoundTag t = new CompoundTag();
		ListTag list = new ListTag();
		for (String p : placed) {
			list.add(StringTag.valueOf(p));
		}
		t.put("placed", list);
		return t;
	}
}
