package hu.kriwdi.intersolarsystem.travel;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** The destination catalogue, loaded once from the mod's data file. */
public final class SolarSystem {
	public static final String RESOURCE = "/data/intersolarsystem/solar_system.json";

	private static List<CelestialBody> bodies;

	private SolarSystem() {
	}

	public static synchronized List<CelestialBody> bodies() {
		if (bodies == null) {
			try (InputStream in = SolarSystem.class.getResourceAsStream(RESOURCE)) {
				if (in == null) {
					throw new IllegalStateException("Missing " + RESOURCE);
				}
				bodies = parse(new InputStreamReader(in, StandardCharsets.UTF_8));
			} catch (IOException e) {
				throw new IllegalStateException("Cannot read " + RESOURCE, e);
			}
		}
		return bodies;
	}

	public static Optional<CelestialBody> byId(String id) {
		return bodies().stream().filter(b -> b.id().equals(id)).findFirst();
	}

	public static List<CelestialBody> parse(Reader reader) {
		JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
		List<CelestialBody> list = new ArrayList<>();
		for (JsonElement e : root.getAsJsonArray("bodies")) {
			JsonObject o = e.getAsJsonObject();
			list.add(new CelestialBody(o.get("id").getAsString(), CelestialBody.Kind.of(o.get("kind").getAsString()),
					optional(o, "parent"), o.get("transfer_days").getAsDouble(), o.get("synodic_days").getAsDouble(),
					CelestialBody.Status.of(o.get("status").getAsString()), optional(o, "assist"),
					o.has("min_tier") ? o.get("min_tier").getAsInt() : 1,
					o.has("via_mothership") && o.get("via_mothership").getAsBoolean()));
		}
		return List.copyOf(list);
	}

	private static Optional<String> optional(JsonObject o, String key) {
		return o.has(key) ? Optional.of(o.get(key).getAsString()) : Optional.empty();
	}
}
