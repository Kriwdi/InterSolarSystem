package hu.kriwdi.intersolarsystem.spaceport;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

/**
 * A crane blueprint: the blocks of one mothership section, relative to the crane console (in its north-facing frame),
 * in building order (bottom up, keel first). Read from /intersolarsystem/blueprints/&lt;section&gt;.txt.gz on the classpath (gzipped text),
 * one block per line: {@code x y z block[prop=value,...]}. Written by tools/worldgen/karman.py.
 */
public final class Blueprint {
	public record Entry(int x, int y, int z, BlockState state) {
	}

	private static final Map<String, List<Entry>> CACHE = new HashMap<>();

	private Blueprint() {
	}

	public static List<Entry> of(String name) {
		return CACHE.computeIfAbsent(name, Blueprint::load);
	}

	private static List<Entry> load(String name) {
		List<Entry> out = new ArrayList<>();
		try (InputStream raw = Blueprint.class.getResourceAsStream("/intersolarsystem/blueprints/" + name + ".txt.gz")) {
			if (raw == null) {
				return out;
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(new java.util.zip.GZIPInputStream(raw),
					StandardCharsets.UTF_8));
			Map<String, BlockState> states = new HashMap<>();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank() || line.startsWith("#")) {
					continue;
				}
				String[] p = line.split(" ", 4);
				BlockState state = states.computeIfAbsent(p[3], Blueprint::parse);
				out.add(new Entry(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]), state));
			}
		} catch (Exception e) {
			hu.kriwdi.intersolarsystem.InterSolarSystem.LOGGER.error("Bad blueprint {}", name, e);
		}
		return out;
	}

	static BlockState parse(String text) {
		String id = text;
		String props = "";
		int b = text.indexOf('[');
		if (b >= 0) {
			id = text.substring(0, b);
			props = text.substring(b + 1, text.length() - 1);
		}
		Block block = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
		BlockState state = block.defaultBlockState();
		if (!props.isEmpty()) {
			for (String kv : props.split(",")) {
				String[] pair = kv.split("=", 2);
				Property<?> property = block.getStateDefinition().getProperty(pair[0]);
				if (property != null) {
					state = with(state, property, pair[1]);
				}
			}
		}
		return block == Blocks.AIR && !id.endsWith(":air") ? Blocks.AIR.defaultBlockState() : state;
	}

	private static <T extends Comparable<T>> BlockState with(BlockState state, Property<T> property, String value) {
		Optional<T> v = property.getValue(value);
		return v.isPresent() ? state.setValue(property, v.get()) : state;
	}

	/** World position of a blueprint entry for a console at {@code console} turned by {@code rotation}. */
	public static BlockPos place(BlockPos console, Rotation rotation, Entry e) {
		int x = e.x();
		int z = e.z();
		int rx = switch (rotation) {
			case CLOCKWISE_90 -> -z;
			case CLOCKWISE_180 -> -x;
			case COUNTERCLOCKWISE_90 -> z;
			default -> x;
		};
		int rz = switch (rotation) {
			case CLOCKWISE_90 -> x;
			case CLOCKWISE_180 -> -z;
			case COUNTERCLOCKWISE_90 -> -x;
			default -> z;
		};
		return console.offset(rx, e.y(), rz);
	}
}
