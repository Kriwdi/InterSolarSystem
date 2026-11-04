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

	/**
	 * Server -> client: open the console with the ship's engine tier, the server's game time, why it cannot launch
	 * ({@code blocker}: a translation key, "" when it can) and where the ship is ({@code here}: "earth", "earth_orbit",
	 * "moon", "mars", "mothership") and where the mothership is parked ({@code mothershipAt}: "mars_orbit"; "" under
	 * way or not built).
	 */
	public record Open(int engineTier, long gameTime, String blocker, String here, String mothershipAt,
			String discovered) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Open> TYPE = new CustomPacketPayload.Type<>(ModIds.id("nav_open"));
		public static final StreamCodec<ByteBuf, Open> CODEC = StreamCodec.of(
				(buf, p) -> {
					VarInt.write(buf, p.engineTier());
					buf.writeLong(p.gameTime());
					writeString(buf, p.blocker(), 128);
					writeString(buf, p.here(), MAX_ID_LENGTH);
					writeString(buf, p.mothershipAt(), MAX_ID_LENGTH);
					writeString(buf, p.discovered(), 512);
				},
				buf -> {
					int engine = VarInt.read(buf);
					long time = buf.readLong();
					String blocker = readString(buf, 128);
					String here = readString(buf, MAX_ID_LENGTH);
					String mothership = readString(buf, MAX_ID_LENGTH);
					String discovered = readString(buf, 512);
					buf.skipBytes(buf.readableBytes());
					return new Open(engine, time, blocker, here, mothership, discovered);
				});

		public boolean canLaunch() {
			return blocker.isEmpty();
		}

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	private static void writeString(ByteBuf buf, String value, int max) {
		byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
		int length = Math.min(bytes.length, max);
		VarInt.write(buf, length);
		buf.writeBytes(bytes, 0, length);
	}

	private static String readString(ByteBuf buf, int max) {
		int length = Math.max(0, Math.min(max, VarInt.read(buf)));
		byte[] bytes = new byte[Math.min(length, buf.readableBytes())];
		buf.readBytes(bytes);
		return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
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
