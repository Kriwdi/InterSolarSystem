package hu.kriwdi.intersolarsystem.ship;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/** Packets of the navigation console. The client computes the shown times from the same catalogue and maths. */
public final class NavigationPayloads {
	private static final int MAX_ID_LENGTH = 64;

	private NavigationPayloads() {
	}

	/** Server -> client: open the console with the ship's engine tier, the server's game time and whether it can launch. */
	public record Open(int engineTier, long gameTime, boolean canLaunch, boolean inOrbit) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Open> TYPE = new CustomPacketPayload.Type<>(ModIds.id("nav_open"));
		public static final StreamCodec<ByteBuf, Open> CODEC = StreamCodec.of(
				(buf, p) -> {
					VarInt.write(buf, p.engineTier());
					buf.writeLong(p.gameTime());
					buf.writeBoolean(p.canLaunch());
					buf.writeBoolean(p.inOrbit());
				},
				buf -> new Open(VarInt.read(buf), buf.readLong(), buf.readBoolean(), buf.readBoolean()));

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Client -> server: launch towards this body. */
	public record Launch(String bodyId) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Launch> TYPE = new CustomPacketPayload.Type<>(ModIds.id("nav_launch"));
		public static final StreamCodec<ByteBuf, Launch> CODEC = StreamCodec.of(
				(buf, p) -> {
					byte[] bytes = p.bodyId().getBytes(java.nio.charset.StandardCharsets.UTF_8);
					VarInt.write(buf, bytes.length);
					buf.writeBytes(bytes);
				},
				buf -> {
					int length = Math.max(0, Math.min(MAX_ID_LENGTH, VarInt.read(buf)));
					byte[] bytes = new byte[Math.min(length, buf.readableBytes())];
					buf.readBytes(bytes);
					buf.skipBytes(buf.readableBytes());
					return new Launch(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
				});

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
