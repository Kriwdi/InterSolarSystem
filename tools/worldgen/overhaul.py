"""Phase 5: the older ruins rebuilt with real interiors (every room furnished, every building with a purpose and a
story), richer shapes and outbuildings. The builders here replace the first-generation ones in make_structures.py.

Conventions as in big_structures.py: y = 0 is the ground layer of surface ruins; underground ones (bunkers, the
drone site) have their floor at y = 0 and the surface at y = SURFACE (= -depth of the structure).
"""
import math

from make_structures import AIR, CONCRETE, GLASS, GREY, LGREY, WHITE, S, Build
from big_structures import FACE, collapse, concrete, dead_ground, door, ground, skeleton

OPP = {"north": "south", "south": "north", "east": "west", "west": "east"}


# ---------------------------------------------------------------- furnishing helpers
def partition_x(b, x, z0, z1, y0, y1, st):
    for z in range(z0, z1 + 1):
        for y in range(y0, y1 + 1):
            b.set(x, y, z, st)


def partition_z(b, z, x0, x1, y0, y1, st):
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            b.set(x, y, z, st)


def iron_door(b, x, y, z, facing, open_=False):
    b.set(x, y, z, S("iron_door", facing=facing, half="lower", hinge="left", open=open_, powered="false"))
    b.set(x, y + 1, z, S("iron_door", facing=facing, half="upper", hinge="left", open=open_, powered="false"))


def wood_door(b, x, y, z, facing, wood="spruce", open_=False):
    b.set(x, y, z, S(f"{wood}_door", facing=facing, half="lower", hinge="right", open=open_, powered="false"))
    b.set(x, y + 1, z, S(f"{wood}_door", facing=facing, half="upper", hinge="right", open=open_, powered="false"))


def table(b, x, y, z, top="spruce_slab", leg="spruce_fence"):
    b.set(x, y, z, S(leg, north="false", south="false", east="false", west="false", waterlogged="false"))
    b.set(x, y + 1, z, S(top, type="bottom", waterlogged="false"))


def long_table(b, x0, x1, y, z, top="spruce_slab", chairs="spruce_stairs"):
    """A table along x with chairs on both sides (stairs facing the table)."""
    for x in range(x0, x1 + 1):
        b.set(x, y, z, S(top, type="top", waterlogged="false"))
        if chairs:
            b.set(x, y, z - 1, S(chairs, facing="south", half="bottom", shape="straight", waterlogged="false"))
            b.set(x, y, z + 1, S(chairs, facing="north", half="bottom", shape="straight", waterlogged="false"))


def lights(b, x0, z0, x1, z1, y, step=4, st=None):
    st = st or S("ceiling_light")
    for x in range(x0, x1 + 1, step):
        for z in range(z0, z1 + 1, step):
            b.set(x, y, z, st)


def carpet(b, x0, z0, x1, z1, y, colour):
    for x in range(x0, x1 + 1):
        for z in range(z0, z1 + 1):
            if b.get(x, y, z) in (None, AIR):
                b.set(x, y, z, S(f"{colour}_carpet"))


def disc(b, cx, cz, y, r, st, hollow=False):
    for x in range(int(cx - r - 1), int(cx + r + 2)):
        for z in range(int(cz - r - 1), int(cz + r + 2)):
            d = math.hypot(x - cx, z - cz)
            if d <= r + 0.3 and (not hollow or d >= r - 0.9):
                b.set(x, y, z, st)


def cylinder(b, cx, cz, y0, y1, r, wall, cap=None, inside=AIR):
    for y in range(y0, y1 + 1):
        for x in range(int(cx - r - 1), int(cx + r + 2)):
            for z in range(int(cz - r - 1), int(cz + r + 2)):
                d = math.hypot(x - cx, z - cz)
                if d <= r + 0.3:
                    b.set(x, y, z, wall if d >= r - 0.9 else inside)
    if cap:
        for k in range(0, int(r) + 1):
            disc(b, cx, cz, y1 + 1 + k, math.sqrt(max(0.0, r * r - (k * 1.2) ** 2)), cap)


def gable_roof(b, x0, z0, x1, z1, y, stairs, ridge="x", gable=None):
    """A pitched roof of stairs over the rectangle; ridge along x (slopes to north and south) or z."""
    if ridge == "x":
        depth = z1 - z0
        for k in range(0, depth // 2 + 1):
            for x in range(x0 - 1, x1 + 2):
                b.set(x, y + k, z0 - 1 + k, S(stairs, facing="south", half="bottom", shape="straight",
                                              waterlogged="false"))
                b.set(x, y + k, z1 + 1 - k, S(stairs, facing="north", half="bottom", shape="straight",
                                              waterlogged="false"))
            if gable:
                for z in range(z0 + k, z1 - k + 1):
                    b.set(x0, y + k, z, gable)
                    b.set(x1, y + k, z, gable)
        if depth % 2 == 0:
            for x in range(x0 - 1, x1 + 2):
                b.set(x, y + depth // 2 + 1, z0 + depth // 2, S(stairs.replace("_stairs", "_slab"), type="bottom",
                                                                 waterlogged="false"))
    else:
        width = x1 - x0
        for k in range(0, width // 2 + 1):
            for z in range(z0 - 1, z1 + 2):
                b.set(x0 - 1 + k, y + k, z, S(stairs, facing="east", half="bottom", shape="straight",
                                              waterlogged="false"))
                b.set(x1 + 1 - k, y + k, z, S(stairs, facing="west", half="bottom", shape="straight",
                                              waterlogged="false"))
            if gable:
                for x in range(x0 + k, x1 - k + 1):
                    b.set(x, y + k, z0, gable)
                    b.set(x, y + k, z1, gable)
        if width % 2 == 0:
            for z in range(z0 - 1, z1 + 2):
                b.set(x0 + width // 2, y + width // 2 + 1, z, S(stairs.replace("_stairs", "_slab"), type="bottom",
                                                                 waterlogged="false"))


def windows_band(b, x0, z0, x1, z1, y, every, st, skip_corners=True):
    for x in range(x0 + 1, x1):
        if (x - x0) % every:
            b.set(x, y, z0, st)
            b.set(x, y, z1, st)
    for z in range(z0 + 1, z1):
        if (z - z0) % every:
            b.set(x0, y, z, st)
            b.set(x1, y, z, st)


def pane(colour=None):
    name = f"{colour}_stained_glass_pane" if colour else "glass_pane"
    return S(name, north="false", south="false", east="false", west="false", waterlogged="false")


def fence_line(b, x0, z0, x1, z1, y, st, gap=None):
    for x in range(min(x0, x1), max(x0, x1) + 1):
        for z in range(min(z0, z1), max(z0, z1) + 1):
            if gap and gap(x, z):
                continue
            b.set(x, y, z, st)


def perimeter(b, x0, z0, x1, z1, y, st, gate=None, wire=None):
    """A fence around a rectangle; gate: (x, z) cells left open; wire: a block on top."""
    gate = gate or set()
    for x in range(x0, x1 + 1):
        for z in (z0, z1):
            if (x, z) not in gate:
                b.set(x, y, z, st)
                if wire:
                    b.set(x, y + 1, z, wire)
    for z in range(z0, z1 + 1):
        for x in (x0, x1):
            if (x, z) not in gate:
                b.set(x, y, z, st)
                if wire:
                    b.set(x, y + 1, z, wire)


BARS = S("iron_bars", north="false", south="false", east="false", west="false", waterlogged="false")
CHAIN_Y = S("iron_chain", axis="y", waterlogged="false")


def bars(**sides):
    p = {k: "false" for k in ("north", "south", "east", "west")}
    p.update({k: "true" for k, v in sides.items() if v})
    return S("iron_bars", waterlogged="false", **p)


