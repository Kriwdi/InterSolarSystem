"""Structures of the dead Earth (phase 7): structure templates (NBT), jigsaw pools, structures, structure sets,
biome tags and the loot tables of their containers.

Every template is built here from code (a small voxel builder) instead of by hand in a structure block, so the ruins
are reproducible and easy to change. Decay (missing blocks, cracks, rust, rubble) comes from a seeded random.

    python3 tools/worldgen/make_structures.py
"""
import json
import math
import random
from pathlib import Path

import nbt

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data"
NS = "intersolarsystem:"
DATA_VERSION = 5023  # the version the vanilla 26.3 templates are saved with


def write(rel, data):
    path = DATA / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


def bid(name):
    return name if ":" in name else "minecraft:" + name


# Every block with a blockstate file in the mod's assets is a mod block.
MOD_BLOCKS = {p.stem for p in (ROOT / "src/main/resources/assets/intersolarsystem/blockstates").glob("*.json")}

SKELETONS = tuple(NS + n for n in ("skeleton_lying", "skeleton_sitting", "skeleton_curled", "skeleton_suited",
                                     "dog_skeleton", "cow_skeleton", "horse_skeleton", "data_terminal"))


def S(name, **props):
    """A block state: S('chest', facing='north'). Mod blocks get the mod namespace."""
    full = NS + name if name in MOD_BLOCKS else bid(name)
    return (full, tuple(sorted((k, str(v).lower()) for k, v in props.items())))


AIR = S("air")


class Build:
    def __init__(self, name, seed):
        self.name = name
        self.rnd = random.Random(seed)
        self.blocks = {}
        self.nbt = {}

    # -- placing
    def set(self, x, y, z, state, tag=None):
        self.blocks[(x, y, z)] = state
        if tag is not None:
            self.nbt[(x, y, z)] = tag
        else:
            self.nbt.pop((x, y, z), None)

    def get(self, x, y, z):
        return self.blocks.get((x, y, z))

    def fill(self, x0, y0, z0, x1, y1, z1, state):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, state)

    def room(self, x0, y0, z0, x1, y1, z1, wall, floor=None, ceiling=None):
        """Walls, floor and ceiling of a box, air inside."""
        self.fill(x0, y0, z0, x1, y1, z1, AIR)
        for x in range(x0, x1 + 1):
            for z in range(z0, z1 + 1):
                self.set(x, y0, z, floor or wall)
                if ceiling is not False:
                    self.set(x, y1, z, ceiling or wall)
        for y in range(y0 + 1, y1):
            for x in range(x0, x1 + 1):
                self.set(x, y, z0, wall)
                self.set(x, y, z1, wall)
            for z in range(z0, z1 + 1):
                self.set(x0, y, z, wall)
                self.set(x1, y, z, wall)

    def windows(self, x0, y, z0, x1, z1, every, state):
        for x in range(x0 + 1, x1):
            if (x - x0) % every == 0:
                self.set(x, y, z0, state)
                self.set(x, y, z1, state)
        for z in range(z0 + 1, z1):
            if (z - z0) % every == 0:
                self.set(x0, y, z, state)
                self.set(x1, y, z, state)

    def chest(self, x, y, z, loot, facing="north", kind="chest"):
        props = {"facing": facing}
        if kind == "chest":
            props.update(type="single", waterlogged="false")
        else:
            props.update(open="false")
        self.set(x, y, z, S(kind, **props), {"id": bid(kind), "LootTable": NS + "chests/" + loot})

    def column(self, x, z, y0, y1, state):
        for y in range(y0, y1 + 1):
            self.set(x, y, z, state)

    # -- ruin
    def decay(self, amount, keep=(), cracked=None, top_bias=True):
        """Removes blocks (more near the top), cracks some, scatters rubble on floors. Story props always stay."""
        keep = tuple(keep) + SKELETONS
        if not self.blocks:
            return
        ys = [p[1] for p in self.blocks]
        lo, hi = min(ys), max(ys)
        for pos, st in list(self.blocks.items()):
            if st == AIR or st[0] in keep or pos in self.nbt:
                continue
            h = (pos[1] - lo) / max(1, hi - lo) if top_bias else 0.5
            r = self.rnd.random()
            if r < amount * (0.4 + 1.2 * h):
                self.set(*pos, AIR)
            elif cracked and r < amount * 2.2 and st[0] in cracked:
                self.set(*pos, S(cracked[st[0]]))
        # Rubble lands on floors under the holes.
        for (x, y, z), st in list(self.blocks.items()):
            if st == AIR and self.rnd.random() < amount * 0.5:
                below = self.get(x, y - 1, z)
                if below and below != AIR and below[0] not in ("minecraft:chest", "minecraft:barrel"):
                    self.set(x, y, z, S("rubble"))

    def clear_above(self, height):
        """Air over every ground column (y = 0) up to height wherever nothing is placed: the template then cuts the
        structure out of hills (terrain adaptation alone only shaves the surface of large hills)."""
        for (x, y, z) in [p for p in self.blocks if p[1] == 0]:
            for yy in range(1, height + 1):
                if (x, yy, z) not in self.blocks:
                    self.blocks[(x, yy, z)] = AIR

    # -- output
    def save(self, rel):
        xs = [p[0] for p in self.blocks]
        ys = [p[1] for p in self.blocks]
        zs = [p[2] for p in self.blocks]
        ox, oy, oz = min(xs), min(ys), min(zs)
        palette, index, blocks = [], {}, []
        for pos in sorted(self.blocks, key=lambda p: (p[1], p[2], p[0])):
            st = self.blocks[pos]
            if st not in index:
                index[st] = len(palette)
                entry = {"id": st[0]}
                if st[1]:
                    entry["properties"] = dict(st[1])
                palette.append(entry)
            b = {"pos": [nbt.Int(pos[0] - ox), nbt.Int(pos[1] - oy), nbt.Int(pos[2] - oz)],
                 "state": nbt.Int(index[st])}
            if pos in self.nbt:
                b["nbt"] = self.nbt[pos]
            blocks.append(b)
        root = {"size": [nbt.Int(max(xs) - ox + 1), nbt.Int(max(ys) - oy + 1), nbt.Int(max(zs) - oz + 1)],
                "entities": [], "blocks": blocks, "palette": palette, "DataVersion": nbt.Int(DATA_VERSION)}
        path = DATA / "intersolarsystem/structure" / (rel + ".nbt")
        path.parent.mkdir(parents=True, exist_ok=True)
        nbt.write(str(path), root)
        return max(xs) - ox + 1, max(ys) - oy + 1, max(zs) - oz + 1


def jigsaw(name, target, pool, orientation, final="minecraft:air", joint="aligned"):
    """A jigsaw block: orientation like 'east_up' (facing out of the piece) or 'up_north'."""
    return (S("jigsaw", orientation=orientation),
            {"id": "minecraft:jigsaw", "name": NS + name, "target": NS + target, "pool": NS + pool,
             "joint": joint, "final_state": final, "selection_priority": nbt.Int(0),
             "placement_priority": nbt.Int(0)})


CONCRETE = {"minecraft:gray_concrete": "cracked_stone_bricks", "minecraft:light_gray_concrete": "andesite",
            "minecraft:stone_bricks": "cracked_stone_bricks", "minecraft:white_concrete": "calcite"}
GREY = S("gray_concrete")
LGREY = S("light_gray_concrete")
WHITE = S("white_concrete")
GLASS = S("glass")
BARS = S("iron_bars")
LAMP = S("redstone_lamp", lit="false")
HULL = S("station_hull")
RUST = S("rusted_panel")


