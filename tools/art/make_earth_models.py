"""Models, blockstates and item definitions of the dead Earth content (phase 7).

    python3 tools/art/make_earth_models.py
"""
from make_models import ASSETS, NS, box, facing_blockstate, item_from_block, model, write

CUBES = ["ash", "cinder_rock", "dried_silt", "salt_crust", "salt_block", "lithium_brine_crust", "irradiated_soil",
         "trinitite", "acidic_mud", "compacted_junk", "tar_sand", "oil_shale", "silica_sand", "petrified_wood",
         "bauxite_ore", "deepslate_bauxite_ore", "titanium_ore", "deepslate_titanium_ore", "uraninite_ore",
         "deepslate_uraninite_ore", "spodumene_ore", "deepslate_spodumene_ore", "monazite_ore",
         "deepslate_monazite_ore"]
# Ground blocks that get random rotations so large areas do not tile visibly.
ROTATED = {"ash", "cinder_rock", "dried_silt", "salt_crust", "irradiated_soil", "acidic_mud", "tar_sand",
           "silica_sand", "lithium_brine_crust"}
PILLARS = ["petrified_log", "dead_log"]

ITEMS = ["raw_bauxite", "aluminium_ingot", "raw_titanium", "titanium_ingot", "raw_uranium", "uranium_ingot",
         "lithium_dust", "rare_earth_dust", "silicon", "bitumen", "titanium_plate", "solar_cell", "battery_cell",
         "rare_earth_magnet", "uranium_fuel_rod", "ion_drive_kit", "fusion_drive_kit"]
TOOLS = ["titanium_pickaxe", "titanium_axe", "titanium_shovel", "titanium_hoe", "titanium_sword"]


def cubes():
    for name in CUBES:
        write(ASSETS / "models/block" / (name + ".json"),
              {"parent": "minecraft:block/cube_all", "textures": {"all": NS + "block/" + name}})
        variant = {"model": NS + "block/" + name}
        if name in ROTATED:
            states = [dict(variant, **({"y": y} if y else {})) for y in (0, 90, 180, 270)]
            write(ASSETS / "blockstates" / (name + ".json"), {"variants": {"": states}})
        else:
            write(ASSETS / "blockstates" / (name + ".json"), {"variants": {"": variant}})
        item_from_block(name, "block/" + name)
    for name in PILLARS:
        write(ASSETS / "models/block" / (name + ".json"),
              {"parent": "minecraft:block/cube_column",
               "textures": {"end": NS + "block/" + name + "_top", "side": NS + "block/" + name}})
        write(ASSETS / "models/block" / (name + "_horizontal.json"),
              {"parent": "minecraft:block/cube_column_horizontal",
               "textures": {"end": NS + "block/" + name + "_top", "side": NS + "block/" + name}})
        m = NS + "block/" + name
        write(ASSETS / "blockstates" / (name + ".json"), {"variants": {
            "axis=y": {"model": m},
            "axis=z": {"model": m + "_horizontal", "x": 90},
            "axis=x": {"model": m + "_horizontal", "x": 90, "y": 90}}})
        item_from_block(name, "block/" + name)


