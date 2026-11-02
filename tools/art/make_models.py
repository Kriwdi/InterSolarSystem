"""Writes the shaped block models, blockstates and item definitions of the mod.

Models are built from boxes (Minecraft model elements). All models face north (the front is the z=0 side); the
blockstates rotate them for the other directions, exactly like vanilla's furnace or lectern.

    python3 tools/art/make_models.py
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/intersolarsystem"
NS = "intersolarsystem:"
DIRS = ("north", "east", "south", "west", "up", "down")


def box(frm, to, tex, faces=DIRS, uv=None, rotation=None, cull=None, shade=True):
    """One element. tex: a texture key for every face or a dict face -> key. uv: optional dict face -> uv."""
    el = {"from": list(frm), "to": list(to), "faces": {}}
    for d in faces:
        t = tex[d] if isinstance(tex, dict) else tex
        if t is None:
            continue
        face = {"texture": "#" + t}
        if uv and d in uv:
            face["uv"] = list(uv[d])
        if cull and d in cull:
            face["cullface"] = d
        el["faces"][d] = face
    if rotation:
        el["rotation"] = rotation
    if not shade:
        el["shade"] = False
    return el


def model(name, textures, elements, parent="minecraft:block/block"):
    data = {"parent": parent, "textures": {k: NS + v if ":" not in v else v for k, v in textures.items()},
            "elements": elements}
    write(ASSETS / "models" / (name + ".json"), data)


def facing_blockstate(block, model_name):
    variants = {}
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        v = {"model": NS + model_name}
        if y:
            v["y"] = y
        variants["facing=" + facing] = v
    write(ASSETS / "blockstates" / (block + ".json"), {"variants": variants})


def item_from_block(item, model_name):
    write(ASSETS / "items" / (item + ".json"), {"model": {"type": "minecraft:model", "model": NS + model_name}})


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


def repair_station():
    t = {"particle": "block/repair_station_side", "top": "block/repair_station_top",
         "board": "block/repair_station_board", "side": "block/repair_station_side",
         "box": "block/repair_station_toolbox", "metal": "block/rusted_panel"}
    els = [
        # Pegboard at the back, with the tool outlines facing the player.
        box((0, 0, 13), (16, 16, 16), {"north": "board", "east": "side", "south": "side", "west": "side",
                                       "up": "side", "down": "side"}, cull=("east", "south", "west", "up", "down")),
        # Bench top; the yellow safety edge of the side texture runs along its front.
        box((0, 10, 1), (16, 12, 13), {"north": "side", "east": "side", "south": "side", "west": "side",
                                       "up": "top", "down": "side"},
            uv={"north": (0, 0, 16, 2), "east": (0, 0, 12, 2), "west": (0, 0, 12, 2), "up": (0, 1, 16, 13)},
            cull=("east", "west")),
        # Legs and a lower shelf.
        box((1, 0, 2), (3, 10, 4), "side", cull=("down",)),
        box((13, 0, 2), (15, 10, 4), "side", cull=("down",)),
        box((1, 3, 4), (15, 4, 13), "side"),
        # Red toolbox with a handle.
        box((2, 12, 4), (7, 15, 8), "box", uv={"north": (0, 5, 5, 8), "south": (0, 5, 5, 8), "east": (0, 5, 4, 8),
                                                "west": (0, 5, 4, 8), "up": (0, 0, 5, 4), "down": (0, 0, 5, 4)}),
        box((3, 15, 5.5), (6, 16, 6.5), "metal"),
        # Bench vise: base, two jaws and the screw handle.
        box((10, 12, 3), (14, 13, 8), "metal"),
        box((11, 13, 3), (13, 15, 4), "metal"),
        box((11, 13, 6), (13, 15, 7), "metal"),
        box((11.5, 13.5, 1), (12.5, 14.5, 3), "metal"),
        box((10.5, 13.5, 1), (13.5, 14.5, 1.5), "metal"),
    ]
    model("block/repair_station", t, els)
    facing_blockstate("repair_station", "block/repair_station")
    item_from_block("repair_station", "block/repair_station")


def diagnostic_console():
    t = {"particle": "block/diagnostic_console_cabinet", "cab": "block/diagnostic_console_cabinet",
         "keys": "block/diagnostic_console_keys", "screen": "block/diagnostic_console_screen",
         "housing": "block/diagnostic_console_housing", "red": "block/ship_hatch_wheel"}
    els = [
        # Desk cabinet; its front shows the vents and the status lamps.
        box((1, 0, 2), (15, 8, 16), "cab", uv={"north": (1, 2, 15, 16), "east": (0, 8, 14, 16),
                                               "west": (2, 8, 16, 16), "south": (1, 8, 15, 16)},
            cull=("down",)),
        # Sloped keyboard, tilted towards the operator.
        box((1, 7, 2), (15, 9, 10), {"north": "housing", "east": "housing", "west": "housing", "south": "housing",
                                     "up": "keys", "down": "housing"},
            uv={"up": (1, 1, 15, 15)},
            rotation={"origin": [8, 8, 6], "axis": "x", "angle": 22.5}),
        # Monitor: housing, recessed glowing screen (not shaded, so it reads as lit), and a hood.
        box((2, 8, 11), (14, 16, 15), "housing"),
        box((3, 9, 10.9), (13, 15, 11), {"north": "screen"}, faces=("north",), uv={"north": (1, 2, 15, 14)},
            shade=False),
        box((2, 15.5, 10), (14, 16, 11), "housing"),
        # Antenna with a red tip.
        box((12.5, 16, 13), (13.5, 19, 14), "housing"),
        box((12.25, 19, 12.75), (13.75, 20, 14.25), "red"),
    ]
    model("block/diagnostic_console", t, els)
    facing_blockstate("diagnostic_console", "block/diagnostic_console")
    item_from_block("diagnostic_console", "block/diagnostic_console")


def ship_hatch():
    t = {"particle": "block/ship_hatch_frame", "frame": "block/ship_hatch_frame", "door": "block/ship_hatch_door",
         "wheel": "block/ship_hatch_wheel", "metal": "block/rusted_panel"}
    spoke = {"origin": [8, 8, -0.5], "axis": "z", "angle": 45}
    els = [
        # Body of the hatch: a full block so a hatch in a hull wall keeps the wall sealed.
        box((0, 0, 1), (16, 16, 16), {"north": None, "east": "frame", "south": "frame", "west": "frame",
                                      "up": "frame", "down": "frame"},
            faces=("east", "south", "west", "up", "down"), cull=("east", "south", "west", "up", "down")),
        # Raised frame ring around the door.
        box((0, 13, 0), (16, 16, 1), "frame", cull=("east", "west", "up")),
        box((0, 0, 0), (16, 3, 1), "frame", cull=("east", "west", "down")),
        box((0, 3, 0), (3, 13, 1), "frame", cull=("west",)),
        box((13, 3, 0), (16, 13, 1), "frame", cull=("east",)),
        # The door plate, set back into the frame.
        box((3, 3, 0.5), (13, 13, 1), {"north": "door"}, faces=("north",), uv={"north": (0, 0, 16, 16)}),
        # Valve wheel: rim, hub and two crossed spokes.
        box((5, 11, -0.5), (11, 12, 0.5), "wheel"),
        box((5, 4, -0.5), (11, 5, 0.5), "wheel"),
        box((4, 5, -0.5), (5, 11, 0.5), "wheel"),
        box((11, 5, -0.5), (12, 11, 0.5), "wheel"),
        box((4.5, 7.5, -0.25), (11.5, 8.5, 0.25), "metal", rotation=spoke),
        box((7.5, 4.5, -0.25), (8.5, 11.5, 0.25), "metal", rotation=spoke),
        box((7, 7, -1), (9, 9, 0.5), "wheel"),
    ]
    model("block/ship_hatch", t, els)
    facing_blockstate("ship_hatch", "block/ship_hatch")
    item_from_block("ship_hatch", "block/ship_hatch")


def rusted_panel():
    for i, suffix in enumerate(("", "_2", "_3")):
        write(ASSETS / "models/block" / ("rusted_panel" + suffix + ".json"),
              {"parent": "minecraft:block/cube_all", "textures": {"all": NS + "block/rusted_panel" + suffix}})
    # Random variant per position, like vanilla stone; the plain panel is the most common.
    write(ASSETS / "blockstates/rusted_panel.json", {"variants": {"": [
        {"model": NS + "block/rusted_panel", "weight": 3},
        {"model": NS + "block/rusted_panel_2", "weight": 2},
        {"model": NS + "block/rusted_panel_3"},
    ]}})
    item_from_block("rusted_panel", "block/rusted_panel")


def items():
    handheld = {"multitool"}
    for name in ("repair_kit", "scrap_sensor", "scrap_metal", "salvaged_wiring", "salvaged_circuit", "multitool"):
        parent = "minecraft:item/handheld" if name in handheld else "minecraft:item/generated"
        write(ASSETS / "models/item" / (name + ".json"),
              {"parent": parent, "textures": {"layer0": NS + "item/" + name}})
        write(ASSETS / "items" / (name + ".json"),
              {"model": {"type": "minecraft:model", "model": NS + "item/" + name}})


if __name__ == "__main__":
    repair_station()
    diagnostic_console()
    ship_hatch()
    rusted_panel()
    items()
    print("models written")
