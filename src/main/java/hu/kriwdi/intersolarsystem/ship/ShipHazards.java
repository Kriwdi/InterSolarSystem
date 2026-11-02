package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
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

		/**
		 * One list element: a broken entry decodes to empty (with a warning) instead of failing the whole list, so a
		 * single corrupted incident cannot take the others with it.
		 */
		static final Codec<Optional<RawIncident>> LENIENT = new Codec<>() {
			@Override
			public <T> DataResult<Pair<Optional<RawIncident>, T>> decode(DynamicOps<T> ops, T input) {
				DataResult<Pair<RawIncident, T>> result = CODEC.decode(ops, input);
				if (result.isSuccess()) {
					return result.map(p -> p.mapFirst(Optional::of));
				}
				LOGGER.warn("Ignoring unreadable ship incident: {}", result.error().map(e -> e.message()).orElse("?"));
				return DataResult.success(Pair.of(Optional.empty(), ops.empty()));
			}

			@Override
			public <T> DataResult<T> encode(Optional<RawIncident> input, DynamicOps<T> ops, T prefix) {
				return input.map(r -> CODEC.encode(r, ops, prefix)).orElseGet(() -> DataResult.success(prefix));
			}
		};
	}

	public static final Codec<ShipHazards> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.lenientOptionalFieldOf("seed", 0L).forGetter(ShipHazards::seed),
			Codec.INT.lenientOptionalFieldOf("rolls_done", 0).forGetter(ShipHazards::rollsDone),
			RawIncident.LENIENT.listOf().lenientOptionalFieldOf("incidents", List.of()).forGetter(h -> h.incidents().stream()
					.map(i -> Optional.of(new RawIncident(i.subsystem().id(), i.type().id(), i.deadlineTick()))).toList())
	).apply(instance, ShipHazards::fromRaw));

	private static ShipHazards fromRaw(long seed, int rollsDone, List<Optional<RawIncident>> rawList) {
		List<ShipIncident> incidents = new ArrayList<>();
		for (Optional<RawIncident> entry : rawList) {
			if (entry.isEmpty()) {
				continue;
			}
			RawIncident r = entry.get();
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