# ---------------------------------------------------------------- bunkers
def bunker_small():
    b = Build("bunker_small", 1001)
    # A concrete hatch house on the surface (y 12..15), a shaft with a ladder, one room at the bottom (y 0..5).
    b.room(0, 0, 0, 8, 5, 8, GREY, S("polished_andesite"))
    b.room(3, 5, 3, 5, 15, 5, GREY, ceiling=S("iron_trapdoor", facing="north", half="top", open="false"))
    for y in range(1, 15):
        b.set(4, y, 4, S("ladder", facing="south"))
    b.set(4, 15, 4, S("iron_trapdoor", facing="north", half="top", open="false"))
    b.set(4, 5, 4, AIR)
    b.fill(1, 1, 1, 1, 3, 1, S("stone_brick_wall", up="true"))
    for x in (2, 6):
        b.set(x, 4, 4, S("lantern", hanging="true"))
    b.set(1, 1, 7, S("white_bed", facing="north", part="foot"))
    b.set(1, 1, 6, S("white_bed", facing="north", part="head"))
    b.chest(7, 1, 7, "bunker", "west")
    b.chest(7, 1, 1, "bunker_food", "west", "barrel")
    b.set(4, 1, 7, S("crafting_table"))
    b.set(5, 1, 7, S("furnace", facing="north", lit="false"))
    b.set(7, 1, 4, S("data_terminal", facing="west", log=8))
    # The last one awake: curled up on the bunk, the dog at the foot of the bed.
    b.set(2, 1, 6, S("skeleton_curled", facing="east"))
    b.set(2, 1, 4, S("dog_skeleton", facing="south"))
    b.decay(0.03, keep=("minecraft:ladder",), cracked=CONCRETE, top_bias=False)
    return b


def bunker_large():
    b = Build("bunker_large", 1002)
    b.room(0, 0, 0, 16, 5, 12, GREY, S("polished_andesite"))
    b.room(0, 5, 0, 16, 10, 12, GREY, S("polished_andesite"))
    b.room(6, 10, 4, 10, 18, 8, GREY)
    b.fill(7, 11, 5, 9, 17, 7, AIR)
    for y in range(1, 18):
        b.set(8, y, 6, S("ladder", facing="south") if y < 18 else AIR)
    b.fill(7, 5, 5, 9, 5, 7, AIR)
    b.fill(7, 10, 5, 9, 10, 7, AIR)
    b.fill(8, 5, 6, 8, 5, 6, S("ladder", facing="south"))
    b.fill(8, 10, 6, 8, 10, 6, S("ladder", facing="south"))
    b.set(8, 18, 6, S("iron_trapdoor", facing="north", half="top", open="false"))
    # Upper floor: dormitory and mess. Lower floor: generator room, armoury, archive.
    for x in (2, 4, 12, 14):
        b.set(x, 6, 10, S("white_bed", facing="north", part="foot"))
        b.set(x, 6, 9, S("white_bed", facing="north", part="head"))
    b.fill(2, 6, 2, 5, 6, 2, S("smooth_stone_slab", type="top"))
    b.chest(15, 6, 2, "bunker_food", "west", "barrel")
    b.chest(15, 6, 3, "bunker", "west")
    b.fill(1, 1, 1, 4, 3, 3, S("iron_block"))
    b.set(2, 4, 2, S("lightning_rod") if False else S("iron_bars"))
    b.chest(15, 1, 1, "military", "west")
    b.chest(15, 1, 2, "military", "west", "barrel")
    b.chest(1, 1, 11, "bunker", "east")
    b.set(14, 1, 11, S("data_terminal", facing="north", log=8))
    b.set(13, 1, 10, S("skeleton_sitting", facing="north"))  # by the terminal, waiting for the all-clear
    b.set(3, 6, 6, S("skeleton_lying", facing="east"))
    b.set(12, 1, 11, S("repair_station", facing="north"))
    b.fill(8, 1, 1, 8, 4, 11, S("iron_bars"))
    b.fill(8, 1, 5, 8, 3, 7, AIR)
    for x in (3, 13):
        for y in (4, 9):
            b.set(x, y, 6, S("lantern", hanging="true"))
    b.decay(0.035, keep=("minecraft:ladder",), cracked=CONCRETE, top_bias=False)
    return b


# ---------------------------------------------------------------- space centre
def space_center():
    b = Build("space_center", 1003)
    # Launch pad: a concrete plate with a flame trench, the gantry tower, the toppled rocket, the blockhouse.
    b.fill(0, 0, 0, 30, 0, 30, LGREY)
    b.fill(12, 0, 4, 18, 0, 26, S("blackstone"))
    b.fill(13, -3, 6, 17, -1, 24, AIR)
    b.fill(12, -4, 5, 18, -4, 25, S("polished_blackstone"))
    for y in range(1, 34):  # gantry: four truss legs with platforms
        for (x, z) in ((5, 12), (9, 12), (5, 16), (9, 16)):
            b.set(x, y, z, S("truss"))
        if y % 6 == 0:
            b.fill(5, y, 12, 9, y, 16, S("iron_bars") if y % 12 else S("truss"))
            b.fill(10, y, 14, 12, y, 14, S("truss"))
    b.fill(6, 1, 13, 8, 33, 15, AIR)
    for y in range(1, 33):
        b.set(7, y, 14, S("ladder", facing="north"))
    b.fill(4, 34, 11, 10, 34, 17, S("truss"))
    b.set(7, 35, 14, S("antenna_dish"))
    b.set(7, 34, 14, S("station_light"))
    # The rocket lies broken across the pad: white hull rings, a scorched engine section, a nose cone.
    for i in range(0, 22):
        z = 3 + i
        r = 2 if i > 2 else 1
        for dx in range(-r, r + 1):
            for dy in range(-r, r + 1):
                if dx * dx + dy * dy <= r * r + 1 and b.rnd.random() > 0.08:
                    st = S("station_hull") if i % 6 else S("station_foil")
                    if i > 18:
                        st = S("blackstone")
                    b.set(24 + dx, 3 + dy, z, st)
    b.set(24, 3, 26, S("lander_engine"))
    b.set(23, 3, 26, S("lander_engine"))
    b.set(25, 3, 26, S("lander_engine"))
    # Blockhouse.
    b.room(0, 0, 22, 9, 5, 30, GREY, S("polished_andesite"))
    b.windows(0, 3, 22, 9, 30, 2, S("tinted_glass"))
    b.set(4, 1, 22, AIR)
    b.set(4, 2, 22, AIR)
    for x in range(2, 8):
        b.set(x, 1, 29, S("data_terminal", facing="north", log=(9, 6, 9, 4, 10, 9)[x - 2]))
    b.set(4, 1, 27, S("skeleton_sitting", facing="north"))  # the launch director never left his desk
    b.set(20, 1, 10, S("skeleton_suited", facing="south"))  # a pad crew member by the wreck
    b.chest(1, 1, 23, "space_center", "east")
    b.chest(8, 1, 23, "space_center", "west")
    b.chest(1, 1, 28, "lab", "east", "barrel")
    b.decay(0.04, keep=("minecraft:ladder", NS + "truss"), cracked=CONCRETE)
    return b


# ---------------------------------------------------------------- research
def research_lab():
    b = Build("research_lab", 1004)
    b.room(0, 0, 0, 14, 5, 10, WHITE, S("smooth_stone"))
    b.room(0, 5, 0, 14, 9, 10, WHITE, S("smooth_stone"))
    b.windows(0, 3, 0, 14, 10, 3, GLASS)
    b.windows(0, 7, 0, 14, 10, 3, GLASS)
    b.fill(1, 5, 1, 2, 5, 2, AIR)
    for y in range(1, 6):
        b.set(1, y, 1, S("ladder", facing="south"))
    for x in range(3, 12, 2):  # lab benches with brewing stands and terminals
        b.set(x, 1, 5, S("smooth_stone_slab", type="top"))
        b.set(x, 2, 5, S("brewing_stand", has_bottle_0="false", has_bottle_1="false", has_bottle_2="false"))
    b.set(13, 1, 5, S("data_terminal", facing="west", log=6))
    b.set(6, 1, 3, S("skeleton_lying", facing="west"))
    b.set(10, 6, 3, S("skeleton_curled", facing="north"))
    b.chest(13, 1, 9, "lab", "west")
    b.chest(13, 6, 9, "lab", "west")
    b.chest(1, 6, 9, "medical", "east", "barrel")
    b.fill(4, 6, 8, 10, 8, 9, S("glass"))  # specimen tanks
    b.fill(5, 6, 8, 9, 7, 8, S("toxic_barrel"))
    b.set(7, 10, 5, S("antenna_dish"))
    b.decay(0.05, cracked={"minecraft:white_concrete": "calcite"})
    return b


