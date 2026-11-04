package hu.kriwdi.intersolarsystem.space;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.content.ModSounds;
import hu.kriwdi.intersolarsystem.core.ModIds;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;

/**
 * Spacewalks in Earth orbit (the server side; the floating itself is client-side, see ZeroGravity):
 * <ul>
 * <li><b>Safety tether</b>: used on a block it clips on; the line ({@link #BASE_LENGTH} blocks, plus
 * {@link #PER_SPOOL} for every tether spool carried, up to {@link #MAX_SPOOLS}) stops the drift and reels the walker
 * back. Sneak-use unclips. A broken anchor snaps the line.</li>
 * <li><b>Manoeuvring unit</b>: the client reports how long the jets fired; the propellant is the unit's wear.</li>
 * <li><b>Debris streams</b>: every few minutes the station's orbit crosses a stream of fragments. A warning first;
 * then for twenty seconds anyone outside who is not behind something on the stream's side may be hit (damage, a torn
 * suit).</li>
 * </ul>
 */
public final class OrbitalEva {
	public static final int BASE_LENGTH = 16;
	public static final int PER_SPOOL = 16;
	public static final int MAX_SPOOLS = 3;
	public static final ResourceKey<DamageType> DEBRIS = ResourceKey.create(Registries.DAMAGE_TYPE,
			ModIds.id("orbital_debris"));
	private static final int WARNING_TICKS = 300;
	private static final int PASS_TICKS = 400;
	private static final DustParticleOptions LINE = new DustParticleOptions(0xF2C744, 0.7F);

	private record Clip(BlockPos anchor, int length) {
	}

	private static final Map<UUID, Clip> CLIPS = new HashMap<>();
	private static long nextStream = -1L;
	private static Vec3 streamDir = new Vec3(1, 0, 0);

	private OrbitalEva() {
	}

