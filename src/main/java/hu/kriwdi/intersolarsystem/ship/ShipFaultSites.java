package hu.kriwdi.intersolarsystem.ship;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import hu.kriwdi.intersolarsystem.content.ConduitBlock;
import hu.kriwdi.intersolarsystem.content.FaultBlock;
import hu.kriwdi.intersolarsystem.content.ModBlocks;
import hu.kriwdi.intersolarsystem.content.ModItems;
import hu.kriwdi.intersolarsystem.ship.ShipInteriorLayout.Cell;

/**
 * Incidents show up at a place in the ship that belongs to the failing subsystem: a short circuit sparks from a wall
 * panel, overheating vents steam, a leak sprays from the coolant pipe, a fire burns on the floor. The crew fixes it
 * there with the right tool (soldering iron, multitool, sealant, extinguisher) or a repair kit; the repair station
 * still works too. When the incident is gone, by whatever way, the ship's own part comes back.
 */
public final class ShipFaultSites {
	/** Wall panel, floor cell (for fires) and pipe cell of each subsystem. */
	private record Site(Cell wall, Cell floor, Cell pipe) {
	}

	private static final Map<ShipSubsystem, Site> SITES = new EnumMap<>(ShipSubsystem.class);

	static {
		SITES.put(ShipSubsystem.ENGINE, new Site(new Cell(0, 2, 3), new Cell(1, 1, 3), new Cell(1, 5, 3)));
		SITES.put(ShipSubsystem.LANDING_LAUNCH, new Site(new Cell(8, 2, 3), new Cell(7, 1, 3), new Cell(1, 5, 4)));
		SITES.put(ShipSubsystem.FUEL_ENERGY, new Site(new Cell(0, 2, 1), new Cell(2, 1, 4), new Cell(1, 5, 2)));
		SITES.put(ShipSubsystem.LIFE_SUPPORT, new Site(new Cell(8, 2, 4), new Cell(6, 1, 4), new Cell(1, 5, 6)));
		SITES.put(ShipSubsystem.CARGO, new Site(new Cell(8, 2, 7), new Cell(6, 1, 7), new Cell(1, 5, 8)));
		SITES.put(ShipSubsystem.HULL_ARMOR, new Site(new Cell(8, 2, 10), new Cell(6, 1, 11), new Cell(1, 5, 10)));
		SITES.put(ShipSubsystem.STABILIZER, new Site(new Cell(0, 2, 13), new Cell(2, 1, 13), new Cell(1, 5, 12)));
		SITES.put(ShipSubsystem.NAVIGATION_COMMS, new Site(new Cell(0, 2, 15), new Cell(1, 1, 15), new Cell(1, 5, 17)));
	}

	private ShipFaultSites() {
	}

	/** Where an incident shows, and which fault block marks it. */
	static Cell cellFor(ShipIncident incident) {
		Site site = SITES.get(incident.subsystem());
		return switch (incident.type()) {
			case SHORT_CIRCUIT, OVERHEAT -> site.wall();
			case LEAK, PRESSURE_LOSS -> site.pipe();
			case FIRE -> site.floor();
		};
	}

	static Block blockFor(ShipIncidentType type) {
		return switch (type) {
			case SHORT_CIRCUIT -> ModBlocks.SPARKING_PANEL;
			case OVERHEAT -> ModBlocks.STEAM_VENT;
			case LEAK, PRESSURE_LOSS -> ModBlocks.LEAKING_PIPE;
			case FIRE -> ModBlocks.SHIP_FIRE;
		};
	}

	/** The tool that fixes each kind of fault (a repair kit always works too). */
	static Item toolFor(FaultBlock.Kind kind) {
		return switch (kind) {
			case SPARKS -> ModItems.SOLDERING_IRON;
			case STEAM -> ModItems.MULTITOOL;
			case LEAK -> ModItems.SEALANT;
			case FIRE -> ModItems.FIRE_EXTINGUISHER;
		};
	}

