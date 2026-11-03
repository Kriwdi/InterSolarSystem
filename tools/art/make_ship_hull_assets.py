"""Textures, models, blockstates and item definitions of the research ship's shaped outer hull and the two-block hatch.

    python3 tools/art/make_ship_hull_assets.py   (after make_ship_textures.py, which draws the plating)
"""
import json
import math
import random

from PIL import Image

from make_textures import TEX, HAZARD_K, HAZARD_Y, bevel, grime, hexc, metal_base, mix, rivet, rust_drip, save, shade
from make_models import ASSETS, NS, box, facing_blockstate, item_from_block, model, write


def stripe():
    """The plating with the ship's livery: a deep blue band edged with orange pinstripes."""
    img = Image.open(TEX / "block/ship_plating.png").convert("RGBA")
    px = img.load()
    rnd = random.Random(611)
    blue = hexc("23406e")
    orange = hexc("e2782a")
    for x in range(16):
        for y in range(3, 13):
            if y in (3, 12):
                c = orange
            elif y in (4, 11):
                c = shade(blue, 0.75)
            else:
                c = shade(blue, 1.0 + 0.06 * math.sin(x * 0.7 + y))
            px[x, y] = shade(c, 1 + rnd.uniform(-0.04, 0.04))
    # Worn paint: a few chips showing the steel below.
    for _ in range(5):
        x, y = rnd.randrange(16), rnd.randrange(5, 11)
        px[x, y] = hexc("8d918e")
    save(img, "block/ship_stripe")


def light(name, core, glow):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            if r < 3.2:
                px[x, y] = mix(hexc("ffffff"), core, min(1.0, r / 3.2))
            elif r < 5.6:
                px[x, y] = mix(core, glow, (r - 3.2) / 2.4)
            elif r < 6.8:
                px[x, y] = hexc("3a3f44") if (x + y) % 2 else hexc("5d646b")
            else:
                px[x, y] = shade(hexc("c9ccc8"), 0.95 + 0.05 * ((x * 7 + y * 3) % 3))
    save(img, "block/" + name)


