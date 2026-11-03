package hu.kriwdi.intersolarsystem.lore;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/** Server -> client: show a lore text ({@code kind}: "terminal"; {@code id}: its entry in the lore files). */
public record LoreOpenPayload(String kind, String id) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<LoreOpenPayload> TYPE = new CustomPacketPayload.Type<>(
			ModIds.id("lore_open"));
	public static final StreamCodec<ByteBuf, LoreOpenPayload> CODEC = StreamCodec.of(
			(buf, p) -> {
				write(buf, p.kind());
				write(buf, p.id());
			},
			buf -> {
				LoreOpenPayload p = new LoreOpenPayload(read(buf), read(buf));
				buf.skipBytes(buf.readableBytes());
				return p;
			});

	private static void write(ByteBuf buf, String s) {
		byte[] b = s.getBytes(StandardCharsets.UTF_8);
		VarInt.write(buf, Math.min(b.length, 64));
		buf.writeBytes(b, 0, Math.min(b.length, 64));
	}

	private static String read(ByteBuf buf) {
		int n = Math.max(0, Math.min(64, VarInt.read(buf)));
		byte[] b = new byte[Math.min(n, buf.readableBytes())];
		buf.readBytes(b);
		return new String(b, StandardCharsets.UTF_8);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
