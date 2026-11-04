package hu.kriwdi.intersolarsystem.ship;

import java.util.Optional;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import hu.kriwdi.intersolarsystem.InterSolarSystem;
import hu.kriwdi.intersolarsystem.space.MoonSites;
import hu.kriwdi.intersolarsystem.travel.CelestialBody;
import hu.kriwdi.intersolarsystem.travel.SolarSystem;
import hu.kriwdi.intersolarsystem.travel.TravelMath;

/** Server side of the navigation console: opens the star map and starts journeys after checking everything again. */
public final class ShipNavigation {
	private ShipNavigation() {
	}

	private static boolean canLaunch(ShipState state) {
		return blocker(state).isEmpty();
	}

	/** Why the ship cannot launch right now (a translation key), or "" when it can. */
	static String blocker(ShipState state) {
		if (state.isReadOnly()) {
			return "message.intersolarsystem.nav.blocked.read_only";
		}
		if (state.phase() != ShipFlightPhase.LANDED) {
			return "message.intersolarsystem.nav.blocked.flying";
		}
		if (state.landingSite().isEmpty()) {
			return "message.intersolarsystem.nav.blocked.no_site";
		}
		if (!ShipFlightRules.canTakeOff(state.condition(ShipSubsystem.ENGINE),
				state.condition(ShipSubsystem.LANDING_LAUNCH))) {
			return "command.intersolarsystem.ship.fly_too_damaged";
		}
		return "";
	}

	/** The place id ("earth", "earth_orbit", "moon") of a dimension. */
	public static String placeOf(String dimension) {
		if (ShipDimensions.ORBIT.identifier().toString().equals(dimension)) {
			return "earth_orbit";
		}
		if (ShipDimensions.MOON.identifier().toString().equals(dimension)) {
			return "moon";
		}
		if (ShipDimensions.MARS.identifier().toString().equals(dimension)) {
			return "mars";
		}
		if (ShipDimensions.MOTHERSHIP.identifier().toString().equals(dimension)) {
			return "mothership";
		}
		var far = hu.kriwdi.intersolarsystem.space.Worlds.byDimension(dimension);
		if (far.isPresent()) {
			return far.get().body();
		}
		return "earth";
	}

	static String here(ShipState state) {
		return placeOf(state.landingSite().map(ShipState.LandingSite::dimension).orElse(ShipTravel.OVERWORLD));
	}

	static boolean inOrbit(ShipState state) {
		return "earth_orbit".equals(here(state));
	}

	/** The Earth-Moon system: the small ship's own range. */
	public static final java.util.Set<String> LOCAL = java.util.Set.of("earth", "earth_orbit", "moon");
	private static final java.util.Set<String> LOCAL_STATIONS = java.util.Set.of("earth_orbit", "moon_orbit");

	/** Without a mothership (tests, old callers). */
	public static boolean reachable(CelestialBody body, String here) {
		return reachable(body, here, "");
	}

	/**
	 * Whether a body can be chosen from {@code here}. The small ship flies within the Earth-Moon system; beyond it only
	 * from the mothership when that orbits the body ({@code mothershipAt}: its station, "" when it is under way or
	 * not built), and back up to it.
	 */
	public static boolean reachable(CelestialBody body, String here, String mothershipAt) {
		if (body.id().equals(here) || body.status() == CelestialBody.Status.LOCKED || !SolarSystem.visible(body)) {
			return false;
		}
		boolean dockedLocal = "mothership".equals(here) && LOCAL_STATIONS.contains(mothershipAt);
		boolean local = LOCAL.contains(here) || dockedLocal;
		if (body.status() == CelestialBody.Status.DOCK) {
			if (mothershipAt.isEmpty()) {
				return false;
			}
			if (LOCAL_STATIONS.contains(mothershipAt)) {
				return LOCAL.contains(here);
			}
			return SolarSystem.byId(here).map(h -> h.station().equals(mothershipAt)).orElse(false);
		}
		if (body.viaMothership()) {
			if (!body.station().equals(mothershipAt)) {
				return false;
			}
			// From the mothership, or across from a neighbour in the same system (a moon of the same planet).
			return "mothership".equals(here)
					|| SolarSystem.byId(here).map(h -> h.viaMothership() && h.station().equals(mothershipAt)).orElse(false);
		}
		if (!local) {
			// Stranded far away without the mothership: only the long burn home.
			return body.status() == CelestialBody.Status.SURFACE && !mothershipAt.equals(
					SolarSystem.byId(here).map(CelestialBody::station).orElse(""));
		}
		return switch (body.status()) {
			case FLYBY -> "earth".equals(here) || "earth_orbit".equals(here);
			case ORBIT, LANDING -> true;
			case SURFACE -> !"earth".equals(here);
			default -> false;
		};
	}

	/** Where the mothership is parked for the small ship ("" while it travels or before it is built). */
	public static String mothershipAt(MinecraftServer server) {
		return hu.kriwdi.intersolarsystem.spaceport.Mothership.get(server).station();
	}