def tall_door():
    """The hatch door, two blocks tall: a heavy plate with a seal ring around the wheel, drawn as two halves."""
    full = Image.new("RGBA", (16, 32))
    lower = metal_base(44, light=3)
    upper = metal_base(48, light=3)
    full.paste(upper, (0, 0))
    full.paste(lower, (0, 16))
    px = full.load()
    for y in range(32):
        for x in range(16):
            r = math.hypot(x - 7.5, (y - 15.5) * 0.9)
            if 6.0 <= r <= 7.0:
                px[x, y] = hexc("2b2f33")
            elif 5.2 <= r < 6.0:
                px[x, y] = shade(px[x, y], 1.15)
            # Hazard chevrons at the bottom edge, a stencilled arrow at the top.
            if y >= 28 and ((x + y) // 2) % 2 == 0:
                px[x, y] = HAZARD_Y
            elif y >= 28:
                px[x, y] = HAZARD_K
    for (x, y) in [(7, 3), (8, 3), (6, 4), (7, 4), (8, 4), (9, 4), (7, 5), (8, 5), (7, 6), (8, 6)]:
        px[x, y] = mix(px[x, y], hexc("d8d2c0"), 0.75)
    upper_img = full.crop((0, 0, 16, 16))
    lower_img = full.crop((0, 16, 16, 32))
    for img, seed in ((upper_img, 49), (lower_img, 45)):
        rust_drip(img, 3, 2, 5, seed)
        grime(img, seed + 1)
    save(upper_img, "block/ship_hatch_door_upper")
    save(lower_img, "block/ship_hatch_door_lower")

    # Tall frame: hazard stripes on the sides and the top only.
    f = metal_base(41, light=2)
    px = f.load()
    for y in range(16):
        for x in range(16):
            if x < 3 or x > 12:
                stripe = ((x + y) // 2) % 2 == 0
                px[x, y] = shade(HAZARD_Y if stripe else HAZARD_K, random.Random(x * 31 + y).uniform(0.9, 1.05))
    bevel(f, 0, 0, 15, 15)
    for (x, y) in [(1, 2), (14, 2), (1, 13), (14, 13)]:
        rivet(f, x, y)
    save(f, "block/ship_hatch_frame_side")


def hatch_models():
    t = {"particle": "block/ship_hatch_frame", "frame": "block/ship_hatch_frame",
         "side": "block/ship_hatch_frame_side", "lower": "block/ship_hatch_door_lower",
         "upper": "block/ship_hatch_door_upper", "wheel": "block/ship_hatch_wheel", "metal": "block/rusted_panel"}
    spoke = {"origin": [8, 16, -0.5], "axis": "z", "angle": 45}
    els = [
        # Body: two full blocks behind the door (the upper one is the hatch's top block).
        box((0, 0, 1), (16, 16, 16), {"east": "side", "south": "side", "west": "side", "down": "frame"},
            faces=("east", "south", "west", "down"), cull=("east", "south", "west", "down")),
        box((0, 16, 1), (16, 32, 16), {"east": "side", "south": "side", "west": "side", "up": "frame"},
            faces=("east", "south", "west", "up")),
        # Raised frame around the door: sill, lintel and jambs.
        box((0, 29, 0), (16, 32, 1), "frame"),
        box((0, 0, 0), (16, 2, 1), "frame"),
        box((0, 2, 0), (3, 29, 1), "side", uv={"north": (0, 0, 3, 16), "east": (0, 0, 1, 16),
                                               "west": (0, 0, 1, 16), "south": (0, 0, 3, 16)}),
        box((13, 2, 0), (16, 29, 1), "side", uv={"north": (13, 0, 16, 16), "east": (0, 0, 1, 16),
                                                 "west": (0, 0, 1, 16), "south": (13, 0, 16, 16)}),
        # The door plate, set back into the frame, in two halves.
        box((3, 2, 0.5), (13, 16, 1), {"north": "lower"}, faces=("north",), uv={"north": (3, 2, 13, 16)}),
        box((3, 16, 0.5), (13, 29, 1), {"north": "upper"}, faces=("north",), uv={"north": (3, 3, 13, 16)}),
        # Valve wheel at the middle of the door: rim, hub and crossed spokes.
        box((5, 19, -0.5), (11, 20, 0.5), "wheel"),
        box((5, 12, -0.5), (11, 13, 0.5), "wheel"),
        box((4, 13, -0.5), (5, 19, 0.5), "wheel"),
        box((11, 13, -0.5), (12, 19, 0.5), "wheel"),
        box((4.5, 15.5, -0.25), (11.5, 16.5, 0.25), "metal", rotation=spoke),
        box((7.5, 12.5, -0.25), (8.5, 19.5, 0.25), "metal", rotation=spoke),
        box((7, 15, -1), (9, 17, 0.5), "wheel"),
        # Grab bar and a warning lamp above the door.
        box((13.5, 10, -1), (14.5, 22, 0), "metal"),
        box((7, 30, -1), (9, 31.5, 0), "wheel"),
    ]
    model("block/ship_hatch", t, els)
    facing_blockstate("ship_hatch", "block/ship_hatch")
    # The item shows the lower block only (an item model of 2 blocks would be too tall).
    item_from_block("ship_hatch", "block/ship_hatch")
    # The top block draws nothing; the lower block's model covers it.
    write(ASSETS / "models/block/ship_hatch_top.json", {"textures": {"particle": NS + "block/ship_hatch_frame"}})
    facing_blockstate("ship_hatch_top", "block/ship_hatch_top")
    item_from_block("ship_hatch_top", "block/ship_hatch")


def hull_models():
    p = NS + "block/ship_plating"
    write(ASSETS / "models/block/ship_plating_slab.json",
          {"parent": "minecraft:block/slab", "textures": {"bottom": p, "top": p, "side": p}})
    write(ASSETS / "models/block/ship_plating_slab_top.json",
          {"parent": "minecraft:block/slab_top", "textures": {"bottom": p, "top": p, "side": p}})
    write(ASSETS / "blockstates/ship_plating_slab.json", {"variants": {
        "type=bottom": {"model": NS + "block/ship_plating_slab"},
        "type=top": {"model": NS + "block/ship_plating_slab_top"},
        "type=double": {"model": NS + "block/ship_plating"},
    }})
    item_from_block("ship_plating_slab", "block/ship_plating_slab")
    three = {"bottom": p, "top": p, "side": p}
    for kind, name in (("stairs", "ship_plating_stairs"), ("inner_stairs", "ship_plating_stairs_inner"),
                       ("outer_stairs", "ship_plating_stairs_outer")):
        write(ASSETS / "models/block" / (name + ".json"), {"parent": "minecraft:block/" + kind, "textures": three})
    variants = {}
    facing_y = {"east": 0, "south": 90, "west": 180, "north": 270}
    for facing, fy in facing_y.items():
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                name = NS + "block/" + ("ship_plating_stairs" if shape == "straight" else
                                        "ship_plating_stairs_inner" if shape.startswith("inner")
                                        else "ship_plating_stairs_outer")
                y = fy
                if shape in ("inner_left", "outer_left"):
                    y = (fy + 270) % 360
                v = {"model": name}
                if half == "top":
                    v["x"] = 180
                    if shape in ("inner_left", "outer_left"):
                        y = fy
                    elif shape in ("inner_right", "outer_right"):
                        y = (fy + 90) % 360
                if y:
                    v["y"] = y
                if half == "top" or y:
                    v["uvlock"] = True
                variants[f"facing={facing},half={half},shape={shape}"] = v
    write(ASSETS / "blockstates/ship_plating_stairs.json", {"variants": variants})
    item_from_block("ship_plating_stairs", "block/ship_plating_stairs")
    for name in ("ship_stripe", "ship_light_red", "ship_light_green"):
        write(ASSETS / "models/block" / (name + ".json"),
              {"parent": "minecraft:block/cube_all", "textures": {"all": NS + "block/" + name}})
        write(ASSETS / "blockstates" / (name + ".json"), {"variants": {"": {"model": NS + "block/" + name}}})
        item_from_block(name, "block/" + name)


if __name__ == "__main__":
    stripe()
    light("ship_light_red", hexc("ff3b2b"), hexc("8e1610"))
    light("ship_light_green", hexc("4dff6a"), hexc("127a2a"))
    tall_door()
    hatch_models()
    hull_models()
    print("ship hull assets written")


def seat_textures():
    # Padded fabric: quilted navy cushions with light stitching.
    f = Image.new("RGBA", (16, 16))
    px = f.load()
    rnd = random.Random(631)
    navy = hexc("27334a")
    for y in range(16):
        for x in range(16):
            cell_y = y % 5
            c = shade(navy, 1.12 - 0.07 * abs(cell_y - 2) + rnd.uniform(-0.03, 0.03))
            if cell_y == 4:
                c = shade(navy, 0.7)
            if cell_y == 4 and x % 2 == 0:
                c = hexc("7d8aa3")
            px[x, y] = c
    save(f, "block/astronaut_seat_fabric")
    # Frame: painted light grey steel with bolts.
    m = metal_base(632, light=4)
    bevel(m, 0, 0, 15, 15)
    for (x, y) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        rivet(m, x, y)
    save(m, "block/astronaut_seat_frame")
    # Harness: orange webbing with a steel buckle.
    h = Image.new("RGBA", (16, 16))
    px = h.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(hexc("e0782c"), 1.0 + (0.08 if x % 4 == 0 else 0) - (0.1 if y % 3 == 0 else 0))
    for y in range(6, 10):
        for x in range(4, 12):
            px[x, y] = hexc("c9ccc8") if (x in (4, 11) or y in (6, 9)) else hexc("8d918e")
    save(h, "block/astronaut_seat_harness")


def seat_model():
    t = {"particle": "block/astronaut_seat_fabric", "fabric": "block/astronaut_seat_fabric",
         "frame": "block/astronaut_seat_frame", "harness": "block/astronaut_seat_harness"}
    els = [
        # Pedestal with a foot plate.
        box((3, 0, 3), (13, 1, 13), "frame", cull=("down",)),
        box((6, 1, 6), (10, 4, 10), "frame"),
        # Seat pan: frame and cushion, slightly raised at the front edge.
        box((2, 4, 1), (14, 5, 14), "frame"),
        box((2.5, 5, 1.5), (13.5, 7, 13.5), "fabric"),
        box((2.5, 7, 1.5), (13.5, 7.5, 3), "fabric"),
        # Backrest (leaning back) and the headrest above it, with a gap between them.
        box((2.5, 5, 13), (13.5, 19, 15), "fabric", rotation={"origin": [8, 5, 14], "axis": "x", "angle": 22.5}),
        box((3, 5, 15), (13, 18, 15.5), "frame", rotation={"origin": [8, 5, 14], "axis": "x", "angle": 22.5}),
        box((7, 19, 14), (9, 21, 15), "frame", rotation={"origin": [8, 5, 14], "axis": "x", "angle": 22.5}),
        box((4.5, 21, 13.5), (11.5, 25, 15), "fabric", rotation={"origin": [8, 5, 14], "axis": "x", "angle": 22.5}),
        # Harness straps over the shoulders, meeting at the buckle.
        box((4.5, 9, 12.6), (6, 18.5, 13), "harness", rotation={"origin": [8, 5, 14], "axis": "x", "angle": 22.5}),
        box((10, 9, 12.6), (11.5, 18.5, 13), "harness", rotation={"origin": [8, 5, 14], "axis": "x", "angle": 22.5}),
        box((6.5, 7, 6), (9.5, 7.6, 9), "harness"),
        # Armrests with their posts.
        box((0, 9, 2), (2, 10.5, 13), "frame"),
        box((14, 9, 2), (16, 10.5, 13), "frame"),
        box((0.5, 5, 10), (1.5, 9, 11.5), "frame"),
        box((14.5, 5, 10), (15.5, 9, 11.5), "frame"),
        box((0, 10.5, 2), (2, 11, 7), "fabric"),
        box((14, 10.5, 2), (16, 11, 7), "fabric"),
    ]
    model("block/astronaut_seat", t, els)
    facing_blockstate("astronaut_seat", "block/astronaut_seat")
    item_from_block("astronaut_seat", "block/astronaut_seat")


if __name__ == "__main__":
    seat_textures()
    seat_model()
    print("seat written")


def radiator():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    rnd = random.Random(641)
    for y in range(16):
        for x in range(16):
            rib = x % 4
            c = hexc("e8e9e4") if rib in (1, 2) else hexc("b9bcb8") if rib == 0 else hexc("d3d5d0")
            px[x, y] = shade(c, 1 + rnd.uniform(-0.03, 0.02))
    for x in range(16):
        px[x, 0] = hexc("8d918e")
        px[x, 15] = hexc("8d918e")
    # Micrometeoroid pits and a scorched patch.
    for _ in range(6):
        px[rnd.randrange(16), rnd.randrange(1, 15)] = hexc("7b7f7c")
    save(img, "block/station_radiator")
    data = json.loads((ASSETS / "models/block/solar_panel.json").read_text())
    data["textures"] = {"particle": NS + "block/station_radiator", "cells": NS + "block/station_radiator",
                        "back": NS + "block/station_radiator", "frame": NS + "block/truss"}
    write(ASSETS / "models/block/station_radiator.json", data)
    write(ASSETS / "blockstates/station_radiator.json", {"variants": {"": {"model": NS + "block/station_radiator"}}})
    item_from_block("station_radiator", "block/station_radiator")


if __name__ == "__main__":
    radiator()


def station_glass():
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for i in range(16):
        px[i, 0] = px[i, 15] = px[0, i] = px[15, i] = hexc("b7babb")
    for i in range(3):
        px[4 + i, 6 - i] = (225, 238, 255, 255)
    save(img, "block/station_glass")
    write(ASSETS / "models/block/station_glass.json",
          {"parent": "minecraft:block/cube_all", "textures": {"all": NS + "block/station_glass"}})
    write(ASSETS / "blockstates/station_glass.json", {"variants": {"": {"model": NS + "block/station_glass"}}})
    item_from_block("station_glass", "block/station_glass")


if __name__ == "__main__":
    station_glass()
