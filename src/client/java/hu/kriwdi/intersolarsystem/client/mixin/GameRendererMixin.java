package hu.kriwdi.intersolarsystem.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import hu.kriwdi.intersolarsystem.client.sky.ShipView;

/** Shakes the camera inside the ship during takeoff, flight and landing (added where vanilla tilts it when hurt). */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	@Inject(method = "bobHurt", at = @At("TAIL"))
	private void intersolarsystem$shipShake(final CameraRenderState cameraState, final PoseStack poseStack,
			final CallbackInfo ci) {
		float shake = ShipView.shake(0.0F);
		if (shake <= 0.0F) {
			return;
		}
		float t = ShipView.time(0.0F) + (System.nanoTime() % 1_000_000_000L) / 50_000_000.0F;
		poseStack.rotateDegrees(Axis.ZP, shake * (Mth.sin(t * 2.3F) + 0.5F * Mth.sin(t * 5.1F)));
		poseStack.rotateDegrees(Axis.XP, shake * 0.6F * Mth.sin(t * 3.7F + 1.0F));
	}
}
