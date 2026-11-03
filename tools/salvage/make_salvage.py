"""What everything is made of: the salvage tables (single source of truth).

* SALVAGE: what the recycler turns an item into. Outputs are (item, count) at 100% yield; the machine's yield (about
  55-75% for a bare recycler, close to 100% with modules) scales them. "rare" outputs only come out with a sorter
  module. Later planets and moons add their own rows here (their materials, their machines).
* DISASSEMBLY: placed props, wrecks and machines that are taken apart where they stand, step by step with a tool.
  Each step gives its parts; the block is gone after the last one.

    python3 tools/salvage/make_salvage.py
writes src/main/java/hu/kriwdi/intersolarsystem/salvage/SalvageTable.java and DisassemblyTable.java.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "src/main/java/hu/kriwdi/intersolarsystem/salvage"

M = "intersolarsystem:"
V = "minecraft:"


def n(i):
    return i if ":" in i else M + i


# ---- components -> raw materials
COMPONENTS = {
    "scrap_metal": [("minecraft:iron_nugget", 6)],
    "salvaged_wiring": [("minecraft:copper_ingot", 1), ("plastic_scrap", 1)],
    "salvaged_circuit": [("silicon", 1), ("minecraft:gold_nugget", 2), ("minecraft:copper_ingot", 1),
                         ("rare:rare_earth_dust", 1)],
    "plastic_scrap": [("bitumen", 1)],
    "rubber_scrap": [("bitumen", 1), ("minecraft:string", 1)],
    "glass_shards": [("minecraft:glass", 1)],
    "battery_cell": [("lithium_dust", 2), ("aluminium_ingot", 1), ("scrap_metal", 1)],
    "solar_cell": [("silicon", 2), ("minecraft:glass", 1), ("rare:rare_earth_dust", 1)],
    "rare_earth_magnet": [("rare_earth_dust", 3), ("minecraft:iron_nugget", 3)],
    "titanium_plate": [("titanium_ingot", 2)],
    "repair_kit": [("scrap_metal", 2), ("salvaged_wiring", 1)],
    "multitool": [("scrap_metal", 3), ("rubber_scrap", 1)],
    "soldering_iron": [("scrap_metal", 1), ("salvaged_wiring", 1), ("plastic_scrap", 1)],
    "fire_extinguisher": [("scrap_metal", 3), ("rubber_scrap", 1)],
    "air_filter": [("plastic_scrap", 2), ("minecraft:white_wool", 1)],
    "cutting_torch": [("scrap_metal", 3), ("salvaged_wiring", 1)],
    "pipe": [("minecraft:copper_ingot", 1)],
    "cable": [("salvaged_wiring", 1)],
    "space_helmet": [("titanium_plate", 1), ("glass_shards", 2), ("rubber_scrap", 2)],
    "space_suit": [("titanium_plate", 2), ("rubber_scrap", 4), ("minecraft:white_wool", 4), ("battery_cell", 1)],
    "space_pants": [("rubber_scrap", 3), ("minecraft:white_wool", 3)],
    "space_boots": [("rubber_scrap", 2), ("minecraft:white_wool", 1)],
}

# ---- vanilla things found in the ruins
VANILLA = {
    "minecraft:iron_ingot": [("minecraft:iron_nugget", 9)],
    "minecraft:bucket": [("minecraft:iron_ingot", 3)],
    "minecraft:rail": [("minecraft:iron_nugget", 3)],
    "minecraft:iron_bars": [("minecraft:iron_nugget", 3)],
    "minecraft:iron_trapdoor": [("minecraft:iron_ingot", 4)],
    "minecraft:iron_door": [("minecraft:iron_ingot", 2)],
    "minecraft:chain": [("minecraft:iron_nugget", 2)],
    "minecraft:lantern": [("minecraft:iron_nugget", 6)],
    "minecraft:iron_sword": [("minecraft:iron_ingot", 2)],
    "minecraft:iron_pickaxe": [("minecraft:iron_ingot", 3)],
    "minecraft:iron_axe": [("minecraft:iron_ingot", 3)],
    "minecraft:iron_shovel": [("minecraft:iron_ingot", 1)],
    "minecraft:iron_hoe": [("minecraft:iron_ingot", 2)],
    "minecraft:iron_helmet": [("minecraft:iron_ingot", 5)],
    "minecraft:iron_chestplate": [("minecraft:iron_ingot", 8)],
    "minecraft:iron_leggings": [("minecraft:iron_ingot", 7)],
    "minecraft:iron_boots": [("minecraft:iron_ingot", 4)],
    "minecraft:shears": [("minecraft:iron_ingot", 2)],
    "minecraft:compass": [("minecraft:iron_ingot", 4), ("minecraft:redstone", 1)],
    "minecraft:clock": [("minecraft:gold_ingot", 4), ("minecraft:redstone", 1)],
    "minecraft:crossbow": [("minecraft:iron_ingot", 1), ("minecraft:string", 2), ("minecraft:stick", 3)],
    "minecraft:redstone_lamp": [("minecraft:redstone", 4), ("minecraft:glowstone_dust", 4)],
    "minecraft:glass_bottle": [("glass_shards", 1)],
    "minecraft:glass": [("glass_shards", 2)],
    "minecraft:glass_pane": [("glass_shards", 1)],
    "minecraft:tinted_glass": [("glass_shards", 2), ("minecraft:amethyst_shard", 1)],
    "minecraft:copper_block": [("minecraft:copper_ingot", 9)],
    "minecraft:iron_block": [("minecraft:iron_ingot", 9)],
    "minecraft:tnt": [("minecraft:gunpowder", 5), ("minecraft:sand", 4)],
}

# ---- blocks of the dead Earth (as items, when they are fed into the recycler)
BLOCKS = {
    "rusted_panel": [("scrap_metal", 2)],
    "station_hull": [("aluminium_ingot", 1), ("scrap_metal", 1)],
    "station_foil": [("aluminium_ingot", 1), ("rare:minecraft:gold_nugget", 1)],
    "station_window": [("glass_shards", 2), ("aluminium_ingot", 1)],
    "station_glass": [("glass_shards", 2)],
    "station_light": [("glass_shards", 1), ("salvaged_wiring", 1), ("rare:minecraft:glowstone_dust", 1)],
    "truss": [("aluminium_ingot", 1), ("scrap_metal", 1)],
    "solar_panel": [("solar_cell", 1), ("aluminium_ingot", 1), ("salvaged_wiring", 1)],
    "antenna_dish": [("aluminium_ingot", 2), ("salvaged_circuit", 1)],
    "data_terminal": [("salvaged_circuit", 2), ("glass_shards", 2), ("scrap_metal", 2), ("plastic_scrap", 1)],
    "scrap_pile": [("scrap_metal", 2), ("salvaged_wiring", 1)],
    "mining_drone": [("salvaged_circuit", 2), ("salvaged_wiring", 2), ("scrap_metal", 4), ("battery_cell", 1)],
    "lander_engine": [("titanium_plate", 2), ("scrap_metal", 4), ("rare:rare_earth_magnet", 1)],
    "toxic_barrel": [("scrap_metal", 2)],
    "radio_set": [("salvaged_circuit", 1), ("salvaged_wiring", 2), ("plastic_scrap", 2), ("rare:rare_earth_magnet", 1)],
    "office_desk": [("minecraft:oak_planks", 3), ("scrap_metal", 1), ("salvaged_circuit", 1), ("glass_shards", 1)],
    "office_chair": [("plastic_scrap", 2), ("scrap_metal", 1), ("minecraft:white_wool", 1)],
    "filing_cabinet": [("scrap_metal", 4), ("minecraft:paper", 6)],
    "server_rack": [("salvaged_circuit", 4), ("salvaged_wiring", 3), ("scrap_metal", 4), ("rare:minecraft:gold_nugget", 3)],
    "locker": [("scrap_metal", 4)],
    "hospital_bed": [("scrap_metal", 3), ("minecraft:white_wool", 2), ("rubber_scrap", 1)],
    "iv_stand": [("scrap_metal", 1), ("plastic_scrap", 1)],
    "medical_cabinet": [("scrap_metal", 2), ("glass_shards", 2), ("rare:minecraft:glistering_melon_slice", 1)],
    "wheelchair": [("scrap_metal", 2), ("rubber_scrap", 2)],
    "vending_machine": [("scrap_metal", 5), ("glass_shards", 3), ("salvaged_circuit", 1), ("salvaged_wiring", 2)],
    "airport_seats": [("scrap_metal", 3), ("plastic_scrap", 3)],
    "departure_board": [("salvaged_circuit", 2), ("salvaged_wiring", 2), ("scrap_metal", 2), ("rare:minecraft:glowstone_dust", 2)],
    "traffic_light": [("scrap_metal", 3), ("glass_shards", 3), ("salvaged_wiring", 2)],
    "street_lamp": [("scrap_metal", 3), ("glass_shards", 1), ("salvaged_wiring", 1)],
    "fire_hydrant": [("scrap_metal", 3)],
    "jet_engine": [("titanium_plate", 4), ("aluminium_ingot", 4), ("scrap_metal", 6), ("rare:rare_earth_magnet", 2)],
    "car_wreck_red": [("scrap_metal", 8), ("rubber_scrap", 4), ("glass_shards", 3), ("battery_cell", 1)],
    "car_wreck_blue": [("scrap_metal", 8), ("rubber_scrap", 4), ("glass_shards", 3), ("battery_cell", 1)],
    "car_wreck_white": [("scrap_metal", 8), ("rubber_scrap", 4), ("glass_shards", 3), ("battery_cell", 1)],
    "taxi_wreck": [("scrap_metal", 8), ("rubber_scrap", 4), ("glass_shards", 3), ("battery_cell", 1)],
    "police_car_wreck": [("scrap_metal", 8), ("rubber_scrap", 4), ("salvaged_circuit", 1), ("battery_cell", 1)],
    "ambulance_wreck": [("scrap_metal", 9), ("rubber_scrap", 4), ("rare:minecraft:golden_apple", 1), ("battery_cell", 1)],
    "cryo_pod": [("scrap_metal", 4), ("glass_shards", 3), ("salvaged_circuit", 2), ("rare:battery_cell", 1)],
    "lab_bench": [("scrap_metal", 3), ("glass_shards", 2), ("rare:minecraft:amethyst_shard", 1)],
    "fume_hood": [("scrap_metal", 4), ("glass_shards", 2), ("salvaged_wiring", 1)],
    "specimen_tank": [("glass_shards", 4), ("scrap_metal", 2)],
    "bunk_bed": [("scrap_metal", 3), ("minecraft:white_wool", 2)],
    "diesel_generator": [("scrap_metal", 6), ("salvaged_wiring", 3), ("salvaged_circuit", 1)],
    "blast_door": [("titanium_plate", 2), ("scrap_metal", 8)],
    "metal_grating": [("scrap_metal", 1)],
    "ceiling_light": [("glass_shards", 1), ("salvaged_wiring", 1)],
    "curtain_wall": [("glass_shards", 2), ("aluminium_ingot", 1)],
    "broken_curtain_wall": [("glass_shards", 1), ("aluminium_ingot", 1)],
    "shipping_crate": [("minecraft:oak_planks", 4)],
    "sandbags": [("minecraft:sand", 3)],
    "barbed_wire": [("minecraft:iron_nugget", 4)],
    "quarantine_barrier": [("plastic_scrap", 2), ("scrap_metal", 1)],
    "warning_sign": [("scrap_metal", 1)],
    "runway_light": [("glass_shards", 1), ("salvaged_wiring", 1)],
    "runway_bollard": [("plastic_scrap", 2)],
    "park_bench": [("minecraft:oak_planks", 2), ("scrap_metal", 1)],
}

SALVAGE = {**COMPONENTS, **VANILLA, **BLOCKS}

# ---- step-by-step disassembly of placed props: (tool, description key, [(item, count)])
W, C = "multitool", "cutting_torch"


def car(extra):
    return [(W, "wheels", [("rubber_scrap", 4), ("scrap_metal", 2)]),
            (W, "battery", [("battery_cell", 1), ("salvaged_wiring", 2)]),
            (C, "body", [("scrap_metal", 6), ("glass_shards", 3)]),
            (W, "engine", [("scrap_metal", 4), ("salvaged_wiring", 1)] + extra)]


DISASSEMBLY = {
    "car_wreck_red": car([]), "car_wreck_blue": car([]), "car_wreck_white": car([]), "taxi_wreck": car([]),
    "police_car_wreck": car([("salvaged_circuit", 1)]), "ambulance_wreck": car([("minecraft:glistering_melon_slice", 1)]),
    "jet_engine": [(W, "cowling", [("aluminium_ingot", 3)]), (C, "fan", [("titanium_plate", 2), ("scrap_metal", 3)]),
                   (W, "core", [("titanium_plate", 2), ("rare_earth_magnet", 1), ("salvaged_wiring", 2)])],
    "server_rack": [(W, "panels", [("scrap_metal", 2)]), (W, "boards", [("salvaged_circuit", 3)]),
                    (C, "frame", [("scrap_metal", 2), ("salvaged_wiring", 3)])],
    "vending_machine": [(W, "front", [("glass_shards", 3), ("plastic_scrap", 1)]),
                        (W, "electronics", [("salvaged_circuit", 1), ("salvaged_wiring", 2)]),
                        (C, "cabinet", [("scrap_metal", 5)])],
    "diesel_generator": [(W, "panel", [("salvaged_circuit", 1)]), (W, "wiring", [("salvaged_wiring", 3)]),
                         (C, "engine", [("scrap_metal", 6)])],
    "departure_board": [(W, "display", [("salvaged_circuit", 2), ("minecraft:glowstone_dust", 1)]),
                        (C, "frame", [("scrap_metal", 2), ("salvaged_wiring", 2)])],
    "cryo_pod": [(W, "control", [("salvaged_circuit", 2)]), (W, "canopy", [("glass_shards", 3)]),
                 (C, "shell", [("scrap_metal", 4), ("battery_cell", 1)])],
    "data_terminal": [(W, "screen", [("glass_shards", 2), ("plastic_scrap", 1)]),
                      (W, "board", [("salvaged_circuit", 2)]), (C, "case", [("scrap_metal", 2)])],
    "mining_drone": [(W, "cells", [("battery_cell", 1)]), (W, "electronics", [("salvaged_circuit", 2), ("salvaged_wiring", 2)]),
                     (C, "chassis", [("scrap_metal", 4)])],
    "lander_engine": [(W, "lines", [("salvaged_wiring", 1)]), (C, "bell", [("titanium_plate", 2), ("scrap_metal", 4)])],
    "traffic_light": [(W, "lamps", [("glass_shards", 3), ("salvaged_wiring", 2)]), (C, "pole", [("scrap_metal", 3)])],
    "street_lamp": [(W, "lamp", [("glass_shards", 1), ("salvaged_wiring", 1)]), (C, "pole", [("scrap_metal", 3)])],
    "radio_set": [(W, "case", [("plastic_scrap", 2)]), (W, "boards", [("salvaged_circuit", 1), ("salvaged_wiring", 2)])],
    "fume_hood": [(W, "sash", [("glass_shards", 2)]), (C, "cabinet", [("scrap_metal", 4), ("salvaged_wiring", 1)])],
    "medical_cabinet": [(W, "doors", [("glass_shards", 2)]), (C, "cabinet", [("scrap_metal", 2)])],
    "hospital_bed": [(W, "mattress", [("minecraft:white_wool", 2)]), (C, "frame", [("scrap_metal", 3), ("rubber_scrap", 1)])],
    "blast_door": [(W, "bolts", [("scrap_metal", 2)]), (C, "leaf", [("titanium_plate", 2), ("scrap_metal", 6)])],
    "antenna_dish": [(W, "feed", [("salvaged_circuit", 1)]), (C, "dish", [("aluminium_ingot", 2)])],
    "solar_panel": [(W, "cell", [("solar_cell", 1)]), (W, "frame", [("aluminium_ingot", 1), ("salvaged_wiring", 1)])],
    "airport_seats": [(W, "seats", [("plastic_scrap", 3)]), (C, "beam", [("scrap_metal", 3)])],
    "locker": [(C, "locker", [("scrap_metal", 4)])],
    "filing_cabinet": [(W, "drawers", [("minecraft:paper", 6)]), (C, "cabinet", [("scrap_metal", 4)])],
}

STEP_TEXT = {
    "wheels": ("Unbolt the wheels", "Räder abschrauben", "Kerekek lecsavarozása"),
    "battery": ("Pull the battery", "Batterie ausbauen", "Akkumulátor kiszerelése"),
    "body": ("Cut up the body", "Karosserie zerschneiden", "Karosszéria szétvágása"),
    "engine": ("Strip the engine", "Motor zerlegen", "Motor szétszedése"),
    "cowling": ("Unbolt the cowling", "Verkleidung abschrauben", "Burkolat lecsavarozása"),
    "fan": ("Cut out the fan", "Fan herausschneiden", "Ventilátor kivágása"),
    "core": ("Strip the core", "Kerntriebwerk zerlegen", "Mag szétszedése"),
    "panels": ("Take off the panels", "Abdeckungen abnehmen", "Burkolatlemezek leszedése"),
    "boards": ("Pull the boards", "Platinen ziehen", "Áramköri lapok kihúzása"),
    "frame": ("Cut up the frame", "Rahmen zerschneiden", "Váz szétvágása"),
    "front": ("Remove the front", "Front abnehmen", "Előlap leszedése"),
    "electronics": ("Pull the electronics", "Elektronik ausbauen", "Elektronika kiszerelése"),
    "cabinet": ("Cut up the cabinet", "Gehäuse zerschneiden", "Szekrény szétvágása"),
    "panel": ("Remove the control panel", "Bedienfeld ausbauen", "Kezelőpanel kiszerelése"),
    "wiring": ("Pull the wiring", "Verkabelung ziehen", "Vezetékek kihúzása"),
    "display": ("Remove the display", "Anzeige ausbauen", "Kijelző kiszerelése"),
    "control": ("Remove the control unit", "Steuereinheit ausbauen", "Vezérlőegység kiszerelése"),
    "canopy": ("Lift off the canopy", "Haube abnehmen", "Fedél leemelése"),
    "shell": ("Cut up the shell", "Hülle zerschneiden", "Héj szétvágása"),
    "screen": ("Remove the screen", "Bildschirm ausbauen", "Képernyő kiszerelése"),
    "board": ("Pull the main board", "Hauptplatine ziehen", "Alaplap kihúzása"),
    "case": ("Open the case", "Gehäuse öffnen", "Ház szétszedése"),
    "cells": ("Pull the cells", "Zellen ausbauen", "Cellák kiszerelése"),
    "chassis": ("Cut up the chassis", "Fahrgestell zerschneiden", "Alváz szétvágása"),
    "lines": ("Disconnect the lines", "Leitungen trennen", "Vezetékek leválasztása"),
    "bell": ("Cut up the nozzle", "Düse zerschneiden", "Fúvóka szétvágása"),
    "lamps": ("Take out the lamps", "Lampen ausbauen", "Lámpák kiszerelése"),
    "pole": ("Cut down the pole", "Mast absägen", "Oszlop levágása"),
    "lamp": ("Take out the lamp", "Lampe ausbauen", "Lámpa kiszerelése"),
    "sash": ("Lift out the sash", "Frontscheibe ausbauen", "Tolóablak kiemelése"),
    "doors": ("Take off the doors", "Türen abnehmen", "Ajtók leszedése"),
    "mattress": ("Take off the mattress", "Matratze abnehmen", "Matrac leszedése"),
    "bolts": ("Undo the bolts", "Bolzen lösen", "Csavarok kioldása"),
    "leaf": ("Cut up the door leaf", "Türblatt zerschneiden", "Ajtólap szétvágása"),
    "feed": ("Remove the feed horn", "Speisehorn ausbauen", "Tápfej kiszerelése"),
    "dish": ("Cut up the dish", "Schüssel zerschneiden", "Tányér szétvágása"),
    "cell": ("Lift out the cell", "Zelle ausbauen", "Cella kiemelése"),
    "seats": ("Unbolt the seats", "Sitze abschrauben", "Ülések lecsavarozása"),
    "beam": ("Cut the beam", "Träger schneiden", "Tartógerenda elvágása"),
    "locker": ("Cut open the locker", "Spind aufschneiden", "Szekrény felvágása"),
    "drawers": ("Empty the drawers", "Schubladen leeren", "Fiókok kiürítése"),
}


def java_string(s):
    return '"' + s.replace('\\', '\\\\').replace('"', '\\"') + '"'


def write_tables():
    OUT.mkdir(parents=True, exist_ok=True)
    rows = []
    for item, outs in SALVAGE.items():
        parts = []
        for out, count in outs:
            rare = out.startswith("rare:")
            out = out[5:] if rare else out
            parts.append(f"o({java_string(n(out))}, {count}, {str(rare).lower()})")
        rows.append(f"\t\tadd({java_string(n(item))}, {', '.join(parts)});")
    (OUT / "SalvageTable.java").write_text('''package hu.kriwdi.intersolarsystem.salvage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What the recycler turns things into, at 100% yield. GENERATED by tools/salvage/make_salvage.py: edit the table
 * there. Item ids are resolved when used, so rows for content that is not registered (yet) are simply skipped.
 */
public final class SalvageTable {
	/** One output: {@code rare} ones only come out of a recycler with a sorter module. */
	public record Output(String item, int count, boolean rare) {
	}

	private static final Map<String, List<Output>> TABLE = new HashMap<>();

	static {
''' + "\n".join(rows) + '''
	}

	private SalvageTable() {
	}

	private static Output o(String item, int count, boolean rare) {
		return new Output(item, count, rare);
	}

	private static void add(String item, Output... outputs) {
		TABLE.put(item, List.of(outputs));
	}

	/** The outputs for an item id, or null if it cannot be salvaged. */
	public static List<Output> get(String itemId) {
		return TABLE.get(itemId);
	}
}
''')
    rows = []
    for block, steps in DISASSEMBLY.items():
        parts = []
        for tool, key, drops in steps:
            d = ", ".join(f"o({java_string(n(i))}, {c}, false)" for i, c in drops)
            parts.append(f"s({java_string(n(tool))}, {java_string(key)}, {d})")
        rows.append(f"\t\tadd({java_string(n(block))},\n\t\t\t\t{(','+chr(10)+chr(9)+chr(9)+chr(9)+chr(9)).join(parts)});")
    (OUT / "DisassemblyTable.java").write_text('''package hu.kriwdi.intersolarsystem.salvage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.kriwdi.intersolarsystem.salvage.SalvageTable.Output;

/**
 * Placed props, wrecks and machines taken apart step by step where they stand. GENERATED by
 * tools/salvage/make_salvage.py: edit the table there.
 */
public final class DisassemblyTable {
	/** One step: the tool it needs, its text key (salvage.intersolarsystem.step.&lt;key&gt;) and the parts it gives. */
	public record Step(String tool, String key, List<Output> parts) {
	}

	private static final Map<String, List<Step>> TABLE = new HashMap<>();

	static {
''' + "\n".join(rows) + '''
	}

	private DisassemblyTable() {
	}

	private static Output o(String item, int count, boolean rare) {
		return new Output(item, count, rare);
	}

	private static Step s(String tool, String key, Output... parts) {
		return new Step(tool, key, List.of(parts));
	}

	private static void add(String block, Step... steps) {
		TABLE.put(block, List.of(steps));
	}

	/** The steps for a block id, or null if it is not taken apart this way. */
	public static List<Step> get(String blockId) {
		return TABLE.get(blockId);
	}
}
''')
    print(len(SALVAGE), "salvage rows,", len(DISASSEMBLY), "disassembly entries")


def lang_rows():
    """key|en|de|hu rows for the step texts (fed to tools/add_lang_rows.py when new)."""
    return [f"salvage.intersolarsystem.step.{k}|{en}|{de}|{hu}" for k, (en, de, hu) in STEP_TEXT.items()]


if __name__ == "__main__":
    write_tables()
    missing = {k for steps in DISASSEMBLY.values() for _, k, _ in steps} - set(STEP_TEXT)
    assert not missing, missing
    import sys
    if "--lang" in sys.argv:
        print("\n".join(lang_rows()))
