package hu.kriwdi.intersolarsystem.ship;

import java.nio.charset.StandardCharsets;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import hu.kriwdi.intersolarsystem.core.ModIds;

/**
 * Server -> client, to players inside the ship: what the view out of the windows should show. {@code from}: where the
 * ship stands or left from ("earth", "earth_orbit", "moon"); {@code to}: the journey's target (empty when landed);
 * the phase with its remaining and total ticks drives the takeoff, flight and landing animation. {@code groundFrom} and
 * {@code groundTo} name the painted ground under the ship where it stands and where it will land (the biome there,
 * or "moon"; empty in orbit).
 */
public record ShipViewPayload(int phase, int remainingTicks, int totalTicks, String from, String to, String groundFrom,
		String groundTo)
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
				writeString(buf, p.groundFrom());
				writeString(buf, p.groundTo());
			},
			buf -> {
				int phase = VarInt.read(buf);
				int remaining = VarInt.read(buf);
				int total = VarInt.read(buf);
				String from = readString(buf);
				String to = readString(buf);
				String groundFrom = readString(buf);
				String groundTo = readString(buf);
				buf.skipBytes(buf.readableBytes());
				return new ShipViewPayload(phase, remaining, total, from, to, groundFrom, groundTo);
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

	public static ShipViewPayload of(MinecraftServer server, ShipState state, long now) {
		long remaining = ShipFlightTimeline.remainingTicks(state, now);
		long total = state.phase() == ShipFlightPhase.LANDED ? 0 : state.phaseDuration(state.phase());
		String to = state.phase() == ShipFlightPhase.LANDED ? "" : state.travel().target();
		String here = ShipNavigation.here(state);
		String groundFrom = switch (here) {
			case "moon" -> "moon";
			case "mars" -> "mars";
			case "earth" -> state.landingSite().map(s -> earthGround(server, s.x(), s.z())).orElse("ash_desert");
			default -> "";
		};
		String groundTo = "";
		if ("moon".equals(to)) {
			groundTo = "moon";
		} else if ("mars".equals(to)) {
			groundTo = "mars";
		} else if ("earth".equals(to)) {
			groundTo = state.destination().map(d -> earthGround(server, d.x(), d.z()))
					.orElseGet(() -> state.travel().surface().map(s -> earthGround(server, s.x(), s.z()))
							.orElse("ash_desert"));
		}
		return new ShipViewPayload(state.phase().ordinal(), (int) Math.min(Integer.MAX_VALUE, remaining),
				(int) Math.min(Integer.MAX_VALUE, total), here, to == null ? "" : to, groundFrom, groundTo);
	}

	private static final Map<Long, String> GROUND_CACHE = new ConcurrentHashMap<>();

	/** The dead Earth's biome at a column (its painted ground has the same name); ash desert elsewhere. */
	static String earthGround(MinecraftServer server, int x, int z) {
		long key = ((long) x << 32) ^ (z & 0xFFFFFFFFL);
		return GROUND_CACHE.computeIfAbsent(key, k -> {
			ServerLevel level = server.overworld();
			return level.getBiome(new BlockPos(x, 64, z)).unwrapKey()
					.filter(b -> b.identifier().getNamespace().equals(ModIds.MOD_ID))
					.map(b -> b.identifier().getPath()).orElse("ash_desert");
		});
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
