package hu.kriwdi.intersolarsystem.core;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import hu.kriwdi.intersolarsystem.content.ModLoot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CI helper: with {@code -Dintersolarsystem.smoketest=true} the dedicated server stops right after it has fully started
 * (data packs, recipes, loot tables and dimensions loaded). The CI job then checks the log for errors from this mod.
 */
public final class SmokeTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(ModIds.MOD_ID);

	private SmokeTest() {
	}

	public static void register() {
		if (!Boolean.getBoolean("intersolarsystem.smoketest")) {
			return;
		}
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			int injected = ModLoot.INJECTED.get();
			if (injected < ModLoot.SALVAGE_CHESTS.size()) {
				LOGGER.error("[SMOKETEST-FAIL] salvage pool reached {} of {} chest loot tables", injected,
						ModLoot.SALVAGE_CHESTS.size());
			} else {
				LOGGER.info("[SMOKETEST] salvage pool in {} chest loot tables", injected);
			}
			LOGGER.info("[SMOKETEST] server started, stopping");
			server.halt(false);
		});
	}
}
