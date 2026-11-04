"""The ground, clouds and special blocks of the outer system (Ganymede, Callisto, Saturn and its rings, Titan,
Enceladus, Uranus, Miranda, Titania, Neptune, Triton and the moon nobody names), its new items (the drive kits of
tiers 5 to 7, the biosample, tholin resin), and what goes with them: textures, models, blockstates, the generated
Java (OuterBlocks.java), loot tables, mining tags and language rows. One table drives it all.

Textures are drawn from tileable value noise mapped onto a four-colour ramp, with a surface pattern per block
(striations, cracks, pebbles, frost sparkle, crystal facets, flow lines...), so every block reads as its own material.

    python3 tools/art/make_outer_assets.py
"""
import json
import math
import random
from pathlib import Path

from PIL import Image

from make_textures import hexc, mix, save, shade
from make_models import ASSETS, NS, box, item_from_block, model, write

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "src/main/java/hu/kriwdi/intersolarsystem/content/OuterBlocks.java"
LOOT = ROOT / "src/main/generated/data/intersolarsystem/loot_table/blocks"
TAGS = ROOT / "src/main/resources/data/minecraft/tags/block/mineable"


# ---------------------------------------------------------------- noise and patterns
def tile_noise(seed, cells):
    """Tileable smooth value noise, 16 x 16, 0..1."""
    rnd = random.Random(seed)
    g = [[rnd.random() for _ in range(cells)] for _ in range(cells)]
    out = [[0.0] * 16 for _ in range(16)]
    for y in range(16):
        for x in range(16):
            fx, fy = x / 16 * cells, y / 16 * cells
            ix, iy = int(fx), int(fy)
            tx, ty = fx - ix, fy - iy
            tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
            a, b = g[iy % cells][ix % cells], g[iy % cells][(ix + 1) % cells]
            c, d = g[(iy + 1) % cells][ix % cells], g[(iy + 1) % cells][(ix + 1) % cells]
            top = a + (b - a) * tx
            bot = c + (d - c) * tx
            out[y][x] = top + (bot - top) * ty
    return out


def ramp(pal, t):
    t = max(0.0, min(0.999, t))
    i = int(t * (len(pal) - 1))
    return mix(pal[i], pal[i + 1], t * (len(pal) - 1) - i)


def surface(seed, pal, style):
    pal = [hexc(c) for c in pal]
    rnd = random.Random(seed)
    n1, n2, n3 = tile_noise(seed, 4), tile_noise(seed + 1, 8), tile_noise(seed + 2, 16)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 0.5 * n1[y][x] + 0.32 * n2[y][x] + 0.18 * n3[y][x]
            if style == "striations":
                v = 0.55 * v + 0.45 * (0.5 + 0.5 * math.sin((x + y * 0.35) * 1.25 + n1[y][x] * 2))
            elif style == "dunes":
                v = 0.6 * v + 0.4 * (0.5 + 0.5 * math.sin(y * 0.9 + 1.6 * math.sin(x * 0.4)))
            elif style == "flows":
                v = 0.5 * v + 0.5 * (0.5 + 0.5 * math.sin(x * 0.55 + 3 * n1[y][x]))
            elif style == "cliff":
                v = 0.6 * v + 0.4 * (0.5 + 0.5 * math.sin(y * 1.5 + n2[y][x] * 3))
            elif style == "wet":
                v = v * 0.8 + (0.25 if n3[y][x] > 0.75 else 0.0)
            px[x, y] = ramp(pal, v)
    if style in ("cracks", "veins"):
        col = shade(pal[0], 0.7) if style == "cracks" else pal[3]
        for _ in range(3 if style == "cracks" else 4):
            x, y = rnd.randrange(16), rnd.randrange(16)
            for _ in range(rnd.randint(6, 12)):
                px[x % 16, y % 16] = col
                if style == "veins":
                    px[(x + 1) % 16, y % 16] = mix(col, pal[2], 0.5)
                x += rnd.choice((-1, 0, 1, 1))
                y += rnd.choice((0, 1, 1))
    if style in ("pebbles", "dust", "dimples"):
        for _ in range(9 if style == "pebbles" else 14):
            x, y = rnd.randrange(16), rnd.randrange(16)
            if style == "dimples":
                for (dx, dy) in ((0, 0), (1, 0), (0, 1), (1, 1)):
                    px[(x + dx) % 16, (y + dy) % 16] = shade(px[(x + dx) % 16, (y + dy) % 16], 0.82)
                px[(x + 2) % 16, y % 16] = pal[3]
            else:
                px[x, y] = pal[3] if rnd.random() < 0.5 else shade(pal[0], 0.8)
                if style == "pebbles":
                    px[(x + 1) % 16, (y + 1) % 16] = shade(pal[0], 0.75)
    if style in ("sparkle", "crystal"):
        for _ in range(7 if style == "sparkle" else 5):
            x, y = rnd.randrange(16), rnd.randrange(16)
            px[x, y] = hexc("ffffff")
            if style == "crystal":
                for k in range(1, 3):
                    px[(x + k) % 16, (y + k) % 16] = mix(pal[3], hexc("ffffff"), 0.5)
                    px[(x - k) % 16, (y + k) % 16] = shade(pal[1], 0.9)
    if style == "colony":
        for _ in range(6):
            x, y = rnd.randrange(16), rnd.randrange(16)
            for (dx, dy) in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
                px[(x + dx) % 16, (y + dy) % 16] = pal[3] if (dx, dy) == (0, 0) else mix(pal[2], pal[3], 0.4)
    return img


