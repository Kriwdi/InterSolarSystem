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
import hu.kriwdi.intersolarsystem.client.sky.ShipView;
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

	@Shadow
	@Final
	private GpuBuffer moonBuffer;

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

	@Shadow
	private void renderStars(final RenderPass renderPass, final float starBrightness, final PoseStack poseStack) {
		throw new AssertionError();
	}

	@Unique
	private static float intersolarsystem$partialTicks;

	/** Remembers the frame's partial tick for the ship view (the sky render itself does not get it). */
	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void intersolarsystem$rememberPartialTick(final net.minecraft.client.multiplayer.ClientLevel level,
			final float partialTicks, final net.minecraft.client.Camera camera,
			final net.minecraft.client.renderer.state.level.SkyRenderState state, final CallbackInfo ci) {
		intersolarsystem$partialTicks = partialTicks;
	}

	@Inject(method = "renderMoon", at = @At("HEAD"), cancellable = true)
	private void intersolarsystem$noMoonOnTheMoon(final RenderPass renderPass, final MoonPhase moonPhase,
			final float rainBrightness, final PoseStack poseStack, final CallbackInfo ci) {
		if (EarthInSky.current() == EarthInSky.View.FROM_MOON || ShipView.active()) {
			ci.cancel();
		}
	}

	@Inject(method = "renderSunMoonAndStars", at = @At("TAIL"))
	private void intersolarsystem$renderEarth(final RenderPass renderPass, final PoseStack poseStack,
			final float sunAngle, final float moonAngle, final float starAngle, final MoonPhase moonPhase,
			final float rainBrightness, final float starBrightness, final CallbackInfo ci) {
		if (intersolarsystem$earthPhases == null) {
			intersolarsystem$earthPhases = intersolarsystem$buildEarthPhases(celestialsAtlas);
			intersolarsystem$disc = intersolarsystem$buildDisc();
		}
		if (ShipView.active()) {
			intersolarsystem$renderShipView(renderPass, poseStack, sunAngle);
			return;
		}
		EarthInSky.View view = EarthInSky.current();
		if (view == EarthInSky.View.NONE) {
			return;
		}
		intersolarsystem$drawEarth(renderPass, poseStack, view.pitch(), view.roll(), view.scale(),
				EarthInSky.phase(view, sunAngle));
	}

	/** The view out of the ship's viewports (the interior dimension's sky), see {@link ShipView}. */
	@Unique
	private void intersolarsystem$renderShipView(final RenderPass renderPass, final PoseStack poseStack,
			final float sunAngle) {
		float partial = intersolarsystem$partialTicks;
		ShipView.Scene scene = ShipView.scene(partial);
		if (scene.stars() > 0.0F) {
			poseStack.pushPose();
			poseStack.rotateDegrees(Axis.YP, -90.0F);
			poseStack.rotateDegrees(Axis.XP, scene.starAngle());
			renderStars(renderPass, scene.stars(), poseStack);
			poseStack.popPose();
		}
		if (scene.earth() != null) {
			ShipView.Body e = scene.earth();
			intersolarsystem$drawEarth(renderPass, poseStack, e.pitch(), e.roll(), e.scale(),
					EarthInSky.phase(EarthInSky.View.FROM_ORBIT, sunAngle));
		}
		if (scene.moon() != null) {
			ShipView.Body m = scene.moon();
			poseStack.pushPose();
			poseStack.rotateDegrees(Axis.YP, -90.0F);
			poseStack.rotateDegrees(Axis.XP, m.pitch());
			poseStack.rotateDegrees(Axis.ZP, m.roll());
			intersolarsystem$drawDisc(renderPass, poseStack, EarthInSky.DISTANCE + 0.5F, m.scale() * 0.9F, 0x000000);
			Matrix4f matrix = applyCelestialBodyTransform(poseStack, EarthInSky.DISTANCE, m.scale());
			GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(matrix,
					new Vector4f(1.0F, 1.0F, 1.0F, 1.0F));
			drawCelestialBody(() -> "Moon (ship view)", renderPass, transforms, quadIndices.getBuffer(6), moonBuffer, 0);
			poseStack.popPose();
		}
		// Ground and sky are flat discs around the ship: the ground drops away as the ship climbs.
		if (scene.ground() != -1 && scene.groundDepth() <= 450.0F) {
			intersolarsystem$drawDisc(renderPass, poseStack, -scene.groundDepth(), 500.0F, scene.ground());
		}
		if (scene.ceiling() != -1) {
			intersolarsystem$drawDisc(renderPass, poseStack, 20.0F, 500.0F, scene.ceiling());
		}
	}

	@Unique
	private void intersolarsystem$drawDisc(final RenderPass renderPass, final PoseStack poseStack, final float height,
			final float scale, final int rgb) {
		Matrix4f matrix = applyCelestialBodyTransform(poseStack, height, scale);
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(matrix,
				new Vector4f(((rgb >> 16) & 0xFF) / 255.0F, ((rgb >> 8) & 0xFF) / 255.0F, (rgb & 0xFF) / 255.0F, 1.0F));
		renderPass.pushDebugGroup(() -> "InterSolarSystem disc");
		renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.SKY));
		RenderSystem.bindDefaultUniforms(renderPass);
		renderPass.setUniform("DynamicTransforms", transforms);
		renderPass.setVertexBuffer(0, intersolarsystem$disc.slice());
		renderPass.draw(EarthInSky.DISC_VERTICES, 1, 0, 0);
		renderPass.popDebugGroup();
	}

	@Unique
	private void intersolarsystem$drawEarth(final RenderPass renderPass, final PoseStack poseStack, final float pitch,
			final float roll, final float scale, final int phase) {
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.YP, -90.0F);
		poseStack.rotateDegrees(Axis.XP, pitch);
		poseStack.rotateDegrees(Axis.ZP, roll);
		// Occluder: a black disc slightly smaller than the sprite (the atmosphere rim stays visible).
		intersolarsystem$drawDisc(renderPass, poseStack, EarthInSky.DISTANCE + 0.5F, scale * 0.94F, 0x000000);
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
