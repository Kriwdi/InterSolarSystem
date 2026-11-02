package hu.kriwdi.intersolarsystem.ship;

import java.util.Optional;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

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

	public static void open(MinecraftServer server, ServerPlayer player) {
		ShipState state = ShipStateAccess.get(server);
		ServerPlayNetworking.send(player, new NavigationPayloads.Open(state.travel().engineTier(),
				server.overworld().getGameTime(), canLaunch(state)));
	}

	public static void launch(MinecraftServer server, ServerPlayer player, String bodyId) {
		ShipState state = ShipStateAccess.get(server);
		if (!player.level().dimension().equals(ShipDimensions.INTERIOR)) {
			return;
		}
		Optional<CelestialBody> body = SolarSystem.byId(bodyId);
		if (body.isEmpty() || body.get().status() != CelestialBody.Status.FLYBY) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.nav.unreachable"));
			return;
		}
		if (!canLaunch(state)) {
			player.sendSystemMessage(Component.translatable(state.phase() == ShipFlightPhase.LANDED
					? "command.intersolarsystem.ship.fly_too_damaged" : "command.intersolarsystem.ship.fly_not_landed"));
			return;
		}
		long now = server.overworld().getGameTime();
		long flightTicks = TravelMath.flybyTicks(body.get(), state.travel().engineTier(), now);
		ShipState.LandingSite site = state.landingSite().get();
		if (state.beginTakeoff(site.x(), site.z(), now, server.overworld().getRandom().nextLong(), ShipFlightKind.NORMAL,
				body.get().id(), flightTicks)) {
			ShipFlightController.broadcast(server, Component.translatable("message.intersolarsystem.nav.launch",
					Component.translatable(body.get().translationKey()), ShipFlightTimeline.formatClock(flightTicks)));
		}
	}
}
