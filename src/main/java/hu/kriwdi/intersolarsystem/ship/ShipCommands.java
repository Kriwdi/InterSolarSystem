package hu.kriwdi.intersolarsystem.ship;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Debug commands for the ship state: {@code /iss ship info} and {@code /iss ship set <subsystem> <value>}. */
public final class ShipCommands {
	private ShipCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("iss")
						.then(Commands.literal("difficulty")
								.executes(ShipCommands::difficulty)
								.then(Commands.argument("level", StringArgumentType.word())
										.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
										.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
												java.util.Arrays.stream(hu.kriwdi.intersolarsystem.core.Difficulty.values())
														.map(hu.kriwdi.intersolarsystem.core.Difficulty::id), builder))
										.executes(ShipCommands::setDifficulty)))
						// The navigation console as a command (works the same, from inside the ship).
						.then(Commands.literal("nav")
								.then(Commands.argument("destination", StringArgumentType.word())
										.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
												hu.kriwdi.intersolarsystem.travel.SolarSystem.bodies().stream()
														.map(hu.kriwdi.intersolarsystem.travel.CelestialBody::id), builder))
										.executes(ShipCommands::nav)))
						.then(Commands.literal("ship")
								.then(Commands.literal("info").executes(ShipCommands::info))
								.then(Commands.literal("fly")
										.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
										.then(Commands.argument("x", IntegerArgumentType.integer())
												.then(Commands.argument("z", IntegerArgumentType.integer())
														.executes(ShipCommands::fly))))
								.then(Commands.literal("meteors")
										.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
										.executes(ShipCommands::meteors))
								.then(Commands.literal("engine")
										.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
										.then(Commands.argument("tier", IntegerArgumentType.integer(1,
														hu.kriwdi.intersolarsystem.travel.TravelMath.ENGINE_MULTIPLIERS.length))
												.executes(ShipCommands::engine)))
								.then(Commands.literal("set")
										.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
										.then(Commands.argument("subsystem", StringArgumentType.word())
												.suggests((ctx, builder) ->
														SharedSuggestionProvider.suggest(ShipSubsystem.ids(), builder))
												.then(Commands.argument("value",
																IntegerArgumentType.integer(ShipState.MIN_CONDITION, ShipState.MAX_CONDITION))
														.executes(ShipCommands::set)))))));
	}

	private static int nav(CommandContext<CommandSourceStack> ctx) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		return ShipNavigation.launch(ctx.getSource().getServer(), player,
				StringArgumentType.getString(ctx, "destination")) ? 1 : 0;
	}

	private static int difficulty(CommandContext<CommandSourceStack> ctx) {
		var crew = hu.kriwdi.intersolarsystem.core.ModSettings.crew(ctx.getSource().getServer());
		ctx.getSource().sendSuccess(() -> Component.translatable("command.intersolarsystem.difficulty.info",
				Component.translatable("difficulty.intersolarsystem." + crew.difficulty().id()), crew.size(),
				crew.maxParallel()), false);
		return 1;
	}

	private static int setDifficulty(CommandContext<CommandSourceStack> ctx) {
		String id = StringArgumentType.getString(ctx, "level");
		var level = hu.kriwdi.intersolarsystem.core.Difficulty.fromId(id);
		if (level.isEmpty()) {
			ctx.getSource().sendFailure(Component.translatable("command.intersolarsystem.difficulty.unknown", id));
			return 0;
		}
		hu.kriwdi.intersolarsystem.core.ModSettings.get(ctx.getSource().getServer()).setDifficulty(level.get());
		ctx.getSource().sendSuccess(() -> Component.translatable("command.intersolarsystem.difficulty.set",
				Component.translatable("difficulty.intersolarsystem." + id)), true);
		return 1;
	}

	private static int meteors(CommandContext<CommandSourceStack> ctx) {
		MinecraftServer server = ctx.getSource().getServer();
		boolean started = ShipTravelEvents.startMeteorShower(server, ShipStateAccess.get(server),
				server.overworld().getGameTime());
		if (!started) {
			ctx.getSource().sendFailure(Component.translatable("command.intersolarsystem.ship.meteors_failed"));
		}
		return started ? 1 : 0;
	}

	private static int info(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		ShipState state = ShipStateAccess.get(source.getServer());
		source.sendSuccess(() -> Component.translatable("command.intersolarsystem.ship.info",
				Component.translatable(hullKey(state.hullClass()))), false);
		for (ShipSubsystem subsystem : ShipSubsystem.values()) {
			source.sendSuccess(() -> Component.translatable("command.intersolarsystem.ship.subsystem_line",
					Component.translatable(subsystemKey(subsystem)), state.condition(subsystem)), false);
		}
		source.sendSuccess(() -> Component.translatable("command.intersolarsystem.ship.engine_line",
				state.travel().engineTier(),
				hu.kriwdi.intersolarsystem.travel.TravelMath.multiplier(state.travel().engineTier())), false);
		source.sendSuccess(() -> Component.translatable("command.intersolarsystem.ship.phase",
				Component.translatable(phaseKey(state.phase()))), false);
		if (state.phase() != ShipFlightPhase.LANDED && !"earth".equals(state.travel().target())) {
			source.sendSuccess(() -> Component.translatable("command.intersolarsystem.ship.journey",
					Component.translatable("body.intersolarsystem." + state.travel().target())), false);
		}
		if (state.phase() != ShipFlightPhase.LANDED) {
			long now = source.getServer().overworld().getGameTime();
			source.sendSuccess(() -> Component.translatable("command.intersolarsystem.ship.phase_remaining",
					ShipFlightTimeline.formatClock(
							ShipFlightTimeline.remainingTicks(state, now))), false);
		}
		long nowTick = source.getServer().overworld().getGameTime();
		for (ShipIncident incident : state.hazards().incidents()) {
			source.sendSuccess(() -> Component.translatable("command.intersolarsystem.ship.incident_line",
					Component.translatable(incidentKey(incident.type())),
					Component.translatable(subsystemKey(incident.subsystem())),
					ShipFlightTimeline.formatClock(incident.deadlineTick() - nowTick)), false);
		}
		state.destination().ifPresent(d -> source.sendSuccess(() ->
				Component.translatable("command.intersolarsystem.ship.destination", d.x(), d.z()), false));
		return 1;
	}

	private static int fly(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		MinecraftServer server = source.getServer();
		if (!(source.getEntity() instanceof ServerPlayer player)
				|| !player.level().dimension().equals(ShipDimensions.INTERIOR)) {
			source.sendFailure(Component.translatable("command.intersolarsystem.ship.fly_not_inside"));
			return 0;
		}
		ShipState state = ShipStateAccess.get(server);
		if (state.isReadOnly()) {
			source.sendFailure(Component.translatable("command.intersolarsystem.ship.read_only"));
			return 0;
		}
		int x = IntegerArgumentType.getInteger(ctx, "x");
		int z = IntegerArgumentType.getInteger(ctx, "z");
		if (!server.overworld().getWorldBorder().isWithinBounds((double) x, (double) z)) {
			source.sendFailure(Component.translatable("command.intersolarsystem.ship.fly_out_of_border"));
			return 0;
		}
		if (state.phase() != ShipFlightPhase.LANDED) {
			source.sendFailure(Component.translatable("command.intersolarsystem.ship.fly_not_landed"));
			return 0;
		}
		if (!ShipFlightRules.canTakeOff(state.condition(ShipSubsystem.ENGINE), state.condition(ShipSubsystem.LANDING_LAUNCH))) {
			source.sendFailure(Component.translatable("command.intersolarsystem.ship.fly_too_damaged"));
			return 0;
		}
		if (!state.beginTakeoff(x, z, server.overworld().getGameTime(),
				server.overworld().getRandom().nextLong())) {
			source.sendFailure(Component.translatable("command.intersolarsystem.ship.fly_not_landed"));
			return 0;
		}
		source.sendSuccess(() -> Component.translatable("command.intersolarsystem.ship.fly_started", x, z), true);
		return 1;
	}

	private static int engine(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		ShipState state = ShipStateAccess.get(source.getServer());
		if (state.isReadOnly()) {
			source.sendFailure(Component.translatable("command.intersolarsystem.ship.read_only"));
			return 0;
		}
		int tier = IntegerArgumentType.getInteger(ctx, "tier");
		state.setEngineTier(tier);
		source.sendSuccess(() -> Component.translatable("command.intersolarsystem.ship.engine", tier,
				hu.kriwdi.intersolarsystem.travel.TravelMath.multiplier(tier)), true);
		return 1;
	}

	private static int set(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		String id = StringArgumentType.getString(ctx, "subsystem");
		int value = IntegerArgumentType.getInteger(ctx, "value");
		ShipState current = ShipStateAccess.get(source.getServer());
		if (current.isReadOnly()) {
			source.sendFailure(Component.translatable("command.intersolarsystem.ship.read_only"));
			return 0;
		}
		return ShipSubsystem.fromId(id).map(subsystem -> {
			current.setCondition(subsystem, value);
			source.sendSuccess(() -> Component.translatable("command.intersolarsystem.ship.set",
					Component.translatable(subsystemKey(subsystem)), current.condition(subsystem)), true);
			return 1;
		}).orElseGet(() -> {
			source.sendFailure(Component.translatable("command.intersolarsystem.ship.unknown_subsystem", id));
			return 0;
		});
	}

	public static String hullKey(ShipHullClass hullClass) {
		return "ship.intersolarsystem.hull." + hullClass.id();
	}

	public static String incidentKey(ShipIncidentType type) {
		return "ship.intersolarsystem.incident." + type.id();
	}

	public static String phaseKey(ShipFlightPhase phase) {
		return "ship.intersolarsystem.phase." + phase.id();
	}

	public static String subsystemKey(ShipSubsystem subsystem) {
		return "ship.intersolarsystem.subsystem." + subsystem.id();
	}
}
