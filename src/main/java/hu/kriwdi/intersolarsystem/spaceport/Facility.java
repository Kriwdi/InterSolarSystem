package hu.kriwdi.intersolarsystem.spaceport;

import java.util.List;

import hu.kriwdi.intersolarsystem.ship.RepairMinigame;

/**
 * The five facilities of a Kármán spaceport, restored in this order. Each needs materials delivered to its control
 * cabinet, the debris around the cabinet cleared, and a calibration (a repair board) once everything else is done.
 * Item ids are resolved when used, so later content (other worlds' materials) can be listed here as well.
 */
public enum Facility {
	POWER("power", RepairMinigame.PRESSURE, 5, List.of(
			new Need("intersolarsystem:cable", 16), new Need("intersolarsystem:salvaged_circuit", 4),
			new Need("intersolarsystem:titanium_plate", 6), new Need("intersolarsystem:uranium_fuel_rod", 2))),
	CONTROL("control", RepairMinigame.WIRING, 6, List.of(
			new Need("intersolarsystem:salvaged_circuit", 8), new Need("intersolarsystem:salvaged_wiring", 8),
			new Need("intersolarsystem:glass_shards", 8), new Need("intersolarsystem:battery_cell", 2))),
	FABRICATION("fabrication", RepairMinigame.VALVE, 4, List.of(
			new Need("intersolarsystem:titanium_plate", 8), new Need("intersolarsystem:rare_earth_magnet", 4),
			new Need("intersolarsystem:salvaged_circuit", 6), new Need("minecraft:iron_ingot", 16))),
	FUEL("fuel", RepairMinigame.PRESSURE, 5, List.of(
			new Need("intersolarsystem:pipe", 16), new Need("intersolarsystem:scrap_metal", 16),
			new Need("intersolarsystem:aluminium_ingot", 8), new Need("intersolarsystem:salvaged_circuit", 2))),
	CRANE("crane", RepairMinigame.WIRING, 6, List.of(
			new Need("intersolarsystem:cable", 8), new Need("intersolarsystem:titanium_plate", 8),
			new Need("intersolarsystem:battery_cell", 4), new Need("intersolarsystem:rare_earth_magnet", 2)));

	/** One material: an item id and how many. */
	public record Need(String item, int count) {
	}

	private final String id;
	private final RepairMinigame calibration;
	private final int boardSize;
	private final List<Need> needs;

	Facility(String id, RepairMinigame calibration, int boardSize, List<Need> needs) {
		this.id = id;
		this.calibration = calibration;
		this.boardSize = boardSize;
		this.needs = needs;
	}

	public String id() {
		return id;
	}

	public RepairMinigame calibration() {
		return calibration;
	}

	public int boardSize() {
		return boardSize;
	}

	public List<Need> needs() {
		return needs;
	}

	/** The facility that must be online first (null for the power plant). */
	public Facility previous() {
		return ordinal() == 0 ? null : values()[ordinal() - 1];
	}
}
