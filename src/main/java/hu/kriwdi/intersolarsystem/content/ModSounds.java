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

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = ModIds.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void initialize() {
	}
}
