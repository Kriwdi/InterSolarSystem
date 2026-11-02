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
 * The hidden side of the ship's health: latent faults, how many diagnostic scans ran (the scan roll index) and when the
 * last one ran (cooldown).
 */
public record ShipDiagnostics(List<ShipLatentFault> faults, int scansDone, long lastScanTick) {
	public static final ShipDiagnostics NONE = new ShipDiagnostics(List.of(), 0, Long.MIN_VALUE);

	private static final Logger LOGGER = LoggerFactory.getLogger(ModIds.MOD_ID);

	public ShipDiagnostics {
		faults = List.copyOf(faults);
	}

	private record RawFault(String subsystem, boolean revealed) {
		static final Codec<RawFault> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.STRING.fieldOf("subsystem").forGetter(RawFault::subsystem),
				Codec.BOOL.optionalFieldOf("revealed", false).forGetter(RawFault::revealed)
		).apply(instance, RawFault::new));

		/** A broken entry is skipped with a warning instead of failing the whole list. */
		static final Codec<Optional<RawFault>> LENIENT = new Codec<>() {
			@Override
			public <T> DataResult<Pair<Optional<RawFault>, T>> decode(DynamicOps<T> ops, T input) {
				DataResult<Pair<RawFault, T>> result = CODEC.decode(ops, input);
				if (result.isSuccess()) {
					return result.map(p -> p.mapFirst(Optional::of));
				}
				LOGGER.warn("Ignoring unreadable ship fault: {}", result.error().map(e -> e.message()).orElse("?"));
				return DataResult.success(Pair.of(Optional.empty(), ops.empty()));
			}

			@Override
			public <T> DataResult<T> encode(Optional<RawFault> input, DynamicOps<T> ops, T prefix) {
				return input.map(r -> CODEC.encode(r, ops, prefix)).orElseGet(() -> DataResult.success(prefix));
			}
		};
	}

	public static final Codec<ShipDiagnostics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RawFault.LENIENT.listOf().lenientOptionalFieldOf("faults", List.of()).forGetter(d -> d.faults().stream()
					.map(f -> Optional.of(new RawFault(f.subsystem().id(), f.revealed()))).toList()),
			Codec.INT.lenientOptionalFieldOf("scans_done", 0).forGetter(ShipDiagnostics::scansDone),
			Codec.LONG.lenientOptionalFieldOf("last_scan_tick", Long.MIN_VALUE).forGetter(ShipDiagnostics::lastScanTick)
	).apply(instance, ShipDiagnostics::fromRaw));

	private static ShipDiagnostics fromRaw(List<Optional<RawFault>> raw, int scansDone, long lastScanTick) {
		List<ShipLatentFault> faults = new ArrayList<>();
		for (Optional<RawFault> entry : raw) {
			if (entry.isEmpty()) {
				continue;
			}
			RawFault r = entry.get();
			ShipSubsystem.fromId(r.subsystem()).ifPresentOrElse(s -> {
				if (faults.stream().noneMatch(f -> f.subsystem() == s)) {
					faults.add(new ShipLatentFault(s, r.revealed()));
				}
			}, () -> LOGGER.warn("Ignoring ship fault with unknown subsystem '{}'", r.subsystem()));
		}
		return new ShipDiagnostics(faults, Math.max(0, scansDone), lastScanTick);
	}

	public boolean hasFault(ShipSubsystem subsystem) {
		return faults.stream().anyMatch(f -> f.subsystem() == subsystem);
	}

	public List<ShipLatentFault> revealed() {
		return faults.stream().filter(ShipLatentFault::revealed).toList();
	}

	public ShipDiagnostics withFaults(List<ShipLatentFault> next) {
		return new ShipDiagnostics(next, scansDone, lastScanTick);
	}
}
