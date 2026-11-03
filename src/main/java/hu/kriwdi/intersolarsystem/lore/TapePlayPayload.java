package hu.kriwdi.intersolarsystem.lore;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/** Server -> client: a data tape starts playing in the deck at (x, y, z) (a data terminal or a radio set). */
public record TapePlayPayload(String id, int x, int y, int z) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<TapePlayPayload> TYPE = new CustomPacketPayload.Type<>(
			ModIds.id("tape_play"));
	public static final StreamCodec<ByteBuf, TapePlayPayload> CODEC = StreamCodec.of(
			(buf, p) -> {
				byte[] b = p.id().getBytes(StandardCharsets.UTF_8);
				VarInt.write(buf, Math.min(b.length, 64));
				buf.writeBytes(b, 0, Math.min(b.length, 64));
				buf.writeInt(p.x());
				buf.writeInt(p.y());
				buf.writeInt(p.z());
			},
			buf -> {
				int n = Math.max(0, Math.min(64, VarInt.read(buf)));
				byte[] b = new byte[Math.min(n, buf.readableBytes())];
				buf.readBytes(b);
				return new TapePlayPayload(new String(b, StandardCharsets.UTF_8), buf.readInt(), buf.readInt(), buf.readInt());
			});

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
