package hu.kriwdi.intersolarsystem.client.mixin;

import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.MoonPhase;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import hu.kriwdi.intersolarsystem.client.sky.EarthInSky;
import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Adds the Earth to the sky of the orbit and Moon dimensions, using vanilla's own celestial drawing (the same pipeline,
 * atlas and quad layout as the sun and moon). In orbit the Earth fills the sky below; on the Moon it hangs at a fixed
 * point (the Moon is tidally locked) and vanilla's moon is not drawn. A black disc is drawn first so the stars do not
 * shine through the night side.
 */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Shadow
	@Final
	private TextureAtlas celestialsAtlas;

	@Shadow
	@Final
	private RenderSystem.AutoStorageIndexBuffer quadIndices;

	@Unique
	private GpuBuffer intersolarsystem$earthPhases;

	@Unique
	private GpuBuffer intersolarsystem$disc;

	@Shadow
	private Matrix4f applyCelestialBodyTransform(final PoseStack poseStack, final float height, final float scale) {
		throw new AssertionError();
	}

	@Shadow
	private void drawCelestialBody(final Supplier<String> label, final RenderPass renderPass,
			final GpuBufferSlice dynamicTransforms, final GpuBuffer indexBuffer, final GpuBuffer vertexBuffer,
			final int baseVertex) {
		throw new AssertionError();
	}

	@Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
	private void intersolarsystem$noMoonOnTheMoon(final RenderPass renderPass, final MoonPhase moonPhase,
			final float rainBrightness, final PoseStack poseStack, final CallbackInfo ci) {
		if (EarthInSky.current() == EarthInSky.View.FROM_MOON) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSunMoonAndStars", at = @At("TAIL"))
	private void intersolarsystem$renderEarth(final RenderPass renderPass, final PoseStack poseStack,
			final float sunAngle, final float moonAngle, final float starAngle, final MoonPhase moonPhase,
			final float rainBrightness, final float starBrightness, final CallbackInfo ci) {
		EarthInSky.View view = EarthInSky.current();
		if (view == EarthInSky.View.NONE) {
			return;
		}
		if (intersolarsystem$earthPhases == null) {
			intersolarsystem$earthPhases = intersolarsystem$buildEarthPhases(celestialsAtlas);
			intersolarsystem$disc = intersolarsystem$buildDisc();
		}
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.YP, -90.0F);
		poseStack.rotateDegrees(Axis.XP, view.pitch());
		poseStack.rotateDegrees(Axis.ZP, view.roll());
		float scale = view.scale();
		// Occluder: a black disc slightly smaller than the sprite (the atmosphere rim stays visible).
		Matrix4f discMatrix = applyCelestialBodyTransform(poseStack, EarthInSky.DISTANCE + 0.5F, scale * 0.94F);
		GpuBufferSlice discTransforms = RenderSystem.getDynamicUniforms().writeTransform(discMatrix,
				new Vector4f(0.0F, 0.0F, 0.0F, 1.0F));
		renderPass.pushDebugGroup(() -> "Earth occluder");
		renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.SKY));
		RenderSystem.bindDefaultUniforms(renderPass);
		renderPass.setUniform("DynamicTransforms", discTransforms);
		renderPass.setVertexBuffer(0, intersolarsystem$disc.slice());
		renderPass.draw(EarthInSky.DISC_VERTICES, 1, 0, 0);
		renderPass.popDebugGroup();

		int phase = EarthInSky.phase(view, sunAngle);
		Matrix4f matrix = applyCelestialBodyTransform(poseStack, EarthInSky.DISTANCE, scale);
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(matrix,
				new Vector4f(1.0F, 1.0F, 1.0F, 1.0F));
		drawCelestialBody(() -> "Earth", renderPass, transforms, quadIndices.getBuffer(6), intersolarsystem$earthPhases,
				phase * 4);
		poseStack.popPose();
	}

	@Inject(method = "close", at = @At("TAIL"))
	private void intersolarsystem$close(final CallbackInfo ci) {
		if (intersolarsystem$earthPhases != null) {
			intersolarsystem$earthPhases.close();
			intersolarsystem$disc.close();
		}
	}

	/** Eight quads, one per lighting phase, in the same layout as vanilla's moon phases. */
	@Unique
	private static GpuBuffer intersolarsystem$buildEarthPhases(final TextureAtlas atlas) {
		VertexFormat format = DefaultVertexFormat.POSITION_TEX;
		try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(EarthInSky.PHASES * 4 * format.getVertexSize())) {
			BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, format);
			for (int i = 0; i < EarthInSky.PHASES; i++) {
				TextureAtlasSprite sprite = atlas.getSprite(ModIds.id("earth/phase_" + i));
				builder.addVertex(-1.0F, 0.0F, -1.0F).setUv(sprite.getU1(), sprite.getV1());
				builder.addVertex(1.0F, 0.0F, -1.0F).setUv(sprite.getU0(), sprite.getV1());
				builder.addVertex(1.0F, 0.0F, 1.0F).setUv(sprite.getU0(), sprite.getV0());
				builder.addVertex(-1.0F, 0.0F, 1.0F).setUv(sprite.getU1(), sprite.getV0());
			}
			try (MeshData mesh = builder.buildOrThrow()) {
				return RenderSystem.getDevice().createBuffer(() -> "Earth phases", 32, mesh.vertexBuffer());
			}
		}
	}

	/** A flat disc (triangle fan) of radius 1 in the XZ plane, for the sky pipeline. */
	@Unique
	private static GpuBuffer intersolarsystem$buildDisc() {
		try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(
				EarthInSky.DISC_VERTICES * DefaultVertexFormat.POSITION.getVertexSize())) {
			BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
			builder.addVertex(0.0F, 0.0F, 0.0F);
			int steps = EarthInSky.DISC_VERTICES - 2;
			for (int i = 0; i <= steps; i++) {
				float a = i * Mth.TWO_PI / steps;
				builder.addVertex(Mth.cos(a), 0.0F, Mth.sin(a));
			}
			try (MeshData mesh = builder.buildOrThrow()) {
				return RenderSystem.getDevice().createBuffer(() -> "Earth occluder disc", 32, mesh.vertexBuffer());
			}
		}
	}
}
