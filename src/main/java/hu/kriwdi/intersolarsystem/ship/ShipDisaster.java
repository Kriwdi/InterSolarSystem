package hu.kriwdi.intersolarsystem.ship;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;

/**
 * A takeoff that goes wrong: an explosion at the launch site, the takeoff is aborted and the ship stays where it is,
 * badly damaged. The explosion hurts creatures nearby but breaks no blocks, so a failed launch never wrecks the base.
 */
public final class ShipDisaster {
	public static final float EXPLOSION_POWER = 3.0F;

	private ShipDisaster() {
	}

	public static void failTakeoff(MinecraftServer server, ShipState state, long now) {
		// Critical state first (see ShipFlightController#transition): damage, faults, phase.
		state.applyWear(ShipFaultRules.disasterDamage());
		state.revealAllFaults();
		state.abortTakeoff(now);

		state.landingSite().ifPresent(site -> {
			ServerLevel level = ShipBoarding.levelOf(server, site);
			if (level != null) {
				level.explode(null, site.x() + 0.5, site.y() + 0.5, site.z() + 0.5, EXPLOSION_POWER,
						Level.ExplosionInteraction.NONE);
			}
		});
		ShipFlightController.broadcast(server, Component.translatable("message.intersolarsystem.ship.takeoff_failed"));
		ShipFlightController.playInterior(server, SoundEvents.GENERIC_EXPLODE.value());
	}
}
