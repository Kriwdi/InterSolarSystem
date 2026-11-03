package hu.kriwdi.intersolarsystem.spaceport;

import java.util.List;

import hu.kriwdi.intersolarsystem.spaceport.Facility.Need;

/**
 * What the fabricator makes and the crane builds: the mothership's sections in building order, then the ships made
 * here. Earth materials build the basic ship; the engines, the reactor and the laboratory need the Moon's
 * (helium-3 regolith, ilmenite, mare basalt). Later worlds' sections and ships are added to this list.
 */
public enum Section {
	KEEL("keel", "intersolarsystem:keel_kit", true, List.of(new Need("intersolarsystem:titanium_plate", 24),
			new Need("intersolarsystem:truss", 16), new Need("minecraft:iron_block", 8))),
	HULL("hull", "intersolarsystem:hull_kit", true, List.of(new Need("intersolarsystem:aluminium_ingot", 48),
			new Need("intersolarsystem:titanium_plate", 16), new Need("intersolarsystem:station_glass", 16))),
	ENGINES("engines", "intersolarsystem:engine_kit", true, List.of(new Need("intersolarsystem:titanium_plate", 16),
			new Need("intersolarsystem:rare_earth_magnet", 8), new Need("intersolarsystem:helium3_regolith", 16),
			new Need("intersolarsystem:uranium_fuel_rod", 4))),
	REACTOR("reactor", "intersolarsystem:reactor_kit", true, List.of(new Need("intersolarsystem:uranium_fuel_rod", 8),
			new Need("intersolarsystem:ilmenite_ore", 8), new Need("intersolarsystem:titanium_plate", 12),
			new Need("intersolarsystem:salvaged_circuit", 8))),
	LIFE_SUPPORT("life_support", "intersolarsystem:life_support_kit", true, List.of(
			new Need("intersolarsystem:air_filter", 8), new Need("intersolarsystem:pipe", 32),
			new Need("intersolarsystem:battery_cell", 8), new Need("intersolarsystem:salvaged_circuit", 6))),
	HYDROPONICS("hydroponics", "intersolarsystem:hydroponics_kit", true, List.of(
			new Need("intersolarsystem:living_soil", 32), new Need("intersolarsystem:clean_water_canister", 8),
			new Need("intersolarsystem:solar_cell", 8), new Need("minecraft:glass", 32))),
	LABORATORY("laboratory", "intersolarsystem:laboratory_kit", true, List.of(
			new Need("intersolarsystem:salvaged_circuit", 12), new Need("intersolarsystem:lab_bench", 2),
			new Need("intersolarsystem:specimen_tank", 2), new Need("intersolarsystem:mare_basalt", 8))),
	QUARTERS("quarters", "intersolarsystem:quarters_kit", true, List.of(new Need("intersolarsystem:bunk_bed", 8),
			new Need("minecraft:white_wool", 32), new Need("intersolarsystem:vending_machine", 1),
			new Need("intersolarsystem:office_desk", 4))),
	HANGAR("hangar", "intersolarsystem:hangar_kit", true, List.of(new Need("intersolarsystem:titanium_plate", 16),
			new Need("intersolarsystem:cable", 32), new Need("intersolarsystem:crane_hook", 2),
			new Need("intersolarsystem:aluminium_ingot", 24))),
	BRIDGE("bridge", "intersolarsystem:bridge_kit", true, List.of(new Need("intersolarsystem:navigation_console", 1),
			new Need("intersolarsystem:diagnostic_console", 2), new Need("intersolarsystem:salvaged_circuit", 16),
			new Need("intersolarsystem:station_window", 16))),
	RESEARCH_SHIP("research_ship", "intersolarsystem:research_ship_kit", false, List.of(
			new Need("intersolarsystem:titanium_plate", 12), new Need("intersolarsystem:salvaged_circuit", 8),
			new Need("intersolarsystem:battery_cell", 6), new Need("intersolarsystem:repair_kit", 2)));

	private final String id;
	private final String kit;
	private final boolean mothership;
	private final List<Need> needs;

	Section(String id, String kit, boolean mothership, List<Need> needs) {
		this.id = id;
		this.kit = kit;
		this.mothership = mothership;
		this.needs = needs;
	}

	public String id() {
		return id;
	}

	public String kit() {
		return kit;
	}

	/** Part of the mothership (built by the crane in order); otherwise a ship of its own. */
	public boolean mothership() {
		return mothership;
	}

	public List<Need> needs() {
		return needs;
	}

	public static int mothershipSections() {
		int n = 0;
		for (Section s : values()) {
			if (s.mothership) {
				n++;
			}
		}
		return n;
	}
}