def truck(b, x0, z0, facing_east=True, colour="green_terracotta", burnt=False):
    """A six-wheeled army truck: cab, bonnet, canvas-covered bed. Along x, the cab at the east end."""
    body = S("black_concrete") if burnt else S(colour)
    canvas = S("coal_block") if burnt else S("green_wool")
    sx = 1 if facing_east else -1
    def X(i):
        return x0 + sx * i
    for i in (1, 2, 6, 9):
        for z in (z0, z0 + 3):
            b.set(X(i), 1, z, S("black_concrete"))
    for i in range(0, 11):
        for z in range(z0, z0 + 4):
            b.set(X(i), 2, z, S("gray_concrete") if i < 8 else body)
    for i in range(0, 8):
        for z in range(z0, z0 + 4):
            for y in (3, 4):
                if z in (z0, z0 + 3) or i == 0:
                    b.set(X(i), y, z, canvas)
            b.set(X(i), 5, z, canvas)
    for z in range(z0, z0 + 4):
        for y in (3, 4):
            b.set(X(8), y, z, body)
            b.set(X(9), y, z, pane() if y == 4 and z in (z0 + 1, z0 + 2) else body)
        b.set(X(9), 5, z, body)
        b.set(X(8), 5, z, body)
        b.set(X(10), 3, z, body)
    b.set(X(10), 3, z0 + 1, S("iron_trapdoor", facing="east" if facing_east else "west", half="bottom",
                                open="true", powered="false", waterlogged="false"))


def jeep(b, x0, z0, colour="green_terracotta"):
    for x, z in ((x0, z0), (x0 + 3, z0), (x0, z0 + 2), (x0 + 3, z0 + 2)):
        b.set(x, 1, z, S("black_concrete"))
    for x in range(x0, x0 + 4):
        for z in range(z0, z0 + 3):
            b.set(x, 2, z, S(colour))
    b.set(x0 + 1, 3, z0 + 1, S("spruce_stairs", facing="west", half="bottom", shape="straight", waterlogged="false"))
    b.set(x0 + 2, 3, z0, pane())
    b.set(x0 + 2, 3, z0 + 2, pane())
    b.set(x0 + 2, 3, z0 + 1, S("lever", face="floor", facing="east", powered="false"))


def lattice_tower(b, cx, cz, y0, height, half=2, step=4):
    """A steel lattice mast: four corner legs, cross-bracing every few blocks, rest platforms."""
    for y in range(y0, y0 + height):
        h = half if y < y0 + height * 0.6 else max(1, half - 1)
        for dx in (-h, h):
            for dz in (-h, h):
                b.set(cx + dx, y, cz + dz, S("iron_block") if (y - y0) % step == 0 else bars())
        if (y - y0) % step == 0:
            for d in range(-h + 1, h):
                b.set(cx + d, y, cz - h, bars(east=True, west=True))
                b.set(cx + d, y, cz + h, bars(east=True, west=True))
                b.set(cx - h, y, cz + d, bars(north=True, south=True))
                b.set(cx + h, y, cz + d, bars(north=True, south=True))
    for y in range(y0 + 1, y0 + height):
        b.set(cx, y, cz - half + 1 if y < y0 + height * 0.6 else cz, S("ladder", facing="south"))


def dish(b, cx, y, cz, r, facing_up=True):
    """A parabolic dish: a bowl of concrete and a feed horn on struts."""
    for x in range(-r, r + 1):
        for z in range(-r, r + 1):
            d = math.hypot(x, z)
            if d <= r + 0.3:
                h = int(round((d * d) / (r * 1.6)))
                b.set(cx + x, y + h, cz + z, WHITE if d < r - 0.5 else LGREY)
    for k in range(1, r):
        b.set(cx, y + k, cz, bars())
    b.set(cx, y + r, cz, S("lightning_rod", facing="up", powered="false", waterlogged="false"))