def cloud(seed, base, light, dark):
    base, light, dark = hexc(base), hexc(light), hexc(dark)
    n1, n2 = tile_noise(seed, 4), tile_noise(seed + 5, 8)
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            v = 0.65 * n1[y][x] + 0.35 * n2[y][x] + 0.08 * math.sin(y * 0.8)
            px[x, y] = mix(dark, base, min(1.0, v * 1.6)) if v < 0.6 else mix(base, light, min(1.0, (v - 0.6) * 2.5))
    return img


# name: (texture, tool, strength, sound, light, en, de, hu, drops)
B = {
    # Ganymede
    "ganymede_dark_ice": (("3a342e", "4e463d", "62584c", "7a6e60"), "pebbles", "pickaxe", 1.4, "GLASS", 0,
                          "Dark Ganymedean Ice", "Dunkles Ganymed-Eis", "Sötét ganümédészi jég"),
    "grooved_ice": (("9ea6a8", "b8bfc0", "d0d6d6", "e8ecec"), "striations", "pickaxe", 1.4, "GLASS", 0,
                    "Grooved Ice", "Rilleneis", "Barázdás jég"),
    "ganymede_frost": (("c8d0d8", "dde4ea", "eef2f6", "ffffff"), "sparkle", "shovel", 0.4, "SNOW", 0,
                       "Polar Frost", "Polarreif", "Sarki dér"),
    # Callisto
    "callisto_regolith": (("241f1b", "312a24", "3f362e", "4d4237"), "dust", "shovel", 0.6, "SAND", 0,
                          "Callistoan Regolith", "Kallisto-Regolith", "Kallisztói regolit"),
    "callisto_ice": (("6f7478", "8c9296", "a9aeb2", "c6cacd"), "cracks", "pickaxe", 1.2, "GLASS", 0,
                     "Callistoan Ice", "Kallisto-Eis", "Kallisztói jég"),
    "pinnacle_frost": (("d6dee4", "e6ecf0", "f4f8fa", "ffffff"), "crystal", "pickaxe", 0.8, "GLASS", 3,
                       "Pinnacle Frost", "Zinnenreif", "Tüskedér"),
    # Saturn
    "saturn_haze": ("cloud", ("f1e2b8", "fff4d6", "d9c48e"), "shovel", 0.3, "WOOL", 0,
                    "Saturnian Haze", "Saturn-Dunst", "Szaturnuszi pára"),
    "saturn_belt_cloud": ("cloud", ("c9a873", "e2c799", "9c7b4c"), "shovel", 0.4, "WOOL", 0,
                          "Saturnian Belt Cloud", "Saturn-Bandwolke", "Szaturnuszi sávfelhő"),
    "hexagon_storm_cloud": ("cloud", ("5a7088", "7e94aa", "34465a"), "shovel", 0.4, "WOOL", 0,
                            "Hexagon Storm Cloud", "Hexagon-Sturmwolke", "Hatszögvihar-felhő"),
    # The rings
    "ring_ice": (("c8dbe6", "dbe8f0", "eaf2f7", "ffffff"), "crystal", "pickaxe", 0.9, "GLASS", 0,
                 "Ring Ice", "Ringeis", "Gyűrűjég"),
    "dusty_ring_ice": (("7e7a74", "98938b", "b2ada3", "ccc6bb"), "pebbles", "pickaxe", 0.9, "GLASS", 0,
                       "Dusty Ring Ice", "Staubiges Ringeis", "Poros gyűrűjég"),
    "deuterium_ice": (("4f86c0", "7aaee0", "a8ccf0", "e0f0ff"), "crystal", "pickaxe", 2.5, "AMETHYST", 5,
                      "Heavy-Water Ice", "Schwerwassereis", "Nehézvízjég"),
    # Titan
    "tholin_sand": (("7a4a1e", "8f5a28", "a66c34", "bd8044"), "dunes", "shovel", 0.6, "SAND", 0,
                    "Tholin Sand", "Tholin-Sand", "Tolinhomok"),
    "titan_bedrock_ice": (("5c5246", "6e6355", "807463", "958877"), "pebbles", "pickaxe", 1.6, "STONE", 0,
                          "Titanian Ice Bedrock", "Titan-Eisgestein", "Titáni jég-alapkőzet"),
    "hydrocarbon_sludge": (("1e1810", "2a2216", "372d1c", "4a3c26"), "wet", "shovel", 0.5, "MUD", 0,
                           "Hydrocarbon Sludge", "Kohlenwasserstoffschlamm", "Szénhidrogén-iszap"),
    "cryolava_ice": (("9fc0cc", "b6d2db", "cde3ea", "e6f3f7"), "flows", "pickaxe", 1.4, "GLASS", 0,
                     "Cryolava Ice", "Kryolava-Eis", "Krioláva-jég"),
    # Enceladus
    "enceladus_snow": (("e2ecf2", "eef5f9", "f7fbfd", "ffffff"), "sparkle", "shovel", 0.3, "SNOW", 0,
                       "Geyser Snow", "Geysirschnee", "Gejzírhó"),
    "enceladus_ice": (("b8c8d4", "ccdae4", "dfe9f0", "f2f7fa"), "cracks", "pickaxe", 1.2, "GLASS", 0,
                      "Enceladan Ice", "Enceladus-Eis", "Enceladusi jég"),
    "fissure_ice": (("2f6f9a", "3f86b5", "5aa2cf", "9ad4f2"), "veins", "pickaxe", 1.4, "GLASS", 3,
                    "Tiger Stripe Ice", "Tigerstreifen-Eis", "Tigriscsík-jég"),
    "vent_colony": (("1c3a34", "2c5a4e", "3f7c68", "8affd0"), "colony", "shovel", 0.5, "SLIME_BLOCK", 9,
                    "Vent Colony", "Schlotkolonie", "Kürtőtelep"),
    # Uranus
    "uranus_methane_cloud": ("cloud", ("9fdfe4", "c4f0f2", "6fb9c2"), "shovel", 0.3, "WOOL", 0,
                             "Uranian Methane Cloud", "Uranus-Methanwolke", "Uránuszi metánfelhő"),
    "deep_sulfide_cloud": ("cloud", ("8e98a4", "aab2bc", "66707c"), "shovel", 0.4, "WOOL", 0,
                           "Hydrogen Sulfide Cloud", "Schwefelwasserstoffwolke", "Kén-hidrogén-felhő"),
    "diamond_hail": (("5f98b0", "9fd2e4", "d4f2fa", "ffffff"), "crystal", "pickaxe", 3.0, "AMETHYST", 6,
                     "Diamond Hail", "Diamantenhagel", "Gyémántjégeső"),
    # Miranda
    "miranda_ice": (("7d8288", "92979c", "a7acb0", "bcc0c4"), "pebbles", "pickaxe", 1.2, "GLASS", 0,
                    "Mirandan Ice", "Miranda-Eis", "Mirandai jég"),
    "corona_ice": (("4f5358", "63676c", "777b80", "8b8f94"), "striations", "pickaxe", 1.2, "GLASS", 0,
                   "Corona Ice", "Koronaeis", "Koronajég"),
    "scarp_ice": (("a8acae", "bcbfc0", "d0d2d2", "e4e5e5"), "cliff", "pickaxe", 1.8, "STONE", 0,
                  "Scarp Ice", "Steilwandeis", "Szirtjég"),
    # Titania
    "titania_ice": (("6c6862", "807b74", "948f86", "a8a398"), "cracks", "pickaxe", 1.3, "GLASS", 0,
                    "Titania Ice", "Titania-Eis", "Titániai jég"),
    "titania_frost": (("d2d8dc", "e0e6e8", "eef2f3", "ffffff"), "sparkle", "shovel", 0.4, "SNOW", 0,
                      "Carbon Dioxide Frost", "Kohlendioxidreif", "Szén-dioxid dér"),
    # Neptune
    "neptune_methane_cloud": ("cloud", ("3f63c8", "6286e0", "2a4496"), "shovel", 0.3, "WOOL", 0,
                              "Neptunian Methane Cloud", "Neptun-Methanwolke", "Neptunuszi metánfelhő"),
    "dark_spot_cloud": ("cloud", ("1c2a5a", "2c3e78", "10183a"), "shovel", 0.4, "WOOL", 0,
                        "Dark Spot Cloud", "Wolke des Dunklen Flecks", "A Sötét Folt felhője"),
    # Triton
    "nitrogen_ice": (("c9b4b0", "d8c6c2", "e6d8d4", "f4ebe8"), "sparkle", "pickaxe", 1.0, "GLASS", 0,
                     "Nitrogen Ice", "Stickstoffeis", "Nitrogénjég"),
    "cantaloupe_ice": (("7e8270", "92957f", "a5a88f", "b8baa0"), "dimples", "pickaxe", 1.3, "GLASS", 0,
                       "Cantaloupe Terrain Ice", "Kantalupen-Eis", "Sárgadinnyehéj-jég"),
    "geyser_soot": (("1a1614", "26201c", "332b25", "40362e"), "dust", "shovel", 0.4, "SAND", 0,
                    "Geyser Soot", "Geysirruß", "Gejzírkorom"),
    # The moon nobody names
    "nameless_rock": (("2a2826", "383532", "46423e", "55504a"), "cracks", "pickaxe", 2.4, "STONE", 0,
                    "Dark Moon Rock", "Dunkles Mondgestein", "Sötét holdkőzet"),
    "silent_crystal": (("2a2440", "4a3a78", "8a6ac8", "d8c8ff"), "crystal", "pickaxe", 6.0, "AMETHYST", 7,
                       "Silent Crystal", "Stummer Kristall", "Néma kristály"),
}

