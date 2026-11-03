package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Input;

import hu.kriwdi.intersolarsystem.content.SeatEntity;
import hu.kriwdi.intersolarsystem.travel.MeteorField;
import hu.kriwdi.intersolarsystem.travel.TravelEventRules;
import hu.kriwdi.intersolarsystem.travel.TravelEventType;

/**
 * Flying through a meteor shower. The pilot in the pilot's seat steers with the movement keys (W/S up and down,
 * A/D sideways); every rock that reaches the ship where it is hits the hull. Without a pilot the ship drifts, unless
 * someone engaged the autopilot at the navigation console (it dodges, at a fuel cost). The server owns the ship's
 * position and the hits; the crew gets the field and the position a few times a second.
 */
public final class ShipManeuver {
	public static final int HULL_DAMAGE_PER_HIT = 5;
	public static final int NAV_DAMAGE_PER_HIT = 1;

	private static long deadline = -1;
	private static List<MeteorField.Meteor> field = List.of();
	private static long seed;
	private static int next;
	private static float x;
	private static float y;
	private static float vx;
	private static float vy;
	private static int hits;

	private ShipManeuver() {
	}

	public static void reset() {
		deadline = -1;
		hits = 0;
	}

	/** Rocks that hit during the current (or last) shower. */
	public static int hits() {
		return hits;
	}

	public static void tick(MinecraftServer server, ShipState state, long now) {
		Optional<ShipTravel.ActiveEvent> event = state.travel().event()
				.filter(e -> e.type() == TravelEventType.METEOR_SHOWER);
		if (event.isEmpty()) {
			if (deadline != -1) {
				deadline = -1;
				send(server, ManeuverPayload.end());
			}
			return;
		}
		ShipTravel.ActiveEvent shower = event.get();
		if (deadline != shower.deadlineTick()) {
			deadline = shower.deadlineTick();
			seed = state.hazards().seed() ^ deadline;
			List<MeteorField.Meteor> rocks = new ArrayList<>(MeteorField.meteors(seed, TravelEventRules.RESPONSE_TICKS));
			rocks.sort(Comparator.comparingInt(MeteorField.Meteor::arrive));
			field = rocks;
			next = 0;
			x = y = vx = vy = 0.0F;
			hits = 0;
		}
		int elapsed = (int) (now - (deadline - TravelEventRules.RESPONSE_TICKS));
		ServerPlayer pilot = pilot(server);
		int inX = 0;
		int inY = 0;
		if (pilot != null) {
			Input in = pilot.getLastClientInput();
			inX = (in.right() ? 1 : 0) - (in.left() ? 1 : 0);
			inY = (in.forward() ? 1 : 0) - (in.backward() ? 1 : 0);
		} else if (shower.evaded()) {
			int[] dodge = autopilot(elapsed);
			inX = dodge[0];
			inY = dodge[1];
		}
		float[] s = MeteorField.step(x, y, vx, vy, inX, inY);
		x = s[0];
		y = s[1];
		vx = s[2];
		vy = s[3];
		while (next < field.size() && field.get(next).arrive() <= elapsed) {
			MeteorField.Meteor rock = field.get(next++);
			if (rock.hits(x, y)) {
				hits++;
				state.setCondition(ShipSubsystem.HULL_ARMOR, state.condition(ShipSubsystem.HULL_ARMOR) - HULL_DAMAGE_PER_HIT);
				state.setCondition(ShipSubsystem.NAVIGATION_COMMS,
						state.condition(ShipSubsystem.NAVIGATION_COMMS) - NAV_DAMAGE_PER_HIT);
				ShipFlightController.playInterior(server, SoundEvents.GENERIC_EXPLODE.value());
			}
		}
		if (now % 2 == 0) {
			send(server, new ManeuverPayload(seed, elapsed, TravelEventRules.RESPONSE_TICKS, x, y, hits, pilot != null));
		}
	}

	/** The autopilot pushes away from the next rock that would hit. */
	private static int[] autopilot(int elapsed) {
		for (int i = next; i < field.size(); i++) {
			MeteorField.Meteor rock = field.get(i);
			if (rock.arrive() - elapsed > 30) {
				break;
			}
			if (rock.hits(x, y)) {
				int dx = rock.x() > x ? -1 : 1;
				int dy = rock.y() > y ? -1 : 1;
				if (Math.abs(x) > 0.85F) {
					dx = x > 0 ? -1 : 1;
				}
				if (Math.abs(y) > 0.85F) {
					dy = y > 0 ? -1 : 1;
				}
				return new int[] {dx, dy};
			}
		}
		return new int[] {0, 0};
	}

	/** Whoever sits in the pilot's seat. */
	public static ServerPlayer pilot(MinecraftServer server) {
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior == null) {
			return null;
		}
		BlockPos seat = ShipInteriorBuilder.at(ShipInteriorLayout.capsule().pilotSeat());
		for (ServerPlayer player : interior.players()) {
			if (player.getVehicle() instanceof SeatEntity s && s.blockPosition().equals(seat)) {
				return player;
			}
		}
		return null;
	}

	private static void send(MinecraftServer server, ManeuverPayload payload) {
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior != null) {
			interior.players().forEach(p -> ServerPlayNetworking.send(p, payload));
		}
	}
}
