package hu.kriwdi.intersolarsystem.ship;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import hu.kriwdi.intersolarsystem.travel.TravelEventType;
import hu.kriwdi.intersolarsystem.travel.TravelMath;

/**
 * The travel side of the ship: engine tier, the current flight plan (where to and how long the flight phase lasts) and
 * the travel event in progress.
 *
 * @param target      "earth" for a flight to surface coordinates, otherwise the body id of a flyby
 * @param flightTicks length of the flight phase; 0 means the default for the flight kind
 */
public record ShipTravel(int engineTier, String target, long flightTicks, int eventsDone,
		Optional<ActiveEvent> event) {
	public static final ShipTravel NONE = new ShipTravel(1, "earth", 0, 0, Optional.empty());

	/** A travel event waiting for the crew; {@code evaded}: the crew responded in time. */
	public record ActiveEvent(TravelEventType type, long deadlineTick, boolean evaded) {
		private record Raw(String type, long deadlineTick, boolean evaded) {
		}

		private static final Codec<Raw> RAW = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("type").forGetter(Raw::type),
				Codec.LONG.fieldOf("deadline_tick").forGetter(Raw::deadlineTick),
				Codec.BOOL.optionalFieldOf("evaded", false).forGetter(Raw::evaded)
		).apply(instance, Raw::new));

		/** An unknown type decodes to nothing rather than failing the ship. */
		static final Codec<Optional<ActiveEvent>> CODEC = RAW.xmap(
				r -> TravelEventType.fromId(r.type()).map(t -> new ActiveEvent(t, r.deadlineTick(), r.evaded())),
				e -> e.map(a -> new Raw(a.type().id(), a.deadlineTick(), a.evaded()))
						.orElse(new Raw("none", 0L, false)));
	}

	public static final Codec<ShipTravel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.lenientOptionalFieldOf("engine_tier", 1).forGetter(ShipTravel::engineTier),
			Codec.STRING.lenientOptionalFieldOf("target", "earth").forGetter(ShipTravel::target),
			Codec.LONG.lenientOptionalFieldOf("flight_ticks", 0L).forGetter(ShipTravel::flightTicks),
			Codec.INT.lenientOptionalFieldOf("events_done", 0).forGetter(ShipTravel::eventsDone),
			ActiveEvent.CODEC.lenientOptionalFieldOf("event", Optional.empty()).forGetter(ShipTravel::event)
	).apply(instance, (tier, target, ticks, done, event) -> new ShipTravel(TravelMath.clampTier(tier), target,
			Math.max(0, ticks), Math.max(0, done), event)));

	public ShipTravel withPlan(String newTarget, long newFlightTicks) {
		return new ShipTravel(engineTier, newTarget, newFlightTicks, 0, Optional.empty());
	}

	public ShipTravel withEngineTier(int tier) {
		return new ShipTravel(TravelMath.clampTier(tier), target, flightTicks, eventsDone, event);
	}

	public ShipTravel withEventsDone(int done) {
		return new ShipTravel(engineTier, target, flightTicks, done, event);
	}

	public ShipTravel withEvent(Optional<ActiveEvent> next) {
		return new ShipTravel(engineTier, target, flightTicks, eventsDone, next);
	}

	/** Back home: the plan is cleared, the engine stays. */
	public ShipTravel landed() {
		return new ShipTravel(engineTier, "earth", 0, 0, Optional.empty());
	}
}