	public static void register() {
		UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
			ItemStack held = player.getItemInHand(hand);
			if (!held.is(ModItems.SAFETY_TETHER) || !Worlds.weightless(level.dimension())) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer sp && !player.isShiftKeyDown()) {
				clip(sp, hit.getBlockPos());
			}
			return InteractionResult.SUCCESS;
		});
		UseItemCallback.EVENT.register((player, level, hand) -> {
			ItemStack held = player.getItemInHand(hand);
			if (!held.is(ModItems.SAFETY_TETHER)) {
				return InteractionResult.PASS;
			}
			if (player instanceof ServerPlayer sp) {
				if (CLIPS.containsKey(sp.getUUID()) && sp.isShiftKeyDown()) {
					unclip(sp, "eva.intersolarsystem.tether_unclipped");
				} else if (!CLIPS.containsKey(sp.getUUID())) {
					sp.sendOverlayMessage(Component.translatable("eva.intersolarsystem.tether_hint"));
				}
			}
			return InteractionResult.PASS;
		});
		ServerPlayNetworking.registerGlobalReceiver(EvaPayloads.Thrust.TYPE, (payload, context) -> {
			ServerPlayer player = context.player();
			context.server().execute(() -> burn(player, Math.max(0, Math.min(40, payload.ticks()))));
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> CLIPS.remove(handler.getPlayer().getUUID()));
		ServerTickEvents.END_SERVER_TICK.register(OrbitalEva::tick);
	}

	/** The line a player could pay out: the base length and the spools in the inventory. */
	public static int lineLength(Player player) {
		int spools = 0;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack s = player.getInventory().getItem(i);
			if (s.is(ModItems.TETHER_SPOOL)) {
				spools += s.getCount();
			}
		}
		return BASE_LENGTH + PER_SPOOL * Math.min(MAX_SPOOLS, spools);
	}

	public static boolean tethered(ServerPlayer player) {
		return CLIPS.containsKey(player.getUUID());
	}

	private static void clip(ServerPlayer player, BlockPos anchor) {
		int length = lineLength(player);
		CLIPS.put(player.getUUID(), new Clip(anchor.immutable(), length));
		player.level().playSound(null, anchor, SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0F, 1.3F);
		player.sendOverlayMessage(Component.translatable("eva.intersolarsystem.tether_clipped", length)
				.withStyle(ChatFormatting.YELLOW));
		ServerPlayNetworking.send(player, new EvaPayloads.Tether(true, anchor, length));
	}

	private static void unclip(ServerPlayer player, String message) {
		Clip c = CLIPS.remove(player.getUUID());
		if (c != null) {
			player.level().playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 0.8F, 1.2F);
			player.sendOverlayMessage(Component.translatable(message).withStyle(ChatFormatting.GOLD));
			ServerPlayNetworking.send(player, new EvaPayloads.Tether(false, c.anchor(), 0));
		}
	}

	/** Uses up the manoeuvring unit's propellant (its wear) for the jets' burn. */
	private static void burn(ServerPlayer player, int ticks) {
		if (ticks == 0 || !Worlds.weightless(player.level().dimension())) {
			return;
		}
		ItemStack mmu = findMmu(player);
		if (mmu.isEmpty()) {
			return;
		}
		int damage = Math.min(mmu.getMaxDamage(), mmu.getDamageValue() + ticks);
		mmu.setDamageValue(damage);
		if (damage >= mmu.getMaxDamage()) {
			player.sendOverlayMessage(Component.translatable("eva.intersolarsystem.mmu_empty").withStyle(ChatFormatting.RED));
		}
	}

	/** The first manoeuvring unit with propellant left (empty stack if none). */
	public static ItemStack findMmu(Player player) {
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack s = player.getInventory().getItem(i);
			if (s.is(ModItems.MMU_PACK) && s.getDamageValue() < s.getMaxDamage()) {
				return s;
			}
		}
		return ItemStack.EMPTY;
	}

	private static void tick(MinecraftServer server) {
		ServerLevel orbit = server.getLevel(ShipDimensions.ORBIT);
		if (orbit == null) {
			CLIPS.clear();
			return;
		}
		long now = orbit.getGameTime();
		for (Iterator<Map.Entry<UUID, Clip>> it = CLIPS.entrySet().iterator(); it.hasNext();) {
			Map.Entry<UUID, Clip> e = it.next();
			ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
			Clip clip = e.getValue();
			if (player == null) {
				it.remove();
				continue;
			}
			if (player.level() != orbit) {
				it.remove();
				ServerPlayNetworking.send(player, new EvaPayloads.Tether(false, clip.anchor(), 0));
				continue;
			}
			if (orbit.getBlockState(clip.anchor()).isAir()) {
				it.remove();
				player.sendOverlayMessage(Component.translatable("eva.intersolarsystem.tether_snapped")
						.withStyle(ChatFormatting.RED));
				orbit.playSound(null, player.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.0F, 0.7F);
				ServerPlayNetworking.send(player, new EvaPayloads.Tether(false, clip.anchor(), 0));
				continue;
			}
			Vec3 anchor = Vec3.atCenterOf(clip.anchor());
			Vec3 waist = player.position().add(0, 0.9, 0);
			double d = anchor.distanceTo(waist);
			if (d > clip.length()) {
				Vec3 dir = anchor.subtract(waist).normalize();
				Vec3 v = player.getDeltaMovement();
				double out = -v.dot(dir);
				if (out > 0) {
					v = v.add(dir.scale(out));
				}
				v = v.add(dir.scale(Math.min(0.2, (d - clip.length()) * 0.06)));
				if (d > clip.length() + 10) {
					Vec3 back = anchor.add(waist.subtract(anchor).normalize().scale(clip.length()));
					player.teleportTo(back.x, back.y - 0.9, back.z);
				}
				player.setDeltaMovement(v);
				player.needsSync = true;
				player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
			}
			if (now % 3 == 0) {
				int steps = Math.max(2, (int) (d / 1.2));
				for (int i = 1; i < steps; i++) {
					Vec3 p = anchor.add(waist.subtract(anchor).scale(i / (double) steps));
					orbit.sendParticles(LINE, p.x, p.y, p.z, 1, 0, 0, 0, 0);
				}
			}
		}
		debris(orbit, now);
	}

	/** The debris streams: scheduled, announced, then passing for a while. */
	private static void debris(ServerLevel orbit, long now) {
		if (orbit.players().isEmpty()) {
			return;
		}
		if (nextStream < 0 || nextStream + PASS_TICKS < now - 100) {
			nextStream = now + 6000 + orbit.getRandom().nextInt(6000);
		}
		long warn = nextStream - WARNING_TICKS;
		if (now == warn) {
			double a = orbit.getRandom().nextDouble() * Math.PI * 2;
			streamDir = new Vec3(Math.cos(a), (orbit.getRandom().nextDouble() - 0.5) * 0.3, Math.sin(a)).normalize();
			for (ServerPlayer p : orbit.players()) {
				p.sendSystemMessage(Component.translatable("eva.intersolarsystem.debris_warning", WARNING_TICKS / 20)
						.withStyle(ChatFormatting.RED));
				orbit.playSound(null, p.blockPosition(), ModSounds.OXYGEN_ALARM, SoundSource.PLAYERS, 0.8F, 0.7F);
				ServerPlayNetworking.send(p, new EvaPayloads.Debris((byte) 1, WARNING_TICKS, (float) streamDir.x,
						(float) streamDir.y, (float) streamDir.z));
			}
		}
		if (now == nextStream) {
			for (ServerPlayer p : orbit.players()) {
				ServerPlayNetworking.send(p, new EvaPayloads.Debris((byte) 2, PASS_TICKS, (float) streamDir.x,
						(float) streamDir.y, (float) streamDir.z));
			}
		}
		if (now >= nextStream && now < nextStream + PASS_TICKS) {
			for (ServerPlayer p : orbit.players()) {
				for (int i = 0; i < 6; i++) {
					Vec3 from = p.position().add((orbit.getRandom().nextDouble() - 0.5) * 30,
							(orbit.getRandom().nextDouble() - 0.5) * 18, (orbit.getRandom().nextDouble() - 0.5) * 30)
							.subtract(streamDir.scale(18));
					orbit.sendParticles(i % 3 == 0 ? ParticleTypes.SMALL_FLAME : ParticleTypes.CRIT, from.x, from.y,
							from.z, 0, streamDir.x, streamDir.y, streamDir.z, 1.8);
				}
				if (p.isCreative() || p.isSpectator() || sheltered(orbit, p)) {
					continue;
				}
				if (orbit.getRandom().nextInt(45) == 0) {
					p.hurtServer(orbit, VacuumBreathing.source(orbit, DEBRIS), 4.0F);
					orbit.playSound(null, p.blockPosition(), SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.0F, 1.6F);
					if (orbit.getRandom().nextFloat() < 0.5F) {
						VacuumBreathing.puncture(p);
					}
				}
			}
		}
		if (now == nextStream + PASS_TICKS) {
			for (ServerPlayer p : orbit.players()) {
				p.sendOverlayMessage(Component.translatable("eva.intersolarsystem.debris_clear").withStyle(ChatFormatting.GREEN));
				ServerPlayNetworking.send(p, new EvaPayloads.Debris((byte) 0, 0, 0, 0, 0));
			}
			nextStream = now + 6000 + orbit.getRandom().nextInt(6000);
		}
	}

	/** Something solid between the player and where the stream comes from. */
	private static boolean sheltered(ServerLevel level, ServerPlayer player) {
		Vec3 eye = player.getEyePosition();
		Vec3 far = eye.subtract(streamDir.scale(48));
		return level.clip(new ClipContext(eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player))
				.getType() != HitResult.Type.MISS;
	}

	/** Test hook: clip a player on. */
	public static void testClip(ServerPlayer player, BlockPos anchor) {
		clip(player, anchor);
	}

	/** Test hook: start a stream now (warning skipped). */
	public static void startStreamNow(ServerLevel orbit) {
		nextStream = orbit.getGameTime() + 1;
	}
}
