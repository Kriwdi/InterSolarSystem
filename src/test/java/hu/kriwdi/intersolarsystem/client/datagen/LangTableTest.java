package hu.kriwdi.intersolarsystem.client.datagen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.jupiter.api.Test;

class LangTableTest {
	private static final Pattern PLACEHOLDER = Pattern.compile("%(\\d+\\$)?s");

	private static Map<String, String> committed(ModLangTable.Language language) throws IOException {
		Path dir = Path.of(System.getProperty("intersolarsystem.projectDir", "."));
		Path file = dir.resolve("src/main/generated/assets/intersolarsystem/lang/" + language.code() + ".json");
		String json = Files.readString(file, StandardCharsets.UTF_8);
		return new TreeMap<>(new Gson().<Map<String, String>>fromJson(json, new TypeToken<Map<String, String>>() {
		}.getType()));
	}

	@Test
	void committedLangFilesMatchTheTable() throws IOException {
		for (ModLangTable.Language language : ModLangTable.Language.values()) {
			Map<String, String> expected = new TreeMap<>();
			ModLangTable.rows().forEach(row -> expected.put(row.key(), row.text(language)));
			assertEquals(expected, committed(language),
					language.code() + ".json is out of date: run `python3 tools/sync_lang.py` or `./gradlew runDatagen`");
		}
	}

	private static int placeholders(String text) {
		Matcher m = PLACEHOLDER.matcher(text.replace("%%", ""));
		int count = 0;
		while (m.find()) {
			count++;
		}
		return count;
	}

	@Test
	void everyLanguageHasTheSamePlaceholders() {
		for (ModLangTable.Row row : ModLangTable.rows()) {
			int english = placeholders(row.en());
			assertEquals(english, placeholders(row.de()), "German placeholders differ: " + row.key());
			assertEquals(english, placeholders(row.hu()), "Hungarian placeholders differ: " + row.key());
			assertEquals(row.en().contains("%%"), row.hu().contains("%%"), "Percent sign differs: " + row.key());
		}
	}
}