# ---------------------------------------------------------------- bunkers (underground, floor at y = 0)
def bunker_small():
    """A family's fallout shelter: hatch house on the surface, a ladder shaft, an airlock and a furnished living room
    with bunk room, kitchen, radio corner, generator room, store and washroom. The last one awake curled up on the
    bunk, the dog at the foot of the bed; the radio still tuned to the emergency band."""
    SUR = 12
    b = Build("bunker_small", 1001)
    wall, floor = S("gray_concrete"), S("polished_andesite")
    # Main room and side rooms.
    b.room(0, 0, 0, 14, 5, 10, wall, floor)
    b.room(14, 0, 0, 20, 5, 5, wall, floor)     # generator room
    b.room(14, 0, 5, 20, 5, 10, wall, floor)    # store
    b.room(-5, 0, 5, 0, 5, 10, S("white_terracotta"), S("wall_tiles"))   # washroom
    b.room(4, 0, -6, 10, 5, 0, wall, S("hazard_concrete"))               # airlock
    for x in range(1, 14):
        for z in range(1, 10):
            b.set(x, 0, z, S("polished_andesite") if (x + z) % 2 else S("andesite"))
    # Doorways.
    door(b, 7, 1, 0)
    iron_door(b, 7, 1, 0, "south", open_=True)
    door(b, 14, 1, 2)
    door(b, 14, 1, 7)
    door(b, 0, 1, 8)
    wood_door(b, 0, 1, 8, "west")
    # The airlock: blast door to the shaft, a decontamination shower, suits on the wall.
    b.set(7, 1, -5, S("ladder", facing="south"))
    for y in range(1, SUR + 1):
        b.set(7, y, -5, S("ladder", facing="south"))
        b.set(7, y, -6, wall)
        b.set(6, y, -5, wall)
        b.set(8, y, -5, wall)
        b.set(7, y, -4, wall if y > 4 else AIR)
    b.set(5, 1, -4, S("locker", facing="east"))
    b.set(5, 1, -2, S("locker", facing="east"))
    b.set(9, 4, -3, S("leaking_pipe"))
    b.set(9, 1, -3, S("cauldron"))
    b.set(9, 3, -1, S("warning_sign", facing="west"))
    b.set(5, 3, -1, S("poster_quarantine", facing="east"))
    # Bunk room (west): three bunks along the wall, a footlocker each.
    for z in (2, 5):
        b.set(1, 1, z, S("bunk_bed", facing="east"))
        b.chest(2, 1, z, "bunker", "east")
    b.set(1, 1, 8, S("white_bed", facing="south", part="head"))
    b.set(1, 1, 9, S("white_bed", facing="south", part="foot"))
    skeleton(b, 1, 2, 5, "east", "skeleton_curled")
    b.set(2, 1, 9, S("dog_skeleton", facing="north"))
    partition_x(b, 4, 1, 3, 1, 4, S("spruce_planks"))
    carpet(b, 1, 1, 3, 9, 1, "brown")
    # Living area: table and four chairs, a shelf of books and board games.
    long_table(b, 7, 8, 1, 5)
    b.set(6, 1, 5, S("spruce_stairs", facing="east", half="bottom", shape="straight", waterlogged="false"))
    b.set(9, 1, 5, S("spruce_stairs", facing="west", half="bottom", shape="straight", waterlogged="false"))
    b.set(7, 2, 5, S("candle", candles=3, lit="false", waterlogged="false"))
    for x in (5, 6, 7, 8, 9):
        b.set(x, 1, 9, S("bookshelf"))
        b.set(x, 2, 9, S("chiseled_bookshelf", facing="north", **{f"slot_{i}_occupied": "false" for i in range(6)})
              if x % 2 else S("bookshelf"))
    b.set(7, 3, 9, S("poster_exodus", facing="north"))
    # Kitchen (east wall): stove, sink, pantry barrels, a calendar of crossed-out days (item frames are entities).
    b.set(13, 1, 2, S("furnace", facing="west", lit="false"))
    b.set(13, 1, 3, S("smoker", facing="west", lit="false"))
    b.set(13, 1, 4, S("cauldron"))
    b.set(13, 1, 5, S("crafting_table"))
    b.chest(13, 1, 6, "bunker_food", "west", "barrel")
    b.chest(13, 2, 6, "bunker_food", "west", "barrel")
    for z in range(2, 6):
        b.set(13, 3, z, S("spruce_trapdoor", facing="west", half="top", open="false", powered="false",
                          waterlogged="false"))
    # Radio corner (north-east): the radio, the terminal with the log, a chair turned to them.
    b.set(12, 1, 9, S("radio_set", facing="north"))
    b.set(11, 1, 9, S("data_terminal", facing="north", log=8))
    b.set(10, 1, 9, S("filing_cabinet", facing="north"))
    b.set(11, 1, 8, S("office_chair", facing="north"))
    skeleton(b, 12, 1, 8, "north", "skeleton_sitting")
    lights(b, 3, 2, 12, 9, 4, 4)
    # Generator room: generator, battery bank and cable along the wall, fuel drums.
    b.set(19, 1, 2, S("diesel_generator", facing="west"))
    b.set(19, 1, 4, S("battery_bank", facing="west"))
    for x in range(15, 19):
        b.set(x, 4, 1, S("cable", east="true", west="true", north="false", south="false", up="false",
                         down="false"))
    for x, z in ((15, 1), (16, 1), (15, 4)):
        b.set(x, 1, z, S("toxic_barrel"))
    b.set(17, 4, 3, S("ceiling_light"))
    # Store: shelves of barrels and crates.
    for x in range(15, 20):
        b.chest(x, 1, 9, "bunker" if x % 2 else "bunker_food", "north", "barrel")
        b.set(x, 2, 9, S("shipping_crate") if x % 3 else S("barrel", facing="up", open="false"))
    b.chest(19, 1, 6, "bunker", "west")
    b.set(17, 4, 7, S("ceiling_light"))
    # Washroom.
    b.set(-4, 1, 9, S("cauldron"))
    b.set(-4, 1, 8, S("cauldron"))
    b.set(-1, 1, 6, S("white_concrete"))
    b.set(-2, 3, 9, S("iron_trapdoor", facing="south", half="top", open="false", powered="false",
                      waterlogged="false"))
    # Air vent: a barred shaft up to a mushroom cap on the surface.
    for y in range(5, SUR + 1):
        b.set(3, y, 2, bars())
    b.set(3, 4, 2, S("iron_trapdoor", facing="north", half="top", open="false", powered="false", waterlogged="false"))
    b.set(3, SUR + 1, 2, bars())
    b.set(3, SUR + 2, 2, S("smooth_stone_slab", type="bottom", waterlogged="false"))
    # Hatch house on the surface: concrete walls, a steel door, sandbags, a mast.
    b.room(4, SUR, -9, 10, SUR + 4, -2, wall, S("gray_concrete"))
    b.set(7, SUR, -5, S("iron_trapdoor", facing="north", half="top", open="true", powered="false",
                         waterlogged="false"))
    iron_door(b, 7, SUR + 1, -2, "south")
    b.set(5, SUR + 1, -8, S("locker", facing="east"))
    b.set(9, SUR + 1, -8, S("toxic_barrel"))
    b.set(9, SUR + 3, -3, S("ceiling_light"))
    b.set(6, SUR + 2, -2, S("warning_sign", facing="south"))
    for x in range(3, 12):
        b.set(x, SUR + 1, 0, S("sandbags", facing="south") if x != 7 else AIR)
    b.set(3, SUR + 1, -1, S("sandbags", facing="west"))
    b.set(11, SUR + 1, -1, S("sandbags", facing="east"))
    for y in range(SUR + 5, SUR + 10):
        b.set(9, y, -8, bars())
    b.set(9, SUR + 10, -8, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    b.set(8, SUR + 8, -8, S("antenna_dish"))
    b.decay(0.02, keep=("minecraft:ladder", "minecraft:iron_door"), cracked=CONCRETE, top_bias=False)
    return b


def bunker_large():
    """A government continuity bunker on two levels under a concrete blockhouse: command room with the big map
    table, communications, dormitory, mess, infirmary; below the generator hall, water plant, armoury and archive.
    Staircases between the levels, a ladder and a blast door to the surface."""
    SUR = 15
    b = Build("bunker_large", 1002)
    wall, floor = S("gray_concrete"), S("polished_andesite")
    # Level B2: y 0..5. Level B1: y 6..11 (shares the slab at 6... separate shells for clarity).
    b.room(0, 0, 0, 32, 5, 22, wall, floor)
    b.room(0, 6, 0, 32, 11, 22, wall, floor)
    for x in range(1, 32):
        for z in range(1, 22):
            b.set(x, 6, z, S("polished_andesite") if (x // 2 + z // 2) % 2 else S("polished_deepslate"))
            b.set(x, 0, z, S("hazard_concrete") if z in (10, 11) else floor)
    # Central corridor along x on both levels (z 10..12), rooms north and south of it.
    for y0 in (0, 6):
        partition_z(b, 9, 1, 31, y0 + 1, y0 + 4, wall)
        partition_z(b, 13, 1, 31, y0 + 1, y0 + 4, wall)
        for x in (11, 21):
            partition_x(b, x, 1, 8, y0 + 1, y0 + 4, wall)
            partition_x(b, x, 14, 21, y0 + 1, y0 + 4, wall)
        for x in (5, 16, 26):
            iron_door(b, x, y0 + 1, 9, "north", open_=x != 26)
            iron_door(b, x, y0 + 1, 13, "south", open_=x != 16)
        lights(b, 2, 11, 31, 11, y0 + 4, 5)
        for x in range(1, 32, 6):
            b.set(x, y0 + 4, 10, S("pipe", east="true", west="true", north="false", south="false", up="false",
                                   down="false"))
    # Stairs from B1 down to B2 at the west end of the corridor.
    for k in range(0, 6):
        b.set(2 + k, 6 - k, 11, S("stone_brick_stairs", facing="west", half="bottom", shape="straight",
                                  waterlogged="false"))
        b.set(2 + k, 6 - k, 10, S("stone_brick_stairs", facing="west", half="bottom", shape="straight",
                                  waterlogged="false"))
        for y in range(7 - k, 9):
            b.set(2 + k, y, 11, AIR)
            b.set(2 + k, y, 10, AIR)
    # Shaft to the surface (east end): ladder and a blast door at the top.
    for y in range(7, SUR + 1):
        b.set(30, y, 11, S("ladder", facing="west"))
        b.set(31, y, 11, wall)
        b.set(30, y, 10, wall)
        b.set(30, y, 12, wall)
        b.set(29, y, 11, wall if y > 11 else AIR)
    b.set(30, 6, 11, floor)
    # --- B1 north: command room (x 1..10), communications (12..20), dormitory (22..31).
    # Command: the map table under a lamp, chairs round it, wall of server racks and terminals.
    for x in range(3, 9):
        for z in range(3, 7):
            b.set(x, 7, z, S("light_blue_terracotta") if (x + z) % 3 else S("cyan_terracotta"))
    for x in range(2, 10):
        b.set(x, 7, 2, S("office_chair", facing="south"))
        b.set(x, 7, 7, S("office_chair", facing="north"))
    for x in range(1, 11):
        b.set(x, 7, 1, S("server_rack", facing="south") if x % 3 else S("data_terminal", facing="south", log=8))
        b.set(x, 8, 1, S("server_rack", facing="south"))
    b.set(10, 9, 4, S("poster_exodus", facing="west"))
    skeleton(b, 6, 7, 7, "north", "skeleton_sitting")
    lights(b, 3, 3, 9, 7, 10, 3)
    # Communications: radios on desks, a chair overturned.
    for x in range(13, 20, 2):
        b.set(x, 7, 1, S("office_desk", facing="south"))
        b.set(x, 8, 1, S("radio_set", facing="south"))
        b.set(x, 7, 2, S("office_chair", facing="north"))
    b.set(20, 7, 5, S("filing_cabinet", facing="west"))
    b.set(20, 7, 6, S("filing_cabinet", facing="west"))
    b.set(12, 7, 7, S("radio_set", facing="east"))
    b.set(12, 7, 5, S("data_terminal", facing="east", log=6))
    lights(b, 14, 3, 19, 7, 10, 4)
    # Dormitory: bunks in two rows, lockers between.
    for x in range(22, 32, 2):
        b.set(x, 7, 1, S("bunk_bed", facing="south"))
        b.set(x, 7, 8, S("bunk_bed", facing="north"))
        b.set(x + 1, 7, 1, S("locker", facing="south"))
        b.set(x + 1, 7, 8, S("locker", facing="north"))
    b.chest(31, 7, 4, "bunker", "west")
    skeleton(b, 24, 8, 1, "east", "skeleton_lying")
    lights(b, 23, 3, 30, 7, 10, 4)
    # --- B1 south: mess (1..10), kitchen (12..20), infirmary (22..31).
    for z in (16, 19):
        long_table(b, 3, 8, 7, z)
    b.set(1, 7, 21, S("vending_machine", facing="east"))
    lights(b, 3, 15, 9, 20, 10, 3)
    for x in range(12, 21):
        b.set(x, 7, 21, S("smooth_stone"))
        b.set(x, 8, 21, S("smoker", facing="north", lit="false") if x % 3 == 0 else S("cauldron") if x % 3 == 1
              else S("crafting_table"))
    for x in range(13, 20, 3):
        b.chest(x, 7, 14, "bunker_food", "south", "barrel")
    b.set(16, 7, 17, S("smooth_stone_slab", type="top", waterlogged="false"))
    b.set(17, 7, 17, S("smooth_stone_slab", type="top", waterlogged="false"))
    lights(b, 13, 16, 19, 19, 10, 3)
    for x in range(23, 32, 3):
        b.set(x, 7, 21, S("hospital_bed", facing="north"))
        b.set(x + 1, 7, 21, S("iv_stand"))
    b.set(31, 7, 15, S("medical_cabinet", facing="west"))
    b.chest(31, 7, 16, "medical", "west")
    b.set(23, 7, 14, S("wheelchair", facing="south"))
    skeleton(b, 26, 8, 21, "north", "skeleton_lying")
    b.set(22, 9, 18, S("poster_quarantine", facing="east"))
    lights(b, 24, 16, 30, 20, 10, 3)
    # --- B2 north: generator hall (1..20), water plant (22..31).
    for x in (4, 10, 16):
        b.set(x, 1, 3, S("diesel_generator", facing="south"))
        b.set(x + 1, 1, 3, S("diesel_generator", facing="south"))
        b.set(x, 1, 6, S("battery_bank", facing="north"))
    for x in range(1, 21):
        b.set(x, 4, 1, S("cable", east="true", west="true", north="false", south="false", up="false",
                         down="false"))
        b.set(x, 4, 8, S("pipe", east="true", west="true", north="false", south="false", up="false",
                         down="false"))
    for x in (2, 7, 13, 19):
        b.set(x, 1, 1, S("toxic_barrel"))
    b.set(20, 1, 5, S("control_cabinet", facing="west"))
    lights(b, 3, 3, 19, 7, 4, 4)
    for x in (24, 28):
        cylinder(b, x, 4.5, 1, 4, 1.6, S("rusted_panel"))
    b.set(31, 1, 2, S("coolant_tank", facing="west", level=0))
    b.set(31, 1, 7, S("leaking_pipe"))
    # --- B2 south: armoury (1..10), archive (12..20), workshop (22..31).
    for x in range(2, 10, 2):
        b.set(x, 1, 21, S("locker", facing="north"))
        b.chest(x + 1, 1, 21, "military", "north")
    b.set(1, 1, 15, S("sandbags", facing="east"))
    b.set(1, 2, 15, S("sandbags", facing="east"))
    for x in range(13, 21):
        for y in (1, 2, 3):
            b.set(x, y, 21, S("filing_cabinet", facing="north") if (x + y) % 2 else S("bookshelf"))
    b.set(16, 1, 17, S("office_desk", facing="south"))
    b.set(16, 1, 16, S("office_chair", facing="south"))
    b.set(14, 1, 15, S("data_terminal", facing="south", log=8))
    b.chest(20, 1, 15, "bunker", "west")
    b.set(25, 1, 21, S("repair_station", facing="north"))
    b.set(27, 1, 21, S("radio_set", facing="north"))
    b.set(29, 1, 21, S("anvil", facing="east"))
    b.set(31, 1, 18, S("shipping_crate"))
    b.set(31, 2, 18, S("shipping_crate"))
    lights(b, 3, 16, 30, 20, 4, 5)
    # The blockhouse on the surface: thick walls, firing slits, the blast door over the shaft.
    b.room(25, SUR, 6, 35, SUR + 5, 16, S("sooty_concrete"), S("gray_concrete"))
    b.set(30, SUR, 11, S("iron_trapdoor", facing="west", half="top", open="true", powered="false",
                          waterlogged="false"))
    b.set(25, SUR + 1, 11, S("blast_door", facing="west"))
    door(b, 25, SUR + 2, 11)
    for z in (8, 14):
        b.set(25, SUR + 3, z, bars())
        b.set(35, SUR + 3, z, bars())
    for x in range(22, 25):
        for z in (9, 13):
            b.set(x, SUR + 1, z, S("sandbags", facing="west"))
            b.set(x, SUR + 2, z, S("sandbags", facing="west"))
    b.set(33, SUR + 6, 8, S("antenna_dish"))
    for y in range(SUR + 6, SUR + 12):
        b.set(34, y, 14, bars())
    b.set(34, SUR + 12, 14, S("ship_light_red"))
    b.set(28, SUR + 4, 9, S("ceiling_light"))
    b.decay(0.02, keep=("minecraft:ladder", "minecraft:iron_door"), cracked=CONCRETE, top_bias=False)
    return b


# ---------------------------------------------------------------- farmstead
def farmhouse():
    """A family farm: the two-storey farmhouse with porch, kitchen, living room and bedrooms; a gambrel barn with
    stalls, hayloft and the old tractor; a grain silo, a windpump over the dry well, fields of dead crops with a
    scarecrow and a fenced paddock."""
    b = Build("farmhouse", 1015)
    ground(b, -6, -10, 40, 34, lambda x, z: S("coarse_dirt") if b.rnd.random() < 0.6 else S("dirt_path")
           if b.rnd.random() < 0.3 else S("podzol"))
    # --- House: x 0..10, z 0..8, floors at y 0 and 5.
    plank, log = S("spruce_planks"), S("stripped_spruce_log", axis="y")
    for y in range(1, 10):
        for x in range(0, 11):
            for z in range(0, 9):
                edge = x in (0, 10) or z in (0, 8)
                corner = x in (0, 10) and z in (0, 8)
                if y == 5:
                    b.set(x, y, z, plank if not edge else S("stripped_spruce_log", axis="x" if z in (0, 8) else "z"))
                elif edge:
                    b.set(x, y, z, log if corner else S("white_terracotta") if y < 5 else plank)
                else:
                    b.set(x, y, z, AIR)
    for x in range(0, 11):
        for z in range(0, 9):
            b.set(x, 0, z, S("spruce_planks") if 0 < x < 10 and 0 < z < 8 else S("cobblestone"))
    for y in (2, 3, 7, 8):
        for x in (2, 3, 7, 8):
            b.set(x, y, 0, pane())
            b.set(x, y, 8, pane())
        for z in (3, 5):
            b.set(0, y, z, pane())
            b.set(10, y, z, pane())
    gable_roof(b, 0, 0, 10, 8, 10, "dark_oak_stairs", "x", gable=plank)
    wood_door(b, 5, 1, 0, "south")
    # Porch: posts, deck, a roof of slabs, the rocking chair.
    for x in range(-1, 12):
        for z in (-3, -2, -1):
            b.set(x, 0, z, S("spruce_planks"))
            b.set(x, 4, z, S("dark_oak_slab", type="bottom", waterlogged="false"))
    for x in (-1, 3, 7, 11):
        for y in range(1, 4):
            b.set(x, y, -3, S("spruce_fence", north="false", south="false", east="false", west="false",
                              waterlogged="false"))
    b.set(2, 1, -2, S("spruce_stairs", facing="north", half="bottom", shape="straight", waterlogged="false"))
    b.set(8, 1, -2, S("composter", level=0))
    skeleton(b, 9, 1, -2, "south", "skeleton_sitting")
    # Ground floor: living room (west) with fireplace, kitchen (east).
    partition_x(b, 5, 3, 7, 1, 4, plank)
    for z in range(2, 7):
        b.set(1, 1, z, S("bricks") if z in (2, 6) else AIR)
    b.set(1, 1, 4, S("campfire", facing="east", lit="false", signal_fire="false", waterlogged="false"))
    for y in range(2, 10):
        b.set(1, y, 4, S("bricks"))
        b.set(0, y, 4, S("bricks"))
    b.set(0, 10, 4, S("bricks"))
    b.set(0, 11, 4, S("bricks"))
    b.set(0, 12, 4, S("bricks"))
    b.set(3, 1, 3, S("spruce_stairs", facing="west", half="bottom", shape="straight", waterlogged="false"))
    b.set(3, 1, 5, S("spruce_stairs", facing="west", half="bottom", shape="straight", waterlogged="false"))
    carpet(b, 2, 2, 4, 6, 1, "red")
    b.set(1, 1, 7, S("bookshelf"))
    b.set(4, 1, 7, S("bookshelf"))
    b.set(2, 1, 1, S("note_block", instrument="harp", note=0, powered="false"))
    b.set(9, 1, 7, S("furnace", facing="north", lit="false"))
    b.set(8, 1, 7, S("cauldron"))
    b.set(7, 1, 7, S("smoker", facing="north", lit="false"))
    b.chest(9, 1, 6, "farm", "west", "barrel")
    for x in (7, 8, 9):
        b.set(x, 3, 7, S("spruce_trapdoor", facing="north", half="top", open="false", powered="false",
                         waterlogged="false"))
    table(b, 8, 1, 3)
    b.set(8, 1, 2, S("spruce_stairs", facing="south", half="bottom", shape="straight", waterlogged="false"))
    b.set(7, 1, 3, S("spruce_stairs", facing="east", half="bottom", shape="straight", waterlogged="false"))
    b.set(9, 4, 3, S("lantern", hanging="true", waterlogged="false"))
    b.set(3, 4, 4, S("lantern", hanging="true", waterlogged="false"))
    # Stairs up along the front wall (east half), a hole in the upper floor over them.
    for k in range(0, 4):
        b.set(9 - k, 1 + k, 1, S("spruce_stairs", facing="west", half="bottom", shape="straight",
                                 waterlogged="false"))
    for x in range(6, 9):
        b.set(x, 5, 1, AIR)
    # Upper floor: the parents' bedroom, the children's room, a landing.
    partition_x(b, 5, 1, 5, 6, 9, plank)
    b.set(2, 6, 2, S("red_bed", facing="north", part="head"))
    b.set(2, 6, 3, S("red_bed", facing="north", part="foot"))
    b.set(3, 6, 2, S("red_bed", facing="north", part="head"))
    b.set(3, 6, 3, S("red_bed", facing="north", part="foot"))
    skeleton(b, 2, 7, 2, "south", "skeleton_lying")
    skeleton(b, 3, 7, 3, "north", "skeleton_curled")
    b.chest(1, 6, 6, "farm", "east")
    b.set(4, 6, 1, S("barrel", facing="up", open="false"))
    carpet(b, 1, 4, 4, 7, 6, "brown")
    b.set(9, 6, 3, S("light_blue_bed", facing="east", part="head"))
    b.set(8, 6, 3, S("light_blue_bed", facing="east", part="foot"))
    b.set(9, 6, 6, S("light_blue_bed", facing="east", part="head"))
    b.set(8, 6, 6, S("light_blue_bed", facing="east", part="foot"))
    b.set(9, 6, 7, S("crafting_table"))
    b.set(6, 6, 4, S("dog_skeleton", facing="east"))
    carpet(b, 6, 4, 9, 5, 6, "yellow")
    # A hole in the roof where a storm took the tiles.
    collapse(b, 6, 0, 11, 8, 10, 15, 0.25, rubble=True)
    # --- Barn: x 16..30, z -2..12, gambrel roof along x.
    for y in range(1, 8):
        for x in range(16, 31):
            for z in range(-2, 13):
                edge = x in (16, 30) or z in (-2, 12)
                b.set(x, y, z, (S("red_terracotta") if (x + y) % 5 else S("stripped_dark_oak_log", axis="y"))
                      if edge else AIR)
    for x in range(16, 31):
        for z in range(-2, 13):
            b.set(x, 0, z, S("coarse_dirt"))
    profile = [(8, -1), (9, 1), (10, 3), (11, 5), (12, 6)]
    for y, inset in profile:
        for x in range(15, 32):
            for z in range(-3 + inset, 14 - inset):
                edge = z in (-3 + inset, 13 - inset)
                if edge or y == 12:
                    b.set(x, y, z, S("dark_oak_planks"))
                elif x in (16, 30):
                    b.set(x, y, z, S("red_terracotta"))
                else:
                    b.set(x, y, z, AIR)
    for x in range(15, 32):
        b.set(x, 8, -2, S("dark_oak_planks"))
        b.set(x, 8, 12, S("dark_oak_planks"))
    for y in range(1, 7):
        for z in range(3, 8):
            b.set(16, y, z, AIR)
    for z in (3, 7):
        for y in range(1, 6):
            b.set(16, y, z, S("dark_oak_planks"))
    # Stalls along the north wall, the hayloft over the east half, the tractor in the middle.
    for x in range(18, 30, 3):
        for z in range(-1, 3):
            b.set(x, 1, z, S("spruce_fence", north="true", south="true", east="false", west="false",
                             waterlogged="false"))
        b.set(x + 1, 1, -1, S("hay_block", axis="y"))
    b.set(20, 1, 0, S("cow_skeleton", facing="south"))
    b.set(26, 1, 0, S("horse_skeleton", facing="south"))
    for x in range(24, 30):
        for z in range(-1, 12):
            b.set(x, 5, z, S("spruce_slab", type="top", waterlogged="false"))
            if b.rnd.random() < 0.5:
                b.set(x, 6, z, S("hay_block", axis="x"))
    for y in range(1, 6):
        b.set(23, y, 11, S("ladder", facing="north"))
    # Tractor: big rear wheels, small front wheels, the green bonnet, a seat and a steering wheel.
    tx, tz = 18, 6
    for dz in (0, 3):
        for (dx, dy) in ((0, 1), (1, 1), (0, 2), (1, 2), (0, 3), (1, 3)):
            b.set(tx + dx, dy, tz + dz, S("black_concrete"))
        b.set(tx + 5, 1, tz + dz, S("black_concrete"))
    for dx in range(0, 7):
        for dz in (1, 2):
            b.set(tx + dx, 2, tz + dz, S("green_terracotta") if dx > 1 else S("gray_concrete"))
            if dx >= 3:
                b.set(tx + dx, 3, tz + dz, S("green_terracotta"))
    b.set(tx + 1, 3, tz + 1, S("spruce_stairs", facing="west", half="bottom", shape="straight", waterlogged="false"))
    b.set(tx + 2, 4, tz + 1, S("lever", face="floor", facing="east", powered="false"))
    b.set(tx + 6, 4, tz + 2, bars())
    b.set(tx + 6, 5, tz + 2, S("black_concrete"))
    # Plough and tools.
    for dz in range(5, 10):
        b.set(27, 1, dz, S("iron_bars", north="true", south="true", east="false", west="false", waterlogged="false"))
    b.chest(29, 1, 10, "farm", "west")
    b.set(29, 1, 4, S("grindstone", face="floor", facing="west"))
    # --- Silo: x 34, z 4.
    cylinder(b, 35, 4, 1, 16, 2.6, S("iron_block"), cap=S("light_gray_concrete"))
    for y in range(1, 17, 4):
        disc(b, 35, 4, y, 2.7, S("rusted_panel"), hollow=True)
    for y in range(1, 17):
        b.set(32, y, 4, S("ladder", facing="west"))
    for y in range(1, 15):
        b.set(35, y, 4, S("hay_block", axis="y") if y < 6 else AIR)
    # --- Windpump over the dry well: x 12, z 22.
    disc(b, 12, 22, 1, 1.5, S("cobblestone"), hollow=True)
    b.set(12, 0, 22, S("mud"))
    b.set(12, 1, 22, S("cauldron"))
    for y in range(1, 9):
        for dx, dz in ((-1, -1), (1, 1), (-1, 1), (1, -1)):
            b.set(12 + dx, y, 22 + dz, S("iron_block") if y % 4 == 0 else bars())
    for y in range(9, 14):
        b.set(12, y, 22, S("iron_block") if y == 13 else bars())
    for k in range(1, 4):
        for (dx, dy) in ((k, 0), (-k, 0), (0, k), (0, -k)):
            b.set(12 + dx, 13 + dy, 21, S("iron_trapdoor", facing="north", half="bottom", open="true",
                                          powered="false", waterlogged="false"))
    b.set(12, 13, 23, S("iron_trapdoor", facing="south", half="bottom", open="true", powered="false",
                         waterlogged="false"))
    # --- Fields with dead crops and a scarecrow, a fenced paddock.
    for row in range(0, 8):
        for x in range(-4, 10):
            z = 14 + row * 2
            b.set(x, 0, z, S("farmland", moisture=0))
            r = b.rnd.random()
            if r < 0.45:
                b.set(x, 1, z, S("dead_bush"))
            elif r < 0.6:
                b.set(x, 1, z, S("wheat", age=7))
            b.set(x, 0, z + 1, S("coarse_dirt"))
    b.set(3, 1, 21, S("spruce_fence", north="false", south="false", east="false", west="false", waterlogged="false"))
    b.set(3, 2, 21, S("hay_block", axis="y"))
    b.set(2, 2, 21, S("spruce_fence", north="false", south="false", east="true", west="false", waterlogged="false"))
    b.set(4, 2, 21, S("spruce_fence", north="false", south="false", east="false", west="true", waterlogged="false"))
    b.set(3, 3, 21, S("carved_pumpkin", facing="south"))
    perimeter(b, 18, 16, 36, 30, 1, S("dead_fence", north="false", south="false", east="false", west="false",
                                      waterlogged="false"), gate={(18, 22), (18, 23)})
    b.set(30, 1, 26, S("horse_skeleton", facing="west"))
    b.set(24, 1, 20, S("cow_skeleton", facing="north"))
    b.set(32, 1, 18, S("cauldron"))
    # The mailbox and the family's car by the road.
    b.set(-4, 1, -6, S("spruce_fence", north="false", south="false", east="false", west="false", waterlogged="false"))
    b.set(-4, 2, -6, S("barrel", facing="east", open="false"))
    b.set(-1, 1, -7, S("car_wreck_blue", facing="east"))
    b.decay(0.03, keep=("minecraft:farmland",), cracked={"minecraft:white_terracotta": "light_gray_terracotta"})
    return b


# ---------------------------------------------------------------- radio station
def radio_tower():
    """An emergency broadcast station: a 50-block lattice mast with platforms, dishes and the warning lamp; the
    concrete transmitter building (studio, transmitter hall, generator, the engineer's bunk room); a fallen dish,
    a fenced compound with a gate."""
    b = Build("radio_tower", 1010)
    ground(b, -14, -14, 30, 24, lambda x, z: S("gravel") if b.rnd.random() < 0.5 else dead_ground(b))
    for x in range(-2, 3):
        for z in range(-2, 3):
            b.set(x, 0, z, S("gray_concrete"))
    lattice_tower(b, 0, 0, 1, 50, half=2, step=4)
    for y in (16, 32, 44):
        h = 2 if y < 30 else 1
        for x in range(-h - 1, h + 2):
            for z in range(-h - 1, h + 2):
                if abs(x) == h + 1 or abs(z) == h + 1:
                    b.set(x, y, z, S("metal_grating"))
                    b.set(x, y + 1, z, bars())
        b.set(h + 1, y + 1, 0, S("antenna_dish"))
        b.set(-h - 1, y + 1, 0, S("antenna_dish"))
    for y in range(51, 56):
        b.set(0, y, 0, bars())
    b.set(0, 56, 0, S("ship_light_red"))
    b.set(0, 57, 0, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    # Guy anchors: concrete blocks with chains rising a little.
    for (x, z) in ((-12, -12), (12, -12), (-12, 12), (12, 12)):
        b.set(x, 0, z, S("gray_concrete"))
        b.set(x, 1, z, S("gray_concrete"))
        b.set(x, 2, z, CHAIN_Y)
    # Transmitter building: x 8..24, z 6..18.
    b.room(8, 0, 6, 24, 6, 18, S("light_gray_concrete"), S("polished_andesite"))
    partition_x(b, 16, 7, 17, 1, 5, S("light_gray_concrete"))
    partition_z(b, 13, 17, 23, 1, 5, S("light_gray_concrete"))
    door(b, 16, 1, 10)
    door(b, 20, 1, 13)
    iron_door(b, 12, 1, 6, "north", open_=True)
    windows_band(b, 8, 6, 24, 18, 3, 4, pane())
    # Studio: desk with microphone and mixing desk, the on-air lamp, soundproofing.
    for x in range(9, 16):
        for y in range(1, 6):
            b.set(x, y, 18, S("gray_wool") if (x + y) % 2 else S("light_gray_wool"))
    b.set(12, 1, 15, S("office_desk", facing="south"))
    b.set(13, 1, 15, S("office_desk", facing="south"))
    b.set(12, 2, 15, S("radio_set", facing="south"))
    b.set(13, 2, 15, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    b.set(12, 1, 14, S("office_chair", facing="north"))
    skeleton(b, 13, 1, 14, "north", "skeleton_sitting")
    b.set(9, 4, 12, S("ship_light_red"))
    b.set(10, 1, 7, S("filing_cabinet", facing="south"))
    b.set(11, 1, 7, S("filing_cabinet", facing="south"))
    b.set(15, 1, 17, S("data_terminal", facing="north", log=6))
    # Transmitter hall: racks of valves, the big transmitter cabinets, cable trays to the mast.
    for x in range(17, 24):
        b.set(x, 1, 7, S("server_rack", facing="south"))
        b.set(x, 2, 7, S("server_rack", facing="south"))
        b.set(x, 5, 9, S("cable", east="true", west="true", north="false", south="false", up="false",
                         down="false"))
    b.set(23, 1, 11, S("control_cabinet", facing="west"))
    b.set(17, 1, 12, S("sparking_panel"))
    # Bunk room of the engineer.
    b.set(23, 1, 17, S("bunk_bed", facing="west"))
    b.chest(23, 1, 15, "bunker", "west")
    b.set(18, 1, 17, S("smoker", facing="north", lit="false"))
    b.set(17, 1, 17, S("barrel", facing="up", open="false"))
    lights(b, 10, 8, 23, 17, 5, 4)
    # Generator shed and fuel tank outside.
    b.set(26, 1, 9, S("diesel_generator", facing="west"))
    b.set(26, 1, 11, S("diesel_generator", facing="west"))
    cylinder(b, 28, 15, 1, 3, 1.6, S("rusted_panel"))
    for x in range(25, 28):
        b.set(x, 4, 8, S("dark_oak_slab", type="bottom", waterlogged="false"))
        b.set(x, 4, 12, S("dark_oak_slab", type="bottom", waterlogged="false"))
    # A fallen dish on its side, the feed arm snapped.
    for x in range(-6, 7):
        for y in range(0, 10):
            d = math.hypot(x, y - 5)
            if d <= 5.3:
                depth = int(round(d * d / 9))
                b.set(-10 + depth, y + 1, 14 + x, WHITE if d < 4.6 else LGREY)
    b.set(-6, 2, 14, bars())
    b.set(-5, 2, 14, bars())
    # Fence with barbed wire and a gate.
    perimeter(b, -14, -14, 30, 24, 1, S("dead_fence", north="false", south="false", east="false", west="false",
                                         waterlogged="false"), gate={(4, 24), (5, 24), (6, 24)},
              wire=S("barbed_wire", facing="north"))
    b.set(3, 2, 25, S("warning_sign", facing="south"))
    b.decay(0.03, keep=("minecraft:ladder",), cracked=CONCRETE)
    return b


# ---------------------------------------------------------------- military post
def barracks():
    """A quarantine-era army post: wire perimeter with a gate and sentry box, two barrack blocks (bunks, lockers,
    the sergeant's office), mess hall with kitchen, armoury, motor pool with trucks and a jeep, watchtowers, a helipad
    with the wreck of a helicopter, sandbagged positions."""
    b = Build("barracks", 1012)
    ground(b, -2, -2, 50, 40, lambda x, z: S("gravel") if b.rnd.random() < 0.35 else S("coarse_dirt"))
    perimeter(b, 0, 0, 48, 38, 1, S("iron_bars", north="false", south="false", east="false", west="false",
                                    waterlogged="false"), gate={(22, 0), (23, 0), (24, 0), (25, 0)},
              wire=S("barbed_wire", facing="north"))
    perimeter(b, 0, 0, 48, 38, 2, bars(), gate={(22, 0), (23, 0), (24, 0), (25, 0)})
    # Gate: boom barrier, sentry box.
    for x in range(22, 26):
        b.set(x, 2, -1, S("spruce_fence", north="false", south="false", east="true", west="true", waterlogged="false"))
    b.room(18, 0, 1, 21, 4, 4, S("green_terracotta"), S("gray_concrete"))
    for y in (2, 3):
        b.set(21, y, 2, pane())
        b.set(19, y, 1, pane())
    door(b, 20, 1, 4)
    b.set(19, 1, 3, S("office_chair", facing="east"))
    b.set(20, 1, 2, S("radio_set", facing="east"))
    b.set(26, 1, 1, S("quarantine_barrier", facing="north"))
    b.set(27, 2, 1, S("warning_sign", facing="north"))
    # Barrack blocks: x 3..19, z 8..16 and x 3..19, z 20..28.
    for z0 in (8, 20):
        b.room(3, 0, z0, 19, 5, z0 + 8, S("green_terracotta"), S("spruce_planks"))
        gable_roof(b, 3, z0, 19, z0 + 8, 5, "dark_oak_stairs", "x", gable=S("green_terracotta"))
        windows_band(b, 3, z0, 19, z0 + 8, 3, 3, pane())
        door(b, 19, 1, z0 + 4)
        wood_door(b, 19, 1, z0 + 4, "east")
        partition_x(b, 15, z0 + 1, z0 + 7, 1, 4, S("spruce_planks"))
        door(b, 15, 1, z0 + 4)
        for x in range(4, 15, 2):
            b.set(x, 1, z0 + 1, S("bunk_bed", facing="south"))
            b.set(x, 1, z0 + 7, S("bunk_bed", facing="north"))
            b.set(x + 1, 1, z0 + 1, S("locker", facing="south"))
            b.set(x + 1, 1, z0 + 7, S("locker", facing="north"))
        b.chest(4, 1, z0 + 2, "military", "east")
        lights(b, 5, z0 + 4, 13, z0 + 4, 4, 4, S("lantern", hanging="true", waterlogged="false"))
        b.set(17, 1, z0 + 6, S("office_desk", facing="west"))
        b.set(16, 1, z0 + 6, S("office_chair", facing="east"))
        b.set(17, 1, z0 + 2, S("filing_cabinet", facing="west"))
        b.set(18, 1, z0 + 1, S("radio_set", facing="south"))
        b.set(16, 3, z0 + 7, S("poster_quarantine", facing="north"))
    skeleton(b, 6, 2, 9, "east", "skeleton_lying")
    skeleton(b, 16, 1, 25, "east", "skeleton_sitting")
    b.set(17, 1, 25, S("data_terminal", facing="west", log=8))
    # Mess hall: x 26..44, z 8..18.
    b.room(26, 0, 8, 44, 6, 18, S("green_terracotta"), S("smooth_stone"))
    gable_roof(b, 26, 8, 44, 18, 6, "dark_oak_stairs", "x", gable=S("green_terracotta"))
    windows_band(b, 26, 8, 44, 18, 3, 3, pane())
    door(b, 26, 1, 13)
    door(b, 26, 1, 12)
    for z in (11, 15):
        long_table(b, 28, 37, 1, z)
    partition_x(b, 39, 9, 17, 1, 4, S("white_concrete"))
    door(b, 39, 1, 13)
    for z in range(9, 18):
        b.set(43, 1, z, S("smoker", facing="west", lit="false") if z % 3 == 0 else S("cauldron") if z % 3 == 1
              else S("smooth_stone"))
    b.chest(41, 1, 17, "bunker_food", "north", "barrel")
    b.chest(41, 1, 9, "bunker_food", "south", "barrel")
    b.set(27, 1, 17, S("vending_machine", facing="south"))
    lights(b, 28, 10, 38, 16, 5, 5, S("lantern", hanging="true", waterlogged="false"))
    # Armoury: a concrete box with racks.
    b.room(26, 0, 22, 34, 5, 28, S("sooty_concrete"), S("gray_concrete"))
    iron_door(b, 30, 1, 22, "north")
    for x in range(27, 34):
        b.chest(x, 1, 27, "military", "north") if x % 2 else b.set(x, 1, 27, S("locker", facing="north"))
    b.set(27, 1, 23, S("anvil", facing="north"))
    b.set(33, 1, 23, S("repair_station", facing="west"))
    b.set(30, 4, 25, S("ceiling_light"))
    # Motor pool: a carport roof on posts, two trucks and a jeep.
    for x in range(36, 47):
        for z in range(22, 36):
            b.set(x, 0, z, S("gray_concrete") if b.rnd.random() < 0.8 else S("tar_puddle"))
            b.set(x, 7, z, S("smooth_stone_slab", type="bottom", waterlogged="false"))
    for x in (36, 41, 46):
        for z in (22, 35):
            for y in range(1, 7):
                b.set(x, y, z, S("iron_bars"))
    truck(b, 37, 23, True)
    truck(b, 37, 29, True, burnt=True)
    jeep(b, 42, 33)
    for x, z in ((45, 24), (45, 25), (44, 24)):
        b.set(x, 1, z, S("toxic_barrel"))
    # Helipad and the helicopter.
    disc(b, 10, 34, 0, 4.5, S("gray_concrete"))
    for (dx, dz) in ((-1, -2), (-1, -1), (-1, 0), (-1, 1), (-1, 2), (1, -2), (1, -1), (1, 0), (1, 1), (1, 2), (0, 0)):
        b.set(10 + dx, 0, 34 + dz, S("yellow_concrete"))
    hx, hz = 7, 33
    for dx in range(0, 6):
        for dz in range(0, 3):
            for y in (1, 2, 3):
                if dx < 4 or y > 1:
                    b.set(hx + dx, y, hz + dz, S("green_terracotta") if not (dx == 0 and y == 2) else pane())
    for dx in range(6, 12):
        b.set(hx + dx, 2, hz + 1, S("green_terracotta"))
    b.set(hx + 11, 3, hz + 1, S("green_terracotta"))
    b.set(hx + 2, 4, hz + 1, S("iron_block"))
    # The rotor: one blade drooping along the tail, one across, a broken one in the dust.
    blade = S("iron_trapdoor", facing="east", half="top", open="false", powered="false", waterlogged="false")
    for k in range(1, 6):
        b.set(hx + 2 + k, 5 if k < 4 else 4, hz + 1, blade)
    for k in (1, 2, 3):
        b.set(hx + 2, 5, hz + 1 - k, blade)
        b.set(hx + 2, 5, hz + 1 + k, blade)
    for k in range(0, 4):
        b.set(hx - 3 + k, 1, hz + 4, blade)
    skeleton(b, hx + 1, 2, hz + 1, "west")
    # Watchtowers at two corners.
    for (tx, tz) in ((1, 37), (47, 1)):
        for y in range(1, 9):
            for dx, dz in ((0, 0), (2, 0), (0, 2), (2, 2)):
                b.set(tx + dx - 1, y, tz + dz - 1, S("dark_oak_fence", north="false", south="false", east="false",
                                                     west="false", waterlogged="false"))
        for dx in range(-2, 3):
            for dz in range(-2, 3):
                b.set(tx + dx, 9, tz + dz, S("dark_oak_planks"))
                if abs(dx) == 2 or abs(dz) == 2:
                    b.set(tx + dx, 10, tz + dz, S("sandbags", facing="north"))
                b.set(tx + dx, 13, tz + dz, S("dark_oak_slab", type="bottom", waterlogged="false"))
        for y in range(1, 9):
            b.set(tx, y, tz, S("ladder", facing="north"))
        b.set(tx + 1, 10, tz + 1, S("street_lamp", facing="north"))
    # Sandbagged firing positions by the gate.
    for (x0, z0) in ((14, 5), (30, 5)):
        for dx in range(0, 4):
            b.set(x0 + dx, 1, z0, S("sandbags", facing="north"))
            b.set(x0 + dx, 2, z0, S("sandbags", facing="north"))
        b.set(x0, 1, z0 + 1, S("sandbags", facing="west"))
        b.set(x0 + 3, 1, z0 + 1, S("sandbags", facing="east"))
    # Flagpole.
    for y in range(1, 12):
        b.set(24, y, 19, bars())
    b.set(25, 10, 19, S("white_wool"))
    b.set(26, 10, 19, S("white_wool"))
    b.set(25, 9, 19, S("red_wool"))
    b.decay(0.03, cracked={"minecraft:green_terracotta": "brown_terracotta"})
    return b


# ---------------------------------------------------------------- oil
def oil_pumpjack():
    """A working-scale pumpjack (samson post, walking beam, horse head over the well, crank and counterweights, the
    motor), its storage tanks with ladders, the flow line, the operator's shack, spilled oil."""
    b = Build("oil_pumpjack", 1014)
    ground(b, -8, -8, 26, 16, lambda x, z: S("tar_sand") if b.rnd.random() < 0.3 else S("coarse_dirt"))
    for x in range(-2, 14):
        for z in range(-2, 4):
            b.set(x, 0, z, S("gray_concrete"))
    # Samson post: an A-frame.
    for y in range(1, 9):
        k = (8 - y) // 3
        b.set(5 - k, y, 0, S("iron_block") if y % 3 == 0 else bars())
        b.set(5 + k, y, 0, S("iron_block") if y % 3 == 0 else bars())
        b.set(5 - k, y, 2, S("iron_block") if y % 3 == 0 else bars())
        b.set(5 + k, y, 2, S("iron_block") if y % 3 == 0 else bars())
    for z in range(0, 3):
        b.set(5, 9, z, S("iron_block"))
    # Walking beam, tilted (nodding) a little, horse head at the west end.
    for x in range(-1, 12):
        y = 10 + (1 if x < 2 else 0) - (1 if x > 9 else 0)
        b.set(x, y, 1, S("black_concrete") if x % 4 else S("iron_block"))
    for y in range(6, 13):
        b.set(-2, y, 1, S("black_concrete"))
        b.set(-1, y, 1, S("yellow_concrete") if 7 < y < 12 else S("black_concrete"))
    b.set(-3, 9, 1, S("black_concrete"))
    b.set(-3, 10, 1, S("black_concrete"))
    for y in range(2, 6):
        b.set(-2, y, 1, CHAIN_Y)
    # Well head: Christmas tree of pipes and valves.
    b.set(-2, 1, 1, S("iron_block"))
    b.set(-2, 0, 1, S("pipe", up="true", down="true", north="false", south="false", east="false", west="false"))
    b.set(-3, 1, 1, S("pipe", east="true", west="true", north="false", south="false", up="false", down="false"))
    b.set(-2, 1, 0, S("lever", face="wall", facing="north", powered="false"))
    # Crank and counterweights at the east end, the motor.
    for (dx, dy) in ((0, 0), (1, 0), (0, 1), (1, 1), (-1, 0), (2, 0), (0, -1), (1, -1)):
        b.set(11 + dx, 4 + dy, -1, S("gray_concrete"))
        b.set(11 + dx, 4 + dy, 3, S("gray_concrete"))
    for y in range(5, 10):
        b.set(11, y, 0, bars())
        b.set(11, y, 2, bars())
    b.set(13, 1, 1, S("diesel_generator", facing="west"))
    b.set(12, 1, 1, S("iron_block"))
    # Storage tanks and the flow line.
    for (cx, cz) in ((19, -2), (19, 8)):
        cylinder(b, cx, cz, 1, 7, 2.6, S("rusted_panel"), cap=None)
        disc(b, cx, cz, 8, 2.6, S("black_concrete"))
        disc(b, cx, cz, 3, 2.7, S("black_concrete"), hollow=True)
        for y in range(1, 9):
            b.set(cx - 3, y, cz, S("ladder", facing="west"))
        b.set(cx, 9, cz, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    for x in range(-4, 17):
        b.set(x, 1, 5, S("pipe", east="true", west="true", north="false", south="false", up="false", down="false"))
    for z in range(-2, 9):
        b.set(16, 1, z, S("pipe", north="true", south="true", east="false", west="false", up="false", down="false"))
    b.set(10, 1, 5, S("leaking_pipe"))
    # Operator's shack.
    b.room(-8, 0, 8, -2, 4, 14, S("rusted_panel"), S("spruce_planks"))
    door(b, -2, 1, 11)
    b.set(-7, 1, 13, S("bunk_bed", facing="east"))
    b.set(-4, 1, 13, S("office_desk", facing="north"))
    b.set(-4, 2, 13, S("radio_set", facing="north"))
    b.set(-4, 1, 12, S("office_chair", facing="north"))
    b.chest(-7, 1, 9, "bunker", "east")
    b.set(-5, 3, 11, S("lantern", hanging="true", waterlogged="false"))
    b.set(-8, 2, 11, pane())
    # Spilled oil and drums.
    for _ in range(26):
        x, z = b.rnd.randint(-6, 14), b.rnd.randint(4, 14)
        if b.get(x, 1, z) in (None, AIR):
            b.set(x, 1, z, S("tar_puddle"))
    for (x, z) in ((2, 8), (3, 8), (2, 9), (12, 9)):
        b.set(x, 1, z, S("toxic_barrel"))
    skeleton(b, 1, 1, 7, "east", "skeleton_lying")
    b.set(-5, 1, 4, S("warning_sign", facing="south"))
    b.decay(0.015, cracked=CONCRETE)
    return b


# ---------------------------------------------------------------- solar farm
def solar_farm():
    """A utility solar plant: tilted panel rows on truss stands (some smashed), inverter cabinets between the rows,
    the fenced substation with transformers and insulators, the control building, a toppled pylon carrying the line
    away, and a stranded cleaning robot."""
    b = Build("solar_farm", 1008)
    ground(b, -4, -4, 52, 40, lambda x, z: S("gravel") if b.rnd.random() < 0.25 else dead_ground(b))
    for row in range(0, 7):
        z = 2 + row * 5
        for x in range(0, 40):
            if x % 3 == 0:
                b.set(x, 1, z + 1, S("truss"))
            broken = b.rnd.random() < 0.12
            b.set(x, 2, z, S("glass_pane", north="false", south="false", east="true", west="true",
                                waterlogged="false") if broken else S("solar_panel"))
            b.set(x, 2, z + 1, S("solar_panel") if not broken else AIR)
            if broken and b.rnd.random() < 0.5:
                b.set(x, 1, z + 2, S("rubble"))
        b.set(40, 1, z + 1, S("control_cabinet", facing="east"))
        b.set(41, 1, z + 1, S("cable", east="true", west="true", north="false", south="false", up="false",
                              down="false"))
    for z in range(3, 34):
        b.set(42, 0, z, S("cable", north="true", south="true", east="false", west="false", up="false", down="false"))
    # Substation: fence, two transformers with insulators.
    perimeter(b, 44, 2, 52, 14, 1, bars(), gate={(44, 8)}, wire=S("barbed_wire", facing="west"))
    for z in (5, 10):
        for x in (47, 48, 49):
            for y in (1, 2, 3):
                b.set(x, y, z, S("gray_concrete") if y < 3 else S("iron_block"))
            b.set(x, 4, z, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
        b.set(46, 1, z, S("station_radiator"))
    b.set(45, 2, 13, S("warning_sign", facing="east"))
    # Toppled pylon outside the fence.
    for k in range(0, 18):
        x = 54 + k
        y = 1 + (k // 6)
        b.set(x, 1 if k > 2 else y, 7, bars(east=True, west=True))
        b.set(x, 1, 6 if k % 4 == 0 else 8, bars(north=True, south=True))
    # Control building.
    b.room(44, 0, 20, 52, 5, 30, S("white_concrete"), S("floor_tiles"))
    windows_band(b, 44, 20, 52, 30, 3, 3, pane())
    door(b, 44, 1, 25)
    for z in range(21, 30, 2):
        b.set(51, 1, z, S("server_rack", facing="west"))
    b.set(47, 1, 21, S("office_desk", facing="south"))
    b.set(48, 1, 21, S("office_desk", facing="south"))
    b.set(47, 2, 21, S("data_terminal", facing="south", log=6))
    b.set(47, 1, 22, S("office_chair", facing="north"))
    b.set(49, 1, 29, S("battery_bank", facing="north"))
    b.set(46, 1, 29, S("battery_bank", facing="north"))
    b.chest(45, 1, 29, "lab", "south")
    skeleton(b, 48, 1, 23, "north", "skeleton_sitting")
    lights(b, 46, 22, 50, 28, 4, 3)
    b.set(48, 6, 25, S("antenna_dish"))
    # The cleaning robot, run aground between two rows.
    b.set(20, 1, 15, S("mining_drone"))
    b.set(21, 1, 15, S("rover_wheel", facing="east"))
    b.decay(0.02, cracked=CONCRETE)
    return b


BUILDERS = {
    "bunker_small": bunker_small,
    "bunker_large": bunker_large,
    "farmhouse": farmhouse,
    "radio_tower": radio_tower,
    "barracks": barracks,
    "oil_pumpjack": oil_pumpjack,
    "solar_farm": solar_farm,
}