DROPS = {"vent_colony": ("biosample", 1, 2), "diamond_hail": ("minecraft:diamond", 1, 2)}

# Special blocks: (Java factory, en, de, hu)
SPECIAL = {
    "geyser_vent": ("Geyser Vent", "Geysirschlot", "Gejzírkürtő"),
    "nitrogen_geyser": ("Nitrogen Geyser", "Stickstoffgeysir", "Nitrogéngejzír"),
    "dragonfly_flight_computer": ("Dragonfly Flight Computer", "Dragonfly-Flugrechner",
                                  "Dragonfly fedélzeti számítógép"),
}
DRAGONFLY_BOXES = [(1, 0, 3, 15, 6, 13), (2, 6, 4, 14, 14, 6), (3, 6, 6, 13, 8, 12)]

ITEMS = {
    "biosample": ("Enceladan Biosample", "Enceladus-Bioprobe", "Enceladusi biominta"),
    "tholin_resin": ("Tholin Resin", "Tholin-Harz", "Tolingyanta"),
    "metallic_hydrogen_drive_kit": ("Metallic Hydrogen Drive Kit (tier 5, mothership)",
                                    "Metallwasserstoff-Antriebsset (Stufe 5, Mutterschiff)",
                                    "Fémeshidrogén-hajtómű készlet (5. szint, anyahajó)"),
    "deuterium_drive_kit": ("Deuterium Torch Drive Kit (tier 6, mothership)",
                            "Deuterium-Fackelantriebsset (Stufe 6, Mutterschiff)",
                            "Deutérium-fáklyahajtómű készlet (6. szint, anyahajó)"),
    "turbo_drive_kit": ("Turbo Drive Kit (tier 7, mothership)", "Turboantriebsset (Stufe 7, Mutterschiff)",
                        "Turbóhajtómű-készlet (7. szint, anyahajó)"),
}
ITEM_COLOURS = {"biosample": ("8affd0", "1c3a34"), "tholin_resin": ("d8903a", "5a2e0e"),
                "metallic_hydrogen_drive_kit": ("9fe4ff", "1e3a52"), "deuterium_drive_kit": ("8fb8ff", "1e2a6a"),
                "turbo_drive_kit": ("d8c8ff", "2a2440")}


