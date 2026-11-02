package hu.kriwdi.intersolarsystem.core;

import net.minecraft.resources.Identifier;

public final class ModIds {
	public static final String MOD_ID = "intersolarsystem";

	private ModIds() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
