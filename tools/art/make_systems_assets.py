"""Textures and models of the ship's systems: pipes and cables, the reactor, coolant tank and life support unit, the
fault blocks (sparking panel, steam vent, leaking pipe, electrical fire) and the repair tools.

    python3 tools/art/make_systems_assets.py
"""
import math
import random

from PIL import Image

from make_textures import (OUTLINE, STEEL, RUST, bevel, grid, grime, hexc, metal_base, mix, rivet, rust_drip, save,
                           shade, TEX)
from make_models import ASSETS, NS, box, item_from_block, model, write

FACINGS = (("north", 0), ("east", 90), ("south", 180), ("west", 270))
SIDES = ("north", "east", "south", "west", "up", "down")


def tex_pipe():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            t = abs(x - 7.5) / 8
            c = mix(hexc("d18a52"), hexc("6e3b1f"), t)
            if y % 8 == 0:
                c = shade(c, 0.7)
            px[x, y] = c
    save(img, "block/pipe")
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            t = abs(x - 7.5) / 8
            c = mix(hexc("2b2f33"), hexc("101214"), t)
            if (y + x // 3) % 6 == 0:
                c = hexc("d9a521")
            px[x, y] = c
    save(img, "block/cable")


def conduit(name, r, texture):
    lo, hi = 8 - r, 8 + r
    t = {"particle": "block/" + texture, "t": "block/" + texture}
    model(f"block/{name}_core", t, [box((lo, lo, lo), (hi, hi, hi), "t")])
    arms = {
        "north": ((lo, lo, 0), (hi, hi, lo)), "south": ((lo, lo, hi), (hi, hi, 16)),
        "west": ((0, lo, lo), (lo, hi, hi)), "east": ((hi, lo, lo), (16, hi, hi)),
        "down": ((lo, 0, lo), (hi, lo, hi)), "up": ((lo, hi, lo), (hi, 16, hi)),
    }
    parts = [{"apply": {"model": NS + f"block/{name}_core"}}]
    for side, (frm, to) in arms.items():
        model(f"block/{name}_{side}", t, [box(frm, to, "t", faces=[f for f in SIDES if f != side])])
        parts.append({"when": {side: "true"}, "apply": {"model": NS + f"block/{name}_{side}"}})
    write(ASSETS / f"blockstates/{name}.json", {"multipart": parts})
    model(f"item/{name}", t, [box((lo, lo, 0), (hi, hi, 16), "t")], parent="minecraft:block/block")
    write(ASSETS / f"items/{name}.json", {"model": {"type": "minecraft:model", "model": NS + f"item/{name}"}})


def machine_textures():
    # Reactor: a steel housing with a glowing uranium window and hazard trefoil.
    r = metal_base(711, light=2)
    px = r.load()
    for y in range(3, 13):
        for x in range(4, 12):
            g = 1 - abs(x - 7.5) / 5
            px[x, y] = mix(hexc("1d4a22"), hexc("8dff7a"), max(0.0, g) * (0.7 + 0.3 * math.sin(y)))
    bevel(r, 3, 2, 12, 13, hi=0.7, lo=1.2)
    for (x, y) in [(1, 1), (14, 1), (1, 14), (14, 14)]:
        rivet(r, x, y)
    save(r, "block/reactor_front")
    s = metal_base(712, light=2)
    px = s.load()
    for y in range(16):
        for x in range(16):
            if y in (4, 11):
                px[x, y] = hexc("d9a521") if x % 4 < 2 else hexc("1e1f21")
    bevel(s, 0, 0, 15, 15)
    save(s, "block/reactor_side")
    # Coolant tank: a blue-grey cylinder with a sight glass.
    c = metal_base(713, light=3)
    px = c.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(mix(px[x, y], hexc("4a7aa8"), 0.45), 1.15 - abs(x - 7.5) / 20)
    for y in range(3, 13):
        px[7, y] = hexc("8fd0ff")
        px[8, y] = hexc("5fa8e8")
    save(c, "block/coolant_tank")
    # Life support unit: white panel, fan grille, filter slot.
    l = Image.new("RGBA", (16, 16))
    px = l.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = shade(hexc("cfd1d0"), 0.95 + 0.05 * ((x + y) % 3))
    for y in range(3, 10):
        for x in range(3, 10):
            d = math.hypot(x - 6, y - 6)
            if d < 3.6:
                px[x, y] = hexc("3a3f44") if int(d * 2) % 2 else hexc("6b737a")
    for x in range(10, 14):
        px[x, 12] = hexc("5dfa98")
        px[x, 4] = hexc("2b2f33")
    bevel(l, 0, 0, 15, 15)
    save(l, "block/life_support_unit")


def machine(name, front, side, top, shape):
    t = {"particle": side, "front": front, "side": side, "top": top}
    model(f"block/{name}", t, [box(shape[0], shape[1], {"north": "front", "south": "side", "east": "side",
                                                         "west": "side", "up": "top", "down": "top"})])
    variants = {}
    for level in range(9):
        for f, y in FACINGS:
            v = {"model": NS + f"block/{name}"}
            if y:
                v["y"] = y
            variants[f"facing={f},level={level}"] = v
    write(ASSETS / f"blockstates/{name}.json", {"variants": variants})
    item_from_block(name, f"block/{name}")


def fault_textures():
    p = Image.open(TEX / "block/rusted_panel.png").convert("RGBA")
    px = p.load()
    rnd = random.Random(721)
    for y in range(4, 12):
        for x in range(4, 12):
            px[x, y] = hexc("121416")
    for _ in range(6):
        x, y = rnd.randrange(4, 12), rnd.randrange(4, 12)
        col = rnd.choice(["c5382a", "d9a521", "3f76e4", "37b56a"])
        for i in range(4):
            px[min(11, x + i % 2), min(11, y + i // 2)] = hexc(col)
    px[7, 7] = px[8, 8] = hexc("ffffa0")
    save(p, "block/sparking_panel")
    v = metal_base(722, light=2)
    px = v.load()
    for y in range(2, 14):
        if y % 3 == 0:
            for x in range(2, 14):
                px[x, y] = hexc("1e1f21")
    bevel(v, 1, 1, 14, 14)
    save(v, "block/steam_vent")
    lp = Image.open(TEX / "block/pipe.png").convert("RGBA")
    px = lp.load()
    for y in range(5, 11):
        px[7 + (y % 2), y] = hexc("1e1f21")
        px[8, y] = hexc("9fd0ff")
    save(lp, "block/leaking_pipe")
    f = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = f.load()
    rnd = random.Random(723)
    for x in range(16):
        h = 8 + int(6 * abs(math.sin(x * 0.9))) + rnd.randrange(3)
        for y in range(16 - h, 16):
            t = (y - (16 - h)) / h
            px[x, y] = (255, int(80 + 160 * t), int(30 * t), 255)
    save(f, "block/ship_fire")


def fault_models():
    for name in ("sparking_panel", "steam_vent"):
        write(ASSETS / f"models/block/{name}.json",
              {"parent": "minecraft:block/cube_all", "textures": {"all": NS + f"block/{name}"}})
        write(ASSETS / f"blockstates/{name}.json", {"variants": {"": {"model": NS + f"block/{name}"}}})
        item_from_block(name, f"block/{name}")
    model("block/leaking_pipe", {"particle": "block/leaking_pipe", "t": "block/leaking_pipe"},
          [box((5.5, 5.5, 0), (10.5, 10.5, 16), "t"), box((6.5, 4.5, 6), (9.5, 11.5, 10), "t")])
    write(ASSETS / "blockstates/leaking_pipe.json", {"variants": {"": {"model": NS + "block/leaking_pipe"}}})
    item_from_block("leaking_pipe", "block/leaking_pipe")
    model("block/ship_fire", {"particle": "block/ship_fire", "f": "block/ship_fire"}, [
        box((0.8, 0, 8), (15.2, 14, 8), {"north": "f", "south": "f"}, faces=("north", "south"),
            rotation={"origin": [8, 8, 8], "axis": "y", "angle": 45}, shade=False),
        box((8, 0, 0.8), (8, 14, 15.2), {"east": "f", "west": "f"}, faces=("east", "west"),
            rotation={"origin": [8, 8, 8], "axis": "y", "angle": 45}, shade=False),
    ])
    write(ASSETS / "blockstates/ship_fire.json", {"variants": {"": {"model": NS + "block/ship_fire"}}})
    item_from_block("ship_fire", "block/ship_fire")


def tools():
    o = OUTLINE
    iron = grid([
        "................",
        "................",
        "............oo..",
        "...........oCCo.",
        "..........oCCo..",
        ".........oGGo...",
        "........oGGo....",
        ".......oSSo.....",
        "......oSSo......",
        ".....oSSo.......",
        "....oRRo........",
        "...oRRo.........",
        "..oRRo..........",
        ".oRRo...........",
        ".ooo............",
        "................",
    ], {"o": o, "C": hexc("ffb347"), "G": hexc("8d959c"), "S": hexc("c9ccc8"), "R": hexc("c5382a")})
    save(iron, "item/soldering_iron")
    sealant = grid([
        "......ooo.......",
        ".....oWWWo......",
        "......oNo.......",
        ".....ooooo......",
        "....oYYYYYo.....",
        "....oYHYYYo.....",
        "....oKKKKKo.....",
        "....oYYYYYo.....",
        "....oYHYYYo.....",
        "....oYYYYYo.....",
        "....oYYYYYo.....",
        "....oDDDDDo.....",
        ".....ooooo......",
        "................",
        "................",
        "................",
    ], {"o": o, "W": hexc("eef0ec"), "N": hexc("8d959c"), "Y": hexc("e8c23a"), "H": hexc("fff3b0"),
        "K": hexc("1e1f21"), "D": hexc("a8851e")})
    save(sealant, "item/sealant")
    ext = grid([
        ".......oo.......",
        "......oNNooo....",
        ".....oNNNNNNo...",
        "......oRRo..o...",
        ".....oRRRRo..o..",
        ".....oRHRRo..o..",
        ".....oRHRRo.....",
        ".....oRRRRo.....",
        ".....oWWWWo.....",
        ".....oRRRRo.....",
        ".....oRRRRo.....",
        ".....oRRRRo.....",
        ".....oRRRRo.....",
        ".....oDDDDo.....",
        "......oooo......",
        "................",
    ], {"o": o, "N": hexc("3a3f44"), "R": hexc("d0312a"), "H": hexc("ff8a7a"), "W": hexc("eef0ec"),
        "D": hexc("8a1f19")})
    save(ext, "item/fire_extinguisher")
    filt = grid([
        "................",
        "...oooooooooo...",
        "...oMMMMMMMMo...",
        "...oMffffffMo...",
        "...oMfFfFfFMo...",
        "...oMffffffMo...",
        "...oMfFfFfFMo...",
        "...oMffffffMo...",
        "...oMfFfFfFMo...",
        "...oMffffffMo...",
        "...oMMMMMMMMo...",
        "...oooooooooo...",
        "................",
        "................",
        "................",
        "................",
    ], {"o": o, "M": hexc("8d959c"), "f": hexc("e9e4d4"), "F": hexc("2b2f33")})
    save(filt, "item/air_filter")
    for name in ("soldering_iron", "sealant", "fire_extinguisher", "air_filter"):
        parent = "minecraft:item/handheld" if name in ("soldering_iron", "fire_extinguisher") else "minecraft:item/generated"
        write(ASSETS / f"models/item/{name}.json", {"parent": parent, "textures": {"layer0": NS + "item/" + name}})
        write(ASSETS / f"items/{name}.json", {"model": {"type": "minecraft:model", "model": NS + "item/" + name}})


if __name__ == "__main__":
    tex_pipe()
    conduit("pipe", 2.5, "pipe")
    conduit("cable", 1.5, "cable")
    machine_textures()
    machine("reactor", NS + "block/reactor_front", NS + "block/reactor_side", NS + "block/reactor_side",
            ((1, 0, 1), (15, 16, 15)))
    machine("coolant_tank", NS + "block/coolant_tank", NS + "block/coolant_tank", NS + "block/reactor_side",
            ((2, 0, 2), (14, 16, 14)))
    machine("life_support_unit", NS + "block/life_support_unit", NS + "block/reactor_side",
            NS + "block/reactor_side", ((1, 0, 2), (15, 16, 14)))
    fault_textures()
    fault_models()
    tools()
    print("systems assets written")
