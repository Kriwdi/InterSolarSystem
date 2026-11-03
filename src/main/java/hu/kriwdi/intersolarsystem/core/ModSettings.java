package hu.kriwdi.intersolarsystem.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import hu.kriwdi.intersolarsystem.ship.ShipDimensions;

/** World settings of the mod (saved with the world): the difficulty, set with {@code /iss difficulty}. */
public class ModSettings extends SavedData {
	public static final Codec<ModSettings> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.optionalFieldOf("difficulty", Difficulty.NORMAL.id()).forGetter(s -> s.difficulty.id()))
			.apply(i, id -> {
				ModSettings s = new ModSettings();
				s.difficulty = Difficulty.fromId(id).orElse(Difficulty.NORMAL);
				return s;
			}));
	public static final SavedDataType<ModSettings> TYPE = new SavedDataType<>(ModIds.id("settings"), ModSettings::new,
			CODEC, null);

	private Difficulty difficulty = Difficulty.NORMAL;

	public static ModSettings get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public Difficulty difficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty difficulty) {
		this.difficulty = difficulty;
		setDirty();
	}

	/** The difficulty with the crew now aboard the ship (at least one). */
	public static Difficulty.Crew crew(MinecraftServer server) {
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		int size = interior == null ? 1 : (int) interior.players().stream().filter(p -> !p.isSpectator()).count();
		return new Difficulty.Crew(get(server).difficulty(), Math.max(1, size));
	}
}
