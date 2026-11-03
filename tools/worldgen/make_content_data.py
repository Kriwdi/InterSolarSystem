"""Data of the dead Earth content (phase 7): block loot, recipes and tags.

    python3 tools/worldgen/make_content_data.py
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data"
NS = "intersolarsystem:"


def write(rel, data):
    path = DATA / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


# ---------------------------------------------------------------- loot
def drop_self(block):
    write(f"intersolarsystem/loot_table/blocks/{block}.json", {
        "type": "minecraft:block",
        "pools": [{"condition": {"type": "minecraft:survives_explosion"},
                   "entries": [{"type": "minecraft:item", "name": NS + block}], "rolls": 1}],
        "random_sequence": f"intersolarsystem:blocks/{block}"})


def drop_ore(block, item, lo=1, hi=1):
    """Silk touch drops the ore itself; otherwise the raw material, with fortune (like vanilla iron or redstone)."""
    if (lo, hi) == (1, 1):
        mods = [{"type": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"}]
    else:
        mods = [{"type": "minecraft:set_count",
                 "count": {"type": "minecraft:uniform", "min": float(lo), "max": float(hi)}},
                {"type": "minecraft:apply_bonus", "enchantment": "minecraft:fortune",
                 "formula": "minecraft:uniform_bonus_count", "parameters": {"bonusMultiplier": 1}}]
    mods.append({"type": "minecraft:explosion_decay"})
    write(f"intersolarsystem/loot_table/blocks/{block}.json", {
        "type": "minecraft:block",
        "pools": [{"entries": [{"type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "condition": "minecraft:tool/can_silk_touch", "name": NS + block},
            {"type": "minecraft:item", "name": NS + item, "modifier": mods}]}], "rolls": 1}],
        "random_sequence": f"intersolarsystem:blocks/{block}"})


def drop_items(block, entries):
    """Debris: drops salvage, never itself."""
    pools = []
    for item, lo, hi, chance in entries:
        pool = {"entries": [{"type": "minecraft:item", "name": item if ":" in item else NS + item,
                             "modifier": [{"type": "minecraft:set_count",
                                           "count": {"type": "minecraft:uniform", "min": float(lo),
                                                     "max": float(hi)}},
                                          {"type": "minecraft:explosion_decay"}]}], "rolls": 1}
        if chance < 1:
            pool["condition"] = {"type": "minecraft:random_chance", "chance": chance}
        pools.append(pool)
    write(f"intersolarsystem/loot_table/blocks/{block}.json", {
        "type": "minecraft:block", "pools": pools, "random_sequence": f"intersolarsystem:blocks/{block}"})


SELF = ["ash", "cinder_rock", "dried_silt", "salt_crust", "salt_block", "irradiated_soil", "trinitite",
        "acidic_mud", "compacted_junk", "tar_sand", "silica_sand", "petrified_log", "petrified_wood", "dead_log",
        "toxic_barrel", "tar_puddle", "lander_engine", "lunar_flag", "rover_wheel", "antenna_dish", "data_terminal"]


def loot():
    for b in SELF:
        drop_self(b)
    for prefix in ("", "deepslate_"):
        drop_ore(prefix + "bauxite_ore", "raw_bauxite")
        drop_ore(prefix + "titanium_ore", "raw_titanium")
        drop_ore(prefix + "uraninite_ore", "raw_uranium")
        drop_ore(prefix + "spodumene_ore", "lithium_dust", 2, 4)
        drop_ore(prefix + "monazite_ore", "rare_earth_dust", 1, 3)
    drop_ore("oil_shale", "bitumen", 1, 2)
    drop_ore("lithium_brine_crust", "lithium_dust", 1, 2)
    drop_items("scrap_pile", [("scrap_metal", 1, 3, 1), ("salvaged_wiring", 1, 1, 0.3)])
    drop_items("rubble", [("minecraft:gravel", 1, 1, 1), ("scrap_metal", 1, 1, 0.25)])
    drop_items("mining_drone", [("salvaged_circuit", 1, 2, 1), ("salvaged_wiring", 1, 3, 1), ("scrap_metal", 2, 4, 1),
                                ("battery_cell", 1, 1, 0.35)])


# ---------------------------------------------------------------- recipes
def cook(name, ingredient, result, xp=0.7, kinds=("smelting", "blasting")):
    for kind in kinds:
        write(f"intersolarsystem/recipe/{name}_from_{kind}.json", {
            "type": "minecraft:" + kind, "cookingtime": 200 if kind == "smelting" else 100, "experience": xp,
            "ingredient": ingredient if ":" in ingredient else NS + ingredient,
            "result": {"id": result if ":" in result else NS + result}})


def shaped(name, pattern, key, result, count=1, category="misc"):
    write(f"intersolarsystem/recipe/{name}.json", {
        "type": "minecraft:crafting_shaped", "category": category,
        "key": {k: (v if v.startswith("#") or ":" in v else NS + v) for k, v in key.items()},
        "pattern": pattern, "result": {"id": result if ":" in result else NS + result, "count": count}})


def shapeless(name, ingredients, result, count=1):
    write(f"intersolarsystem/recipe/{name}.json", {
        "type": "minecraft:crafting_shapeless", "category": "misc",
        "ingredients": [i if i.startswith("#") or ":" in i else NS + i for i in ingredients],
        "result": {"id": result if ":" in result else NS + result, "count": count}})


def recipes():
    cook("aluminium_ingot", "raw_bauxite", "aluminium_ingot")
    cook("titanium_ingot", "raw_titanium", "titanium_ingot", 1.0)
    cook("uranium_ingot", "raw_uranium", "uranium_ingot", 1.0)
    cook("silicon", "silica_sand", "silicon", 0.3)
    # Salvage is the only source of the old metals: no iron or copper ore generates on the dead Earth.
    cook("iron_nugget", "scrap_metal", "minecraft:iron_nugget", 0.1)
    cook("copper_ingot", "salvaged_wiring", "minecraft:copper_ingot", 0.2)
    cook("glass", "salt_crust", "minecraft:glass", 0.1, kinds=("smelting",))
    shapeless("spruce_planks_from_dead_log", ["dead_log"], "minecraft:spruce_planks", 4)
    shapeless("titanium_plate", ["titanium_ingot", "titanium_ingot"], "titanium_plate")
    shaped("solar_cell", ["GGG", "SSS", "AWA"],
           {"G": "minecraft:glass_pane", "S": "silicon", "A": "aluminium_ingot", "W": "salvaged_wiring"},
           "solar_cell", 2)
    shaped("battery_cell", [" C ", "ALA", "ALA"],
           {"C": "minecraft:copper_ingot", "A": "aluminium_ingot", "L": "lithium_dust"}, "battery_cell")
    shaped("rare_earth_magnet", ["R R", "RIR", " R "],
           {"R": "rare_earth_dust", "I": "minecraft:iron_ingot"}, "rare_earth_magnet", 2)
    shaped("uranium_fuel_rod", ["P", "U", "P"], {"P": "titanium_plate", "U": "uranium_ingot"}, "uranium_fuel_rod")
    shaped("ion_drive_kit", ["PMP", "BCB", "PSP"],
           {"P": "titanium_plate", "M": "rare_earth_magnet", "B": "battery_cell", "C": "salvaged_circuit",
            "S": "solar_cell"}, "ion_drive_kit")
    shaped("fusion_drive_kit", ["PHP", "RCR", "PMP"],
           {"P": "titanium_plate", "H": "helium3_regolith", "R": "uranium_fuel_rod", "C": "salvaged_circuit",
            "M": "rare_earth_magnet"}, "fusion_drive_kit")
    m = "#intersolarsystem:titanium_tool_materials"
    for tool, pattern in {"pickaxe": ["XXX", " # ", " # "], "axe": ["XX", "X#", " #"], "shovel": ["X", "#", "#"],
                          "hoe": ["XX", " #", " #"], "sword": ["X", "X", "#"]}.items():
        shaped("titanium_" + tool, pattern, {"X": m, "#": "minecraft:stick"}, "titanium_" + tool,
               category="equipment")


# ---------------------------------------------------------------- tags
def tags():
    write("intersolarsystem/tags/item/titanium_tool_materials.json", {"values": [NS + "titanium_ingot"]})
    write("intersolarsystem/tags/block/incorrect_for_titanium_tool.json", {"values": []})
    pick = ["cinder_rock", "salt_block", "trinitite", "compacted_junk", "oil_shale", "petrified_log",
            "petrified_wood", "scrap_pile", "rubble", "toxic_barrel", "mining_drone"]
    ores = [p + o for p in ("", "deepslate_") for o in ("bauxite_ore", "titanium_ore", "uraninite_ore",
                                                       "spodumene_ore", "monazite_ore")]
    shovel = ["ash", "dried_silt", "salt_crust", "lithium_brine_crust", "irradiated_soil", "acidic_mud", "tar_sand",
              "silica_sand", "tar_puddle"]
    for tool, extra in (("pickaxe", pick + ores), ("shovel", shovel)):
        path = DATA / f"minecraft/tags/block/mineable/{tool}.json"
        values = json.loads(path.read_text())["values"] if path.exists() else []
        for b in extra:
            if NS + b not in values:
                values.append(NS + b)
        write(f"minecraft/tags/block/mineable/{tool}.json", {"replace": False, "values": values})
    write("minecraft/tags/block/mineable/axe.json", {"replace": False, "values": [NS + "dead_log"]})
    write("minecraft/tags/block/needs_stone_tool.json", {"replace": False, "values": [
        NS + o for o in ores if "bauxite" in o or "spodumene" in o] + [NS + "oil_shale"]})
    write("minecraft/tags/block/needs_iron_tool.json", {"replace": False, "values": [
        NS + o for o in ores if "titanium" in o or "uraninite" in o or "monazite" in o]})
    write("minecraft/tags/block/logs_that_burn.json", {"replace": False, "values": [NS + "dead_log"]})
    write("minecraft/tags/item/logs_that_burn.json", {"replace": False, "values": [NS + "dead_log"]})
    write("minecraft/tags/item/pickaxes.json", {"replace": False, "values": [NS + "titanium_pickaxe"]})
    write("minecraft/tags/item/axes.json", {"replace": False, "values": [NS + "titanium_axe"]})
    write("minecraft/tags/item/shovels.json", {"replace": False, "values": [NS + "titanium_shovel"]})
    write("minecraft/tags/item/hoes.json", {"replace": False, "values": [NS + "titanium_hoe"]})
    write("minecraft/tags/item/swords.json", {"replace": False, "values": [NS + "titanium_sword"]})


if __name__ == "__main__":
    loot()
    recipes()
    tags()
    print("content data written")