	/** Every half second: put the fault blocks where incidents are, and the ship's parts back where they are gone. */
	public static void tick(MinecraftServer server, ShipState state, long now) {
		if (now % 10 != 0) {
			return;
		}
		ServerLevel interior = server.getLevel(ShipDimensions.INTERIOR);
		if (interior == null || interior.players().isEmpty() && state.hazards().incidents().isEmpty()) {
			return;
		}
		Map<BlockPos, Block> wanted = new java.util.HashMap<>();
		for (ShipIncident incident : state.hazards().incidents()) {
			wanted.put(ShipInteriorBuilder.at(cellFor(incident)), blockFor(incident.type()));
		}
		for (Site site : SITES.values()) {
			for (Cell cell : new Cell[] {site.wall(), site.floor(), site.pipe()}) {
				BlockPos pos = ShipInteriorBuilder.at(cell);
				BlockState have = interior.getBlockState(pos);
				Block want = wanted.get(pos);
				if (want != null) {
					if (!have.is(want) && interior.getBlockEntity(pos) == null) {
						interior.setBlock(pos, want.defaultBlockState(), Block.UPDATE_ALL);
					}
				} else if (have.getBlock() instanceof FaultBlock) {
					restore(interior, cell, pos);
				}
			}
		}
	}

	private static void restore(ServerLevel interior, Cell cell, BlockPos pos) {
		ShipInteriorLayout.Piece piece = ShipInteriorLayout.capsule().placements().stream()
				.filter(p -> p.cell().equals(cell)).map(ShipInteriorLayout.Placement::piece).findFirst()
				.orElse(ShipInteriorLayout.Piece.AIR);
		BlockState state = switch (piece) {
			case PIPE -> ((ConduitBlock) ModBlocks.PIPE).connected(interior, pos);
			case CABLE -> ((ConduitBlock) ModBlocks.CABLE).connected(interior, pos);
			case HULL_PANEL -> ModBlocks.RUSTED_PANEL.defaultBlockState();
			default -> Blocks.AIR.defaultBlockState();
		};
		interior.setBlock(pos, state, Block.UPDATE_ALL);
	}

	/** A crew member works on a fault block with whatever is in their hand. */
	public static void repairAt(MinecraftServer server, ServerPlayer player, BlockPos pos, FaultBlock.Kind kind,
			ItemStack stack) {
		ShipState state = ShipStateAccess.get(server);
		Optional<ShipIncident> incident = state.hazards().incidents().stream()
				.filter(i -> ShipInteriorBuilder.at(cellFor(i)).equals(pos)).findFirst();
		if (incident.isEmpty()) {
			return; // stale: the next tick puts the ship's part back
		}
		Item tool = toolFor(kind);
		boolean kit = stack.is(ModItems.REPAIR_KIT);
		boolean water = kind == FaultBlock.Kind.FIRE && stack.is(Items.WATER_BUCKET);
		if (!kit && !stack.is(tool) && !water) {
			player.sendSystemMessage(Component.translatable("message.intersolarsystem.fault.needs",
					Component.translatable(ShipCommands.incidentKey(incident.get().type())),
					Component.translatable(tool.getDescriptionId()), Component.translatable(ModItems.REPAIR_KIT
							.getDescriptionId())));
			return;
		}
		if (kind == FaultBlock.Kind.FIRE) {
			// A fire is put out on the spot, no minigame.
			if (state.resolveIncident(incident.get())) {
				if (stack.is(ModItems.FIRE_EXTINGUISHER)) {
					stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
				} else if (water) {
					player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
				} else {
					stack.consume(1, player);
				}
				ServerLevel level = (ServerLevel) player.level();
				level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.4,
						0.4, 0.4, 0.05);
				level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
				player.sendSystemMessage(Component.translatable("message.intersolarsystem.ship.repair_incident",
						Component.translatable(ShipCommands.incidentKey(incident.get().type())),
						Component.translatable(ShipCommands.subsystemKey(incident.get().subsystem()))));
			}
			return;
		}
		Optional<RepairMinigame> game = RepairMinigame.forIncident(incident.get().type());
		if (game.isEmpty()) {
			return;
		}
		ShipRepairBoards.open(server, player, game.get(), incident.get(), ShipRepair.hasMultitool(player),
				kit ? ModItems.REPAIR_KIT : tool);
	}
}