	public static void open(MinecraftServer server, ServerPlayer player) {
		ShipState state = ShipStateAccess.get(server);
		ServerPlayNetworking.send(player, new NavigationPayloads.Open(state.travel().engineTier(),
				server.overworld().getGameTime(), blocker(state), here(state), mothershipAt(server),
				String.join(",", SolarSystem.discovered())));
	}

	/**
	 * Starts a journey towards {@code bodyId}. Every refusal is told to the player (and logged), so a click on LAUNCH
	 * never silently does nothing. Returns whether the takeoff sequence started.
	 */
	public static boolean launch(MinecraftServer server, ServerPlayer player, String bodyId) {
		ShipState state = ShipStateAccess.get(server);
		String result = tryLaunch(server, player, state, bodyId);
		InterSolarSystem.LOGGER.info("[nav] {} asked to launch towards {}: {}", player.getName().getString(), bodyId,
				result.isEmpty() ? "takeoff started" : result);
		if (!result.isEmpty()) {
			player.sendSystemMessage(Component.translatable(result).withStyle(ChatFormatting.RED));
		}
		return result.isEmpty();
	}

	private static String tryLaunch(MinecraftServer server, ServerPlayer player, ShipState state, String bodyId) {
		if (!player.level().dimension().equals(ShipDimensions.INTERIOR)) {
			return "message.intersolarsystem.nav.blocked.not_aboard";
		}
		Optional<CelestialBody> body = SolarSystem.byId(bodyId);
		if (body.isEmpty() || !reachable(body.get(), here(state), mothershipAt(server))) {
			return "message.intersolarsystem.nav.unreachable";
		}
		String blocker = blocker(state);
		if (!blocker.isEmpty()) {
			return blocker;
		}
		if (!body.get().viaMothership() && state.travel().engineTier() < body.get().minTier()) {
			return "message.intersolarsystem.nav.blocked.engine_tier";
		}
		long now = server.overworld().getGameTime();
		long seed = server.overworld().getRandom().nextLong();
		int tier = state.travel().engineTier();
		ShipState.LandingSite site = state.landingSite().get();
		boolean started;
		long flightTicks;
		switch (body.get().status()) {
			case LANDING -> {
				flightTicks = TravelMath.legTicks(here(state), body.get().id(), tier, now);
				var far = hu.kriwdi.intersolarsystem.space.Worlds.byBody(body.get().id());
				boolean mars = "mars".equals(body.get().id());
				int[] zone = far.isPresent() ? new int[] {far.get().landX(), far.get().landZ()}
						: mars ? hu.kriwdi.intersolarsystem.space.MarsSites.LANDING_ZONE : MoonSites.LANDING_ZONE;
				String dim = far.isPresent() ? far.get().dimension().identifier().toString()
						: (mars ? ShipDimensions.MARS : ShipDimensions.MOON).identifier().toString();
				started = state.beginTakeoff(zone[0], zone[1], now, seed, ShipFlightKind.NORMAL, body.get().id(),
						flightTicks, dim);
			}
			case DOCK -> {
				flightTicks = TravelMath.legTicks(here(state), "mothership", tier, now);
				BlockPos port = hu.kriwdi.intersolarsystem.spaceport.Mothership.dockingPort();
				started = state.beginTakeoff(port.getX(), port.getZ(), now, seed, ShipFlightKind.NORMAL, "mothership",
						flightTicks, ShipDimensions.MOTHERSHIP.identifier().toString());
			}
			case ORBIT -> {
				flightTicks = TravelMath.legTicks(here(state), body.get().id(), tier, now);
				started = state.beginTakeoff(site.x(), site.z(), now, seed, ShipFlightKind.NORMAL, body.get().id(),
						flightTicks, ShipDimensions.ORBIT.identifier().toString());
			}
			case SURFACE -> {
				// Back down to where the ship last stood on Earth (or the world spawn).
				flightTicks = TravelMath.legTicks(here(state), "earth", tier, now);
				var spawn = server.getRespawnData().pos();
				ShipTravel.Surface home = state.travel().surface()
						.orElse(new ShipTravel.Surface(spawn.getX(), spawn.getZ()));
				started = state.beginTakeoff(home.x(), home.z(), now, seed, ShipFlightKind.NORMAL, "earth", flightTicks,
						ShipTravel.OVERWORLD);
			}
			default -> {
				flightTicks = TravelMath.flybyTicks(body.get(), tier, now);
				started = state.beginTakeoff(site.x(), site.z(), now, seed, ShipFlightKind.NORMAL, body.get().id(),
						flightTicks);
			}
		}
		if (!started) {
			return "message.intersolarsystem.nav.blocked.flying";
		}
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior != null) {
			ShipNetworks.consume(interior);
		}
		ShipFlightController.broadcast(server, Component.translatable("message.intersolarsystem.nav.launch",
				Component.translatable(body.get().translationKey()), ShipFlightTimeline.formatClock(flightTicks)));
		return "";
	}
}
