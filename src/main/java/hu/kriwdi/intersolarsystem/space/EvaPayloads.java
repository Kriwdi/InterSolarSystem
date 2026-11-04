package hu.kriwdi.intersolarsystem.space;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/** Packets of spacewalks: the tether state (server to client) and the manoeuvring unit's thrust (client to server). */
public final class EvaPayloads {
	private EvaPayloads() {
	}

	/** The player's safety tether: clipped to {@code anchor} with {@code length} blocks of line, or off. */
	public record Tether(boolean on, BlockPos anchor, int length) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Tether> TYPE = new CustomPacketPayload.Type<>(ModIds.id("eva_tether"));
		public static final StreamCodec<ByteBuf, Tether> CODEC = StreamCodec.of(
				(buf, p) -> {
					buf.writeBoolean(p.on());
					buf.writeLong(p.anchor().asLong());
					buf.writeInt(p.length());
				},
				buf -> new Tether(buf.readBoolean(), BlockPos.of(buf.readLong()), buf.readInt()));

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** The client fired the manoeuvring unit's jets for {@code ticks} ticks since the last report. */
	public record Thrust(int ticks) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Thrust> TYPE = new CustomPacketPayload.Type<>(ModIds.id("eva_thrust"));
		public static final StreamCodec<ByteBuf, Thrust> CODEC = StreamCodec.of(
				(buf, p) -> buf.writeShort(p.ticks()), buf -> new Thrust(buf.readShort()));

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** A debris stream: {@code phase} 0 = clear, 1 = warning, 2 = passing; {@code dir} the direction it travels. */
	public record Debris(byte phase, int ticksLeft, float dx, float dy, float dz) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Debris> TYPE = new CustomPacketPayload.Type<>(ModIds.id("eva_debris"));
		public static final StreamCodec<ByteBuf, Debris> CODEC = StreamCodec.of(
				(buf, p) -> {
					buf.writeByte(p.phase());
					buf.writeInt(p.ticksLeft());
					buf.writeFloat(p.dx());
					buf.writeFloat(p.dy());
					buf.writeFloat(p.dz());
				},
				buf -> new Debris(buf.readByte(), buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readFloat()));

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