def item_icon(name):
    light, dark = (hexc(c) for c in ITEM_COLOURS[name])
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    out = hexc("1e1a16")
    if name == "biosample":
        # A sealed sample vial with glowing green-blue life in it.
        for y in range(2, 15):
            for x in range(5, 11):
                if y < 4:
                    px[x, y] = hexc("8a9096") if x in (5, 10) or y == 2 else hexc("c3c9ce")
                else:
                    glass = x in (5, 10) or y == 14
                    px[x, y] = hexc("d8e8ee") if glass else mix(dark, light, max(0, (y - 6) / 9))
        for (x, y) in ((7, 9), (8, 11), (7, 12), (9, 10)):
            px[x, y] = hexc("ffffff")
    elif name == "tholin_resin":
        for y in range(5, 13):
            for x in range(3, 13):
                if (x - 7.5) ** 2 / 25 + (y - 9) ** 2 / 12 <= 1:
                    px[x, y] = mix(light, dark, (y - 5) / 8)
        px[6, 7] = hexc("ffe0a0")
    else:
        # A drive kit: a crate with a glowing core and coil bands.
        for y in range(3, 14):
            for x in range(2, 14):
                px[x, y] = dark if x in (2, 13) or y in (3, 13) else mix(light, dark, abs(x - 7.5) / 7)
        for x in range(3, 13):
            for y in (5, 11):
                px[x, y] = shade(hexc("c3c9ce"), 0.9)
        for (x, y) in ((7, 7), (8, 7), (7, 8), (8, 8), (6, 8), (9, 7)):
            px[x, y] = hexc("ffffff")
    pts = [(x, y) for y in range(16) for x in range(16) if px[x, y][3]]
    for (x, y) in pts:
        for (dx, dy) in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            if 0 <= x + dx < 16 and 0 <= y + dy < 16 and px[x + dx, y + dy][3] == 0:
                px[x + dx, y + dy] = out
    save(img, "item/" + name)


