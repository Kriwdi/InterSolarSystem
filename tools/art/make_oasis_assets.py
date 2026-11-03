"""Textures, models and item definitions of the oasis: its grass and living soil, and the water canisters.

    python3 tools/art/make_oasis_assets.py
"""
import random

from PIL import Image

from make_textures import OUTLINE, grid, hexc, mix, save, shade
from make_models import ASSETS, NS, item_from_block, write


def living_soil():
    rnd = random.Random(701)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    soil = [hexc("2b1d12"), hexc("3a2817"), hexc("4a331d"), hexc("5b3f24")]
    for y in range(16):
        for x in range(16):
            px[x, y] = soil[rnd.randrange(4)] if rnd.random() < 0.7 else soil[1]
    # Pale rootlets and a few green sprouts: this ground is still alive.
    for _ in range(4):
        x, y = rnd.randrange(16), rnd.randrange(16)
        for i in range(rnd.randint(2, 4)):
            px[(x + i) % 16, (y + i // 2) % 16] = hexc("c9b58c")
    for (x, y) in [(3, 4), (11, 9), (7, 13)]:
        px[x, y] = hexc("5fa83a")
        px[x, y - 1] = hexc("8fd45a")
    save(img, "block/living_soil")
    return img


def oasis_grass(soil):
    rnd = random.Random(702)
    top = Image.new("RGBA", (16, 16))
    px = top.load()
    greens = [hexc("2f6d22"), hexc("3f8a2c"), hexc("52a238"), hexc("6bbd45")]
    for y in range(16):
        for x in range(16):
            px[x, y] = greens[min(3, int(rnd.random() * 4))]
    for (x, y, c) in [(4, 3, "f2e35a"), (12, 6, "ffffff"), (7, 11, "e86aa8"), (13, 13, "f2e35a")]:
        px[x, y] = hexc(c)
    save(top, "block/oasis_grass_top")
    side = soil.copy()
    sp = side.load()
    for x in range(16):
        depth = 3 + (1 if rnd.random() < 0.5 else 0) + (1 if x % 5 == 0 else 0)
        for y in range(depth):
            sp[x, y] = greens[rnd.randrange(1, 4)] if y < depth - 1 else greens[0]
    save(side, "block/oasis_grass_side")


def canisters():
    o = OUTLINE
    rows = [
        "......oooo......",
        ".....oCCCCo.....",
        "......oCCo......",
        "....oooooooo....",
        "...oLLLLLLMMo...",
        "...oLHHHLLMMo...",
        "...oLHLLLLMDo...",
        "...oBBBBBBBBo...",
        "...oBBBBBBBBo...",
        "...oLLLLLLMMo...",
        "...oLHLLLLMDo...",
        "...oLLLLLLMDo...",
        "...oLLLLLLMDo...",
        "...oMMMMMMDDo...",
        "....oooooooo....",
        "................",
    ]
    metal = {"o": o, "C": hexc("4a5056"), "L": hexc("c9ccc8"), "H": hexc("eef0ec"), "M": hexc("9a9e9b"),
             "D": hexc("6b706c")}
    empty = grid(rows, dict(metal, B=hexc("6b706c")))
    save(empty, "item/water_canister")
    full = grid(rows, dict(metal, B=hexc("3f8fe0")))
    save(full, "item/clean_water_canister")


def models():
    write(ASSETS / "models/block/living_soil.json",
          {"parent": "minecraft:block/cube_all", "textures": {"all": NS + "block/living_soil"}})
    write(ASSETS / "blockstates/living_soil.json", {"variants": {"": [
        {"model": NS + "block/living_soil"}, {"model": NS + "block/living_soil", "y": 90},
        {"model": NS + "block/living_soil", "y": 180}]}})
    item_from_block("living_soil", "block/living_soil")
    write(ASSETS / "models/block/oasis_grass.json",
          {"parent": "minecraft:block/cube_bottom_top", "textures": {
              "top": NS + "block/oasis_grass_top", "side": NS + "block/oasis_grass_side",
              "bottom": NS + "block/living_soil"}})
    write(ASSETS / "blockstates/oasis_grass.json", {"variants": {"": [
        {"model": NS + "block/oasis_grass"}, {"model": NS + "block/oasis_grass", "y": 90},
        {"model": NS + "block/oasis_grass", "y": 180}, {"model": NS + "block/oasis_grass", "y": 270}]}})
    item_from_block("oasis_grass", "block/oasis_grass")
    for name in ("water_canister", "clean_water_canister"):
        write(ASSETS / "models/item" / (name + ".json"),
              {"parent": "minecraft:item/generated", "textures": {"layer0": NS + "item/" + name}})
        write(ASSETS / "items" / (name + ".json"),
              {"model": {"type": "minecraft:model", "model": NS + "item/" + name}})


if __name__ == "__main__":
    oasis_grass(living_soil())
    canisters()
    models()
    print("oasis assets written")
