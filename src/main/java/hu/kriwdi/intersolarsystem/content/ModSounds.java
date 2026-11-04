package hu.kriwdi.intersolarsystem.content;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import hu.kriwdi.intersolarsystem.core.ModIds;

/** The mod's own sounds (synthesised by tools/audio/make_sounds.py, listed in assets/.../sounds.json). */
public final class ModSounds {
	public static final SoundEvent SUIT_BREATH = register("suit_breath");
	public static final SoundEvent OXYGEN_ALARM = register("oxygen_alarm");
	public static final SoundEvent SUFFOCATE = register("suffocate");
	public static final SoundEvent HELMET_BLOCKED = register("helmet_blocked");
	public static final SoundEvent TAPE_START = register("tape_start");
	public static final SoundEvent TAPE_STATIC = register("tape_static");
	public static final SoundEvent TAPE_VOICE = register("tape_voice");
	public static final SoundEvent SUIT_LEAK = register("suit_leak");
	public static final SoundEvent HEARTBEAT = register("heartbeat");
	public static final SoundEvent EAR_RING = register("ear_ring");
	/**
	 * Biome ambience (loops and one-shot additions, see tools/audio/make_ambience.py); the biomes refer to them by
	 * name. The list comes from intersolarsystem/ambience.txt, written by the same script.
	 */
	public static final java.util.List<SoundEvent> AMBIENCE = registerAmbience();

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = ModIds.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	private static java.util.List<SoundEvent> registerAmbience() {
		java.util.List<SoundEvent> out = new java.util.ArrayList<>();
		try (java.io.InputStream in = ModSounds.class.getResourceAsStream("/intersolarsystem/ambience.txt")) {
			if (in != null) {
				for (String line : new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).split("\n")) {
					if (!line.isBlank()) {
						out.add(register(line.trim()));
					}
				}
			}
		} catch (java.io.IOException e) {
			throw new java.io.UncheckedIOException(e);
		}
		return java.util.List.copyOf(out);
	}

	public static void initialize() {
	}
}