def texture(name, spec):
    seed = sum(ord(c) * (i + 1) for i, c in enumerate(name))
    if spec[0] == "cloud":
        return cloud(seed, *spec[1])
    return surface(seed, spec[0], spec[1])


def vent_top(dark):
    rim = hexc("2a2420") if dark else hexc("eaf2f7")
    hole = hexc("0c0a08") if dark else hexc("1d4a6a")
    img = surface(717 if dark else 616, ("7e8270", "92957f", "a5a88f", "b8baa0") if dark else
                  ("b8c8d4", "ccdae4", "dfe9f0", "f2f7fa"), "cracks")
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = math.hypot(x - 7.5, y - 7.5)
            if d < 3.2:
                px[x, y] = hole
            elif d < 4.6:
                px[x, y] = rim
            elif d < 6.5 and (x + y) % 3 == 0:
                px[x, y] = hexc("1a1614") if dark else hexc("ffffff")
    return img


def dragonfly_textures():
    rnd = random.Random(2028)
    foil = Image.new("RGBA", (16, 16))
    px = foil.load()
    for y in range(16):
        for x in range(16):
            v = 0.5 + 0.3 * math.sin(x * 1.3 + y * 0.7) * math.cos(y * 1.1) + rnd.uniform(-0.12, 0.12)
            px[x, y] = mix(hexc("8a6a1e"), hexc("f0d070"), max(0.0, min(1.0, v)))
    for x in range(16):
        px[x, 0] = px[x, 15] = hexc("5a4612")
    save(foil, "block/dragonfly_casing")
    screen = Image.new("RGBA", (16, 16), hexc("2b2f33"))
    px = screen.load()
    for y in range(2, 14):
        for x in range(2, 14):
            px[x, y] = hexc("0e1a14")
    for x in range(3, 13):
        px[x, 6 + int(2 * math.sin(x * 0.9))] = hexc("6ce07a")
    for (x, y) in ((3, 11), (5, 11), (7, 11)):
        px[x, y] = hexc("f2d84a")
    save(screen, "block/dragonfly_screen")


