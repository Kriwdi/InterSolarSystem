package hu.kriwdi.intersolarsystem.space;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import hu.kriwdi.intersolarsystem.content.UrbanBlocks;
import hu.kriwdi.intersolarsystem.lore.LoreOpenPayload;

/**
 * The Ark-1 wake console (log 26): the codes are accepted, the hall wakes up, and pod after pod reports what the
 * player half expects. Nobody wakes. Then the watch officer's last words. The first time it plays out in the cryo
 * hall (frost venting from the pods, the alarm, the lines one by one); afterwards the console only shows its log.
 * It gives nothing: the colony waited, and it is over.
 */
public final class ArkWake {
	private static final String FLAG = "ark1_woken";
	private static final int[] SHOWN_PODS = {1, 2, 3, 4, 17, 58, 121, 240};

	private record Run(ServerLevel level, BlockPos console, ServerPlayer player, List<BlockPos> pods, long start) {
	}

	private static final List<Run> RUNS = new ArrayList<>();

	private ArkWake() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(ArkWake::tick);
	}

	/** Called by the console. Returns false when the sequence already ran in this world (show the log instead). */
	public static boolean start(ServerLevel level, BlockPos console, ServerPlayer player) {
		SpaceSites sites = SpaceSites.get(level.getServer());
		if (sites.flagged(FLAG) || RUNS.stream().anyMatch(r -> r.console().equals(console))) {
			return false;
		}
		List<BlockPos> pods = new ArrayList<>();
		for (BlockPos p : BlockPos.betweenClosed(console.offset(-10, -2, -10), console.offset(10, 3, 10))) {
			if (level.getBlockState(p).is(UrbanBlocks.CRYO_POD)) {
				pods.add(p.immutable());
			}
		}
		RUNS.add(new Run(level, console, player, pods, level.getGameTime()));
		return true;
	}

	private static void tick(MinecraftServer server) {
		for (Iterator<Run> it = RUNS.iterator(); it.hasNext();) {
			Run run = it.next();
			long t = run.level().getGameTime() - run.start();
			if (run.player().isRemoved() || run.player().level() != run.level()) {
				it.remove();
				continue;
			}
			if (step(run, t)) {
				SpaceSites.get(server).flag(FLAG);
				it.remove();
			}
		}
	}

	/** One tick of the sequence; true when it is over. */
	private static boolean step(Run run, long t) {
		ServerLevel level = run.level();
		ServerPlayer player = run.player();
		if (t == 0) {
			level.playSound(null, run.console(), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 0.6F);
			say(player, Component.translatable("wake.intersolarsystem.ark1.accepted").withStyle(ChatFormatting.AQUA));
		}
		if (t == 40) {
			say(player, Component.translatable("wake.intersolarsystem.ark1.thawing"));
		}
		if (t >= 40 && t < 160 && t % 6 == 0) {
			// Frost venting from the pods, round the hall.
			for (BlockPos p : run.pods()) {
				if (level.getRandom().nextInt(3) == 0) {
					level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5, p.getY() + 1.2, p.getZ() + 0.5, 3, 0.2, 0.3,
							0.2, 0.01);
				}
			}
			if (t % 30 == 0) {
				level.playSound(null, run.console(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7F, 0.5F);
			}
		}
		// The pods report one by one, then all at once.
		for (int i = 0; i < SHOWN_PODS.length; i++) {
			if (t == 170 + i * (i < 4 ? 30L : 16L)) {
				say(player, Component.translatable("wake.intersolarsystem.ark1.pod",
						String.format("%03d", SHOWN_PODS[i])).withStyle(ChatFormatting.RED));
				level.playSound(null, run.console(), hu.kriwdi.intersolarsystem.content.ModSounds.OXYGEN_ALARM, SoundSource.BLOCKS, 0.6F, 0.5F);
			}
		}
		long all = 170 + 4 * 30 + 4 * 16 + 30;
		if (t == all) {
			say(player, Component.translatable("wake.intersolarsystem.ark1.all").withStyle(ChatFormatting.DARK_RED));
			level.playSound(null, run.console(), SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 0.5F);
		}
		if (t == all + 60) {
			say(player, Component.translatable("wake.intersolarsystem.ark1.message").withStyle(ChatFormatting.GRAY,
					ChatFormatting.ITALIC));
		}
		if (t == all + 120) {
			ServerPlayNetworking.send(player, new LoreOpenPayload("terminal", "terminal_26"));
			return true;
		}
		return false;
	}

	private static void say(ServerPlayer player, Component line) {
		player.sendSystemMessage(line);
	}
}
