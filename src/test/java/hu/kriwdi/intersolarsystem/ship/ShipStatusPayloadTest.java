package hu.kriwdi.intersolarsystem.ship;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.network.VarInt;

import org.junit.jupiter.api.Test;

class ShipStatusPayloadTest {
	private static List<Integer> conditions(int value) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < ShipSubsystem.values().length; i++) {
			list.add(value - i);
		}
		return list;
	}

	@Test
	void roundTripKeepsEverything() {
		ShipStatusSnapshot snapshot = new ShipStatusSnapshot(ShipFlightPhase.FLIGHT, 345, conditions(90),
				List.of(new ShipStatusSnapshot.IncidentView(ShipSubsystem.ENGINE, ShipIncidentType.OVERHEAT, 400)));
		ByteBuf buf = Unpooled.buffer();
		ShipStatusPayload.CODEC.encode(buf, new ShipStatusPayload(snapshot));
		assertEquals(snapshot, ShipStatusPayload.CODEC.decode(buf).snapshot());
		assertEquals(0, buf.readableBytes());
	}

	@Test
	void unknownFormatGivesNeutralStatusAndConsumesThePacket() {
		ByteBuf buf = Unpooled.buffer();
		VarInt.write(buf, ShipStatusPayload.FORMAT + 1);
		VarInt.write(buf, 3);
		VarInt.write(buf, 77);
		assertEquals(ShipStatusSnapshot.unknown(), ShipStatusPayload.CODEC.decode(buf).snapshot());
		assertEquals(0, buf.readableBytes());
	}

	@Test
	void extraSubsystemsFromANewerServerAreSkipped() {
		ByteBuf buf = Unpooled.buffer();
		VarInt.write(buf, ShipStatusPayload.FORMAT);
		VarInt.write(buf, ShipFlightPhase.LANDED.ordinal());
		VarInt.write(buf, 0);
		int count = ShipSubsystem.values().length + 2;
		VarInt.write(buf, count);
		for (int i = 0; i < count; i++) {
			VarInt.write(buf, 50);
		}
		VarInt.write(buf, 1);
		VarInt.write(buf, ShipSubsystem.CARGO.ordinal());
		VarInt.write(buf, ShipIncidentType.FIRE.ordinal());
		VarInt.write(buf, 20);
		ShipStatusSnapshot decoded = ShipStatusPayload.CODEC.decode(buf).snapshot();
		assertEquals(ShipSubsystem.values().length, decoded.conditions().size());
		assertEquals(List.of(new ShipStatusSnapshot.IncidentView(ShipSubsystem.CARGO, ShipIncidentType.FIRE, 20)),
				decoded.incidents());
	}

	@Test
	void missingSubsystemsFromAnOlderServerShowAsFull() {
		ByteBuf buf = Unpooled.buffer();
		VarInt.write(buf, ShipStatusPayload.FORMAT);
		VarInt.write(buf, ShipFlightPhase.LANDED.ordinal());
		VarInt.write(buf, 0);
		VarInt.write(buf, 2);
		VarInt.write(buf, 10);
		VarInt.write(buf, 20);
		VarInt.write(buf, 0);
		ShipStatusSnapshot decoded = ShipStatusPayload.CODEC.decode(buf).snapshot();
		assertEquals(10, decoded.conditions().get(0));
		assertEquals(20, decoded.conditions().get(1));
		assertEquals(ShipState.MAX_CONDITION, decoded.conditions().get(ShipSubsystem.values().length - 1));
	}
}
