package hu.kriwdi.intersolarsystem.ship;

import java.util.List;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Dynamic;

import hu.kriwdi.intersolarsystem.core.ModIds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Upgrades raw saved ship data to the current format before it is decoded into typed values.
 * Each migration turns data of version {@code from} into data of version {@code from + 1}.
 */
public final class ShipStateMigrations {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModIds.MOD_ID);
	private static final String VERSION_KEY = "format_version";

	public record Migration<T>(int from, UnaryOperator<Dynamic<T>> step) {
	}

	private ShipStateMigrations() {
	}

	/**
	 * v1 -> v2 added optional fields (landing site, interior flag) and v2 -> v3 added optional flight fields
	 * (phase, destination) and v3 -> v4 the optional hazards object, so no existing data has to change.
	 */
	public static <T> List<Migration<T>> defaultSteps() {
		return List.of(new Migration<>(1, data -> data), new Migration<>(2, data -> data),
				new Migration<>(3, data -> data));
	}

	public static <T> Dynamic<T> migrate(Dynamic<T> data) {
		return migrate(data, defaultSteps(), ShipState.CURRENT_FORMAT_VERSION);
	}

	public static <T> Dynamic<T> migrate(Dynamic<T> data, List<Migration<T>> steps, int targetVersion) {
		int version = Math.max(1, data.get(VERSION_KEY).asInt(1));
		Dynamic<T> current = data;
		while (version < targetVersion) {
			final int from = version;
			Migration<T> migration = steps.stream().filter(m -> m.from() == from).findFirst().orElse(null);
			if (migration == null) {
				LOGGER.warn("No ship save migration from format version {}; stopping at that version", version);
				return current.set(VERSION_KEY, current.createInt(version));
			}
			current = migration.step().apply(current);
			version++;
			current = current.set(VERSION_KEY, current.createInt(version));
		}
		return current;
	}
}
