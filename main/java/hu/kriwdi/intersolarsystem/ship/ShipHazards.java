package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import hu.kriwdi.intersolarsystem.core.ModIds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The random side of a flight: the saved seed (so reloading cannot re-roll), how many rolls already ran and the
 * active incidents.
 */
public record ShipHazards(long seed, int rollsDone, List<ShipIncident> incidents) {
	public static final ShipHazards NONE = new ShipHazards(0L, 0, List.of());

	private static final Logger LOGGER = LoggerFactory.getLogger(ModIds.MOD_ID);

	public ShipHazards {
		incidents = List.copyOf(incidents);
	}

	private record RawIncident(String subsystem, String type, long deadlineTick) {
		static final Codec<RawIncident> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("subsystem").forGetter(RawIncident::subsystem),
				Codec.STRING.fieldOf("type").forGetter(RawIncident::type),
				Codec.LONG.fieldOf("deadline_tick").forGetter(RawIncident::deadlineTick)
		).apply(instance, RawIncident::new));
	}

	public static final Codec<ShipHazards> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.lenientOptionalFieldOf("seed", 0L).forGetter(ShipHazards::seed),
			Codec.INT.lenientOptionalFieldOf("rolls_done", 0).forGetter(ShipHazards::rollsDone),
			RawIncident.CODEC.listOf().lenientOptionalFieldOf("incidents", List.of()).forGetter(h -> h.incidents().stream()
					.map(i -> new RawIncident(i.subsystem().id(), i.type().id(), i.deadlineTick())).toList())
	).apply(instance, ShipHazards::fromRaw));

	private static ShipHazards fromRaw(long seed, int rollsDone, List<RawIncident> raw) {
		List<ShipIncident> incidents = new ArrayList<>();
		for (RawIncident r : raw) {
			var subsystem = ShipSubsystem.fromId(r.subsystem());
			var type = ShipIncidentType.fromId(r.type());
			if (subsystem.isPresent() && type.isPresent()) {
				incidents.add(new ShipIncident(subsystem.get(), type.get(), r.deadlineTick()));
			} else {
				LOGGER.warn("Ignoring ship incident with unknown subsystem '{}' or type '{}'", r.subsystem(), r.type());
			}
		}
		return new ShipHazards(seed, Math.max(0, rollsDone), incidents);
	}
}