def simple_block(name):
    write(ASSETS / "models/block" / (name + ".json"),
          {"parent": "minecraft:block/cube_all", "textures": {"all": NS + "block/" + name}})
    variants = [{"model": NS + "block/" + name}] + [{"model": NS + "block/" + name, "y": r} for r in (90, 180, 270)]
    write(ASSETS / "blockstates" / (name + ".json"), {"variants": {"": variants}})
    item_from_block(name, "block/" + name)


def loot(name, drop=None):
    ref = json.loads((LOOT / "lunar_rock.json").read_text())
    if drop:
        item, lo, hi = drop
        ref["pools"][0]["entries"] = [{"type": "minecraft:item", "name": item if ":" in item else NS + item,
                                       "modifier": {"type": "minecraft:set_count", "count": {
                                           "type": "minecraft:uniform", "min": lo, "max": hi}}}]
        text = json.dumps(ref, indent=2).replace("lunar_rock", name)
    else:
        text = json.dumps(ref, indent=2).replace("lunar_rock", name)
    (LOOT / (name + ".json")).write_text(text)


def java():
    lines = ["package hu.kriwdi.intersolarsystem.content;", "",
             "import net.minecraft.world.level.block.Block;",
             "import net.minecraft.world.level.block.SoundType;",
             "import net.minecraft.world.level.block.state.BlockBehaviour;", "",
             "/**",
             " * The ground, clouds and special blocks of the outer system. Generated by tools/art/make_outer_assets.py: do",
             " * not edit.",
             " */",
             "public final class OuterBlocks {"]
    for name, (spec, *rest) in B.items():
        if spec == "cloud":
            _pal, tool, strength, sound, light = rest[0], rest[1], rest[2], rest[3], rest[4]
        else:
            _style, tool, strength, sound, light = rest[0], rest[1], rest[2], rest[3], rest[4]
        props = f"BlockBehaviour.Properties.of().strength({strength}F, {max(strength, 3.0)}F).sound(SoundType.{sound})"
        if tool == "pickaxe":
            props += ".requiresCorrectToolForDrops()"
        if light:
            props += f".lightLevel(s -> {light})"
        if sound == "WOOL":
            props += ".noOcclusion()"
        lines.append(f"\tpublic static final Block {name.upper()} = ModBlocks.registerExternal(\"{name}\", Block::new,")
        lines.append(f"\t\t\t{props});")
    lines += [
        "\t/** Enceladus: a water geyser that throws you high into the near-weightless sky. */",
        "\tpublic static final Block GEYSER_VENT = ModBlocks.registerExternal(\"geyser_vent\",",
        "\t\t\tp -> new GeyserBlock(p, 1.7, 30, false), BlockBehaviour.Properties.of().strength(1.4F, 3.0F)",
        "\t\t\t\t\t.sound(SoundType.GLASS).requiresCorrectToolForDrops().randomTicks().lightLevel(s -> 2));",
        "\t/** Triton: a nitrogen geyser, a dark plume of gas and dust, weaker than Enceladus' water. */",
        "\tpublic static final Block NITROGEN_GEYSER = ModBlocks.registerExternal(\"nitrogen_geyser\",",
        "\t\t\tp -> new GeyserBlock(p, 0.9, 18, true), BlockBehaviour.Properties.of().strength(1.3F, 3.0F)",
        "\t\t\t\t\t.sound(SoundType.GLASS).requiresCorrectToolForDrops().randomTicks());",
        "\t/** Titan: the Dragonfly rotorcraft's flight computer (repair it, then it hops between the sites). */",
        "\tpublic static final Block DRAGONFLY_FLIGHT_COMPUTER = ModBlocks.registerExternal(\"dragonfly_flight_computer\",",
        "\t\t\tp -> new DragonflyBlock(p, " + ", ".join(
            "new double[] {" + ", ".join(str(v) for v in b) + "}" for b in DRAGONFLY_BOXES) + "),",
        "\t\t\tBlockBehaviour.Properties.of().strength(2.0F, 6.0F).sound(SoundType.METAL).noOcclusion()",
        "\t\t\t\t\t.lightLevel(s -> s.getValue(DragonflyBlock.REPAIRED) ? 7 : 0));",
        "", "\tprivate OuterBlocks() {", "\t}", "", "\tpublic static void initialize() {", "\t}", "}", ""]
    JAVA.write_text("\n".join(lines))


