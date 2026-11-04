"""Item sprites of the spacewalk gear: the safety tether (a coiled yellow line with a carabiner), the tether spool,
the manoeuvring unit (a backpack with jet nozzles and hand controllers) and the nitrogen tank.

    python3 tools/art/make_eva_assets.py
"""
from PIL import Image

from make_textures import hexc, save
from make_models import ASSETS, NS, write

PAL = {
    "o": hexc("1e1a16"), "y": hexc("f2c744"), "Y": hexc("b08a1e"), "s": hexc("c3c9ce"), "S": hexc("7d848a"),
    "w": hexc("eef0f0"), "W": hexc("b9bec2"), "g": hexc("5a6066"), "b": hexc("4fc3f7"), "r": hexc("d8442e"),
    "k": hexc("2c3034"), "n": hexc("3f78b5"), "N": hexc("244a75"), "h": hexc("ffffff"),
}

SPRITES = {
    "safety_tether": [
        "................",
        "........ooo.....",
        ".......osSSo....",
        ".......os..So...",
        "....oooos..So...",
        "...oyyyyoSSo....",
        "..oyYooYyoo.....",
        ".oyYo..oYyo.....",
        ".oyo....oyo.....",
        ".oyo....oyo.....",
        ".oyYo..oYyo.....",
        "..oyYooYyo......",
        "...oyyyyo.......",
        "....oooo........",
        "................",
        "................",
    ],
    "tether_spool": [
        "................",
        "...oooooooooo...",
        "...oSSSSSSSSo...",
        "...oooooooooo...",
        "....oyyyyyyo....",
        "....oYyyyyYo....",
        "....oyyyyyyo....",
        "....oYyyyyYo....",
        "....oyyyyyyo....",
        "....oYyyyyYo....",
        "....oyyyyyyo....",
        "...oooooooooo...",
        "...oSSSSSSSSo...",
        "...oooooooooo...",
        "................",
        "................",
    ],
    "mmu_pack": [
        "................",
        "...oooooooooo...",
        "..owwwwwwwwwwo..",
        ".oowWWwwwwWWwoo.",
        "ogowwwwrrwwwwogo",
        "okowwwwwwwwwwoko",
        "ogowWWwwwwWWwogo",
        ".oowwwwbbwwwwoo.",
        "..owwwwbbwwwwo..",
        "..owWWwwwwWWwo..",
        "..owwwwwwwwwwo..",
        "..oooooooooooo..",
        "..ogo..oo..ogo..",
        "..oko......oko..",
        "...o........o...",
        "................",
    ],
    "nitrogen_tank": [
        "................",
        "......oooo......",
        "......osSo......",
        ".....oooooo.....",
        "....onnnnnno....",
        "...onhnnnnnNo...",
        "...onhnnnnnNo...",
        "...onnnnnnnNo...",
        "...owwwwwwwWo...",
        "...okkwkkwkWo...",
        "...owwwwwwwWo...",
        "...onnnnnnnNo...",
        "...onnnnnnnNo...",
        "....onnnnnNo....",
        ".....oooooo.....",
        "................",
    ],
}


def main():
    for name, rows in SPRITES.items():
        img = Image.new("RGBA", (16, 16))
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch in PAL:
                    img.putpixel((x, y), PAL[ch])
        save(img, "item/" + name)
        write(ASSETS / f"models/item/{name}.json", {"parent": "minecraft:item/generated",
                                                    "textures": {"layer0": NS + "item/" + name}})
        write(ASSETS / f"items/{name}.json", {"model": {"type": "minecraft:model", "model": NS + "item/" + name}})
    print("eva items written")


if __name__ == "__main__":
    main()
