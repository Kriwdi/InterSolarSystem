package hu.kriwdi.intersolarsystem.ship;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/** Packets of the repair minigames. The board is rebuilt from the seed on both sides; the server checks the answer. */
public final class RepairBoardPayloads {
	/** Upper bound for answers (clicks or moves) read from a client. */
	public static final int MAX_ANSWER = 64;

	private RepairBoardPayloads() {
	}

	private static RepairMinigame kindOf(int ordinal) {
		RepairMinigame[] values = RepairMinigame.values();
		return ordinal >= 0 && ordinal < values.length ? values[ordinal] : RepairMinigame.WIRING;
	}

	/** Server -> client: open this board. */
	public record Open(RepairMinigame kind, long seed, int size) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Open> TYPE = new CustomPacketPayload.Type<>(ModIds.id("repair_open"));
		public static final StreamCodec<ByteBuf, Open> CODEC = StreamCodec.of(
				(buf, p) -> {
					VarInt.write(buf, p.kind().ordinal());
					buf.writeLong(p.seed());
					VarInt.write(buf, p.size());
				},
				buf -> new Open(kindOf(VarInt.read(buf)), buf.readLong(), VarInt.read(buf)));

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}

	/** Client -> server: the player's answer (wire connections, click ticks or valve moves, depending on the board). */
	public record Result(RepairMinigame kind, long seed, int[] answer) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<Result> TYPE = new CustomPacketPayload.Type<>(ModIds.id("repair_result"));
		public static final StreamCodec<ByteBuf, Result> CODEC = StreamCodec.of(
				(buf, p) -> {
					VarInt.write(buf, p.kind().ordinal());
					buf.writeLong(p.seed());
					VarInt.write(buf, p.answer().length);
					for (int a : p.answer()) {
						VarInt.write(buf, a);
					}
				},
				buf -> {
					RepairMinigame kind = kindOf(VarInt.read(buf));
					long seed = buf.readLong();
					int count = Math.max(0, Math.min(MAX_ANSWER, VarInt.read(buf)));
					int[] answer = new int[count];
					for (int i = 0; i < count; i++) {
						answer[i] = VarInt.read(buf);
					}
					buf.skipBytes(buf.readableBytes());
					return new Result(kind, seed, answer);
				});

		@Override
		public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
			return TYPE;
		}
	}
}
