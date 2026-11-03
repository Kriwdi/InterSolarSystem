package hu.kriwdi.intersolarsystem.ship;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Server -> client, to players inside the ship: what the view out of the windows should show. {@code from}: where the
 * ship stands or left from ("earth", "earth_orbit", "moon"); {@code to}: the journey's target (empty when landed);
 * the phase with its remaining and total ticks drives the takeoff, flight and landing animation.
 */
public record ShipViewPayload(int phase, int remainingTicks, int totalTicks, String from, String to)
		implements CustomPacketPayload {
	private static final int MAX_ID = 64;
	public static final CustomPacketPayload.Type<ShipViewPayload> TYPE = new CustomPacketPayload.Type<>(
			ModIds.id("ship_view"));
	public static final StreamCodec<ByteBuf, ShipViewPayload> CODEC = StreamCodec.of(
			(buf, p) -> {
				VarInt.write(buf, p.phase());
				VarInt.write(buf, Math.max(0, p.remainingTicks()));
				VarInt.write(buf, Math.max(0, p.totalTicks()));
				writeString(buf, p.from());
				writeString(buf, p.to());
			},
			buf -> {
				int phase = VarInt.read(buf);
				int remaining = VarInt.read(buf);
				int total = VarInt.read(buf);
				String from = readString(buf);
				String to = readString(buf);
				buf.skipBytes(buf.readableBytes());
				return new ShipViewPayload(phase, remaining, total, from, to);
			});

	private static void writeString(ByteBuf buf, String s) {
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		VarInt.write(buf, bytes.length);
		buf.writeBytes(bytes);
	}

	private static String readString(ByteBuf buf) {
		int length = Math.max(0, Math.min(MAX_ID, VarInt.read(buf)));
		byte[] bytes = new byte[Math.min(length, buf.readableBytes())];
		buf.readBytes(bytes);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public static ShipViewPayload of(ShipState state, long now) {
		long remaining = ShipFlightTimeline.remainingTicks(state, now);
		long total = state.phase() == ShipFlightPhase.LANDED ? 0 : state.phaseDuration(state.phase());
		String to = state.phase() == ShipFlightPhase.LANDED ? "" : state.travel().target();
		return new ShipViewPayload(state.phase().ordinal(), (int) Math.min(Integer.MAX_VALUE, remaining),
				(int) Math.min(Integer.MAX_VALUE, total), ShipNavigation.here(state), to == null ? "" : to);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