def special_assets():
    for name, dark in (("geyser_vent", False), ("nitrogen_geyser", True)):
        save(vent_top(dark), f"block/{name}_top")
        side = "cantaloupe_ice" if dark else "enceladus_ice"
        write(ASSETS / "models/block" / (name + ".json"), {"parent": "minecraft:block/cube_bottom_top", "textures": {
            "top": NS + f"block/{name}_top", "bottom": NS + "block/" + side, "side": NS + "block/" + side}})
        write(ASSETS / "blockstates" / (name + ".json"), {"variants": {
            "lit=false": {"model": NS + "block/" + name}, "lit=true": {"model": NS + "block/" + name}}})
        item_from_block(name, "block/" + name)
        loot(name)
    dragonfly_textures()
    els = []
    for i, b in enumerate(DRAGONFLY_BOXES):
        tex = {d: "casing" for d in ("north", "south", "east", "west", "up", "down")}
        if i == 1:
            tex["north"] = "screen"
        els.append(box(b[:3], b[3:], tex))
    model("block/dragonfly_flight_computer", {"casing": "block/dragonfly_casing", "screen": "block/dragonfly_screen",
                                              "particle": "block/dragonfly_casing"}, els)
    variants = {}
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for powered in ("false", "true"):
            v = {"model": NS + "block/dragonfly_flight_computer"}
            if y:
                v["y"] = y
            variants[f"facing={facing},powered={powered}"] = v
    write(ASSETS / "blockstates/dragonfly_flight_computer.json", {"variants": variants})
    item_from_block("dragonfly_flight_computer", "block/dragonfly_flight_computer")
    loot("dragonfly_flight_computer")


def main():
    for name, (spec, *rest) in B.items():
        save(texture(name, (spec, rest[0]) if spec == "cloud" else (spec, rest[0])), "block/" + name)
        simple_block(name)
        loot(name, DROPS.get(name))
    special_assets()
    tool_of = {n: (r[1] if s == "cloud" else r[1]) for n, (s, *r) in B.items()}
    tool_of.update({"geyser_vent": "pickaxe", "nitrogen_geyser": "pickaxe", "dragonfly_flight_computer": "pickaxe"})
    for tool in ("pickaxe", "shovel"):
        path = TAGS / (tool + ".json")
        data = json.loads(path.read_text())
        for name, t in tool_of.items():
            if t == tool and NS + name not in data["values"]:
                data["values"].append(NS + name)
        path.write_text(json.dumps(data, indent=2) + "\n")
    for name in ITEMS:
        item_icon(name)
        write(ASSETS / f"models/item/{name}.json", {"parent": "minecraft:item/generated",
                                                    "textures": {"layer0": NS + "item/" + name}})
        write(ASSETS / f"items/{name}.json", {"model": {"type": "minecraft:model", "model": NS + "item/" + name}})
    java()
    rows = [f"block.intersolarsystem.{n}|{v[-3]}|{v[-2]}|{v[-1]}" for n, v in B.items()]
    rows += [f"block.intersolarsystem.{n}|{v[0]}|{v[1]}|{v[2]}" for n, v in SPECIAL.items()]
    rows += [f"item.intersolarsystem.{n}|{v[0]}|{v[1]}|{v[2]}" for n, v in ITEMS.items()]
    (Path(__file__).resolve().parent / "outer_lang_rows.txt").write_text("\n".join(rows) + "\n")
    print("outer assets:", len(B) + len(SPECIAL), "blocks,", len(ITEMS), "items")


if __name__ == "__main__":
    main()
