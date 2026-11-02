package hu.kriwdi.intersolarsystem.core;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

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
			LOGGER.info("[SMOKETEST] server started, stopping");
			server.halt(false);
		});
	}
}
