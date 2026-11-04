package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import hu.kriwdi.intersolarsystem.space.MarsSites;
import hu.kriwdi.intersolarsystem.space.MoonSites;
import hu.kriwdi.intersolarsystem.space.OrbitStation;
import hu.kriwdi.intersolarsystem.space.Worlds;
import hu.kriwdi.intersolarsystem.spaceport.Mothership;

/**
 * Testing helper: {@code /iss goto <body>} puts the player (operators only) straight onto a body's landing site, with
 * no ship and no travel. Every world in {@link Worlds} shows up by itself, so new bodies need nothing here. The sites
 * are built first, like on a real first landing.
 */
public final class TestTeleport {
	private TestTeleport() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("iss")
						.then(Commands.literal("goto")
								.requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
								.then(Commands.argument("body", StringArgumentType.word())
										.suggests((ctx, builder) -> SharedSuggestionProvider.suggest(targets(), builder))
										.executes(TestTeleport::go)))));
	}

	/** Every place the command knows, in travel order. */
	public static List<String> targets() {
		List<String> out = new ArrayList<>(List.of("earth", "earth_orbit", "moon", "mothership", "mars"));
		for (Worlds.World w : Worlds.ALL) {
			out.add(w.body());
		}
		return out;
	}

	private static int go(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
		ServerPlayer player = ctx.getSource().getPlayerOrException();
		String body = StringArgumentType.getString(ctx, "body");
		MinecraftServer server = ctx.getSource().getServer();
		if (body.equals("mothership")) {
			Mothership.board(player);
			return 1;
		}
		ServerLevel level;
		BlockPos to;
		switch (body) {
			case "earth" -> {
				level = server.overworld();
				BlockPos spawn = server.getRespawnData().pos();
				to = MoonSites.surface(level, spawn.getX(), spawn.getZ());
			}
			case "earth_orbit" -> {
				level = server.getLevel(ShipDimensions.ORBIT);
				to = OrbitStation.CENTRE;
			}
			case "moon" -> {
				level = server.getLevel(ShipDimensions.MOON);
				if (level != null) {
					MoonSites.ensureBuilt(level);
				}
				to = level == null ? null : MoonSites.surface(level, MoonSites.LANDING_ZONE[0], MoonSites.LANDING_ZONE[1]);
			}
			case "mars" -> {
				level = server.getLevel(ShipDimensions.MARS);
				if (level != null) {
					MarsSites.ensureBuilt(level);
				}
				to = level == null ? null : MoonSites.surface(level, MarsSites.LANDING_ZONE[0], MarsSites.LANDING_ZONE[1]);
			}
			default -> {
				var world = Worlds.byBody(body);
				if (world.isEmpty()) {
					ctx.getSource().sendFailure(Component.literal("Unknown body: " + body + " (" + String.join(", ",
							targets()) + ")"));
					return 0;
				}
				level = server.getLevel(world.get().dimension());
				if (level != null) {
					Worlds.ensureSites(level, world.get());
				}
				to = level == null ? null : MoonSites.surface(level, world.get().landX(), world.get().landZ());
			}
		}
		if (level == null || to == null) {
			ctx.getSource().sendFailure(Component.literal("The world of " + body + " is not loaded"));
			return 0;
		}
		player.teleport(new TeleportTransition(level, Vec3.atBottomCenterOf(to), Vec3.ZERO, player.getYRot(), 0.0F,
				TeleportTransition.DO_NOTHING));
		BlockPos at = to;
		ctx.getSource().sendSuccess(() -> Component.literal("Teleported to " + body + " at " + at.toShortString()), true);
		return 1;
	}
}
