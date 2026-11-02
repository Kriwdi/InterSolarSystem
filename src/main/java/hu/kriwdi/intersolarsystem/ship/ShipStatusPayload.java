package hu.kriwdi.intersolarsystem.ship;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Server -> client: the ship status. Starts with a format number and the subsystem count, so a client of another mod
 * version reads what it understands (unknown subsystems are skipped, missing ones shown as 100%) instead of
 * misreading the rest. Enum ordinals are on the wire; the decoder is lenient so a mismatch cannot crash the client.
 */
public record ShipStatusPayload(ShipStatusSnapshot snapshot) implements CustomPacketPayload {
	/** Bump when the layout after the header changes in a way older clients cannot skip. */
	public static final int FORMAT = 1;
	private static final int MAX_SUBSYSTEMS = 64;

	public static final CustomPacketPayload.Type<ShipStatusPayload> TYPE =
			new CustomPacketPayload.Type<>(ModIds.id("ship_status"));

	public static final StreamCodec<ByteBuf, ShipStatusPayload> CODEC = StreamCodec.of(
			ShipStatusPayload::encode, ShipStatusPayload::decode);

	private static void encode(ByteBuf buf, ShipStatusPayload payload) {
		ShipStatusSnapshot s = payload.snapshot();
		VarInt.write(buf, FORMAT);
		VarInt.write(buf, s.phase().ordinal());
		VarInt.write(buf, s.remainingTicks());
		VarInt.write(buf, ShipSubsystem.values().length);
		for (ShipSubsystem subsystem : ShipSubsystem.values()) {
			VarInt.write(buf, s.conditions().get(subsystem.ordinal()));
		}
		VarInt.write(buf, s.incidents().size());
		for (ShipStatusSnapshot.IncidentView i : s.incidents()) {
			VarInt.write(buf, i.subsystem().ordinal());
			VarInt.write(buf, i.type().ordinal());
			VarInt.write(buf, i.remainingTicks());
		}
		VarInt.write(buf, s.knownFaults().size());
		for (ShipSubsystem fault : s.knownFaults()) {
			VarInt.write(buf, fault.ordinal());
		}
	}

	private static ShipStatusPayload decode(ByteBuf buf) {
		int format = VarInt.read(buf);
		if (format != FORMAT) {
			buf.skipBytes(buf.readableBytes());
			return new ShipStatusPayload(ShipStatusSnapshot.unknown());
		}
		int phaseOrdinal = VarInt.read(buf);
		ShipFlightPhase phase = phaseOrdinal >= 0 && phaseOrdinal < ShipFlightPhase.values().length
				? ShipFlightPhase.values()[phaseOrdinal] : ShipFlightPhase.LANDED;
		int remaining = Math.max(0, VarInt.read(buf));
		int sent = Math.min(Math.max(0, VarInt.read(buf)), MAX_SUBSYSTEMS);
		List<Integer> conditions = new ArrayList<>();
		for (int i = 0; i < sent; i++) {
			int value = ShipState.clamp(VarInt.read(buf));
			if (i < ShipSubsystem.values().length) {
				conditions.add(value);
			}
		}
		while (conditions.size() < ShipSubsystem.values().length) {
			conditions.add(ShipState.MAX_CONDITION);
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
		List<ShipSubsystem> knownFaults = new ArrayList<>();
		if (buf.isReadable()) {
			int faultCount = Math.min(Math.max(0, VarInt.read(buf)), MAX_SUBSYSTEMS);
			for (int i = 0; i < faultCount; i++) {
				int ordinal = VarInt.read(buf);
				if (ordinal >= 0 && ordinal < ShipSubsystem.values().length
						&& !knownFaults.contains(ShipSubsystem.values()[ordinal])) {
					knownFaults.add(ShipSubsystem.values()[ordinal]);
				}
			}
		}
		buf.skipBytes(buf.readableBytes());
		return new ShipStatusPayload(new ShipStatusSnapshot(phase, remaining, conditions, incidents, knownFaults));
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
