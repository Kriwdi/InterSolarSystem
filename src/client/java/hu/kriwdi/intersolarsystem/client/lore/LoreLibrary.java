package hu.kriwdi.intersolarsystem.client.lore;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;

/**
 * The texts of notes, tapes, manuals and terminals, read from assets/intersolarsystem/lore/&lt;language&gt;.json (English
 * when the game's language has no file or no entry).
 */
public final class LoreLibrary {
	/** A text: title, a subtitle line (author, date or code), pages; tapes have timed lines instead of pages. */
	public record Entry(String title, String subtitle, List<String> pages, List<Line> lines) {
	}

	/** A line of a tape, shown {@code at} ticks after the tape starts, by {@code who}. */
	public record Line(int at, String who, String text) {
	}

	private static final Map<String, JsonObject> FILES = new HashMap<>();

	private LoreLibrary() {
	}

	private static JsonObject file(String language) {
		return FILES.computeIfAbsent(language, lang -> {
			try (InputStream in = LoreLibrary.class.getResourceAsStream("/assets/intersolarsystem/lore/" + lang + ".json")) {
				if (in == null) {
					return new JsonObject();
				}
				return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
			} catch (Exception e) {
				return new JsonObject();
			}
		});
	}

	/** The entry of a kind ("notes", "tapes", "manuals", "terminals"), or null. */
	public static Entry get(String kind, String id) {
		String language = Minecraft.getInstance().options.languageCode;
		Entry entry = read(file(language), kind, id);
		return entry != null ? entry : read(file("en_us"), kind, id);
	}

	private static Entry read(JsonObject file, String kind, String id) {
		if (!file.has(kind) || !file.getAsJsonObject(kind).has(id)) {
			return null;
		}
		JsonObject o = file.getAsJsonObject(kind).getAsJsonObject(id);
		List<String> pages = new ArrayList<>();
		if (o.has("pages")) {
			for (JsonElement p : o.getAsJsonArray("pages")) {
				pages.add(p.getAsString());
			}
		}
		List<Line> lines = new ArrayList<>();
		if (o.has("lines")) {
			for (JsonElement l : o.getAsJsonArray("lines")) {
				JsonObject line = l.getAsJsonObject();
				lines.add(new Line(line.get("at").getAsInt(), line.has("who") ? line.get("who").getAsString() : "",
						line.get("text").getAsString()));
			}
		}
		return new Entry(o.has("title") ? o.get("title").getAsString() : id,
				o.has("subtitle") ? o.get("subtitle").getAsString() : "", pages, lines);
	}
}
