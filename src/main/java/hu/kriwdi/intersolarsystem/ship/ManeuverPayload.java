package hu.kriwdi.intersolarsystem.ship;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Server -> client, to the crew while a meteor shower is under way: the field's seed, how far into it the ship is
 * ({@code elapsed} of {@code duration} ticks), where the ship is in the field, how many rocks hit so far, and whether
 * someone is flying it. A duration of 0 ends the manoeuvre.
 */
public record ManeuverPayload(long seed, int elapsed, int duration, float x, float y, int hits, boolean piloted,
		boolean dense)
		implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ManeuverPayload> TYPE = new CustomPacketPayload.Type<>(
			ModIds.id("maneuver"));
	public static final StreamCodec<ByteBuf, ManeuverPayload> CODEC = StreamCodec.of(
			(buf, p) -> {
				buf.writeLong(p.seed());
				VarInt.write(buf, Math.max(0, p.elapsed()));
				VarInt.write(buf, Math.max(0, p.duration()));
				buf.writeFloat(p.x());
				buf.writeFloat(p.y());
				VarInt.write(buf, Math.max(0, p.hits()));
				buf.writeBoolean(p.piloted());
				buf.writeBoolean(p.dense());
			},
			buf -> {
				ManeuverPayload p = new ManeuverPayload(buf.readLong(), VarInt.read(buf), VarInt.read(buf),
						buf.readFloat(), buf.readFloat(), VarInt.read(buf), buf.readBoolean(), buf.readBoolean());
				buf.skipBytes(buf.readableBytes());
				return p;
			});

	public static ManeuverPayload end() {
		return new ManeuverPayload(0L, 0, 0, 0.0F, 0.0F, 0, false, false);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
