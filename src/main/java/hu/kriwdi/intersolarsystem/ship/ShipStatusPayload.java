package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Server -> client: the ship status. Enum ordinals are on the wire, which is fine because client and server must run
 * the same mod version; the decoder is lenient so a mismatch cannot crash the client.
 */
public record ShipStatusPayload(ShipStatusSnapshot snapshot) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ShipStatusPayload> TYPE =
			new CustomPacketPayload.Type<>(ModIds.id("ship_status"));

	public static final StreamCodec<ByteBuf, ShipStatusPayload> CODEC = StreamCodec.of(
			ShipStatusPayload::encode, ShipStatusPayload::decode);

	private static void encode(ByteBuf buf, ShipStatusPayload payload) {
		ShipStatusSnapshot s = payload.snapshot();
		VarInt.write(buf, s.phase().ordinal());
		VarInt.write(buf, s.remainingTicks());
		for (ShipSubsystem subsystem : ShipSubsystem.values()) {
			VarInt.write(buf, s.conditions().get(subsystem.ordinal()));
		}
		VarInt.write(buf, s.incidents().size());
		for (ShipStatusSnapshot.IncidentView i : s.incidents()) {
			VarInt.write(buf, i.subsystem().ordinal());
			VarInt.write(buf, i.type().ordinal());
			VarInt.write(buf, i.remainingTicks());
		}
	}

	private static ShipStatusPayload decode(ByteBuf buf) {
		int phaseOrdinal = VarInt.read(buf);
		ShipFlightPhase phase = phaseOrdinal >= 0 && phaseOrdinal < ShipFlightPhase.values().length
				? ShipFlightPhase.values()[phaseOrdinal] : ShipFlightPhase.LANDED;
		int remaining = Math.max(0, VarInt.read(buf));
		List<Integer> conditions = new ArrayList<>();
		for (int i = 0; i < ShipSubsystem.values().length; i++) {
			conditions.add(ShipState.clamp(VarInt.read(buf)));
		}
		int count = Math.min(Math.max(0, VarInt.read(buf)), ShipIncidentRules.MAX_ACTIVE);
		List<ShipStatusSnapshot.IncidentView> incidents = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			int subsystemOrdinal = VarInt.read(buf);
			int typeOrdinal = VarInt.read(buf);
			int incidentRemaining = Math.max(0, VarInt.read(buf));
			if (subsystemOrdinal >= 0 && subsystemOrdinal < ShipSubsystem.values().length
					&& typeOrdinal >= 0 && typeOrdinal < ShipIncidentType.values().length) {
				incidents.add(new ShipStatusSnapshot.IncidentView(ShipSubsystem.values()[subsystemOrdinal],
						ShipIncidentType.values()[typeOrdinal], incidentRemaining));
			}
		}
		buf.skipBytes(buf.readableBytes());
		return new ShipStatusPayload(new ShipStatusSnapshot(phase, remaining, conditions, incidents));
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
