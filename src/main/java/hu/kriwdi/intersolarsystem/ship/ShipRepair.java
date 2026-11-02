package hu.kriwdi.intersolarsystem.ship;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Server-side repair at the repair station. */
public final class ShipRepair {
	private ShipRepair() {
	}

	static Map<ShipSubsystem, Integer> conditions(ShipState state) {
		Map<ShipSubsystem, Integer> map = new EnumMap<>(ShipSubsystem.class);
		for (ShipSubsystem s : ShipSubsystem.values()) {
			map.put(s, state.condition(s));
		}
		return map;
	}

	/** Uses one kit. The kit is consumed (creative mode excepted) only if something was actually repaired. */
	public static void useKit(MinecraftServer server, ServerPlayer player, ItemStack kit) {
		ShipState state = ShipStateAccess.get(server);
		if (state.isReadOnly()) {
			player.sendSystemMessage(Component.translatable("command.intersolarsystem.ship.read_only"));
			return;
		}
		ShipRepairRules.Choice choice = ShipRepairRules.choose(state.hazards().incidents(),
				state.diagnostics().faults(), conditions(state));
		switch (choice.kind()) {
			case FAULT -> {
				ShipSubsystem subsystem = choice.subsystem().orElseThrow();
				if (state.removeFault(subsystem)) {
					kit.consume(1, player);
					player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.repair_fault",
							Component.translatable(ShipCommands.subsystemKey(subsystem))));
				}
			}
			case INCIDENT -> {
				ShipIncident incident = choice.incident().orElseThrow();
				if (state.resolveIncident(incident)) {
					kit.consume(1, player);
					player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.repair_incident",
							Component.translatable(ShipCommands.incidentKey(incident.type())),
							Component.translatable(ShipCommands.subsystemKey(incident.subsystem()))));
				}
			}
			case SUBSYSTEM -> {
				ShipSubsystem subsystem = choice.subsystem().orElseThrow();
				int before = state.condition(subsystem);
				state.setCondition(subsystem, before + ShipRepairRules.REPAIR_AMOUNT);
				kit.consume(1, player);
				player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.repair_subsystem",
						Component.translatable(ShipCommands.subsystemKey(subsystem)),
						state.condition(subsystem) - before));
			}
			case NOTHING -> player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.repair_nothing"));
		}
	}

	/** Using the station without a kit: says what a kit would fix first (an active incident before wear). */
	public static void showNeedsKit(MinecraftServer server, ServerPlayer player) {
		ShipState state = ShipStateAccess.get(server);
		ShipRepairRules.Choice choice = ShipRepairRules.choose(state.hazards().incidents(),
				state.diagnostics().faults(), conditions(state));
		switch (choice.kind()) {
			case FAULT -> player.sendSystemMessage(Component.translatable(
					"message.intersolarsystem.ship.station_needs_kit_fault",
					Component.translatable(ShipCommands.subsystemKey(choice.subsystem().orElseThrow()))));
			case INCIDENT -> {
				ShipIncident incident = choice.incident().orElseThrow();
				player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.station_needs_kit_incident",
						Component.translatable(ShipCommands.incidentKey(incident.type())),
						Component.translatable(ShipCommands.subsystemKey(incident.subsystem()))));
			}
			case SUBSYSTEM -> player.sendSystemMessage(Component.translatable(
					"message.intersolarsystem.ship.station_needs_kit",
					Component.translatable(ShipCommands.subsystemKey(choice.subsystem().orElseThrow()))));
			case NOTHING -> player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.repair_nothing"));
		}
	}

	/** The station is part of the ship's systems and does nothing anywhere else. */
	public static void showOutsideShip(ServerPlayer player) {
		player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.station_outside"));
	}
}
