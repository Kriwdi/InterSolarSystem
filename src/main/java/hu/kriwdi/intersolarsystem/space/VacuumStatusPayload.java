package hu.kriwdi.intersolarsystem.space;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Server -> client, a few times a second while it matters: what the helmet display shows and how hard the vacuum is
 * hitting. {@code oxygen}: the suit's tank 0..1; {@code breath}: the air left in the lungs 0..1; {@code mode}: see
 * {@link VacuumBreathing.Mode}; {@code leak}: the suit is punctured; {@code vacuum}: the player is somewhere without air.
 */
public record VacuumStatusPayload(float oxygen, float breath, byte mode, boolean leak, boolean vacuum)
		implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<VacuumStatusPayload> TYPE = new CustomPacketPayload.Type<>(
			ModIds.id("vacuum_status"));
	public static final StreamCodec<ByteBuf, VacuumStatusPayload> CODEC = StreamCodec.of(
			(buf, p) -> {
				buf.writeFloat(p.oxygen());
				buf.writeFloat(p.breath());
				buf.writeByte(p.mode());
				buf.writeBoolean(p.leak());
				buf.writeBoolean(p.vacuum());
			},
			buf -> new VacuumStatusPayload(buf.readFloat(), buf.readFloat(), buf.readByte(), buf.readBoolean(),
					buf.readBoolean()));

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