def research_station():
    b = Build("research_station", 1005)
    # Prefab modules on stilts, linked by a walkway, with a mast.
    for (x0, z0) in ((0, 0), (10, 0)):
        for (x, z) in ((x0, z0), (x0 + 6, z0), (x0, z0 + 5), (x0 + 6, z0 + 5)):
            b.column(x, z, 0, 2, S("truss"))
        b.room(x0, 3, z0, x0 + 6, 7, z0 + 5, HULL, S("polished_andesite"))
        b.windows(x0, 5, z0, x0 + 6, z0 + 5, 2, S("station_window"))
    b.fill(7, 3, 2, 9, 3, 3, S("truss"))
    b.fill(6, 4, 2, 10, 5, 3, AIR)
    b.set(16, 4, 3, S("data_terminal", facing="west", log=3))
    b.chest(1, 4, 1, "lab", "east")
    b.chest(15, 4, 1, "research_station", "west", "barrel")
    b.set(11, 4, 4, S("repair_station", facing="north"))
    b.column(3, 2, 8, 18, S("iron_bars"))
    b.set(3, 19, 2, S("antenna_dish"))
    for x in range(0, 7):
        b.set(x, 2, 6, S("iron_bars"))
    b.fill(2, 0, 7, 4, 2, 9, S("ladder", facing="north"))
    b.fill(2, 0, 7, 4, 2, 9, AIR)
    for y in range(0, 4):
        b.set(3, y, 6, S("ladder", facing="south"))
    b.decay(0.03, keep=("minecraft:ladder", NS + "truss"))
    return b


