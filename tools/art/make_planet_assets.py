"""The ground of the new worlds beyond Mars (Venus, Mercury, Ceres, the asteroid belt, Jupiter's cloud decks, Io,
Europa), the new items (suit modules, iridium, the plasma drive kit, metallic hydrogen), and the generated Java
(PlanetBlocks.java), loot tables, mining tags and language rows for them. One table drives it all.

    python3 tools/art/make_planet_assets.py
"""
import json
import math
import random
import subprocess
from pathlib import Path

from PIL import Image

from make_textures import TEX, hexc, mix, save, shade
from make_models import ASSETS, NS, item_from_block, write

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "src/main/java/hu/kriwdi/intersolarsystem/content/PlanetBlocks.java"
LOOT = ROOT / "src/main/generated/data/intersolarsystem/loot_table/blocks"
TAGS = ROOT / "src/main/resources/data/minecraft/tags/block/mineable"


def grainy(seed, base, spread, specks, cols, streaks=None, bands=None):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            c = base if not bands else bands[(y // 3) % len(bands)]
            px[x, y] = shade(c, 1 + rnd.uniform(-spread, spread))
    for _ in range(specks):
        x, y = rnd.randrange(16), rnd.randrange(16)
        px[x, y] = rnd.choice(cols)
    for (x0, y0, dx, dy, n, col) in streaks or ():
        for i in range(n):
            x, y = x0 + dx * i, y0 + dy * i
            if 0 <= x < 16 and 0 <= y < 16:
                px[x, y] = col
    return img


def spots(img, points, col, edge=None):
    px = img.load()
    for (x, y) in points:
        for (dx, dy) in ((0, 0), (1, 0), (0, 1), (1, 1)):
            if x + dx < 16 and y + dy < 16:
                px[x + dx, y + dy] = col if (dx, dy) == (0, 0) or not edge else edge
    return img


def cloud(seed, base, light, dark):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 0.5 + 0.25 * math.sin(x * 0.6 + rnd.random() * 0.3) * math.cos(y * 0.5) + rnd.uniform(-0.15, 0.15)
            px[x, y] = mix(dark, light, max(0.0, min(1.0, v)))
    return img


# name: (texture maker, tool, strength, sound, light, en, de, hu)
BLOCKS = {
    # Venus
    "venus_basalt": (lambda: grainy(501, hexc("4a3a30"), 0.08, 26, [hexc("2e241e"), hexc("6a5442"), hexc("8a5a30")],
                                    [(2, 3, 1, 1, 6, hexc("221a15")), (9, 12, 1, -1, 5, hexc("221a15"))]),
                     "pickaxe", 2.2, "STONE", 0, "Venusian Basalt", "Venusbasalt", "Vénuszi bazalt"),
    "venus_regolith": (lambda: grainy(502, hexc("9a6a3e"), 0.08, 20, [hexc("6a4628"), hexc("b8824c"), hexc("4a3a30")]),
                       "shovel", 0.7, "SAND", 0, "Venusian Regolith", "Venus-Regolith", "Vénuszi regolit"),
    "sulfur_crust": (lambda: grainy(503, hexc("d8c24a"), 0.07, 18, [hexc("f2e27a"), hexc("a8922a"), hexc("8a5a30")]),
                     "shovel", 0.6, "CALCITE", 0, "Sulfur Crust", "Schwefelkruste", "Kénkéreg"),
    # Mercury
    "mercury_rock": (lambda: grainy(511, hexc("5a5652"), 0.08, 24, [hexc("3e3b38"), hexc("7a7570"), hexc("2e2c2a")],
                                    [(1, 5, 1, 1, 7, hexc("2a2826"))]),
                     "pickaxe", 2.0, "STONE", 0, "Mercurian Rock", "Merkurgestein", "Merkúri kőzet"),
    "mercury_regolith": (lambda: grainy(512, hexc("6e6a64"), 0.07, 18, [hexc("4e4a46"), hexc("8e8a84")]),
                         "shovel", 0.6, "SAND", 0, "Mercurian Regolith", "Merkur-Regolith", "Merkúri regolit"),
    "hollow_crust": (lambda: grainy(513, hexc("9ab4c4"), 0.06, 16, [hexc("c8dce8"), hexc("6a8494")]),
                     "shovel", 0.5, "CALCITE", 3, "Hollows Crust", "Mulden-Kruste", "Mélyedéskéreg"),
    # Ceres
    "ceres_clay": (lambda: grainy(521, hexc("3c3a38"), 0.07, 18, [hexc("2a2826"), hexc("54504c")]),
                   "shovel", 0.7, "MUD", 0, "Cerean Clay", "Ceres-Ton", "Ceresi agyag"),
    "ceres_ice": (lambda: grainy(522, hexc("b8c4c8"), 0.05, 10, [hexc("8a9498"), hexc("dce6ea")]),
                  "pickaxe", 1.0, "GLASS", 0, "Cerean Ice", "Ceres-Eis", "Ceresi jég"),
    "carbonate_salt": (lambda: grainy(523, hexc("eae8e0"), 0.04, 12, [hexc("ffffff"), hexc("c8c4b8")]),
                       "shovel", 0.5, "CALCITE", 6, "Carbonate Salt", "Karbonatsalz", "Karbonátsó"),
    # The asteroid belt
    "chondrite": (lambda: spots(grainy(531, hexc("6e665c"), 0.08, 22, [hexc("4e4840"), hexc("8e8478")]),
                                [(3, 3), (10, 5), (6, 11), (12, 12)], hexc("9a948a"), hexc("5a544c")),
                  "pickaxe", 2.0, "STONE", 0, "Chondrite", "Chondrit", "Kondrit"),
    "carbonaceous_rock": (lambda: grainy(532, hexc("26241f"), 0.07, 14, [hexc("16150f"), hexc("3a3830")]),
                          "pickaxe", 1.6, "STONE", 0, "Carbonaceous Rock", "Kohliges Gestein", "Széntartalmú kőzet"),
    "nickel_iron_ore": (lambda: spots(grainy(533, hexc("6e665c"), 0.08, 12, [hexc("4e4840")]),
                                      [(2, 2), (5, 9), (11, 4), (13, 12), (8, 13)], hexc("c0c4c8"), hexc("8a9096")),
                        "pickaxe", 3.5, "METAL", 0, "Nickel-Iron Ore", "Nickel-Eisen-Erz", "Nikkel-vas érc"),
    "iridium_ore": (lambda: spots(grainy(534, hexc("8a9096"), 0.06, 10, [hexc("6e747a")]),
                                  [(3, 4), (10, 9), (6, 12)], hexc("e8f0ff"), hexc("a8b8d8")),
                    "pickaxe", 4.5, "METAL", 2, "Iridium Ore", "Iridiumerz", "Irídiumérc"),
    # Jupiter's cloud decks
    "ammonia_cloud": (lambda: cloud(541, hexc("f2efe6"), hexc("ffffff"), hexc("d8d2c4")),
                      "shovel", 0.3, "WOOL", 0, "Ammonia Cloud", "Ammoniakwolke", "Ammóniafelhő"),
    "sulfide_cloud": (lambda: cloud(542, hexc("c8864a"), hexc("e8b07a"), hexc("8a5430")),
                      "shovel", 0.4, "WOOL", 0, "Hydrosulfide Cloud", "Hydrosulfidwolke", "Hidroszulfid-felhő"),
    "water_cloud": (lambda: cloud(543, hexc("6a7e94"), hexc("94a8bc"), hexc("3e4e60")),
                    "shovel", 0.5, "WOOL", 0, "Water Cloud", "Wasserwolke", "Vízfelhő"),
    "storm_cloud": (lambda: cloud(544, hexc("b8483a"), hexc("d87a5a"), hexc("7a2a20")),
                    "shovel", 0.4, "WOOL", 0, "Great Red Spot Cloud", "Wolke des Großen Roten Flecks",
                    "A Nagy Vörös Folt felhője"),
    "metallic_hydrogen": (lambda: spots(grainy(545, hexc("2a3038"), 0.06, 12, [hexc("4a5462")]),
                                        [(4, 4), (11, 6), (7, 11)], hexc("c8f0ff"), hexc("6ab8d8")),
                          "pickaxe", 5.0, "AMETHYST", 9, "Metallic Hydrogen Crystal", "Metallischer Wasserstoff",
                          "Fémes hidrogén kristály"),
    # Io
    "sulfur_plain": (lambda: grainy(551, hexc("e2c84a"), 0.08, 22, [hexc("f8e88a"), hexc("b8962a"), hexc("d87a2a")]),
                     "shovel", 0.6, "SAND", 0, "Ionian Sulfur", "Io-Schwefel", "Iói kén"),
    "io_silicate": (lambda: grainy(552, hexc("6a3a26"), 0.08, 20, [hexc("4a2618"), hexc("8a4e30"), hexc("2a1810")]),
                    "pickaxe", 2.0, "STONE", 0, "Ionian Silicate Rock", "Io-Silikatgestein", "Iói szilikátkőzet"),
    "so2_frost": (lambda: grainy(553, hexc("f0eee4"), 0.04, 10, [hexc("ffffff"), hexc("d8d0b8")]),
                  "shovel", 0.4, "SNOW", 0, "Sulfur Dioxide Frost", "Schwefeldioxid-Reif", "Kén-dioxid dér"),
    # Europa
    "europa_ice": (lambda: grainy(561, hexc("e8e2d8"), 0.04, 12, [hexc("ffffff"), hexc("c8bcae")]),
                   "pickaxe", 1.4, "GLASS", 0, "Europan Ice", "Europa-Eis", "Európai jég"),
    "lineae_ice": (lambda: grainy(562, hexc("dcd2c4"), 0.04, 8, [hexc("f0e8dc")],
                                  [(0, 5, 1, 0, 16, hexc("9a5a3a")), (0, 6, 1, 0, 16, hexc("b87a5a")),
                                   (7, 0, 0, 1, 16, hexc("a86a4a"))]),
                   "pickaxe", 1.4, "GLASS", 0, "Lineae Ice", "Lineae-Eis", "Vonalas jég"),
    "europa_salt": (lambda: grainy(563, hexc("b87a5a"), 0.06, 14, [hexc("d89a7a"), hexc("8a5a3a")]),
                    "shovel", 0.6, "CALCITE", 0, "Europan Salt", "Europa-Salz", "Európai só"),
    "microbial_mat": (lambda: spots(grainy(564, hexc("2a4a48"), 0.1, 18, [hexc("3a7a6a"), hexc("1a2a2a")]),
                                    [(3, 3), (9, 6), (5, 11), (12, 12)], hexc("7affd8"), hexc("3ab898")),
                      "shovel", 0.5, "SLIME_BLOCK", 10, "Microbial Mat", "Mikrobenmatte", "Mikrobaszőnyeg"),
}

# Items: name: (sprite rows or None for a plain generated icon, en, de, hu)
ITEMS = {
    "iridium_ingot": ("Iridium Ingot", "Iridiumbarren", "Irídiumrúd"),
    "plasma_drive_kit": ("Plasma Drive Kit (tier 4, mothership)", "Plasmaantriebsset (Stufe 4, Mutterschiff)",
                         "Plazmahajtómű-készlet (4. szint, anyahajó)"),
    "thermal_module": ("Suit Module: Heat Shield", "Anzugmodul: Hitzeschild", "Ruhamodul: hőpajzs"),
    "pressure_module": ("Suit Module: Pressure Shell", "Anzugmodul: Druckhülle", "Ruhamodul: nyomásálló réteg"),
    "radiation_module": ("Suit Module: Radiation Shielding", "Anzugmodul: Strahlenschutz", "Ruhamodul: sugárvédelem"),
    "tank_module": ("Suit Module: Extended Tank", "Anzugmodul: Großer Tank", "Ruhamodul: nagy tartály"),
}

ITEM_COLOURS = {"iridium_ingot": ("e8f0ff", "8a9ab8"), "plasma_drive_kit": ("b07ae0", "4a2a6a"),
                "thermal_module": ("e88a3a", "6a3a1a"), "pressure_module": ("6a7a8a", "2a3a4a"),
                "radiation_module": ("f2d84a", "4a4a1a"), "tank_module": ("4fc3f7", "1a4a6a")}


def item_icon(name):
    light, dark = (hexc(c) for c in ITEM_COLOURS[name])
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    out = hexc("1e1a16")
    if name == "iridium_ingot":
        for y in range(6, 12):
            for x in range(2 + (11 - y) // 2, 14 - (11 - y) // 2):
                px[x, y] = light if y < 8 else mix(light, dark, (y - 7) / 5)
    elif name == "plasma_drive_kit":
        for y in range(3, 14):
            for x in range(2, 14):
                px[x, y] = dark if x in (2, 13) or y in (3, 13) else mix(light, dark, abs(x - 7.5) / 8)
        for (x, y) in ((7, 6), (8, 6), (6, 7), (9, 7), (6, 8), (9, 8), (7, 9), (8, 9)):
            px[x, y] = hexc("ffffff")
    else:
        # A suit module: a curved plate with clips and a coloured band.
        for y in range(3, 14):
            for x in range(3, 13):
                if (x - 7.5) ** 2 / 25 + (y - 8) ** 2 / 36 <= 1:
                    px[x, y] = light if 6 <= y <= 9 else shade(hexc("c3c9ce"), 1 - abs(x - 7.5) * 0.03)
        for (x, y) in ((4, 4), (11, 4), (4, 12), (11, 12)):
            px[x, y] = dark
    pts = [(x, y) for y in range(16) for x in range(16) if px[x, y][3]]
    for (x, y) in pts:
        for (dx, dy) in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            if 0 <= x + dx < 16 and 0 <= y + dy < 16 and px[x + dx, y + dy][3] == 0:
                px[x + dx, y + dy] = out
    save(img, "item/" + name)


def java():
    lines = ["package hu.kriwdi.intersolarsystem.content;", "",
             "import net.minecraft.world.level.block.Block;",
             "import net.minecraft.world.level.block.SoundType;",
             "import net.minecraft.world.level.block.state.BlockBehaviour;", "",
             "/** The ground of the worlds beyond Mars. Generated by tools/art/make_planet_assets.py: do not edit. */",
             "public final class PlanetBlocks {"]
    for name, (_, tool, strength, sound, light, *_n) in BLOCKS.items():
        const = name.upper()
        props = f"BlockBehaviour.Properties.of().strength({strength}F, {max(strength, 3.0)}F).sound(SoundType.{sound})"
        if tool == "pickaxe":
            props += ".requiresCorrectToolForDrops()"
        if light:
            props += f".lightLevel(s -> {light})"
        if sound == "WOOL":
            props += ".noOcclusion()"
        lines.append(f"\tpublic static final Block {const} = ModBlocks.registerExternal(\"{name}\", Block::new,")
        lines.append(f"\t\t\t{props});")
    lines += ["", "\tprivate PlanetBlocks() {", "\t}", "", "\tpublic static void initialize() {", "\t}", "}", ""]
    JAVA.write_text("\n".join(lines))


def main():
    for name, (make, tool, *_rest) in BLOCKS.items():
        save(make(), "block/" + name)
        write(ASSETS / "models/block" / (name + ".json"),
              {"parent": "minecraft:block/cube_all", "textures": {"all": NS + "block/" + name}})
        variants = [{"model": NS + "block/" + name}] + [{"model": NS + "block/" + name, "y": r} for r in (90, 180, 270)]
        write(ASSETS / "blockstates" / (name + ".json"), {"variants": {"": variants}})
        item_from_block(name, "block/" + name)
        loot = {"type": "minecraft:block", "pools": [{"bonus_rolls": 0.0, "conditions": [
            {"condition": "minecraft:survives_explosion"}], "entries": [{"type": "minecraft:item", "name": NS + name}],
            "rolls": 1.0}], "random_sequence": NS + "blocks/" + name}
        ref = json.loads((LOOT / "lunar_rock.json").read_text())
        (LOOT / (name + ".json")).write_text(json.dumps(ref, indent=2).replace("lunar_rock", name))
    for tool in ("pickaxe", "shovel"):
        path = TAGS / (tool + ".json")
        data = json.loads(path.read_text())
        for name, (_, t, *_r) in BLOCKS.items():
            if t == tool and NS + name not in data["values"]:
                data["values"].append(NS + name)
        path.write_text(json.dumps(data, indent=2) + "\n")
    for name in ITEMS:
        item_icon(name)
        write(ASSETS / f"models/item/{name}.json", {"parent": "minecraft:item/generated",
                                                    "textures": {"layer0": NS + "item/" + name}})
        write(ASSETS / f"items/{name}.json", {"model": {"type": "minecraft:model", "model": NS + "item/" + name}})
    java()
    rows = [f"block.intersolarsystem.{n}|{v[5]}|{v[6]}|{v[7]}" for n, v in BLOCKS.items()]
    rows += [f"item.intersolarsystem.{n}|{v[0]}|{v[1]}|{v[2]}" for n, v in ITEMS.items() if n != "plasma_drive_kit"]
    rows_file = Path(__file__).resolve().parent / "planet_lang_rows.txt"
    rows_file.write_text("\n".join(rows) + "\n")
    print("planet assets:", len(BLOCKS), "blocks,", len(ITEMS), "items")


if __name__ == "__main__":
    main()
