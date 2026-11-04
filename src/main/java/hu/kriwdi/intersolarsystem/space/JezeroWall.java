package hu.kriwdi.intersolarsystem.space;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.content.ModSounds;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;

/**
 * The wall under the Jezero delta, at the bottom of Ark-2's dig. It pulses (a slow beat, faster when someone is
 * close), breathes grey dust out of its seam, and the ground around it turns grey and dead, a little more with every
 * beat, out to a fixed reach: the Grey Bloom, at home. At night something knocks from the other side. Close up the
 * visor picks up fragments that are not quite words. With the rover's memory core in hand the seam answers.
 *
 * <p>Nothing here attacks: the dread is the point. The story is in Dr. Ferreira's terminal (log 27).
 */
public final class JezeroWall {
	/** The seam relative to the Ark-2 colony template's origin (see tools/worldgen/space_sites.py). */
	private static final int SEAM_DX = 0;
	private static final int SEAM_DZ = -50;
	private static final int REACH = 32;
	private static final int BLOOM_REACH = 14;
	private static final int WHISPERS = 5;
	private static BlockPos seam;
	private static boolean searched;

	private JezeroWall() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(JezeroWall::tick);
		net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
			seam = null;
			searched = false;
		});
	}

	/** The glowing seam (an end rod in the wall), found once the colony is placed. */
	static BlockPos seam(ServerLevel mars) {
		if (seam != null || searched) {
			return seam;
		}
		if (!SpaceSites.get(mars.getServer()).placedGroup("mars")) {
			return null;
		}
		searched = true;
		int x = MarsSites.ARK2.x() + SEAM_DX;
		int z = MarsSites.ARK2.z() + SEAM_DZ;
		mars.getChunk(x >> 4, z >> 4);
		for (int y = mars.getMaxY() - 1; y > mars.getMinY(); y--) {
			BlockPos p = new BlockPos(x, y, z);
			if (mars.getBlockState(p).is(Blocks.END_ROD)) {
				seam = p;
				break;
			}
		}
		return seam;
	}

	private static void tick(MinecraftServer server) {
		long now = server.overworld().getGameTime();
		if (now % 10 != 0) {
			return;
		}
		ServerLevel mars = server.getLevel(ShipDimensions.MARS);
		if (mars == null || mars.players().isEmpty()) {
			return;
		}
		BlockPos s = seam(mars);
		if (s == null) {
			return;
		}
		ServerPlayer nearest = null;
		double best = REACH * REACH;
		for (ServerPlayer p : mars.players()) {
			double d = p.distanceToSqr(s.getX() + 0.5, s.getY() + 0.5, s.getZ() + 0.5);
			if (d < best && !p.isSpectator()) {
				best = d;
				nearest = p;
			}
		}
		if (nearest == null) {
			return;
		}
		double dist = Math.sqrt(best);
		// The beat quickens as you come closer: every 8 s at the edge, every 2 s at the wall.
		long beat = dist < 6 ? 40 : dist < 14 ? 80 : 160;
		if (now % beat != 0) {
			return;
		}
		pulse(mars, s, nearest, dist, now);
	}

	private static void pulse(ServerLevel mars, BlockPos s, ServerPlayer near, double dist, long now) {
		RandomSource random = mars.getRandom();
		float loud = (float) Math.max(0.15, 1.0 - dist / REACH);
		mars.playSound(null, s, ModSounds.HEARTBEAT, SoundSource.AMBIENT, 1.6F * loud, 0.45F);
		// Grey dust out of the seam, drifting along the wall.
		mars.sendParticles(new DustParticleOptions(0x8C8A86, 1.6F), s.getX() + 0.5, s.getY() + 0.5, s.getZ() + 1.0,
				24, 0.25, 2.5, 0.4, 0.01);
		// The Bloom spreads: a few blocks of the delta go grey with every beat, nearest first.
		for (int i = 0; i < 3; i++) {
			int r = 2 + random.nextInt(BLOOM_REACH - 1);
			double a = random.nextDouble() * Math.PI * 2;
			int x = s.getX() + (int) Math.round(Math.cos(a) * r);
			int z = s.getZ() + (int) Math.round(Math.sin(a) * r);
			for (int y = s.getY() + 18; y > s.getY() - 8; y--) {
				BlockPos p = new BlockPos(x, y, z);
				BlockState st = mars.getBlockState(p);
				if (st.isAir()) {
					continue;
				}
				if (st.is(ModBlocks.MARS_REGOLITH) || st.is(ModBlocks.MARS_DUST) || st.is(ModBlocks.MARS_SANDSTONE)
						|| st.is(ModBlocks.MARS_ROCK)) {
					mars.setBlock(p, ModBlocks.ASH.defaultBlockState(), Block.UPDATE_ALL);
				}
				break;
			}
		}
		// At night something knocks on the other side, on the beat.
		if (!mars.isBrightOutside() && random.nextInt(3) == 0) {
			mars.playSound(null, s, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.AMBIENT, 0.9F * loud, 0.35F);
		}
		if (dist < 10 && random.nextInt(4) == 0) {
			near.sendOverlayMessage(Component.translatable("jezero.intersolarsystem.whisper." + random.nextInt(WHISPERS))
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
		}
		if (dist < 7 && (near.getMainHandItem().is(ModItems.ROVER_MEMORY_CORE)
				|| near.getOffhandItem().is(ModItems.ROVER_MEMORY_CORE))) {
			mars.sendParticles(new DustParticleOptions(0xE8F4FF, 1.0F), s.getX() + 0.5, s.getY() + 0.5, s.getZ() + 0.8,
					40, 0.1, 3.0, 0.1, 0.02);
			mars.playSound(null, s, ModSounds.EAR_RING, SoundSource.AMBIENT, 0.8F, 0.5F);
			near.sendOverlayMessage(Component.translatable("jezero.intersolarsystem.core_answers")
					.withStyle(ChatFormatting.AQUA));
		}
	}
}
