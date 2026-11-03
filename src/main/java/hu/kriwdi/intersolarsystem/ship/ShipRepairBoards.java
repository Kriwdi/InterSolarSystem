package hu.kriwdi.intersolarsystem.ship;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import hu.kriwdi.intersolarsystem.content.ModItems;

/**
 * Server side of the repair minigames. Using a repair kit on an incident that needs one opens the board; the incident is
 * fixed and the kit used up only when the server accepts the answer. A failed repair damages the subsystem a little.
 */
public final class ShipRepairBoards {
	/** A board stays valid this long (also bounded by the incident's own deadline). */
	public static final int BOARD_TIMEOUT_TICKS = 1200;
	public static final int FAILED_REPAIR_DAMAGE = 5;
	/** Allowance for network delay between the server and the client's tick counter. */
	private static final int CLOCK_SLACK_TICKS = 40;

	/** {@code tool}: what must still be in hand when the answer comes (a repair kit or the fault's own tool). */
	private record Pending(RepairMinigame kind, long seed, int size, ShipIncident incident, long openedTick,
			long expiresTick, net.minecraft.world.item.Item tool) {
	}

	private static final Map<UUID, Pending> PENDING = new HashMap<>();

	/** A board opened by something other than a ship incident (the spaceport's calibrations, later machines). */
	private record External(RepairMinigame kind, long seed, int size, long openedTick, java.util.function.Consumer<Boolean> done) {
	}

	private static final Map<UUID, External> EXTERNAL = new HashMap<>();

	/** Opens a board whose result goes to {@code done} (true: solved). */
	public static void openExternal(MinecraftServer server, ServerPlayer player, RepairMinigame kind, int size,
			java.util.function.Consumer<Boolean> done) {
		long now = server.overworld().getGameTime();
		long seed = server.overworld().getRandom().nextLong();
		PENDING.remove(player.getUUID());
		EXTERNAL.put(player.getUUID(), new External(kind, seed, size, now, done));
		ServerPlayNetworking.send(player, new RepairBoardPayloads.Open(kind, seed, size));
	}

	private ShipRepairBoards() {
	}

	public static void open(MinecraftServer server, ServerPlayer player, RepairMinigame kind, ShipIncident incident,
			boolean multitool) {
		open(server, player, kind, incident, multitool, ModItems.REPAIR_KIT);
	}

	public static void open(MinecraftServer server, ServerPlayer player, RepairMinigame kind, ShipIncident incident,
			boolean multitool, net.minecraft.world.item.Item tool) {
		ShipState state = ShipStateAccess.get(server);
		long now = server.overworld().getGameTime();
		int size = kind.size(state.condition(incident.subsystem()), multitool);
		long seed = server.overworld().getRandom().nextLong();
		PENDING.put(player.getUUID(), new Pending(kind, seed, size, incident, now,
				Math.min(now + BOARD_TIMEOUT_TICKS, incident.deadlineTick()), tool));
		ServerPlayNetworking.send(player, new RepairBoardPayloads.Open(kind, seed, size));
	}

	public static void handleResult(MinecraftServer server, ServerPlayer player, RepairBoardPayloads.Result result) {
		External external = EXTERNAL.get(player.getUUID());
		if (external != null && external.seed() == result.seed() && external.kind() == result.kind()) {
			EXTERNAL.remove(player.getUUID());
			long now = server.overworld().getGameTime();
			int maxTick = (int) Math.min(Integer.MAX_VALUE, now - external.openedTick() + CLOCK_SLACK_TICKS);
			boolean solved = now - external.openedTick() <= BOARD_TIMEOUT_TICKS
					&& external.kind().isSolvedBy(external.seed(), external.size(), result.answer(), maxTick);
			external.done().accept(solved);
			return;
		}
		Pending pending = PENDING.remove(player.getUUID());
		if (pending == null || pending.seed() != result.seed() || pending.kind() != result.kind()) {
			return;
		}
		ShipState state = ShipStateAccess.get(server);
		long now = server.overworld().getGameTime();
		if (now > pending.expiresTick() || !player.level().dimension().equals(ShipDimensions.INTERIOR)
				|| !state.hazards().incidents().contains(pending.incident())) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.board_expired"));
			return;
		}
		ItemStack kit = player.getMainHandItem();
		if (kit.getItem() != pending.tool()) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.board_no_kit"));
			return;
		}
		ShipSubsystem subsystem = pending.incident().subsystem();
		int maxTick = (int) Math.min(Integer.MAX_VALUE, now - pending.openedTick() + CLOCK_SLACK_TICKS);
		if (pending.kind().isSolvedBy(pending.seed(), pending.size(), result.answer(), maxTick)) {
			if (state.resolveIncident(pending.incident())) {
				if (kit.isDamageableItem()) {
					kit.hurtAndBreak(1, player, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
				} else {
					kit.consume(1, player);
				}
				ShipRepair.useMultitool(player);
				player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.repair_incident",
						Component.translatable(ShipCommands.incidentKey(pending.incident().type())),
						Component.translatable(ShipCommands.subsystemKey(subsystem))));
				ShipFlightController.playInterior(server, SoundEvents.BEACON_POWER_SELECT);
			}
		} else {
			state.setCondition(subsystem, state.condition(subsystem) - FAILED_REPAIR_DAMAGE);
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.board_failed."
					+ pending.kind().name().toLowerCase(java.util.Locale.ROOT),
					Component.translatable(ShipCommands.subsystemKey(subsystem)), FAILED_REPAIR_DAMAGE));
			ShipFlightController.playInterior(server, SoundEvents.FIRECHARGE_USE);
		}
	}

	public static void reset() {
		PENDING.clear();
		EXTERNAL.clear();
	}
}
