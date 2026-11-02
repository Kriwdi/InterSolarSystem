package hu.kriwdi.intersolarsystem.ship;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/** Packets of the wiring minigame. The board is rebuilt from the seed on both sides; the server checks the answer. */
public final class WiringPayloads {
	private WiringPayloads() {
	}

	/** Server -> client: open the wiring screen for this board. */
	public record Open(long seed, int wires) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Open> TYPE = new CustomPacketPayload.Type<>(ModIds.id("wiring_open"));
		public static final StreamCodec<ByteBuf, Open> CODEC = StreamCodec.of(
				(buf, p) -> {
					buf.writeLong(p.seed());
					VarInt.write(buf, p.wires());
				},
				buf -> new Open(buf.readLong(), VarInt.read(buf)));

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Client -> server: the player's connections ({@code connections[i]} = right slot joined to left slot i). */
	public record Result(long seed, int[] connections) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Result> TYPE = new CustomPacketPayload.Type<>(ModIds.id("wiring_result"));
		public static final StreamCodec<ByteBuf, Result> CODEC = StreamCodec.of(
				(buf, p) -> {
					buf.writeLong(p.seed());
					VarInt.write(buf, p.connections().length);
					for (int c : p.connections()) {
						VarInt.write(buf, c);
					}
				},
				buf -> {
					long seed = buf.readLong();
					int count = Math.max(0, Math.min(WiringPuzzle.MAX_WIRES, VarInt.read(buf)));
					int[] connections = new int[count];
					for (int i = 0; i < count; i++) {
						connections[i] = VarInt.read(buf);
					}
					buf.skipBytes(buf.readableBytes());
					return new Result(seed, connections);
				});

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
