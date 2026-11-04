package hu.kriwdi.intersolarsystem.travel;

import java.util.LinkedHashSet;
import java.util.Set;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * The hidden bodies this world has found (shared by every player). A hidden body is in the catalogue but the console
 * does not list it until something reveals it: the moon nobody names shows up once its orbit has been read off the
 * terminal at Triton's outpost.
 */
public final class Discoveries extends SavedData {
	public static final Codec<Discoveries> CODEC = CompoundTag.CODEC.xmap(Discoveries::load, Discoveries::save);
	public static final SavedDataType<Discoveries> TYPE = new SavedDataType<>(ModIds.id("discoveries"), Discoveries::new,
			CODEC, null);

	private final Set<String> found = new LinkedHashSet<>();

	public static Discoveries get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public static void register() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> SolarSystem.setDiscovered(get(server).found));
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SolarSystem.setDiscovered(java.util.List.of()));
	}

	/** Reveals a hidden body to everyone (once); returns whether it was new. */
	public static boolean discover(MinecraftServer server, String id) {
		Discoveries d = get(server);
		if (!SolarSystem.hidden(id) || !d.found.add(id)) {
			return false;
		}
		d.setDirty();
		SolarSystem.setDiscovered(d.found);
		server.getPlayerList().broadcastSystemMessage(Component.translatable("nav.intersolarsystem.discovered",
				Component.translatable("body.intersolarsystem." + id)).withStyle(ChatFormatting.LIGHT_PURPLE), false);
		return true;
	}

	private CompoundTag save() {
		CompoundTag t = new CompoundTag();
		ListTag list = new ListTag();
		for (String id : found) {
			list.add(StringTag.valueOf(id));
		}
		t.put("found", list);
		return t;
	}

	private static Discoveries load(CompoundTag t) {
		Discoveries d = new Discoveries();
		for (var e : t.getListOrEmpty("found")) {
			e.asString().ifPresent(d.found::add);
		}
		return d;
	}
}
