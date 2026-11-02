package hu.kriwdi.intersolarsystem.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;

/**
 * Checks the client assets that only a running client would load: every blockstate, item definition and model points at
 * files that exist, element coordinates and rotations are what the game accepts, and textures have valid sizes.
 * A broken model in the game shows as the purple-black "missing" cube, which no server test can catch.
 */
class AssetsTest {
	private static final Path ASSETS = Path.of(System.getProperty("intersolarsystem.projectDir", "."))
			.resolve("src/main/resources/assets/intersolarsystem");
	private static final String NS = "intersolarsystem:";
	private static final Set<String> VANILLA_PARENTS = Set.of("minecraft:block/block", "minecraft:block/cube_all",
			"minecraft:block/cube", "minecraft:item/generated", "minecraft:item/handheld");
	private static final Set<String> ELEMENT_KEYS = Set.of("from", "to", "faces", "rotation", "shade", "__comment");
	private static final Set<String> FACE_KEYS = Set.of("texture", "uv", "cullface", "rotation", "tintindex");
	private static final Set<String> DIRECTIONS = Set.of("north", "south", "east", "west", "up", "down");
	private static final Set<Double> ANGLES = Set.of(-45.0, -22.5, 0.0, 22.5, 45.0);

	private static JsonObject read(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
	}

	private static List<Path> files(String dir, String ext) throws IOException {
		try (Stream<Path> s = Files.walk(ASSETS.resolve(dir))) {
			return s.filter(p -> p.toString().endsWith(ext)).toList();
		}
	}

	private static Path modelPath(String id) {
		assertTrue(id.startsWith(NS), "model outside the mod: " + id);
		return ASSETS.resolve("models/" + id.substring(NS.length()) + ".json");
	}

	@Test
	void blockstatesPointAtExistingModels() throws IOException {
		for (Path p : files("blockstates", ".json")) {
			JsonObject variants = read(p).getAsJsonObject("variants");
			for (String key : variants.keySet()) {
				JsonElement v = variants.get(key);
				List<JsonObject> options = new ArrayList<>();
				if (v.isJsonArray()) {
					v.getAsJsonArray().forEach(e -> options.add(e.getAsJsonObject()));
				} else {
					options.add(v.getAsJsonObject());
				}
				for (JsonObject o : options) {
					assertTrue(Files.exists(modelPath(o.get("model").getAsString())), p + ": " + o);
					if (o.has("y")) {
						assertTrue(Set.of(0, 90, 180, 270).contains(o.get("y").getAsInt()), p + " y rotation");
					}
				}
			}
		}
	}

	@Test
	void itemDefinitionsPointAtExistingModels() throws IOException {
		for (Path p : files("items", ".json")) {
			JsonObject model = read(p).getAsJsonObject("model");
			assertEquals("minecraft:model", model.get("type").getAsString(), p.toString());
			assertTrue(Files.exists(modelPath(model.get("model").getAsString())), p.toString());
		}
	}

	@Test
	void modelsAreValid() throws IOException {
		for (Path p : files("models", ".json")) {
			JsonObject m = read(p);
			if (m.has("parent")) {
				String parent = m.get("parent").getAsString();
				assertTrue(VANILLA_PARENTS.contains(parent) || Files.exists(modelPath(parent)), p + " parent " + parent);
			}
			JsonObject textures = m.has("textures") ? m.getAsJsonObject("textures") : new JsonObject();
			for (String key : textures.keySet()) {
				String ref = textures.get(key).getAsString();
				if (!ref.startsWith("#")) {
					assertTrue(ref.startsWith(NS), p + ": texture outside the mod " + ref);
					Path png = ASSETS.resolve("textures/" + ref.substring(NS.length()) + ".png");
					assertTrue(Files.exists(png), p + ": missing texture " + png);
				}
			}
			if (!m.has("elements")) {
				continue;
			}
			assertTrue(textures.has("particle"), p + ": a model with elements needs a particle texture");
			for (JsonElement e : m.getAsJsonArray("elements")) {
				checkElement(p, e.getAsJsonObject(), textures);
			}
		}
	}

	private static void checkElement(Path p, JsonObject el, JsonObject textures) {
		for (String key : el.keySet()) {
			assertTrue(ELEMENT_KEYS.contains(key), p + ": unknown element key " + key);
		}
		JsonArray from = el.getAsJsonArray("from");
		JsonArray to = el.getAsJsonArray("to");
		for (int i = 0; i < 3; i++) {
			double f = from.get(i).getAsDouble();
			double t = to.get(i).getAsDouble();
			assertTrue(f >= -16 && t <= 32 && f <= t, p + ": element out of range " + from + " " + to);
		}
		if (el.has("rotation")) {
			JsonObject r = el.getAsJsonObject("rotation");
			assertTrue(ANGLES.contains(r.get("angle").getAsDouble()), p + ": rotation angle " + r);
			assertTrue(Set.of("x", "y", "z").contains(r.get("axis").getAsString()), p + ": rotation axis " + r);
			assertEquals(3, r.getAsJsonArray("origin").size(), p + ": rotation origin");
		}
		JsonObject faces = el.getAsJsonObject("faces");
		assertTrue(faces.size() > 0, p + ": element without faces");
		for (String dir : faces.keySet()) {
			assertTrue(DIRECTIONS.contains(dir), p + ": face " + dir);
			JsonObject face = faces.getAsJsonObject(dir);
			for (String key : face.keySet()) {
				assertTrue(FACE_KEYS.contains(key), p + ": unknown face key " + key);
			}
			String tex = face.get("texture").getAsString();
			assertTrue(tex.startsWith("#") && textures.has(tex.substring(1)), p + ": unresolved face texture " + tex);
			if (face.has("uv")) {
				for (JsonElement u : face.getAsJsonArray("uv")) {
					double v = u.getAsDouble();
					assertTrue(v >= 0 && v <= 16, p + ": uv out of range");
				}
			}
			if (face.has("cullface")) {
				assertTrue(DIRECTIONS.contains(face.get("cullface").getAsString()), p + ": cullface");
			}
			if (face.has("rotation")) {
				assertTrue(Set.of(0, 90, 180, 270).contains(face.get("rotation").getAsInt()), p + ": face rotation");
			}
		}
	}

	@Test
	void texturesHaveValidSizes() throws IOException {
		for (Path p : files("textures", ".png")) {
			BufferedImage img = ImageIO.read(p.toFile());
			if (img == null) {
				fail("unreadable png " + p);
			}
			boolean animated = Files.exists(Path.of(p + ".mcmeta"));
			if (animated) {
				assertEquals(0, img.getHeight() % img.getWidth(), p + ": animation frames must be square");
			} else {
				assertEquals(img.getWidth(), img.getHeight(), p + ": texture must be square (or animated)");
			}
			assertEquals(0, img.getWidth() % 16, p + ": width must be a multiple of 16");
		}
	}
}