def props():
    # Scrap pile: a flat heap with a bent sheet and a pipe sticking out.
    model("block/scrap_pile", {"particle": "block/scrap_pile", "s": "block/scrap_pile", "r": "block/rusted_panel"}, [
        box((0, 0, 0), (16, 4, 16), "s", cull=("down", "north", "south", "east", "west")),
        box((2, 4, 3), (13, 8, 12), "s"),
        box((5, 8, 5), (10, 11, 10), "s"),
        box((3, 4, 1), (12, 10, 2), "r", rotation={"origin": [8, 4, 2], "axis": "x", "angle": -22.5}),
        box((10, 6, 9), (16, 8, 11), "r", rotation={"origin": [10, 7, 10], "axis": "y", "angle": 22.5}),
    ])
    write(ASSETS / "blockstates/scrap_pile.json", {"variants": {"": [
        {"model": NS + "block/scrap_pile", **({"y": y} if y else {})} for y in (0, 90, 180, 270)]}})
    item_from_block("scrap_pile", "block/scrap_pile")
    # Rubble: broken concrete slabs with rebar.
    model("block/rubble", {"particle": "block/rubble", "c": "block/rubble", "r": "block/scrap_pile"}, [
        box((0, 0, 0), (16, 3, 16), "c", cull=("down", "north", "south", "east", "west")),
        box((3, 3, 2), (12, 6, 11), "c", rotation={"origin": [8, 3, 6], "axis": "z", "angle": 22.5}),
        box((12, 3, 10), (15, 5, 15), "c"),
        box((6, 5, 6), (7, 11, 7), "r", rotation={"origin": [6, 5, 6], "axis": "x", "angle": 22.5}),
        box((9, 4, 8), (10, 9, 9), "r", rotation={"origin": [9, 4, 8], "axis": "z", "angle": -22.5}),
    ])
    write(ASSETS / "blockstates/rubble.json", {"variants": {"": [
        {"model": NS + "block/rubble", **({"y": y} if y else {})} for y in (0, 90, 180, 270)]}})
    item_from_block("rubble", "block/rubble")
    # Toxic barrel: a drum with rims and a dented lid.
    t = {"particle": "block/toxic_barrel_side", "side": "block/toxic_barrel_side", "top": "block/toxic_barrel_top"}
    model("block/toxic_barrel", t, [
        box((2, 0, 2), (14, 15, 14), {"north": "side", "east": "side", "south": "side", "west": "side", "up": "top",
                                      "down": "top"}, cull=("down",)),
        box((1.5, 3, 1.5), (14.5, 4, 14.5), "side", faces=("north", "east", "south", "west"),
            uv={d: (0, 2, 16, 3) for d in ("north", "east", "south", "west")}),
        box((1.5, 11, 1.5), (14.5, 12, 14.5), "side", faces=("north", "east", "south", "west"),
            uv={d: (0, 12, 16, 13) for d in ("north", "east", "south", "west")}),
        box((9, 15, 4), (11, 15.5, 6), "top", faces=("up", "north", "east", "south", "west"),
            uv={d: (9, 3, 11, 5) for d in ("up", "north", "east", "south", "west")}),
    ])
    write(ASSETS / "blockstates/toxic_barrel.json", {"variants": {"": [
        {"model": NS + "block/toxic_barrel", **({"y": y} if y else {})} for y in (0, 90, 180, 270)]}})
    item_from_block("toxic_barrel", "block/toxic_barrel")
    # Mining drone: two treads, a boxy body, a sensor mast and a drill head to the front (north).
    t = {"particle": "block/mining_drone_body", "b": "block/mining_drone_body", "t": "block/mining_drone_tread",
         "d": "block/mining_drone_drill", "m": "block/rusted_panel"}
    model("block/mining_drone", t, [
        box((1, 0, 3), (4, 4, 15), "t"),
        box((12, 0, 3), (15, 4, 15), "t"),
        box((3, 3, 4), (13, 9, 14), "b"),
        box((5, 9, 9), (7, 13, 11), "m"),
        box((4.5, 13, 8.5), (7.5, 14, 11.5), "b"),
        box((5, 2, 1), (11, 7, 4), "d"),
        box((6.5, 3.5, 0), (9.5, 5.5, 1), "d"),
    ])
    facing_blockstate("mining_drone", "block/mining_drone")
    item_from_block("mining_drone", "block/mining_drone")
    # Tar puddle: a thin glossy sheet.
    model("block/tar_puddle", {"particle": "block/tar_puddle", "p": "block/tar_puddle"}, [
        box((0, 0, 0), (16, 1, 16), "p", cull=("down", "north", "south", "east", "west")),
    ])
    write(ASSETS / "blockstates/tar_puddle.json", {"variants": {"": [
        {"model": NS + "block/tar_puddle", **({"y": y} if y else {})} for y in (0, 90, 180, 270)]}})
    item_from_block("tar_puddle", "block/tar_puddle")


def items():
    for name in ITEMS:
        write(ASSETS / "models/item" / (name + ".json"),
              {"parent": "minecraft:item/generated", "textures": {"layer0": NS + "item/" + name}})
        write(ASSETS / "items" / (name + ".json"),
              {"model": {"type": "minecraft:model", "model": NS + "item/" + name}})
    for name in TOOLS:
        write(ASSETS / "models/item" / (name + ".json"),
              {"parent": "minecraft:item/handheld", "textures": {"layer0": NS + "item/" + name}})
        write(ASSETS / "items" / (name + ".json"),
              {"model": {"type": "minecraft:model", "model": NS + "item/" + name}})


if __name__ == "__main__":
    cubes()
    props()
    items()
    print("earth models written")
