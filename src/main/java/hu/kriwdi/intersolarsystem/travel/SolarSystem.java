package hu.kriwdi.intersolarsystem.travel;

import java.io.IOException;
import java.io.InputStream;
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
	/** Bodies the console never lists until something reveals them ("hidden": true in the catalogue). */
	private static java.util.Set<String> hidden = java.util.Set.of();
	/** Hidden bodies found so far (the server's {@link Discoveries}; on a client, what the console was told). */
	private static volatile java.util.Set<String> discovered = java.util.Set.of();

	private SolarSystem() {
	}

	public static synchronized List<CelestialBody> bodies() {
		if (bodies == null) {
			try (InputStream in = SolarSystem.class.getResourceAsStream(RESOURCE)) {
				if (in == null) {
					throw new IllegalStateException("Missing " + RESOURCE);
				}
				String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
				bodies = parse(new java.io.StringReader(text));
				java.util.Set<String> h = new java.util.HashSet<>();
				for (JsonElement e : JsonParser.parseString(text).getAsJsonObject().getAsJsonArray("bodies")) {
					JsonObject o = e.getAsJsonObject();
					if (o.has("hidden") && o.get("hidden").getAsBoolean()) {
						h.add(o.get("id").getAsString());
					}
				}
				hidden = java.util.Set.copyOf(h);
			} catch (IOException e) {
				throw new IllegalStateException("Cannot read " + RESOURCE, e);
			}
		}
		return bodies;
	}

	/** Whether the console shows a body: not hidden, or hidden and already found. */
	public static boolean visible(CelestialBody body) {
		bodies();
		return !hidden.contains(body.id()) || discovered.contains(body.id());
	}

	public static boolean hidden(String id) {
		bodies();
		return hidden.contains(id);
	}

	public static java.util.Set<String> discovered() {
		return discovered;
	}

	public static void setDiscovered(java.util.Collection<String> ids) {
		discovered = java.util.Set.copyOf(ids);
	}

	/** From the console's packet: comma-separated ids. */
	public static void setDiscovered(String csv) {
		setDiscovered(csv.isEmpty() ? java.util.List.of() : java.util.List.of(csv.split(",")));
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
