package hu.kriwdi.intersolarsystem.ship;

import java.util.Optional;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import hu.kriwdi.intersolarsystem.space.MoonSites;
import hu.kriwdi.intersolarsystem.travel.CelestialBody;
import hu.kriwdi.intersolarsystem.travel.SolarSystem;
import hu.kriwdi.intersolarsystem.travel.TravelMath;

/** Server side of the navigation console: opens the star map and starts journeys after checking everything again. */
public final class ShipNavigation {
	private ShipNavigation() {
	}

	private static boolean canLaunch(ShipState state) {
		return !state.isReadOnly() && state.phase() == ShipFlightPhase.LANDED && state.landingSite().isPresent()
				&& ShipFlightRules.canTakeOff(state.condition(ShipSubsystem.ENGINE),
						state.condition(ShipSubsystem.LANDING_LAUNCH));
	}

	/** The place id ("earth", "earth_orbit", "moon") of a dimension. */
	public static String placeOf(String dimension) {
		if (ShipDimensions.ORBIT.identifier().toString().equals(dimension)) {
			return "earth_orbit";
		}
		if (ShipDimensions.MOON.identifier().toString().equals(dimension)) {
			return "moon";
		}
		return "earth";
	}

	static String here(ShipState state) {
		return placeOf(state.landingSite().map(ShipState.LandingSite::dimension).orElse(ShipTravel.OVERWORLD));
	}

	static boolean inOrbit(ShipState state) {
		return "earth_orbit".equals(here(state));
	}

	/** Whether a body can be chosen from {@code here}: anywhere but where the ship already is. */
	public static boolean reachable(CelestialBody body, String here) {
		return switch (body.status()) {
			case FLYBY -> "earth".equals(here) || "earth_orbit".equals(here);
			case ORBIT, LANDING -> !body.id().equals(here);
			case SURFACE -> !"earth".equals(here);
			case LOCKED -> false;
		};
	}

	public static void open(MinecraftServer server, ServerPlayer player) {
		ShipState state = ShipStateAccess.get(server);
		ServerPlayNetworking.send(player, new NavigationPayloads.Open(state.travel().engineTier(),
				server.overworld().getGameTime(), canLaunch(state), here(state)));
	}

	public static void launch(MinecraftServer server, ServerPlayer player, String bodyId) {
		ShipState state = ShipStateAccess.get(server);
		if (!player.level().dimension().equals(ShipDimensions.INTERIOR)) {
			return;
		}
		Optional<CelestialBody> body = SolarSystem.byId(bodyId);
		if (body.isEmpty() || !reachable(body.get(), here(state))) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.nav.unreachable"));
			return;
		}
		if (!canLaunch(state)) {
			player.sendSystemMessage(Component.translatable(state.phase() == ShipFlightPhase.LANDED
					? "command.intersolarsystem.ship.fly_too_damaged" : "command.intersolarsystem.ship.fly_not_landed"));
			return;
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
				int[] zone = MoonSites.LANDING_ZONE;
				started = state.beginTakeoff(zone[0], zone[1], now, seed, ShipFlightKind.NORMAL, body.get().id(),
						flightTicks, ShipDimensions.MOON.identifier().toString());
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
		if (started) {
			ShipFlightController.broadcast(server, Component.translatable("message.intersolarsystem.nav.launch",
					Component.translatable(body.get().translationKey()), ShipFlightTimeline.formatClock(flightTicks)));
		}
	}
}
