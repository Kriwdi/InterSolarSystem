package hu.kriwdi.intersolarsystem.spaceport;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Every spaceport that has been touched (saved with the world). A site is found by position: the machines of one
 * spaceport all lie within {@link #SITE_RADIUS} blocks of its anchor (the first cabinet or console used there).
 * Several spaceports can be restored independently.
 */
public class SpaceportState extends SavedData {
	public static final int SITE_RADIUS = 400;

	public static final class FacilityState {
		public final Map<String, Integer> delivered = new HashMap<>();
		public boolean calibrated;

		CompoundTag save() {
			CompoundTag t = new CompoundTag();
			CompoundTag d = new CompoundTag();
			delivered.forEach(d::putInt);
			t.put("delivered", d);
			t.putBoolean("calibrated", calibrated);
			return t;
		}

		static FacilityState load(CompoundTag t) {
			FacilityState f = new FacilityState();
			CompoundTag d = t.getCompoundOrEmpty("delivered");
			for (String k : d.keySet()) {
				f.delivered.put(k, d.getIntOr(k, 0));
			}
			f.calibrated = t.getBooleanOr("calibrated", false);
			return f;
		}
	}

	public static final class Site {
		public String dimension = "minecraft:overworld";
		public int x;
		public int z;
		public final Map<Facility, FacilityState> facilities = new EnumMap<>(Facility.class);
		/** Reactor fuel left, in ticks. */
		public long reactorFuel;
		/** The reactor has a fault (offline until repaired). */
		public boolean reactorFault;
		public long nextFaultCheck;
		/** Propellant in the depot's tanks (units). */
		public int propellant;
		/** Fabricator: the selected recipe and what has been delivered for it. */
		public int recipe;
		public final Map<String, Integer> fabricatorDelivered = new HashMap<>();
		/** Crane: sections finished, the section being built (-1: none) and the next block of it. */
		public int sectionsDone;
		public int building = -1;
		public int buildIndex;
		public BlockPos consolePos = BlockPos.ZERO;
		public int consoleRotation;
		public boolean launched;
		public int printed;

		public FacilityState facility(Facility f) {
			return facilities.computeIfAbsent(f, k -> new FacilityState());
		}

		public boolean online(Facility f) {
			FacilityState s = facilities.get(f);
			if (s == null || !s.calibrated) {
				return false;
			}
			return f != Facility.POWER || reactorFuel > 0 && !reactorFault;
		}

		/** Everything except the power plant needs the power plant running. */
		public boolean working(Facility f) {
			return online(f) && (f == Facility.POWER || online(Facility.POWER));
		}

		CompoundTag save() {
			CompoundTag t = new CompoundTag();
			t.putString("dimension", dimension);
			t.putInt("x", x);
			t.putInt("z", z);
			CompoundTag fs = new CompoundTag();
			facilities.forEach((k, v) -> fs.put(k.id(), v.save()));
			t.put("facilities", fs);
			t.putLong("reactorFuel", reactorFuel);
			t.putBoolean("reactorFault", reactorFault);
			t.putLong("nextFaultCheck", nextFaultCheck);
			t.putInt("propellant", propellant);
			t.putInt("recipe", recipe);
			CompoundTag fd = new CompoundTag();
			fabricatorDelivered.forEach(fd::putInt);
			t.put("fabricator", fd);
			t.putInt("sectionsDone", sectionsDone);
			t.putInt("building", building);
			t.putInt("buildIndex", buildIndex);
			t.putInt("cx", consolePos.getX());
			t.putInt("cy", consolePos.getY());
			t.putInt("cz", consolePos.getZ());
			t.putInt("rot", consoleRotation);
			t.putBoolean("launched", launched);
			t.putInt("printed", printed);
			return t;
		}

		static Site load(CompoundTag t) {
			Site s = new Site();
			s.dimension = t.getStringOr("dimension", "minecraft:overworld");
			s.x = t.getIntOr("x", 0);
			s.z = t.getIntOr("z", 0);
			CompoundTag fs = t.getCompoundOrEmpty("facilities");
			for (Facility f : Facility.values()) {
				if (fs.contains(f.id())) {
					s.facilities.put(f, FacilityState.load(fs.getCompoundOrEmpty(f.id())));
				}
			}
			s.reactorFuel = t.getLongOr("reactorFuel", 0L);
			s.reactorFault = t.getBooleanOr("reactorFault", false);
			s.nextFaultCheck = t.getLongOr("nextFaultCheck", 0L);
			s.propellant = t.getIntOr("propellant", 0);
			s.recipe = t.getIntOr("recipe", 0);
			CompoundTag fd = t.getCompoundOrEmpty("fabricator");
			for (String k : fd.keySet()) {
				s.fabricatorDelivered.put(k, fd.getIntOr(k, 0));
			}
			s.sectionsDone = t.getIntOr("sectionsDone", 0);
			s.building = t.getIntOr("building", -1);
			s.buildIndex = t.getIntOr("buildIndex", 0);
			s.consolePos = new BlockPos(t.getIntOr("cx", 0), t.getIntOr("cy", 0), t.getIntOr("cz", 0));
			s.consoleRotation = t.getIntOr("rot", 0);
			s.launched = t.getBooleanOr("launched", false);
			s.printed = t.getIntOr("printed", 0);
			return s;
		}
	}

	public static final Codec<SpaceportState> CODEC = CompoundTag.CODEC.xmap(SpaceportState::load, SpaceportState::save);
	public static final SavedDataType<SpaceportState> TYPE = new SavedDataType<>(ModIds.id("spaceports"),
			SpaceportState::new, CODEC, null);

	private final List<Site> sites = new ArrayList<>();

	public static SpaceportState get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public List<Site> sites() {
		return sites;
	}

	/** The site this position belongs to, created on first use. */
	public Site site(String dimension, BlockPos pos) {
		for (Site s : sites) {
			if (s.dimension.equals(dimension) && Math.abs(s.x - pos.getX()) <= SITE_RADIUS
					&& Math.abs(s.z - pos.getZ()) <= SITE_RADIUS) {
				return s;
			}
		}
		Site s = new Site();
		s.dimension = dimension;
		s.x = pos.getX();
		s.z = pos.getZ();
		sites.add(s);
		setDirty();
		return s;
	}

	public void changed() {
		setDirty();
	}

	private static SpaceportState load(CompoundTag tag) {
		SpaceportState state = new SpaceportState();
		ListTag list = tag.getListOrEmpty("sites");
		for (int i = 0; i < list.size(); i++) {
			state.sites.add(Site.load(list.getCompoundOrEmpty(i)));
		}
		return state;
	}

	private CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		ListTag list = new ListTag();
		for (Site s : sites) {
			list.add(s.save());
		}
		tag.put("sites", list);
		return tag;
	}
}
