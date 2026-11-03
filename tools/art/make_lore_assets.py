"""Item textures and models of the lore items: the handwritten note, the data tape (cassette) and the technical manual.

    python3 tools/art/make_lore_assets.py
"""
import random

from PIL import Image

from make_textures import hexc, mix, save, shade
from make_models import ASSETS, NS, write

OUT = hexc("1e1a16")


def note():
    rnd = random.Random(7)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    paper = hexc("e8dfc4")
    # A torn sheet, slightly rotated: rows shift by one pixel at the middle.
    for y in range(2, 15):
        off = 0 if y < 8 else 1
        left, right = 3 + off, 12 + off
        if y in (2, 14):
            left += 1
        for x in range(left, right + 1):
            c = mix(paper, hexc("c9b88f"), rnd.random() * 0.35 + (0.25 if x == right or y == 14 else 0))
            px[x, y] = c
        px[left - 1, y] = OUT
        px[right + 1, y] = OUT
    for x in range(3, 15):
        px[x, 1] = OUT if 4 <= x <= 13 else px[x, 1]
        px[x, 15] = OUT if 5 <= x <= 14 else px[x, 15]
    # Torn top edge and a coffee ring.
    for x in (5, 8, 11):
        px[x, 2] = (0, 0, 0, 0)
        px[x, 1] = (0, 0, 0, 0)
    for x, y in ((9, 10), (10, 9), (11, 10), (10, 11)):
        px[x, y] = shade(hexc("a07b4a"), 1.0)
    # Handwriting lines.
    ink = hexc("2c3d6b")
    for y in (4, 6, 8, 10, 12):
        off = 0 if y < 8 else 1
        x = 5 + off
        end = (11 if y != 12 else 8) + off
        while x <= end:
            if rnd.random() < 0.8:
                px[x, y] = ink if rnd.random() < 0.8 else shade(ink, 1.4)
            x += 1
    save(img, "item/note")


def data_tape():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    body = hexc("3a3d42")
    for y in range(3, 13):
        for x in range(1, 15):
            edge = x in (1, 14) or y in (3, 12)
            px[x, y] = OUT if edge else mix(body, hexc("23262a"), (y - 3) / 12)
    # Label (Aurora orange stripe) and the window with two reels.
    for x in range(3, 13):
        px[x, 4] = hexc("e9e4d4")
        px[x, 5] = hexc("d9822b")
    for y in range(7, 10):
        for x in range(4, 12):
            px[x, y] = hexc("6f7b80") if y != 8 else hexc("8d9aa0")
    for cx in (5, 10):
        px[cx, 8] = hexc("1a1a1a")
        px[cx - 1, 8] = hexc("5b3a22")
        px[cx + 1, 8] = hexc("5b3a22")
    for x in range(6, 10):
        px[x, 8] = hexc("4a2e1a")
    # Bottom notch with the screw holes.
    for x in range(4, 12):
        px[x, 11] = hexc("2b2e32")
    px[3, 11] = hexc("9aa1a8")
    px[12, 11] = hexc("9aa1a8")
    save(img, "item/data_tape")


def manual():
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    cover = hexc("1f4f86")
    for y in range(1, 15):
        for x in range(3, 14):
            edge = x in (3, 13) or y in (1, 14)
            c = OUT if edge else mix(cover, hexc("153760"), (x - 3) / 14 + (y - 1) / 40)
            px[x, y] = c
    # Spine (ring binding) and the pages' edge.
    for y in range(2, 14):
        px[4, y] = hexc("0f2440")
        if y % 2 == 0:
            px[3, y] = hexc("b8c0c8")
        px[13, y] = hexc("e9e4d4")
    px[14, 2] = OUT
    for y in range(2, 15):
        px[14, y] = OUT
    # Aurora badge: a white ring with an orange arc, and a title bar.
    for x, y in ((8, 4), (9, 4), (10, 5), (10, 6), (9, 7), (8, 7), (7, 6), (7, 5)):
        px[x, y] = hexc("e9e4d4")
    px[8, 5] = hexc("d9822b")
    px[9, 6] = hexc("d9822b")
    for x in range(6, 12):
        px[x, 10] = hexc("e9e4d4")
    for x in range(7, 11):
        px[x, 12] = hexc("8fb1d6")
    save(img, "item/manual")


if __name__ == "__main__":
    note()
    data_tape()
    manual()
    for name in ("note", "data_tape", "manual"):
        write(ASSETS / f"models/item/{name}.json", {"parent": "minecraft:item/generated", "textures": {"layer0": NS + "item/" + name}})
        write(ASSETS / f"items/{name}.json", {"model": {"type": "minecraft:model", "model": NS + "item/" + name}})
    print("lore item assets written")
