package hu.kriwdi.intersolarsystem.ship;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Cell;
import hu.kriwdi.intersolarsystem.travel.TravelEventRules;
import hu.kriwdi.intersolarsystem.travel.TravelEventType;

/** Server side of travel events on long flights. Rolls come from the flight's saved seed, so reloads change nothing. */
public final class ShipTravelEvents {
	public static final int METEOR_HULL_DAMAGE = 15;
	public static final int METEOR_NAV_DAMAGE = 5;
	public static final int EVADE_FUEL_COST = 4;
	public static final int FLARE_NAV_DAMAGE = 8;
	public static final int FLARE_LIFE_DAMAGE = 6;

	private ShipTravelEvents() {
	}

	public static void tick(MinecraftServer server, ShipState state, long now) {
		state.travel().event().ifPresent(event -> {
			if (now >= event.deadlineTick()) {
				resolve(server, state, event);
				state.setTravelEvent(Optional.empty());
			}
		});
		long flightTicks = state.phaseDuration(ShipFlightPhase.FLIGHT);
		int due = TravelEventRules.rollsDue(flightTicks, now - state.phaseStartedTick());
		int done = state.travel().eventsDone();
		for (int index = done + 1; index <= due; index++) {
			int roll = index;
			TravelEventRules.roll(state.hazards().seed(), roll).ifPresent(type -> start(server, state, type, roll, now));
		}
		if (due > done) {
			state.setTravelEventsDone(due);
		}
	}

	private static void start(MinecraftServer server, ShipState state, TravelEventType type, int index, long now) {
		switch (type) {
			case METEOR_SHOWER -> {
				if (state.travel().event().isPresent()) {
					return;
				}
				state.setTravelEvent(Optional.of(new ShipTravel.ActiveEvent(type,
						now + TravelEventRules.RESPONSE_TICKS, false)));
				say(server, "message.intersolarsystem.travel.meteor_warning");
				ShipFlightController.playInterior(server, SoundEvents.BELL_BLOCK);
			}
			case SOLAR_FLARE -> {
				damage(state, ShipSubsystem.NAVIGATION_COMMS, FLARE_NAV_DAMAGE);
				damage(state, ShipSubsystem.LIFE_SUPPORT, FLARE_LIFE_DAMAGE);
				say(server, "message.intersolarsystem.travel.solar_flare");
				ShipFlightController.playInterior(server, SoundEvents.BEACON_DEACTIVATE);
			}
			case SYSTEM_FAILURE -> {
				List<ShipSubsystem> free = java.util.Arrays.stream(ShipSubsystem.values())
						.filter(s -> state.hazards().incidents().stream().noneMatch(i -> i.subsystem() == s))
						.sorted(Comparator.comparingInt(state::condition)).toList();
				if (!free.isEmpty()) {
					ShipSubsystem s = free.get(0);
					ShipIncident incident = new ShipIncident(s, ShipIncidentRules.typeFor(s),
							now + ShipIncidentRules.INCIDENT_DEADLINE_TICKS);
					if (state.addIncident(incident)) {
						ShipFlightController.broadcast(server, Component.translatable(
								"message.intersolarsystem.ship.incident",
								Component.translatable(ShipCommands.incidentKey(incident.type())),
								Component.translatable(ShipCommands.subsystemKey(s))));
						ShipFlightController.playInterior(server, SoundEvents.BEACON_DEACTIVATE);
					}
				}
			}
			case DRIFTING_WRECK -> {
				salvage(server);
				say(server, "message.intersolarsystem.travel.wreck");
				ShipFlightController.playInterior(server, SoundEvents.CHAIN_PLACE);
			}
			case SIGNAL -> {
				int line = TravelEventRules.signalLine(state.hazards().seed(), index);
				ShipFlightController.broadcast(server, Component.translatable(
						"message.intersolarsystem.travel.signal",
						Component.translatable("message.intersolarsystem.travel.signal." + line)));
				ShipFlightController.playInterior(server, SoundEvents.AMETHYST_BLOCK_CHIME);
			}
		}
	}

	private static void resolve(MinecraftServer server, ShipState state, ShipTravel.ActiveEvent event) {
		if (event.type() == TravelEventType.METEOR_SHOWER) {
			if (event.evaded()) {
				say(server, "message.intersolarsystem.travel.meteor_evaded");
			} else {
				damage(state, ShipSubsystem.HULL_ARMOR, METEOR_HULL_DAMAGE);
				damage(state, ShipSubsystem.NAVIGATION_COMMS, METEOR_NAV_DAMAGE);
				say(server, "message.intersolarsystem.travel.meteor_hit");
				ShipFlightController.playInterior(server, SoundEvents.GENERIC_EXPLODE.value());
			}
		}
	}

	/** The crew answers the waiting event at the navigation console. Returns whether there was something to answer. */
	public static boolean respond(MinecraftServer server, ServerPlayer player) {
		ShipState state = ShipStateAccess.get(server);
		Optional<ShipTravel.ActiveEvent> event = state.travel().event();
		if (event.isEmpty() || event.get().evaded() || !event.get().type().needsResponse()) {
			return false;
		}
		state.setTravelEvent(Optional.of(new ShipTravel.ActiveEvent(event.get().type(), event.get().deadlineTick(),
				true)));
		damage(state, ShipSubsystem.FUEL_ENERGY, EVADE_FUEL_COST);
		ShipFlightController.broadcast(server, Component.translatable("message.intersolarsystem.travel.evading",
				player.getDisplayName()));
		ShipFlightController.playInterior(server, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST);
		return true;
	}

	private static void damage(ShipState state, ShipSubsystem subsystem, int amount) {
		state.setCondition(subsystem, state.condition(subsystem) - amount);
	}

	private static void say(MinecraftServer server, String key) {
		ShipFlightController.broadcast(server, Component.translatable(key));
	}

	/** Salvage from the wreck drifts in through the airlock: a few parts land at the ship's entry. */
	private static void salvage(MinecraftServer server) {
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior == null) {
			return;
		}
		RandomSource random = interior.getRandom();
		Item[] pool = {ModItems.SCRAP_METAL, ModItems.SCRAP_METAL, ModItems.SALVAGED_WIRING, ModItems.SALVAGED_CIRCUIT};
		Cell entry = ShipInteriorLayout.capsule().entry();
		BlockPos at = ShipDimensions.ANCHOR.offset(entry.dx(), entry.dy(), entry.dz());
		int drops = 2 + random.nextInt(3);
		for (int i = 0; i < drops; i++) {
			ItemStack stack = new ItemStack(pool[random.nextInt(pool.length)], 1 + random.nextInt(2));
			ItemEntity entity = new ItemEntity(interior, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, stack);
			interior.addFreshEntity(entity);
		}
	}
}
