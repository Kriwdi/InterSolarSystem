package hu.kriwdi.intersolarsystem.space;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Places the hand-made sites of space (the wrecks around the ISS, the Moon missions, the Ark-1 colony) from their
 * templates (tools/worldgen/space_sites.py). Each template is placed by its builder origin: for a Moon site that is
 * the regolith layer under its centre.
 */
public final class SiteTemplates {
	private static Map<String, int[]> origins;

	private SiteTemplates() {
	}

	private static synchronized Map<String, int[]> origins() {
		if (origins == null) {
			origins = new HashMap<>();
			try (InputStream in = SiteTemplates.class.getResourceAsStream("/intersolarsystem/space_sites.json")) {
				if (in != null) {
					JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
					for (Map.Entry<String, JsonElement> e : root.entrySet()) {
						JsonArray a = e.getValue().getAsJsonArray();
						origins.put(e.getKey(), new int[] {a.get(0).getAsInt(), a.get(1).getAsInt(), a.get(2).getAsInt()});
					}
				}
			} catch (IOException e) {
				throw new java.io.UncheckedIOException(e);
			}
		}
		return origins;
	}

	/** Places {@code name} (e.g. "moon/luna9") with its builder origin at {@code origin}. False if it is missing. */
	public static boolean place(ServerLevel level, String name, BlockPos origin) {
		Optional<StructureTemplate> template = level.getServer().getStructureTemplateManager().get(ModIds.id(name));
		if (template.isEmpty()) {
			return false;
		}
		int[] min = origins().getOrDefault(name, new int[3]);
		BlockPos corner = origin.offset(min[0], min[1], min[2]);
		var size = template.get().getSize();
		for (int x = corner.getX() >> 4; x <= (corner.getX() + size.getX()) >> 4; x++) {
			for (int z = corner.getZ() >> 4; z <= (corner.getZ() + size.getZ()) >> 4; z++) {
				level.getChunk(x, z);
			}
		}
		template.get().placeInWorld(level, corner, corner, new StructurePlaceSettings(), level.getRandom(),
				Block.UPDATE_CLIENTS);
		return true;
	}

	/** Whether the template exists (smoke test). */
	public static boolean exists(ServerLevel level, String name) {
		return level.getServer().getStructureTemplateManager().get(ModIds.id(name)).isPresent();
	}
}
