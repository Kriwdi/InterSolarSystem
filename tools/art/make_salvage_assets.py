"""Item sprites of the salvage chain: plastic scrap, rubber scrap, glass shards and the cutting torch.

    python3 tools/art/make_salvage_assets.py
"""
import random

from PIL import Image

from make_textures import hexc, mix, save, shade
from make_models import ASSETS, NS, write

OUT = hexc("1e1a16")


def blob(name, seed, base, dark, pieces):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for (cx, cy, rx, ry) in pieces:
        for y in range(16):
            for x in range(16):
                d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2
                if d <= 1.0:
                    px[x, y] = mix(hexc(base), hexc(dark), min(1.0, d * 0.8 + rnd.random() * 0.25))
    # Outline.
    pts = [(x, y) for y in range(16) for x in range(16) if px[x, y][3]]
    for (x, y) in pts:
        for (dx, dy) in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            if 0 <= x + dx < 16 and 0 <= y + dy < 16 and px[x + dx, y + dy][3] == 0:
                px[x + dx, y + dy] = OUT
    save(img, "item/" + name)
    return img


def shards():
    rnd = random.Random(5)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for (x0, y0, w, h) in ((2, 3, 5, 7), (8, 2, 5, 5), (7, 9, 6, 5), (3, 11, 3, 3)):
        for y in range(h):
            for x in range(w - y * w // (h + 1)):
                c = mix(hexc("cfe6ee"), hexc("8db4c2"), rnd.random() * 0.6)
                if x == 0 or y == 0:
                    c = hexc("f2fbff")
                px[x0 + x, y0 + y] = c
    pts = [(x, y) for y in range(16) for x in range(16) if px[x, y][3]]
    for (x, y) in pts:
        for (dx, dy) in ((1, 0), (0, 1)):
            if x + dx < 16 and y + dy < 16 and px[x + dx, y + dy][3] == 0:
                px[x + dx, y + dy] = hexc("4f6a74")
    save(img, "item/glass_shards")


def torch():
    rows = [
        "............ff..",
        "...........fyf..",
        "..........fyf...",
        ".........ccf....",
        "........cMc.....",
        ".......cMc......",
        "......oMo.......",
        ".....oGGo.......",
        "....oGGo........",
        "...oGRGo........",
        "..oGGGo.........",
        ".oGGGo..........",
        ".ooGo...........",
        "..oo............",
        "................",
        "................",
    ]
    pal = {"f": hexc("ff9a2a"), "y": hexc("fff2a8"), "c": hexc("c3c9ce"), "M": hexc("6f757b"), "o": OUT,
           "G": hexc("d9a521"), "R": hexc("a8302a")}
    img = Image.new("RGBA", (16, 16))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in pal:
                img.putpixel((x, y), pal[ch])
    save(img, "item/cutting_torch")


if __name__ == "__main__":
    blob("plastic_scrap", 1, "e2e0d8", "9fa4a8", [(6, 7, 4, 3), (10, 10, 3.5, 3), (8, 5, 2.5, 2)])
    blob("rubber_scrap", 2, "3a3a3c", "141414", [(7, 8, 5, 3), (9, 5, 3, 2)])
    shards()
    torch()
    for name in ("plastic_scrap", "rubber_scrap", "glass_shards", "cutting_torch"):
        parent = "minecraft:item/handheld" if name == "cutting_torch" else "minecraft:item/generated"
        write(ASSETS / f"models/item/{name}.json", {"parent": parent, "textures": {"layer0": NS + "item/" + name}})
        write(ASSETS / f"items/{name}.json", {"model": {"type": "minecraft:model", "model": NS + "item/" + name}})
    print("salvage items written")
