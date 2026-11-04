package hu.kriwdi.intersolarsystem.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import hu.kriwdi.intersolarsystem.content.ModSounds;
import hu.kriwdi.intersolarsystem.ship.ShipDimensions;
import hu.kriwdi.intersolarsystem.space.EvaPayloads;
import hu.kriwdi.intersolarsystem.space.OrbitalEva;

/**
 * Floating in Earth orbit (the local player's movement is client-side, so this is where weightlessness lives):
 * <ul>
 * <li><b>Handholds</b>: within reach of a block you pull yourself along it with the movement keys (jump up, sneak
 * down, forward follows your view), and stop when you let go.</li>
 * <li><b>Drift</b>: away from everything nothing slows you: you keep going the way you pushed off.</li>
 * <li><b>Manoeuvring unit</b>: with an MMU in the inventory the keys fire its jets (it uses propellant).</li>
 * <li><b>Tether</b>: a clipped line stops you at its length and reels you back.</li>
 * </ul>
 */
public final class ZeroGravity {
	private static Vec3 last = Vec3.ZERO;
	private static boolean free;
	private static int burnt;
	private static boolean thrusting;
	private static EvaPayloads.Tether tether = new EvaPayloads.Tether(false, BlockPos.ZERO, 0);
	private static EvaPayloads.Debris debris = new EvaPayloads.Debris((byte) 0, 0, 0, 0, 0);
	private static long debrisAt;

	private ZeroGravity() {
	}

	public static void accept(EvaPayloads.Tether payload) {
		tether = payload;
	}

	public static void accept(EvaPayloads.Debris payload, long time) {
		debris = payload;
		debrisAt = time;
	}

	public static EvaPayloads.Tether tether() {
		return tether;
	}

	/** 0 clear, 1 warning, 2 passing; and the ticks left of that phase. */
	public static int debrisPhase(long time) {
		return time - debrisAt <= debris.ticksLeft() ? debris.phase() : 0;
	}

	public static int debrisLeft(long time) {
		return (int) Math.max(0, debris.ticksLeft() - (time - debrisAt));
	}

	/** Away from every handhold and not tethered. */
	public static boolean drifting() {
		return free && !tether.on();
	}

	public static boolean thrusting() {
		return thrusting;
	}

	public static void clear() {
		tether = new EvaPayloads.Tether(false, BlockPos.ZERO, 0);
		debris = new EvaPayloads.Debris((byte) 0, 0, 0, 0, 0);
		last = Vec3.ZERO;
		free = false;
	}

	public static void tick(Minecraft mc) {
		LocalPlayer p = mc.player;
		thrusting = false;
		if (p == null || mc.level == null) {
			return;
		}
		if (!mc.level.dimension().equals(ShipDimensions.ORBIT) || p.isSpectator() || p.getAbilities().flying
				|| p.isPassenger()) {
			last = p.getDeltaMovement();
			free = false;
			return;
		}
		Vec3 v = p.getDeltaMovement();
		Vec3 input = input(mc, p);
		boolean near = nearBlock(mc, p);
		ItemStack mmu = OrbitalEva.findMmu(p);
		free = !near;
		if (near) {
			// Handholds: pull yourself along; hold still when the keys are released.
			v = input.lengthSqr() > 0 ? v.lerp(input.scale(0.16), 0.35) : v.scale(0.7);
		} else {
			// Nothing to slow you: keep the last velocity, except where something stopped you.
			v = new Vec3(Math.abs(v.x) < 1.0E-7 ? 0 : last.x, Math.abs(v.y) < 1.0E-7 ? 0 : last.y,
					Math.abs(v.z) < 1.0E-7 ? 0 : last.z);
			if (!mmu.isEmpty() && input.lengthSqr() > 0) {
				v = v.add(input.scale(0.014));
				if (v.length() > 0.55) {
					v = v.normalize().scale(0.55);
				}
				thrusting = true;
				burnt++;
				Vec3 back = p.position().add(0, 1.0, 0).subtract(input.scale(0.6));
				mc.level.addParticle(ParticleTypes.CLOUD, back.x, back.y, back.z, -input.x * 0.2, -input.y * 0.2,
						-input.z * 0.2);
				if (p.tickCount % 4 == 0) {
					mc.level.playLocalSound(p.getX(), p.getY(), p.getZ(), ModSounds.SUIT_LEAK, SoundSource.PLAYERS, 0.25F,
							1.8F, false);
				}
			}
		}
		if (tether.on()) {
			Vec3 anchor = Vec3.atCenterOf(tether.anchor());
			Vec3 waist = p.position().add(0, 0.9, 0);
			double d = anchor.distanceTo(waist);
			if (d > tether.length()) {
				Vec3 dir = anchor.subtract(waist).normalize();
				double out = -v.dot(dir);
				if (out > 0) {
					v = v.add(dir.scale(out));
				}
				v = v.add(dir.scale(Math.min(0.2, (d - tether.length()) * 0.06)));
			}
		}
		p.setDeltaMovement(v);
		last = v;
		if (burnt >= 10 || burnt > 0 && !thrusting) {
			ClientPlayNetworking.send(new EvaPayloads.Thrust(burnt));
			burnt = 0;
		}
	}

	/** The movement keys as a direction: forward along the view, strafing sideways, jump up, sneak down. */
	private static Vec3 input(Minecraft mc, LocalPlayer p) {
		Vec3 look = p.getLookAngle();
		Vec3 right = new Vec3(-look.z, 0, look.x);
		if (right.lengthSqr() < 1.0E-6) {
			right = new Vec3(1, 0, 0);
		}
		right = right.normalize();
		Vec3 v = Vec3.ZERO;
		if (mc.options.keyUp.isDown()) {
			v = v.add(look);
		}
		if (mc.options.keyDown.isDown()) {
			v = v.subtract(look);
		}
		if (mc.options.keyRight.isDown()) {
			v = v.add(right);
		}
		if (mc.options.keyLeft.isDown()) {
			v = v.subtract(right);
		}
		if (mc.options.keyJump.isDown()) {
			v = v.add(0, 1, 0);
		}
		if (mc.options.keyShift.isDown()) {
			v = v.add(0, -1, 0);
		}
		return v.lengthSqr() > 0 ? v.normalize() : Vec3.ZERO;
	}

	/** A solid block within arm's reach of the body. */
	private static boolean nearBlock(Minecraft mc, LocalPlayer p) {
		AABB box = p.getBoundingBox().inflate(0.7);
		for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ),
				BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
			if (!mc.level.getBlockState(pos).getCollisionShape(mc.level, pos).isEmpty()) {
				return true;
			}
		}
		return false;
	}
}
