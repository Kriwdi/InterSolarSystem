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

import hu.kriwdi.intersolarsystem.client.sky.ClientManeuver;
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
		net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
		if (mc.level != null && (mc.level.dimension().equals(hu.kriwdi.intersolarsystem.ship.ShipDimensions.ORBIT)
				|| mc.level.dimension().equals(hu.kriwdi.intersolarsystem.ship.ShipDimensions.MOON)
				|| mc.level.dimension().equals(hu.kriwdi.intersolarsystem.ship.ShipDimensions.MOTHERSHIP))) {
			intersolarsystem$drawSpace(renderPass, poseStack, 1.0F, starAngle * 360.0F);
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
			intersolarsystem$drawSpace(renderPass, poseStack, scene.stars(), scene.starAngle());
			if (ShipView.cruising() && scene.stars() > 0.5F) {
				intersolarsystem$drawStreaks(renderPass, poseStack, ShipView.time(partial));
			}
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
		// The ground under the ship: a plain disc to the horizon with the painted surface of the biome on top (tiles
		// of a seamless aerial view); it drops away as the ship climbs.
		if (scene.ground() != -1 && scene.groundDepth() <= 450.0F) {
			// Turned over so the front faces look up (the sky pipelines cull back faces).
			poseStack.pushPose();
			poseStack.rotateDegrees(Axis.XP, 180.0F);
			intersolarsystem$drawDisc(renderPass, poseStack, scene.groundDepth() + 0.5F, 500.0F, scene.ground());
			if (!scene.groundTexture().isEmpty()) {
				intersolarsystem$drawGround(renderPass, poseStack, scene.groundDepth(), scene.groundTexture());
			}
			poseStack.popPose();
		}
		if (scene.ceiling() != -1) {
			intersolarsystem$drawDisc(renderPass, poseStack, 20.0F, 500.0F, scene.ceiling());
		}
		if (ClientManeuver.active()) {
			intersolarsystem$drawMeteors(renderPass, poseStack, partial);
		}
	}

	@Unique
	private GpuBuffer intersolarsystem$meteor;

	@Unique
	private final java.util.Map<String, GpuBuffer> intersolarsystem$space = new java.util.HashMap<>();

	@Unique
	private void intersolarsystem$sprite(final RenderPass renderPass, final PoseStack poseStack, final String name,
			final float distance, final float scale, final float brightness) {
		GpuBuffer buffer = intersolarsystem$space.computeIfAbsent(name, n -> intersolarsystem$buildSprite(celestialsAtlas, n));
		Matrix4f matrix = applyCelestialBodyTransform(poseStack, distance, scale);
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(matrix,
				new Vector4f(brightness, brightness, brightness, 1.0F));
		drawCelestialBody(() -> "Space " + name, renderPass, transforms, quadIndices.getBuffer(6), buffer, 0);
	}

	/**
	 * The deep sky of space (orbit, the Moon, the mothership, the ship's windows in flight): the Milky Way as a band
	 * of eight segments along a tilted great circle, four coloured nebulae and twelve dense star fields, all drawn
	 * additively so black is nothing. {@code turn}: the sky's slow rotation (degrees).
	 */
	@Unique
	private void intersolarsystem$drawSpace(final RenderPass renderPass, final PoseStack poseStack, final float brightness,
			final float turn) {
		float d = 100.0F;
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.YP, -90.0F);
		poseStack.rotateDegrees(Axis.XP, turn);
		for (int i = 0; i < 12; i++) {
			poseStack.pushPose();
			poseStack.rotateDegrees(Axis.XP, i * 67.0F);
			poseStack.rotateDegrees(Axis.ZP, i * 41.0F + 13.0F);
			intersolarsystem$sprite(renderPass, poseStack, "space/stars_" + (i % 4), d, d * 0.62F, 0.75F * brightness);
			poseStack.popPose();
		}
		poseStack.pushPose();
		poseStack.rotateDegrees(Axis.ZP, 62.0F);
		for (int i = 0; i < 8; i++) {
			poseStack.pushPose();
			poseStack.rotateDegrees(Axis.XP, i * 45.0F);
			poseStack.rotateDegrees(Axis.YP, 90.0F);
			intersolarsystem$sprite(renderPass, poseStack, "space/milky_way_" + i, d, d * 0.42F, 0.9F * brightness);
			poseStack.popPose();
		}
		poseStack.popPose();
		float[][] nebulae = {{35.0F, 20.0F, 0.32F}, {150.0F, -40.0F, 0.26F}, {230.0F, 70.0F, 0.22F}, {300.0F, -15.0F, 0.3F}};
		for (int i = 0; i < nebulae.length; i++) {
			poseStack.pushPose();
			poseStack.rotateDegrees(Axis.XP, nebulae[i][0]);
			poseStack.rotateDegrees(Axis.ZP, nebulae[i][1]);
			intersolarsystem$sprite(renderPass, poseStack, "space/nebula_" + i, d, d * nebulae[i][2], 0.85F * brightness);
			poseStack.popPose();
		}
		poseStack.popPose();
	}

	/** Dust and ice grains streaking past the bow while the ship cruises: the feeling of speed. */
	@Unique
	private void intersolarsystem$drawStreaks(final RenderPass renderPass, final PoseStack poseStack, final float time) {
		java.util.Random rnd = new java.util.Random(4242L);
		for (int i = 0; i < 40; i++) {
			float dirX = rnd.nextFloat() * 70.0F - 35.0F;
			float dirZ = rnd.nextFloat() * 70.0F - 35.0F;
			float speed = 0.35F + rnd.nextFloat() * 0.5F;
			float p = (time * speed / 20.0F + rnd.nextFloat()) % 1.0F;
			float distance = 100.0F - 95.0F * p;
			poseStack.pushPose();
			poseStack.rotateDegrees(Axis.YP, -90.0F);
			poseStack.rotateDegrees(Axis.ZP, -90.0F);
			poseStack.rotateDegrees(Axis.XP, dirX * (1.0F + p * 1.5F));
			poseStack.rotateDegrees(Axis.ZP, dirZ * (1.0F + p * 1.5F));
			intersolarsystem$sprite(renderPass, poseStack, "space/dust", distance, 0.4F + 2.5F * p * p, Math.min(1.0F, p * 2.0F));
			poseStack.popPose();
		}
	}

	/** The meteor shower ahead of the bow: rocks grow as they close in, offset by where the pilot has moved the ship. */
	@Unique
	private void intersolarsystem$drawMeteors(final RenderPass renderPass, final PoseStack poseStack,
			final float partial) {
		if (intersolarsystem$meteor == null) {
			intersolarsystem$meteor = intersolarsystem$buildSprite(celestialsAtlas, "meteor");
		}
		float elapsed = ClientManeuver.elapsed(partial);
		for (hu.kriwdi.intersolarsystem.travel.MeteorField.Meteor rock : ClientManeuver.field()) {
			float p = (elapsed - rock.appear()) / hu.kriwdi.intersolarsystem.travel.MeteorField.APPROACH_TICKS;
			if (p < 0.0F || p > 1.04F) {
				continue;
			}
			float rx = rock.x() - ClientManeuver.shipX();
			float ry = rock.y() - ClientManeuver.shipY();
			// Close rocks spread out of the view (perspective): the offset grows as they approach.
			float spread = 18.0F + 30.0F * p * p;
			poseStack.pushPose();
			poseStack.rotateDegrees(Axis.YP, -90.0F);
			poseStack.rotateDegrees(Axis.XP, rx * spread);
			poseStack.rotateDegrees(Axis.ZP, -90.0F + ry * spread);
			poseStack.rotateDegrees(Axis.YP, (rock.arrive() * 37 % 360) + elapsed * 3.0F);
			float scale = 0.6F + 16.0F * p * p * p * (rock.radius() / 0.25F);
			Matrix4f matrix = applyCelestialBodyTransform(poseStack, EarthInSky.DISTANCE, scale);
			GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(matrix,
					new Vector4f(1.0F, 1.0F, 1.0F, 1.0F));
			drawCelestialBody(() -> "Meteor", renderPass, transforms, quadIndices.getBuffer(6), intersolarsystem$meteor,
					0);
			poseStack.popPose();
		}
	}

	/** One quad of a celestial sprite. */
	@Unique
	private static GpuBuffer intersolarsystem$buildSprite(final TextureAtlas atlas, final String name) {
		VertexFormat format = DefaultVertexFormat.POSITION_TEX;
		try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(4 * format.getVertexSize())) {
			BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, format);
			TextureAtlasSprite sprite = atlas.getSprite(ModIds.id(name));
			builder.addVertex(-1.0F, 0.0F, -1.0F).setUv(sprite.getU1(), sprite.getV1());
			builder.addVertex(1.0F, 0.0F, -1.0F).setUv(sprite.getU0(), sprite.getV1());
			builder.addVertex(1.0F, 0.0F, 1.0F).setUv(sprite.getU0(), sprite.getV0());
			builder.addVertex(-1.0F, 0.0F, 1.0F).setUv(sprite.getU1(), sprite.getV0());
			try (MeshData mesh = builder.buildOrThrow()) {
				return RenderSystem.getDevice().createBuffer(() -> "Sprite " + name, 32, mesh.vertexBuffer());
			}
		}
	}

	/** Tiles per side of the painted ground, and the size of one tile (in sky units, about blocks). */
	@Unique
	private static final int GROUND_TILES = 9;

	@Unique
	private static final float GROUND_TILE = 36.0F;

	@Unique
	private final java.util.Map<String, GpuBuffer> intersolarsystem$grounds = new java.util.HashMap<>();

	@Unique
	private void intersolarsystem$drawGround(final RenderPass renderPass, final PoseStack poseStack, final float depth,
			final String name) {
		GpuBuffer tiles = intersolarsystem$grounds.computeIfAbsent(name,
				n -> intersolarsystem$buildGround(celestialsAtlas, n));
		int indices = 6 * GROUND_TILES * GROUND_TILES;
		Matrix4f matrix = applyCelestialBodyTransform(poseStack, depth, GROUND_TILE / 2.0F);
		// The airless Moon's ground is lit harshly but its regolith is darker than the painting.
		float light = "moon".equals(name) ? 0.72F : 0.95F;
		GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(matrix,
				new Vector4f(light, light, light, 1.0F));
		renderPass.pushDebugGroup(() -> "InterSolarSystem ground");
		renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.CELESTIAL));
		RenderSystem.bindDefaultUniforms(renderPass);
		renderPass.setUniform("DynamicTransforms", transforms);
		renderPass.setUniform("Sampler0", celestialsAtlas.getTextureView(), celestialsAtlas.getSampler());
		renderPass.setVertexBuffer(0, tiles.slice());
		renderPass.setIndexBuffer(quadIndices.getBuffer(indices), quadIndices.type());
		renderPass.drawIndexed(indices, 1, 0, 0, 0);
		renderPass.popDebugGroup();
	}

	/** A square of tiles (two units each) of one ground sprite, centred under the viewer. */
	@Unique
	private static GpuBuffer intersolarsystem$buildGround(final TextureAtlas atlas, final String name) {
		VertexFormat format = DefaultVertexFormat.POSITION_TEX;
		int quads = GROUND_TILES * GROUND_TILES;
		try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(quads * 4 * format.getVertexSize())) {
			BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, format);
			TextureAtlasSprite sprite = atlas.getSprite(ModIds.id("ground/" + name));
			for (int i = 0; i < GROUND_TILES; i++) {
				for (int j = 0; j < GROUND_TILES; j++) {
					float x0 = -GROUND_TILES + 2.0F * i;
					float z0 = -GROUND_TILES + 2.0F * j;
					builder.addVertex(x0, 0.0F, z0).setUv(sprite.getU1(), sprite.getV1());
					builder.addVertex(x0 + 2.0F, 0.0F, z0).setUv(sprite.getU0(), sprite.getV1());
					builder.addVertex(x0 + 2.0F, 0.0F, z0 + 2.0F).setUv(sprite.getU0(), sprite.getV0());
					builder.addVertex(x0, 0.0F, z0 + 2.0F).setUv(sprite.getU1(), sprite.getV0());
				}
			}
			try (MeshData mesh = builder.buildOrThrow()) {
				return RenderSystem.getDevice().createBuffer(() -> "Ground " + name, 32, mesh.vertexBuffer());
			}
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
		intersolarsystem$grounds.values().forEach(GpuBuffer::close);
		if (intersolarsystem$meteor != null) {
			intersolarsystem$meteor.close();
			intersolarsystem$meteor = null;
		}
		intersolarsystem$grounds.clear();
		intersolarsystem$space.values().forEach(GpuBuffer::close);
		intersolarsystem$space.clear();
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
