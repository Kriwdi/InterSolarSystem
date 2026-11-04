package hu.kriwdi.intersolarsystem.space;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import hu.kriwdi.intersolarsystem.worldgen.MarsSurfaceFeature;

/**
 * Humanity's traces on Mars at their real areographic positions, scaled to the 10 600-block world (north is -z):
 * Viking 1 and 2, Pathfinder with Sojourner, Spirit (stuck at Troy), Opportunity (silenced by the 2018 storm),
 * Curiosity on Mount Sharp, Perseverance with Ingenuity, Zhurong, InSight, and the Ark-2 colony on the Jezero delta,
 * where the ship sets down. Placed once per world, on the first landing (see {@link SpaceSites}).
 */
public final class MarsSites {
	public static final double BLOCKS_PER_DEGREE = MarsSurfaceFeature.SIZE / 360.0;

	public record Site(String template, double latitude, double longitude, int dx, int dz) {
		public int x() {
			return (int) Math.round(longitude * BLOCKS_PER_DEGREE) + dx;
		}

		public int z() {
			return (int) Math.round(-latitude * BLOCKS_PER_DEGREE) + dz;
		}
	}

	public static final Site ARK2 = new Site("mars/ark2_colony", 18.38, 77.58, 0, 0);
	public static final List<Site> SITES = List.of(
			new Site("mars/viking1", 22.27, -47.95, 0, 0),
			new Site("mars/viking2", 47.64, -134.29, 0, 0),
			new Site("mars/pathfinder", 19.13, -33.22, 0, 0),
			new Site("mars/spirit", -14.57, 175.47, 0, 0),
			new Site("mars/opportunity", -1.95, -5.53, 0, 0),
			new Site("mars/curiosity", -4.59, 137.44, 0, 0),
			// Perseverance drives the delta a little way from the colony (the real rover landed 2 km from the delta).
			new Site("mars/perseverance", 18.44, 77.45, 70, 40),
			new Site("mars/zhurong", 25.07, 109.93, 0, 0),
			new Site("mars/insight", 4.50, 135.62, 0, 0),
			ARK2);
	/** The ship comes down on the plain south of the colony. */
	public static final int[] LANDING_ZONE = {ARK2.x() + 10, ARK2.z() + 70};

	private MarsSites() {
	}

	public static void ensureBuilt(ServerLevel mars) {
		SpaceSites.ensureMars(mars);
	}

	static BlockPos ground(ServerLevel mars, Site s) {
		return SpaceSites.ground(mars, s.x(), s.z(), s.template().endsWith("colony") ? 40 : 6);
	}
}