def seed_bank():
    b = Build("seed_bank", 1006)
    # Svalbard style: a concrete wedge sticking out of the slope, a long tunnel into the rock, the vault at the end.
    for i in range(0, 8):
        b.fill(3 - i // 3, i, 0 + i, 9 + i // 3, i, 0 + i, GREY)
    b.fill(4, 0, 0, 8, 6, 30, GREY)
    b.fill(5, 1, 0, 7, 4, 30, AIR)
    b.fill(5, 0, 0, 7, 0, 30, S("polished_andesite"))
    b.fill(5, 6, 1, 7, 6, 3, S("light_blue_stained_glass"))  # the light-art panel above the door
    b.room(0, 0, 30, 12, 6, 42, GREY, S("polished_andesite"))
    b.fill(5, 1, 30, 7, 4, 30, AIR)
    for x in range(1, 12, 2):
        for z in range(32, 42, 3):
            b.fill(x, 1, z, x, 4, z, S("barrel", facing="up", open="false"))
    for x in (1, 11):
        b.chest(x, 1, 41, "seed_bank", "north")
    b.chest(6, 1, 41, "seed_bank", "north", "barrel")
    b.set(6, 2, 41, S("data_terminal", facing="north", log=11))
    for z in range(4, 30, 5):
        b.set(6, 4, z, S("lantern", hanging="true"))
    b.decay(0.02, cracked=CONCRETE, top_bias=False)
    return b


def terraformer():
    b = Build("terraformer", 1007)
    # A failed terraforming tower: glass dome over a dead garden, the atmosphere processor stack, toxic overflow.
    r = 8
    for x in range(-r, r + 1):
        for z in range(-r, r + 1):
            d = math.hypot(x, z)
            if d <= r:
                b.set(x, 0, z, S("coarse_dirt") if b.rnd.random() < 0.6 else S("podzol", snowy="false"))
            for y in range(1, r + 1):
                dd = math.sqrt(x * x + y * y + z * z)
                if r - 0.6 <= dd <= r + 0.4:
                    b.set(x, y, z, S("green_stained_glass") if b.rnd.random() > 0.25 else AIR)
                elif dd < r - 0.6 and y >= 1:
                    b.set(x, y, z, AIR)
    for (x, z) in ((-3, -2), (2, 3), (4, -4), (-4, 4)):
        b.column(x, z, 1, 4, S("dead_log", axis="y"))
    b.column(0, 0, 1, 18, S("station_hull"))
    for y in (6, 10, 14):
        b.fill(-1, y, -1, 1, y, 1, S("station_foil"))
    b.set(0, 19, 0, S("station_light"))
    b.set(1, 1, 0, S("data_terminal", facing="east", log=1))
    b.chest(-1, 1, 0, "lab", "west")
    for (x, z) in ((6, 0), (7, 2), (-7, -1)):
        b.set(x, 1, z, S("toxic_barrel"))
    b.decay(0.02, keep=(NS + "station_hull",))
    return b


# ---------------------------------------------------------------- infrastructure
def solar_farm():
    b = Build("solar_farm", 1008)
    for row in range(0, 6):
        z = row * 4
        for x in range(0, 24):
            b.set(x, 1, z, S("truss") if x % 6 == 0 else AIR)
            if b.rnd.random() > 0.15:
                b.set(x, 2, z, S("solar_panel"))
            elif b.rnd.random() > 0.5:
                b.set(x, 1, z + 1, S("scrap_pile"))
    b.room(25, 0, 8, 30, 4, 14, RUST, S("polished_andesite"))
    b.set(25, 1, 11, AIR)
    b.set(25, 2, 11, AIR)
    b.fill(27, 1, 13, 29, 2, 13, S("iron_block"))
    b.chest(29, 1, 9, "industrial", "west")
    b.set(28, 1, 9, S("battery_cell") if False else S("lightning_rod") if False else S("redstone_block"))
    return b


def dam():
    b = Build("dam", 1009)
    # A gravity dam wall across a dry canyon: sloped downstream face, spillway gates, a valve house on the crest.
    for z in range(0, 40):
        for y in range(0, 24):
            thick = 3 + (24 - y) // 3
            for x in range(0, thick):
                b.set(x, y, z, LGREY if (y + z) % 7 else GREY)
    for z in range(16, 24):
        b.fill(0, 20, z, 2, 23, z, AIR)
        b.set(1, 19, z, S("iron_bars"))
    b.room(0, 24, 4, 5, 28, 10, GREY)
    b.windows(0, 26, 4, 5, 10, 2, GLASS)
    b.set(5, 25, 7, AIR)
    b.set(5, 26, 7, AIR)
    b.chest(1, 25, 5, "industrial", "east")
    b.set(4, 25, 9, S("data_terminal", facing="west", log=2))
    for z in range(0, 40, 4):
        b.set(1, 24, z, S("iron_bars"))
    b.decay(0.03, cracked=CONCRETE)
    return b


def radio_tower():
    b = Build("radio_tower", 1010)
    for y in range(0, 40):
        w = max(1, 4 - y // 10)
        for (x, z) in ((4 - w, 4 - w), (4 + w, 4 - w), (4 - w, 4 + w), (4 + w, 4 + w)):
            b.set(x, y, z, S("truss"))
        if y % 5 == 0:
            for d in range(-w, w + 1):
                for (x, z) in ((4 + d, 4 - w), (4 + d, 4 + w), (4 - w, 4 + d), (4 + w, 4 + d)):
                    b.set(x, y, z, S("iron_bars"))
    for y in (18, 28, 36):
        b.set(4, y, 4, S("antenna_dish"))
    b.set(4, 40, 4, S("redstone_lamp", lit="false"))
    b.room(10, 0, 0, 16, 4, 6, RUST, S("polished_andesite"))
    b.set(10, 1, 3, AIR)
    b.set(10, 2, 3, AIR)
    b.set(15, 1, 3, S("data_terminal", facing="west", log=10))
    b.chest(15, 1, 1, "radio", "west")
    b.set(14, 1, 4, S("skeleton_sitting", facing="east"))  # the operator, still at the radio
    b.decay(0.02, keep=(NS + "truss",))
    return b



def barracks():
    b = Build("barracks", 1012)
    b.room(0, 0, 0, 22, 5, 8, S("stone_bricks"), S("polished_andesite"))
    b.windows(0, 3, 0, 22, 8, 3, BARS)
    b.set(11, 1, 0, AIR)
    b.set(11, 2, 0, AIR)
    for x in range(2, 21, 3):
        b.set(x, 1, 7, S("green_bed", facing="north", part="foot"))
        b.set(x, 1, 6, S("green_bed", facing="north", part="head"))
    b.chest(21, 1, 1, "military", "west")
    b.chest(1, 1, 1, "military", "east", "barrel")
    b.chest(21, 1, 4, "bunker_food", "west", "barrel")
    b.set(9, 1, 3, S("skeleton_lying", facing="north"))
    b.set(27, 11, 3, S("skeleton_sitting", facing="north"))  # the last guard on the tower
    # Watchtower and a fence line.
    for y in range(0, 10):
        for (x, z) in ((26, 0), (29, 0), (26, 3), (29, 3)):
            b.set(x, y, z, S("dark_oak_fence", north="false", south="false", east="false", west="false",
                             waterlogged="false"))
    b.fill(25, 10, -1, 30, 10, 4, S("dark_oak_planks"))
    b.fill(25, 11, -1, 30, 11, -1, S("sandbags") if False else S("stone_brick_wall", up="true"))
    b.chest(27, 11, 1, "military", "north")
    for x in range(-4, 34):
        b.set(x, 1, 14, S("iron_bars"))
    b.decay(0.04, cracked=CONCRETE)
    return b



def oil_pumpjack():
    b = Build("oil_pumpjack", 1014)
    b.fill(0, 0, 0, 10, 0, 6, S("gray_concrete"))
    b.fill(2, 1, 3, 2, 6, 3, S("iron_block"))
    b.fill(8, 1, 3, 8, 3, 3, S("iron_block"))
    b.fill(1, 7, 3, 9, 7, 3, S("black_concrete"))  # walking beam
    b.set(0, 7, 3, S("black_concrete"))
    b.fill(0, 4, 3, 0, 6, 3, S("iron_bars"))
    b.set(0, 1, 3, S("tar_puddle"))
    b.fill(9, 1, 2, 10, 2, 4, S("iron_block"))
    # Storage tank.
    for y in range(0, 6):
        for x in range(13, 20):
            for z in range(0, 7):
                d = math.hypot(x - 16, z - 3)
                if 2.5 <= d <= 3.4:
                    b.set(x, y, z, S("rusted_panel") if (x + y) % 3 else S("iron_block"))
                elif d < 2.5:
                    b.set(x, y, z, AIR if y else S("tar_sand"))
    b.chest(4, 1, 1, "industrial", "north", "barrel")
    b.set(5, 1, 1, S("toxic_barrel"))
    b.set(6, 1, 1, S("toxic_barrel"))
    b.decay(0.02, keep=("minecraft:iron_block",))
    return b


def farmhouse():
    b = Build("farmhouse", 1015)
    # Barn with a gambrel-ish roof, a grain silo, rows of dead crops.
    b.room(0, 0, 0, 12, 6, 9, S("spruce_planks"), S("coarse_dirt"), ceiling=False)
    for y in range(7, 11):
        inset = y - 6
        b.fill(0, y, inset, 12, y, inset, S("dark_oak_planks"))
        b.fill(0, y, 9 - inset, 12, y, 9 - inset, S("dark_oak_planks"))
        b.fill(0, y, inset + 1, 0, y, 8 - inset, S("spruce_planks"))
        b.fill(12, y, inset + 1, 12, y, 8 - inset, S("spruce_planks"))
    b.fill(0, 10, 4, 12, 10, 5, S("dark_oak_planks"))
    b.fill(5, 1, 0, 7, 4, 0, AIR)
    b.fill(1, 1, 7, 3, 2, 8, S("hay_block", axis="y"))
    b.set(7, 1, 4, S("cow_skeleton", facing="east"))
    b.set(9, 1, 7, S("dog_skeleton", facing="west"))
    b.set(4, 1, 13, S("horse_skeleton", facing="north"))
    b.chest(11, 1, 8, "farm", "west")
    b.chest(11, 1, 1, "farm", "west", "barrel")
    for y in range(0, 14):
        for x in range(15, 20):
            for z in range(1, 6):
                d = math.hypot(x - 17, z - 3)
                if 1.6 <= d <= 2.5:
                    b.set(x, y, z, S("iron_block") if y % 4 == 0 else S("rusted_panel"))
    for row in range(0, 5):
        for x in range(-1, 14):
            b.set(x, 0, 13 + row * 2, S("farmland", moisture=0))
            if b.rnd.random() < 0.5:
                b.set(x, 1, 13 + row * 2, S("dead_bush"))
    b.decay(0.06, keep=("minecraft:farmland",))
    return b


# ---------------------------------------------------------------- the city

def metro_pieces():
    """Underground metro: a station (start) with stairs up to the surface, tunnels, a collapsed tunnel end."""
    pieces = {}
    b = Build("metro/station", 3001)
    b.room(0, 0, 0, 10, 6, 20, S("stone_bricks"), S("polished_andesite"))
    b.fill(3, 0, 0, 7, 0, 20, S("gravel"))
    for z in range(0, 21):
        b.set(4, 1, z, S("rail", shape="north_south", waterlogged="false"))
        b.set(6, 1, z, S("rail", shape="north_south", waterlogged="false"))
    b.fill(1, 1, 2, 2, 1, 18, S("smooth_stone_slab", type="top"))
    b.fill(8, 1, 2, 9, 1, 18, S("smooth_stone_slab", type="top"))
    # A derailed carriage on one track.
    b.fill(5, 1, 6, 7, 3, 13, S("iron_block"))
    b.fill(5, 2, 7, 7, 2, 12, S("tinted_glass"))
    b.fill(6, 1, 7, 6, 3, 12, AIR)
    b.chest(6, 1, 9, "civic", "west")
    b.chest(1, 2, 10, "bunker_food", "east", "barrel")
    # Stairs to the surface (14 up) from the platform.
    for i in range(0, 13):
        z = 2 + i
        b.fill(-3, 1 + i, z, -1, 4 + i, z, AIR)
        b.set(-3, i, z, S("stone_brick_stairs", facing="south", half="bottom", shape="straight",
                         waterlogged="false"))
        b.set(-2, i, z, S("stone_brick_stairs", facing="south", half="bottom", shape="straight",
                         waterlogged="false"))
        b.set(-1, i, z, S("stone_brick_stairs", facing="south", half="bottom", shape="straight",
                         waterlogged="false"))
        b.set(-4, 1 + i, z, S("stone_bricks"))
        b.set(-4, 2 + i, z, S("stone_bricks"))
    b.fill(0, 1, 2, 0, 3, 3, AIR)
    b.fill(3, 1, 0, 7, 4, 0, AIR)
    b.fill(3, 1, 20, 7, 4, 20, AIR)
    st, tag = jigsaw("tunnel", "tunnel", "metro/tunnels", "north_up", "minecraft:gravel")
    b.set(5, 0, 0, st, tag)
    st, tag = jigsaw("tunnel", "tunnel", "metro/tunnels", "south_up", "minecraft:gravel")
    b.set(5, 0, 20, st, tag)
    b.decay(0.02, keep=("minecraft:rail",), cracked=CONCRETE, top_bias=False)
    pieces["metro/station"] = b
    for variant in ("straight", "collapsed_end"):
        b = Build("metro/" + variant, 3002 if variant == "straight" else 3003)
        length = 24 if variant == "straight" else 8
        b.room(2, 0, 0, 8, 5, length - 1, S("stone_bricks"), S("gravel"))
        b.fill(3, 1, 0, 7, 4, 0, AIR)
        if variant == "straight":
            b.fill(3, 1, length - 1, 7, 4, length - 1, AIR)
        for z in range(0, length):
            b.set(5, 1, z, S("rail", shape="north_south", waterlogged="false"))
            if z % 6 == 3:
                b.set(5, 4, z, S("lantern", hanging="true"))
        if variant == "collapsed_end":
            for z in range(4, length):
                b.fill(3, 1, z, 7, 4 - (length - 1 - z) // 2, z, S("gravel"))
            b.set(4, 1, 2, S("scrap_pile"))
        st, tag = jigsaw("tunnel", "tunnel", "metro/tunnels", "north_up", "minecraft:gravel")
        b.set(5, 0, 0, st, tag)
        if variant == "straight":
            st, tag = jigsaw("tunnel", "tunnel", "metro/tunnels", "south_up", "minecraft:gravel")
            b.set(5, 0, length - 1, st, tag)
            if b.rnd.random() < 1:
                b.chest(3, 1, 12, "civic", "east", "barrel")
        b.decay(0.02, keep=("minecraft:rail",), cracked=CONCRETE, top_bias=False)
        pieces["metro/" + variant] = b
    return pieces


# ---------------------------------------------------------------- crashed things, colonies, wrecks
def cargo_capsule():
    b = Build("cargo_capsule", 1016)
    # A cone capsule on its side in a shallow scorched pit, the parachute spread out behind.
    for x in range(-3, 4):
        for z in range(-3, 4):
            if x * x + z * z <= 10:
                b.set(x, 0, z, S("coal_block") if b.rnd.random() < 0.3 else S("blackstone"))
    for i in range(0, 5):
        r = 2.4 - i * 0.4
        for dy in range(-2, 3):
            for dz in range(-2, 3):
                if dy * dy + dz * dz <= r * r:
                    b.set(i - 2, 2 + dy, dz, S("station_foil") if i == 0 else S("station_hull"))
    b.fill(-1, 1, -1, 1, 3, 1, AIR)
    b.chest(0, 1, 0, "cargo", "east")
    b.set(1, 2, 0, S("station_light"))
    for x in range(4, 10):
        for z in range(-3, 4):
            if b.rnd.random() < 0.7:
                b.set(x, 1, z, S("white_carpet") if (x + z) % 3 else S("orange_carpet"))
    return b


def crashed_satellite():
    b = Build("crashed_satellite", 1017)
    b.fill(-2, 0, -2, 2, 0, 2, S("blackstone"))
    b.fill(-1, 1, -1, 1, 2, 1, S("station_foil"))
    b.set(0, 3, 0, S("antenna_dish"))
    for x in range(2, 8):  # bent solar wings
        b.set(x, 1 + (x > 5), 0, S("solar_panel"))
        b.set(-x, 1, 0, S("solar_panel") if b.rnd.random() > 0.3 else S("scrap_pile"))
    b.chest(0, 1, 2, "space_debris", "south", "barrel")
    return b


def rocket_stage():
    b = Build("rocket_stage", 1018)
    # A spent booster half buried at an angle.
    for i in range(0, 16):
        y = i // 2
        for dx in range(-2, 3):
            for dz in range(-2, 3):
                if dx * dx + dz * dz <= 5:
                    b.set(i, y + dz + 2, dx, S("station_hull") if i % 5 else S("station_foil"))
    for dx in (-1, 0, 1):
        b.set(-1, 2, dx, S("lander_engine"))
    b.fill(2, 2, -1, 12, 8, 1, AIR)
    b.chest(6, 4, 0, "space_debris", "north", "barrel")
    return b


def drone_site():
    b = Build("drone_site", 1019)
    b.fill(0, 0, 0, 12, 0, 12, S("gravel"))
    b.fill(4, -12, 4, 7, 0, 7, AIR)  # mine shaft
    b.fill(3, -12, 3, 8, -12, 8, S("cobbled_deepslate"))
    for y in range(-11, 1):
        b.set(4, y, 4, S("ladder", facing="south"))
    b.fill(3, 1, 3, 8, 1, 3, S("truss"))
    b.fill(3, 1, 8, 8, 1, 8, S("truss"))
    b.set(10, 1, 2, S("mining_drone", facing="west"))
    b.set(1, 1, 10, S("mining_drone", facing="east"))
    b.chest(10, 1, 10, "industrial", "west", "barrel")
    b.chest(6, -11, 6, "industrial", "north")
    b.set(11, 1, 6, S("scrap_pile"))
    return b


def colony(fate):
    b = Build("colony_" + fate, {"overgrown": 1020, "burnt": 1021, "frozen": 1022}[fate])
    # A failed colony: a habitat dome with prefab modules around it. The fate decides the dressing.
    r = 9
    shell = {"overgrown": S("glass"), "burnt": S("tinted_glass"), "frozen": S("glass")}[fate]
    for x in range(-r, r + 1):
        for z in range(-r, r + 1):
            if x * x + z * z <= r * r:
                b.set(x, 0, z, {"overgrown": S("moss_block"), "burnt": S("blackstone"),
                                "frozen": S("packed_ice")}[fate])
            for y in range(1, r):
                d = math.sqrt(x * x + y * y * 1.4 + z * z)
                if r - 0.7 <= d <= r + 0.3:
                    b.set(x, y, z, shell if b.rnd.random() > 0.2 else S("station_hull"))
                elif d < r - 0.7:
                    b.set(x, y, z, AIR)
    for (x, z) in ((-5, -2), (4, 3), (0, -6)):
        b.room(x - 2, 1, z - 1, x + 2, 4, z + 2, HULL, S("polished_andesite"))
        b.set(x, 2, z - 1, AIR)
        b.set(x, 3, z - 1, AIR)
    b.chest(-5, 2, -1, "colony", "south")
    b.chest(4, 2, 4, "colony", "north", "barrel")
    b.set(0, 2, -6, S("data_terminal", facing="south", log=4))
    b.set(-5, 1, 3, S("skeleton_suited" if fate == "frozen" else "skeleton_lying", facing="east"))
    b.set(4, 1, -4, S("skeleton_curled", facing="south"))
    if fate == "overgrown":
        for _ in range(40):
            x, z = b.rnd.randint(-7, 7), b.rnd.randint(-7, 7)
            if b.get(x, 1, z) == AIR:
                b.set(x, 1, z, S("azalea") if b.rnd.random() < 0.3 else S("short_grass"))
        b.set(2, 1, -2, S("oak_log", axis="y"))
        b.fill(1, 4, -3, 3, 5, -1, S("oak_leaves", distance=1, persistent="true", waterlogged="false"))
    elif fate == "burnt":
        for _ in range(30):
            x, z = b.rnd.randint(-7, 7), b.rnd.randint(-7, 7)
            if b.get(x, 1, z) == AIR:
                b.set(x, 1, z, S("rubble") if b.rnd.random() < 0.5 else S("coal_block"))
        b.decay(0.06, keep=(NS + "data_terminal",))
    else:
        for _ in range(60):
            x, z = b.rnd.randint(-8, 8), b.rnd.randint(-8, 8)
            if b.get(x, 1, z) == AIR:
                b.set(x, 1, z, S("snow", layers=b.rnd.randint(1, 3)))
        b.decay(0.02)
    return b


def oasis_refuge():
    """The survivors who kept the oasis alive: a greenhouse dome over living soil, a water purifier tower fed from the
    spring, a prefab habitat with bunks and their records, solar panels, a fence of dead wood, and three graves."""
    b = Build("oasis_refuge", 1030)
    soil = S("living_soil")
    # Ground pad.
    for x in range(-11, 12):
        for z in range(-11, 12):
            if x * x + z * z <= 125:
                b.set(x, 0, z, soil if b.rnd.random() < 0.7 else S("oasis_grass"))
    # Fence of dead wood with a gate gap to the south.
    for x in range(-11, 12):
        for z in range(-11, 12):
            d = x * x + z * z
            if 100 <= d <= 125 and not (abs(x) <= 1 and z > 0):
                b.set(x, 1, z, S("dead_fence") if b.rnd.random() > 0.15 else AIR)
    # Greenhouse dome (radius 6) to the west: glass over rows of crops on farmland, a water channel in the middle.
    cx, cz, r = -4, -2, 6
    for x in range(cx - r, cx + r + 1):
        for z in range(cz - r, cz + r + 1):
            for y in range(1, r + 1):
                d = math.sqrt((x - cx) ** 2 + (y - 0.5) ** 2 * 1.3 + (z - cz) ** 2)
                if r - 0.6 <= d <= r + 0.4:
                    frame = (x - cx) % 3 == 0 or (z - cz) % 3 == 0
                    b.set(x, y, z, S("station_hull") if frame and y < 3 else S("station_glass"))
                elif d < r - 0.6:
                    b.set(x, y, z, AIR)
    crops = [S("wheat", age=7), S("carrots", age=7), S("potatoes", age=7), S("beetroots", age=3)]
    for x in range(cx - 4, cx + 5):
        for z in range(cz - 4, cz + 5):
            if (x - cx) ** 2 + (z - cz) ** 2 > 17:
                continue
            if x == cx:
                b.set(x, 0, z, S("water", level=0))
                continue
            b.set(x, 0, z, S("farmland", moisture=7))
            b.set(x, 1, z, crops[(abs(x - cx) - 1) % 4] if b.rnd.random() > 0.2 else AIR)
    b.set(cx, 1, cz + 5, AIR)
    b.set(cx, 2, cz + 5, AIR)
    # Water purifier tower to the north-east: a steel column with tanks, a pipe down to the spring.
    for y in range(1, 10):
        for x in range(5, 8):
            for z in range(-8, -5):
                edge = x in (5, 7) or z in (-8, -6)
                if edge:
                    b.set(x, y, z, S("iron_block") if y in (1, 5, 9) else S("rusted_panel"))
                else:
                    b.set(x, y, z, AIR)
    b.set(6, 0, -7, S("water", level=0))
    b.set(6, 10, -7, S("lightning_rod") if False else S("iron_bars"))
    b.set(6, 4, -6, S("cauldron"))
    b.set(6, 1, -6, AIR)
    b.set(6, 2, -6, AIR)
    for (x, y, z) in ((4, 6, -7), (4, 6, -6), (8, 6, -7)):
        b.set(x, y, z, S("barrel", facing="up", open="false"))
    # Prefab habitat to the east: hull walls, a window row, bunks, a terminal with their records, the last supplies.
    b.room(3, 0, 0, 10, 5, 6, S("station_hull"), S("dead_planks"))
    b.windows(3, 3, 0, 10, 6, 2, S("station_window"))
    b.set(3, 1, 3, AIR)
    b.set(3, 2, 3, AIR)
    b.set(9, 1, 1, S("red_bed", facing="west", part="head", occupied="false"))
    b.set(8, 1, 1, S("red_bed", facing="west", part="foot", occupied="false"))
    b.set(9, 1, 5, S("red_bed", facing="west", part="head", occupied="false"))
    b.set(8, 1, 5, S("red_bed", facing="west", part="foot", occupied="false"))
    b.chest(9, 1, 3, "oasis_refuge", "west")
    b.chest(5, 1, 5, "oasis_refuge", "north", "barrel")
    b.set(6, 2, 1, S("data_terminal", facing="south", log=5))
    b.set(5, 1, 1, S("crafting_table"))
    b.set(7, 4, 3, S("station_light"))
    # Solar panels on posts along the south side.
    for x in (-8, -5, 6, 9):
        b.set(x, 1, 8, S("iron_bars"))
        b.set(x, 2, 8, S("solar_panel"))
        b.set(x + 1, 2, 8, S("solar_panel"))
    # Three graves under the dome's shade, marked with dead wood and candles.
    for i, x in enumerate((-9, -7, -5)):
        b.set(x, 1, 7, S("coarse_dirt"))
        b.set(x, 1, 6, S("dead_fence"))
        b.set(x, 2, 6, S("candle", candles=1 + i, lit="false"))
    return b


def dry_shipwreck():
    b = Build("dry_shipwreck", 1023)
    # A freighter stranded on the old sea floor, listing, hull plates rusted through.
    for x in range(0, 34):
        half = 4 if 4 < x < 30 else max(1, 4 - abs(x - (4 if x <= 4 else 30)))
        for z in range(-half, half + 1):
            for y in range(0, 7):
                edge = abs(z) == half or y == 0
                if edge and b.rnd.random() > 0.12:
                    b.set(x, y + (1 if z > 0 else 0), z, S("rusted_panel") if (x + y) % 4 else S("iron_block"))
                elif not edge:
                    b.set(x, y + (1 if z > 0 else 0), z, AIR)
    b.room(22, 7, -3, 29, 12, 3, RUST)
    b.windows(22, 10, -3, 29, 3, 2, GLASS)
    b.chest(24, 1, 0, "wreck", "east")
    b.chest(10, 1, 1, "cargo", "north", "barrel")
    b.chest(27, 8, 2, "wreck", "west")
    b.set(25, 8, 0, S("skeleton_sitting", facing="north"))  # the captain stayed with his ship
    b.decay(0.03, keep=("minecraft:chest", "minecraft:barrel"))
    return b


def whale_skeleton():
    b = Build("whale_skeleton", 1024)
    for x in range(0, 26):
        b.set(x, 1, 0, S("bone_block", axis="x"))
        if 4 < x < 20 and x % 2 == 0:
            h = 4 - abs(x - 12) // 4
            for dz in range(1, h + 1):
                b.set(x, 1 + dz, -dz, S("bone_block", axis="y"))
                b.set(x, 1 + dz, dz, S("bone_block", axis="y"))
    b.fill(-3, 1, -2, 0, 3, 2, S("bone_block", axis="y"))
    b.fill(-2, 2, -1, -1, 2, 1, AIR)
    return b


# ---------------------------------------------------------------- loot
def loot_tables():
    def pool(rolls, entries):
        return {"rolls": {"type": "minecraft:uniform", "min": rolls[0], "max": rolls[1]},
                "entries": [{"type": "minecraft:item", "name": (i if ":" in i else NS + i), "weight": w,
                             "modifier": {"type": "minecraft:set_count",
                                          "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}}
                            for (i, w, lo, hi) in entries]}

    salvage = [("scrap_metal", 20, 1, 4), ("salvaged_wiring", 12, 1, 3), ("salvaged_circuit", 6, 1, 2),
               ("minecraft:iron_nugget", 10, 2, 8), ("minecraft:torch", 8, 2, 6)]
    food = [("minecraft:bread", 10, 1, 3), ("minecraft:dried_kelp", 8, 2, 6), ("minecraft:cooked_beef", 3, 1, 2),
            ("minecraft:baked_potato", 6, 1, 3), ("minecraft:honey_bottle", 2, 1, 1), ("minecraft:apple", 4, 1, 2)]
    tables = {
        "bunker": [pool((3, 6), salvage + [("repair_kit", 3, 1, 1), ("minecraft:iron_ingot", 4, 1, 3),
                                           ("minecraft:white_wool", 4, 1, 4), ("minecraft:redstone", 3, 1, 4),
                                           ("battery_cell", 2, 1, 1), ("multitool", 1, 1, 1)])],
        "bunker_food": [pool((3, 7), food + [("minecraft:water_bucket", 2, 1, 1)])],
        "military": [pool((3, 6), [("minecraft:iron_sword", 3, 1, 1), ("minecraft:crossbow", 3, 1, 1),
                                   ("minecraft:arrow", 8, 4, 16), ("minecraft:iron_chestplate", 2, 1, 1),
                                   ("minecraft:iron_helmet", 2, 1, 1), ("minecraft:tnt", 2, 1, 3),
                                   ("titanium_plate", 3, 1, 2), ("repair_kit", 3, 1, 2)] + food[:3])],
        "medical": [pool((2, 5), [("minecraft:golden_apple", 1, 1, 1), ("minecraft:glistering_melon_slice", 3, 1, 2),
                                  ("minecraft:honey_bottle", 6, 1, 2), ("minecraft:paper", 8, 1, 4),
                                  ("minecraft:glass_bottle", 8, 1, 3), ("minecraft:white_wool", 5, 1, 3)])],
        "lab": [pool((3, 6), [("salvaged_circuit", 10, 1, 3), ("silicon", 6, 1, 3), ("rare_earth_dust", 4, 1, 3),
                              ("lithium_dust", 4, 1, 4), ("battery_cell", 3, 1, 2), ("solar_cell", 2, 1, 2),
                              ("minecraft:redstone", 8, 2, 6), ("minecraft:glass_bottle", 5, 1, 3),
                              ("diagnostic_console", 1, 1, 1)])],
        "research_station": [pool((3, 6), [("salvaged_circuit", 8, 1, 3), ("titanium_ingot", 4, 1, 2),
                                           ("aluminium_ingot", 6, 1, 3), ("space_helmet", 1, 1, 1),
                                           ("repair_kit", 4, 1, 1), ("battery_cell", 3, 1, 2)] + food[:2])],
        "seed_bank": [pool((4, 8), [("minecraft:wheat_seeds", 10, 2, 8), ("minecraft:beetroot_seeds", 8, 2, 6),
                                    ("minecraft:melon_seeds", 6, 1, 4), ("minecraft:pumpkin_seeds", 6, 1, 4),
                                    ("minecraft:potato", 6, 1, 4), ("minecraft:carrot", 6, 1, 4),
                                    ("minecraft:oak_sapling", 4, 1, 2), ("minecraft:birch_sapling", 3, 1, 2),
                                    ("minecraft:spruce_sapling", 3, 1, 2), ("minecraft:sweet_berries", 4, 1, 4),
                                    ("minecraft:bone_meal", 6, 2, 8)])],
        "space_center": [pool((4, 7), [("titanium_plate", 6, 1, 3), ("aluminium_ingot", 6, 2, 4),
                                       ("salvaged_circuit", 6, 1, 3), ("space_suit", 1, 1, 1),
                                       ("space_pants", 1, 1, 1), ("space_boots", 1, 1, 1),
                                       ("rare_earth_magnet", 3, 1, 2), ("battery_cell", 4, 1, 2),
                                       ("solar_cell", 3, 1, 2), ("ion_drive_kit", 1, 1, 1)])],
        "space_debris": [pool((2, 4), [("scrap_metal", 10, 1, 4), ("aluminium_ingot", 6, 1, 2),
                                       ("solar_cell", 4, 1, 1), ("salvaged_circuit", 5, 1, 2),
                                       ("titanium_plate", 2, 1, 1)])],
        "cargo": [pool((3, 6), food + salvage[:3] + [("minecraft:iron_ingot", 4, 1, 4), ("repair_kit", 2, 1, 1)])],
        "industrial": [pool((3, 6), salvage + [("minecraft:copper_ingot", 6, 1, 4), ("aluminium_ingot", 4, 1, 3),
                                               ("bitumen", 4, 2, 6), ("minecraft:iron_ingot", 4, 1, 3)])],
        "radio": [pool((2, 4), [("salvaged_circuit", 10, 1, 3), ("salvaged_wiring", 10, 2, 4),
                                ("rare_earth_magnet", 3, 1, 1), ("minecraft:redstone", 6, 2, 6)])],
        "civic": [pool((2, 5), food + salvage[:2] + [("minecraft:paper", 6, 1, 6), ("minecraft:book", 3, 1, 2),
                                                     ("minecraft:white_wool", 6, 1, 4), ("minecraft:leather", 4, 1, 3),
                                                     ("minecraft:string", 4, 2, 6),
                                                     ("minecraft:clock", 1, 1, 1), ("minecraft:compass", 1, 1, 1)])],
        "farm": [pool((3, 6), [("minecraft:wheat_seeds", 10, 2, 6), ("minecraft:wheat", 6, 1, 4),
                               ("minecraft:leather", 6, 1, 4), ("minecraft:white_wool", 6, 1, 4),
                               ("minecraft:iron_hoe", 2, 1, 1), ("minecraft:shears", 2, 1, 1),
                               ("minecraft:bucket", 3, 1, 1), ("minecraft:potato", 5, 1, 3),
                               ("minecraft:bone_meal", 6, 2, 6)])],
        "colony": [pool((3, 6), food + [("titanium_ingot", 3, 1, 2), ("battery_cell", 3, 1, 1),
                                        ("minecraft:white_wool", 5, 2, 5), ("minecraft:leather", 3, 1, 2),
                                        ("minecraft:wheat_seeds", 5, 2, 6), ("repair_kit", 2, 1, 1),
                                        ("space_helmet", 1, 1, 1)])],
        "oasis_refuge": [pool((4, 8), [("minecraft:wheat_seeds", 8, 2, 8), ("minecraft:beetroot_seeds", 6, 2, 6),
                                       ("minecraft:carrot", 6, 1, 4), ("minecraft:potato", 6, 1, 4),
                                       ("minecraft:pumpkin_seeds", 4, 1, 3), ("minecraft:melon_seeds", 4, 1, 3),
                                       ("living_soil", 8, 4, 12), ("clean_water_canister", 6, 1, 3),
                                       ("water_canister", 4, 1, 2), ("minecraft:bone_meal", 6, 2, 8),
                                       ("minecraft:bread", 6, 1, 3), ("minecraft:apple", 5, 1, 3),
                                       ("minecraft:oak_sapling", 3, 1, 2)])],
        "wreck": [pool((3, 6), salvage + [("minecraft:compass", 2, 1, 1), ("minecraft:map", 2, 1, 1),
                                          ("minecraft:gold_ingot", 2, 1, 3), ("minecraft:iron_ingot", 4, 1, 4)])],
    }
    # Story: one roll per chest that may find a note, a data tape or a manual (the rest of the weight is nothing).
    lore = {
        "bunker": [("note", "radio_operator"), ("note", "soldier_short_war"), ("data_tape", "short_war_broadcast"),
                   ("manual", "space_suit"), ("manual", "repairs")],
        "bunker_food": [("note", "farmer_diary"), ("note", "child_letter")],
        "military": [("note", "soldier_short_war"), ("data_tape", "short_war_broadcast"), ("manual", "repairs")],
        "medical": [("note", "nurse_ashlung"), ("note", "triage_doctor")],
        "lab": [("note", "verdant_breach"), ("data_tape", "verdant_alarm"), ("note", "halcyon_memo")],
        "research_station": [("note", "halcyon_memo"), ("data_tape", "seed_sower_briefing"),
                             ("manual", "ship_systems"), ("manual", "diagnostics")],
        "seed_bank": [("note", "seed_vault"), ("manual", "ship_farm")],
        "space_center": [("note", "launch_director"), ("data_tape", "aurora_exodus"), ("note", "exodus_lottery"),
                         ("data_tape", "seed_sower_briefing"), ("manual", "drive_kits"), ("manual", "meteor_piloting"),
                         ("manual", "nav_console"), ("manual", "launch_checklist")],
        "space_debris": [("data_tape", "seed_sower_briefing")],
        "cargo": [("note", "exodus_lottery"), ("note", "child_letter"), ("note", "gate_agent")],
        "industrial": [("note", "drone_tech"), ("note", "dam_engineer")],
        "radio": [("note", "radio_operator"), ("data_tape", "mars_last")],
        "civic": [("note", "child_letter"), ("note", "exodus_lottery"), ("note", "farmer_diary"),
                  ("data_tape", "aurora_exodus"), ("note", "gate_agent")],
        "farm": [("note", "farmer_diary"), ("manual", "ship_farm")],
        "colony": [("note", "colony_overgrown"), ("note", "colony_burnt"), ("note", "colony_frozen")],
        "oasis_refuge": [("note", "oasis_keeper"), ("data_tape", "oasis_message"), ("manual", "ship_farm")],
        "wreck": [("note", "wreck_captain")],
    }
    for name, finds in lore.items():
        entries = [{"type": "minecraft:item", "name": NS + item, "weight": 2,
                    "modifier": [{"type": "minecraft:set_custom_data", "tag": '{lore:"%s"}' % id_}]}
                   for item, id_ in finds]
        entries.append({"type": "minecraft:empty", "weight": len(finds)})
        tables[name].append({"rolls": 1, "entries": entries})
    for name, pools in tables.items():
        write(f"intersolarsystem/loot_table/chests/{name}.json", {"type": "minecraft:chest", "pools": pools})


# ---------------------------------------------------------------- structures and sets
def single_pool(name, template, processors=None):
    write(f"intersolarsystem/worldgen/template_pool/{name}.json", {
        "elements": [{"element": {"element_type": "minecraft:single_pool_element", "location": NS + template,
                                  "processors": processors or {"processors": []}, "projection": "rigid"},
                      "weight": 1}],
        "fallback": "minecraft:empty"})


def pool(name, entries, fallback="minecraft:empty", projection="rigid"):
    write(f"intersolarsystem/worldgen/template_pool/{name}.json", {
        "elements": [{"element": {"element_type": "minecraft:single_pool_element", "location": NS + t,
                                  "processors": {"processors": []}, "projection": projection}, "weight": w}
                     for (t, w) in entries],
        "fallback": fallback})


NO_MOBS = {c: {"bounding_box": "full", "spawns": []} for c in
           ("ambient", "axolotls", "creature", "misc", "monster", "underground_water_creature", "water_ambient",
            "water_creature")}


# Structures that only generate on level ground: (sample radius, allowed height difference, ground level above the
# lowest block, how far above the ground leftover terrain is removed).
FLAT = {"airport": (110, 12, 0, 24), "hospital": (48, 12, 6, 24), "dead_city": (64, 18, 0, 24),
        "verdant_lab": (56, 12, 9, 24)}

# The Earth is 20 000 blocks across (1250 chunks). Rare and important ruins are placed on concentric rings so every
# world has them: (count, ring distance in chunks (the first ring lies at about 4x this), sites in the first ring).
# Every ring set starts at the same angle (vanilla seeds the rings with the world seed only), so the distances are all
# different: the sites of different kinds then lie at least ~250 blocks apart along that direction.
RINGS = {"oasis_refuge": (8, 16, 4), "colony_frozen": (3, 20, 3), "colony_overgrown": (3, 28, 3),
         "colony_burnt": (3, 34, 3), "seed_bank": (3, 40, 3), "dam": (3, 46, 3), "hospital": (5, 52, 5),
         "dead_city": (6, 58, 6), "airport": (4, 66, 4), "space_center": (3, 74, 3), "bunker_network": (3, 24, 3),
         "verdant_lab": (2, 37, 2)}


def structure(name, pool_name, biomes, spacing, separation, salt, depth=0, size=1, adaptation="beard_thin",
              step="surface_structures", project=True, distance=80):
    data = {"type": "minecraft:jigsaw", "biomes": "#" + NS + "has_structure/" + name,
            "max_distance_from_center": distance, "size": size, "spawn_overrides": NO_MOBS,
            "start_height": {"absolute": depth}, "start_pool": NS + pool_name, "step": step,
            "terrain_adaptation": adaptation, "use_expansion_hack": False}
    if project:
        data["project_start_to_heightmap"] = "WORLD_SURFACE_WG"
    if name in FLAT:
        # Big ruins: the jigsaw layout is wrapped in a flat-site check (see FlatSiteStructure.java).
        radius, variation, ground_offset, clear_height = FLAT[name]
        write(f"intersolarsystem/worldgen/structure/{name}_layout.json", data)
        data = {"type": NS + "flat_site", "biomes": data["biomes"], "spawn_overrides": NO_MOBS, "step": step,
                "terrain_adaptation": adaptation, "layout": NS + name + "_layout", "check_radius": radius,
                "max_variation": variation, "ground_offset": ground_offset, "clear_height": clear_height}
    if name in RINGS:
        # Guaranteed: a fixed number of sites on rings around the world centre, each moved into a preferred biome
        # where one is near (otherwise it stands in any land biome). No random sites on top.
        count, distance, spread = RINGS[name]
        if name in FLAT:
            data["guaranteed"] = True
        placement = {"type": "minecraft:concentric_rings", "distance": distance, "spread": spread, "count": count,
                     "preferred_biomes": "#" + NS + "preferred/" + name, "salt": salt}
        write(f"intersolarsystem/tags/worldgen/biome/preferred/{name}.json", {"values": [NS + b for b in biomes]})
        biomes = list(dict.fromkeys(biomes + LAND))
    else:
        placement = {"type": "minecraft:random_spread", "salt": salt, "separation": separation, "spacing": spacing}
    write(f"intersolarsystem/worldgen/structure/{name}.json", data)
    write(f"intersolarsystem/worldgen/structure_set/{name}.json", {
        "placement": placement, "structures": [{"structure": NS + name, "weight": 1}]})
    write(f"intersolarsystem/tags/worldgen/biome/has_structure/{name}.json", {"values": [NS + b for b in biomes]})


LAND = ["ash_desert", "petrified_forest", "frozen_megalopolis", "acid_swamp", "junk_mountains", "salt_flats",
        "deep_canyons", "abandoned_farmland", "oil_field", "irradiated_crater"]
ALL = LAND + ["dried_seabed", "oasis"]


SINGLES = [
    # (name, builder, biomes, spacing, separation, salt, depth, adaptation)
    ("bunker_small", bunker_small, LAND + ["dried_seabed"], 24, 8, 70001, -12, "none"),
    ("bunker_large", bunker_large, ["ash_desert", "petrified_forest", "deep_canyons", "abandoned_farmland",
                                    "junk_mountains", "frozen_megalopolis"], 40, 12, 70002, -15, "none"),
    ("space_center", space_center, ["salt_flats", "ash_desert"], 64, 24, 70003, -4, "beard_box"),
    ("research_lab", research_lab, ["petrified_forest", "frozen_megalopolis", "acid_swamp", "salt_flats"], 36, 12,
     70004, 0, "beard_thin"),
    ("research_station", research_station, ["salt_flats", "frozen_megalopolis", "irradiated_crater",
                                            "dried_seabed"], 32, 10, 70005, 0, "beard_thin"),
    ("seed_bank", seed_bank, ["frozen_megalopolis"], 48, 16, 70006, -1, "beard_box"),
    ("terraformer", terraformer, ["irradiated_crater", "acid_swamp", "salt_flats", "ash_desert"], 40, 14, 70007, 0,
     "beard_thin"),
    ("solar_farm", solar_farm, ["salt_flats", "ash_desert", "abandoned_farmland"], 32, 10, 70008, 0, "beard_thin"),
    ("dam", dam, ["deep_canyons"], 36, 12, 70009, -2, "beard_box"),
    ("radio_tower", radio_tower, ["junk_mountains", "petrified_forest", "frozen_megalopolis", "abandoned_farmland"],
     30, 10, 70010, 0, "beard_thin"),
    ("hospital", lambda: __import__("big_structures").hospital_complex(),
     ["frozen_megalopolis", "ash_desert", "abandoned_farmland", "acid_swamp"], 56, 20, 70011, -7, "beard_box"),
    ("barracks", barracks, ["ash_desert", "deep_canyons", "petrified_forest", "junk_mountains"], 36, 12, 70012, 0,
     "beard_thin"),
    ("airport", lambda: __import__("big_structures").airport_complex(),
     ["salt_flats", "dried_seabed", "ash_desert", "abandoned_farmland"], 64, 24, 70013, -1, "beard_box"),
    ("oil_pumpjack", oil_pumpjack, ["oil_field"], 12, 4, 70014, 0, "beard_thin"),
    ("farmhouse", farmhouse, ["abandoned_farmland"], 16, 5, 70015, 0, "beard_thin"),
    ("cargo_capsule", cargo_capsule, ALL, 20, 6, 70016, -1, "beard_thin"),
    ("crashed_satellite", crashed_satellite, ALL, 22, 7, 70017, -1, "beard_thin"),
    ("rocket_stage", rocket_stage, ALL, 30, 10, 70018, -2, "beard_thin"),
    ("drone_site", drone_site, ["junk_mountains", "deep_canyons", "oil_field"], 24, 8, 70019, -12, "none"),
    ("colony_overgrown", lambda: colony("overgrown"), ["acid_swamp"], 40, 14, 70020, 0, "beard_thin"),
    ("colony_burnt", lambda: colony("burnt"), ["ash_desert", "irradiated_crater"], 44, 16, 70021, 0, "beard_thin"),
    ("colony_frozen", lambda: colony("frozen"), ["frozen_megalopolis"], 40, 14, 70022, 0, "beard_thin"),
    ("dry_shipwreck", dry_shipwreck, ["dried_seabed"], 20, 6, 70023, -1, "beard_thin"),
    ("oasis_refuge", oasis_refuge, ["oasis"], 10, 3, 70025, 0, "beard_thin"),
    ("whale_skeleton", whale_skeleton, ["dried_seabed"], 16, 5, 70024, -1, "beard_thin"),
]


def main():
    sizes = {}
    for (name, builder, biomes, spacing, sep, salt, depth, adaptation) in SINGLES:
        b = builder()
        sizes[name] = b.save(name)
        single_pool(name, name)
        structure(name, name, biomes, spacing, sep, salt, depth, adaptation=adaptation)
    import city_structures
    for rel, b in city_structures.city_pieces().items():
        b.clear_above(10)
        sizes[rel] = b.save(rel)
    for name, entries in city_structures.POOLS.items():
        pool(name, entries)
    structure("dead_city", "city/start", ["frozen_megalopolis", "ash_desert"], 40, 14, 70030, 0, size=7,
              adaptation="beard_thin", distance=112)
    for rel, b in metro_pieces().items():
        sizes[rel] = b.save(rel)
    pool("metro/start", [("metro/station", 1)])
    pool("metro/tunnels", [("metro/straight", 4), ("metro/collapsed_end", 1)], fallback=NS + "metro/ends")
    pool("metro/ends", [("metro/collapsed_end", 1)])
    structure("metro", "metro/start", ["frozen_megalopolis", "ash_desert", "abandoned_farmland", "junk_mountains"],
              30, 10, 70031, -14, size=7, adaptation="none", step="underground_structures", distance=112)
    import complex_structures as cx
    for rel, b in cx.bunker_network_pieces().items():
        sizes[rel] = b.save(rel)
    for name, entries in cx.BUNKER_POOLS.items():
        pool(name, entries, fallback=NS + "bunker_net/ends" if name != "bunker_net/ends" else "minecraft:empty")
    structure("bunker_network", "bunker_net/start", LAND, 64, 24, 70040, -30, size=6, adaptation="none",
              step="underground_structures", distance=96)
    b = cx.verdant_lab()
    sizes["verdant_lab"] = b.save("verdant_lab")
    single_pool("verdant_lab", "verdant_lab")
    structure("verdant_lab", "verdant_lab", ["acid_swamp", "petrified_forest", "abandoned_farmland"], 64, 24, 70041,
              -10, adaptation="beard_box")
    loot_tables()
    for k, v in sizes.items():
        print(f"{k:28s} {v}")


if __name__ == "__main__":
    main()
