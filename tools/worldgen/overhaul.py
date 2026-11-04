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
    b.set(15, 1, 17, S("data_terminal", facing="north", log=10))
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
    b.chest(23, 1, 15, "radio", "west")
    b.chest(10, 1, 8, "radio", "south", "barrel")
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
    b.chest(-7, 1, 9, "industrial", "east")
    b.chest(13, 1, -1, "industrial", "north", "barrel")
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
    b.chest(45, 1, 29, "industrial", "south")
    b.chest(50, 1, 21, "industrial", "west", "barrel")
    skeleton(b, 48, 1, 23, "north", "skeleton_sitting")
    lights(b, 46, 22, 50, 28, 4, 3)
    b.set(48, 6, 25, S("antenna_dish"))
    # The cleaning robot, run aground between two rows.
    b.set(20, 1, 15, S("mining_drone"))
    b.set(21, 1, 15, S("rover_wheel", facing="east"))
    b.decay(0.02, cracked=CONCRETE)
    return b



# ---------------------------------------------------------------- space centre
def rocket_standing(b, cx, cz, height, r=1.6, stripe="black_concrete"):
    """A standing rocket on its fins: white hull with stripes, an interstage ring, the nose cone."""
    for y in range(1, height + 1):
        rr = r if y < height - 3 else r * (height - y + 1) / 4.0
        disc(b, cx, cz, y, rr, S(stripe) if y % 7 == 3 else S("station_hull") if y != height // 2 else S("station_foil"))
    for (dx, dz) in ((2, 0), (-2, 0), (0, 2), (0, -2)):
        for y in (1, 2, 3):
            b.set(cx + dx * (1 if y > 1 else 1), y, cz + dz, S("gray_concrete") if y < 3 else S("station_hull"))
    b.set(cx, height + 1, cz, S("lightning_rod", facing="up", powered="false", waterlogged="false"))


def space_center():
    """The national launch complex, where the last evacuation flights lifted off: the pad with its flame trench and
    the mobile service tower, the rocket that never left lying broken across it, lightning masts, the water tower
    for the sound suppression deluge, propellant spheres, the domed blockhouse with its tiered firing room, a
    grandstand where families watched, and a rocket garden of old boosters."""
    b = Build("space_center", 1003)
    ground(b, -6, -6, 70, 56, lambda x, z: concrete(b, 0.08) if x < 46 and z < 46 else dead_ground(b))
    # Pad: a raised plate, the flame trench crossing it (deep, scorched), the deflector.
    for x in range(4, 42):
        for z in range(4, 42):
            b.set(x, 0, z, S("light_gray_concrete") if (x + z) % 9 else S("hazard_concrete"))
    for x in range(18, 28):
        for z in range(2, 44):
            for y in range(-4, 1):
                b.set(x, y, z, AIR)
            b.set(x, -4, z, S("polished_blackstone") if b.rnd.random() < 0.7 else S("blackstone"))
        for z in (2, 43):
            for y in range(-4, 1):
                b.set(x, y, z, S("gray_concrete"))
    for x in range(17, 29):
        for z in range(2, 44):
            if x in (17, 28):
                for y in range(-4, 1):
                    b.set(x, y, z, S("sooty_concrete"))
    for z in range(18, 28):
        b.set(22, -3 + (z - 18) // 4, z, S("iron_block"))
        b.set(23, -3 + (z - 18) // 4, z, S("iron_block"))
    # Launch mount: four hold-down posts round the trench.
    for (x, z) in ((19, 19), (26, 19), (19, 26), (26, 26)):
        for y in (1, 2):
            b.set(x, y, z, S("iron_block"))
    # Mobile service tower: lattice, swing arms reaching to where the rocket stood.
    for y in range(1, 46):
        for (x, z) in ((8, 18), (13, 18), (8, 27), (13, 27)):
            b.set(x, y, z, S("truss") if y % 5 else S("iron_block"))
        if y % 5 == 0:
            for x in range(8, 14):
                for z in range(18, 28):
                    if x in (8, 13) or z in (18, 27):
                        b.set(x, y, z, S("metal_grating"))
            if y in (20, 30, 40):
                for x in range(14, 21):
                    b.set(x, y, 22, S("metal_grating"))
                    b.set(x, y, 23, S("metal_grating"))
                    b.set(x, y + 1, 21, bars(east=True, west=True))
                    b.set(x, y + 1, 24, bars(east=True, west=True))
    for y in range(1, 46):
        b.set(10, y, 22, S("ladder", facing="south"))
    for x in range(7, 15):
        for z in range(17, 29):
            b.set(x, 46, z, S("truss"))
    b.set(10, 47, 22, S("antenna_dish"))
    b.set(12, 47, 20, S("station_light"))
    # The rocket: broken in three, lying across the pad from the mount to the north-east.
    def ring(cx, cy, z, r, st):
        for dx in range(-r, r + 1):
            for dy in range(-r, r + 1):
                if dx * dx + dy * dy <= r * r + 1:
                    b.set(cx + dx, cy + dy, z, st)
    for i in range(0, 34):
        z = 8 + i
        if 14 <= i <= 15 or 26 <= i <= 27:
            continue   # the breaks
        x = 34 + (i // 14)
        st = S("station_foil") if i % 9 == 4 else S("black_concrete") if i % 9 == 0 else S("station_hull")
        r = 3 if i < 26 else max(1, 3 - (i - 26) // 3)
        ring(x, 4 if i < 26 else 3, z, r, st)
        if 0 < i < 13 and r == 3:
            for dx in range(-2, 3):
                for dy in range(-2, 3):
                    if dx * dx + dy * dy < 5:
                        b.set(x + dx, 4 + dy, z, AIR)
    for (dx, dy) in ((-1, -1), (1, -1), (-1, 1), (1, 1), (0, 0)):
        b.set(34 + dx, 4 + dy, 7, S("lander_engine"))
    for (dx, dy) in ((-4, 0), (4, 0), (0, 4)):
        for z in range(7, 11):
            b.set(34 + dx, 4 + dy, z, S("station_hull"))
    for z in range(9, 20, 2):
        b.set(30, 1, z, S("scrap_pile"))
    skeleton(b, 31, 1, 15, "west", "skeleton_suited")
    # Lightning masts at the pad corners.
    for (x, z) in ((2, 2), (43, 2), (2, 43), (43, 43)):
        for y in range(1, 36):
            b.set(x, y, z, S("iron_block") if y % 6 == 0 else bars())
        b.set(x, 36, z, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
        b.set(x, 35, z, S("ship_light_red"))
    # Water tower for the deluge: a sphere on legs.
    for y in range(1, 18):
        for (dx, dz) in ((-2, -2), (2, -2), (-2, 2), (2, 2)):
            b.set(52 + dx, y, 8 + dz, bars() if y % 4 else S("iron_block"))
    for x in range(-4, 5):
        for y in range(-4, 5):
            for z in range(-4, 5):
                d = math.sqrt(x * x + y * y + z * z)
                if 3.4 <= d <= 4.4:
                    b.set(52 + x, 22 + y, 8 + z, WHITE if y > -2 else LGREY)
    for x in range(28, 50):
        b.set(x, 1, 8, S("pipe", east="true", west="true", north="false", south="false", up="false", down="false"))
    # Propellant spheres (LOX and kerosene) behind a berm.
    for (cx, cz, st) in ((56, 26, WHITE), (56, 36, S("light_gray_concrete"))):
        for x in range(-4, 5):
            for y in range(-4, 5):
                for z in range(-4, 5):
                    d = math.sqrt(x * x + y * y + z * z)
                    if 3.4 <= d <= 4.4 and y > -4:
                        b.set(cx + x, 6 + y, cz + z, st)
        for y in range(1, 3):
            for (dx, dz) in ((-3, 0), (3, 0), (0, -3), (0, 3)):
                b.set(cx + dx, y, cz + dz, S("iron_block"))
    for z in range(20, 42):
        b.set(49, 1, z, S("coarse_dirt"))
        b.set(49, 2, z, S("coarse_dirt") if z % 3 else S("dead_bush"))
    # Blockhouse: a low concrete dome west of the pad, the tiered firing room under it.
    cx, cz = -18, 24
    for x in range(-11, 12):
        for z in range(-11, 12):
            for y in range(0, 10):
                d = math.sqrt(x * x + (y * 1.25) ** 2 + z * z)
                if d <= 11:
                    b.set(cx + x, y, cz + z, S("sooty_concrete") if d > 9.6 else AIR)
            if math.hypot(x, z) <= 11:
                b.set(cx + x, 0, cz + z, S("polished_andesite"))
    for k in range(0, 3):
        for x in range(-6 + k, 7 - k):
            b.set(cx + x, 1 + k, cz + 4 + k * 2, S("smooth_stone"))
            b.set(cx + x, 1 + k, cz + 5 + k * 2, S("smooth_stone"))
    logs = (9, 6, 4, 10, 9, 6)
    for i, x in enumerate(range(-5, 7, 2)):
        b.set(cx + x, 1, cz + 3, S("data_terminal", facing="north", log=logs[i]))
        b.set(cx + x + 1, 1, cz + 3, S("navigation_console", facing="north"))
        b.set(cx + x, 2, cz + 5, S("diagnostic_console", facing="north"))
        b.set(cx + x, 2, cz + 6, S("office_chair", facing="north"))
        b.set(cx + x, 3, cz + 8, S("office_chair", facing="north"))
    for x in range(-6, 7):
        b.set(cx + x, 3, cz - 6, S("tinted_glass"))
        b.set(cx + x, 4, cz - 6, S("tinted_glass")) if abs(x) < 5 else None
    b.set(cx, 1, cz - 2, S("office_desk", facing="north"))
    skeleton(b, cx, 1, cz - 1, "north", "skeleton_sitting")
    skeleton(b, cx - 3, 2, cz + 6, "north", "skeleton_sitting")
    b.chest(cx - 8, 1, cz, "space_center", "east")
    b.chest(cx + 8, 1, cz, "space_center", "west")
    b.chest(cx - 7, 1, cz + 4, "lab", "east", "barrel")
    for x in (-7, -6, 6, 7):
        b.set(cx + x, 1, cz - 3, S("locker", facing="south"))
    skeleton(b, cx + 6, 1, cz - 2, "south", "skeleton_suited")
    b.set(cx, 7, cz, S("ceiling_light"))
    b.set(cx - 4, 6, cz + 4, S("ceiling_light"))
    b.set(cx + 4, 6, cz + 4, S("ceiling_light"))
    for z in range(cz - 2, cz + 1):
        for y in (1, 2):
            b.set(cx + 10, y, z, AIR)
            b.set(cx + 11, y, z, AIR)
    iron_door(b, cx + 10, 1, cz - 1, "east")
    # Periscope tubes on the dome.
    for (dx, dz) in ((-3, -3), (3, -3)):
        for y in range(8, 11):
            b.set(cx + dx, y, cz + dz, S("iron_block") if y == 10 else bars())
    # Grandstand: tiers of seats facing the pad, the families who came to watch.
    for k in range(0, 6):
        for z in range(48, 62):
            b.set(-4 - k, k, z, S("smooth_stone"))
            b.set(-4 - k, k + 1, z, S("blue_concrete") if z % 2 else AIR)
            b.set(-4 - k, k + 1, z, S("spruce_stairs", facing="west", half="bottom", shape="straight",
                                     waterlogged="false"))
    for z in range(48, 62):
        for y in range(1, 10):
            b.set(-10, y, z, S("gray_concrete") if y < 7 else AIR)
        b.set(-10, 9, z, S("smooth_stone_slab", type="bottom", waterlogged="false"))
        b.set(-9, 8, z, S("smooth_stone_slab", type="top", waterlogged="false"))
    for (x, y, z) in ((-5, 2, 50), (-6, 3, 53), (-6, 3, 54), (-8, 5, 58), (-4, 2, 60)):
        skeleton(b, x, y, z, "east", "skeleton_sitting")
    b.set(-5, 2, 55, S("dog_skeleton", facing="east"))
    # Rocket garden: old boosters standing on their fins.
    rocket_standing(b, 62, 48, 18, 1.2)
    rocket_standing(b, 66, 52, 24, 1.6, "red_concrete")
    rocket_standing(b, 60, 54, 14, 1.0)
    # The crawlerway: two gravel lanes leaving the pad to the south.
    for z in range(46, 57):
        for x in list(range(16, 21)) + list(range(25, 30)):
            b.set(x, 0, z, S("gravel"))
    b.decay(0.02, keep=("minecraft:ladder", "intersolarsystem:truss"), cracked=CONCRETE)
    return b


# ---------------------------------------------------------------- research lab
def research_lab():
    """A biomedical research institute: glass lobby with reception and the quarantine checkpoint, the BSL-3 suite
    behind an airlock (fume hoods, specimen tanks, cryo pods, the sequencer racks), offices and a break room
    upstairs, rooftop plant. The vaccine team stayed to the end; their last notes are on the terminal."""
    b = Build("research_lab", 1004)
    W, F = S("white_concrete"), S("floor_tiles")
    ground(b, -4, -8, 34, 24, lambda x, z: concrete(b, 0.1) if z < 0 else dead_ground(b))
    # Shell: two floors of 5, a curtain-wall front.
    for y in range(0, 11):
        for x in range(0, 31):
            for z in range(0, 21):
                edge = x in (0, 30) or z in (0, 20)
                if y in (0, 5, 10):
                    b.set(x, y, z, F if y < 10 and not edge else W)
                elif edge:
                    front = z == 0 and 0 < x < 30
                    b.set(x, y, z, S("curtain_wall") if front and y % 5 != 0 else
                          (pane() if (x + z) % 4 == 2 and y % 5 in (2, 3) else W))
                else:
                    b.set(x, y, z, AIR)
    for x in range(12, 18):
        door(b, x, 1, 0, height=3)
    # Lobby: reception desk, waiting chairs, the checkpoint barrier.
    for x in range(10, 20):
        b.set(x, 1, 6, S("office_desk", facing="south") if x % 3 else S("quartz_block"))
    b.set(14, 1, 7, S("office_chair", facing="north"))
    b.set(16, 1, 7, S("data_terminal", facing="south", log=6))
    for x in range(2, 9):
        b.set(x, 1, 2, S("airport_seats", facing="south"))
    for x in range(21, 29):
        b.set(x, 1, 2, S("airport_seats", facing="south"))
    b.set(2, 1, 5, S("potted_dead_bush"))
    b.set(28, 1, 5, S("potted_dead_bush"))
    for x in range(10, 21):
        b.set(x, 1, 9, S("quarantine_barrier", facing="south") if x != 15 else AIR)
    b.set(9, 2, 9, S("poster_quarantine", facing="south"))
    b.set(21, 2, 9, S("warning_sign", facing="south"))
    skeleton(b, 15, 1, 4, "north", "skeleton_lying")
    partition_z(b, 10, 1, 29, 1, 4, W)
    iron_door(b, 15, 1, 10, "south", open_=True)
    # Airlock and the BSL-3 suite (z 11..19).
    partition_x(b, 13, 11, 13, 1, 4, W)
    partition_x(b, 17, 11, 13, 1, 4, W)
    partition_z(b, 13, 1, 29, 1, 4, S("white_stained_glass"))
    b.set(15, 1, 13, AIR)
    b.set(15, 2, 13, AIR)
    b.set(14, 1, 11, S("locker", facing="east"))
    b.set(16, 1, 11, S("locker", facing="west"))
    b.set(14, 3, 12, S("leaking_pipe"))
    for x in range(2, 12, 3):
        b.set(x, 1, 19, S("fume_hood", facing="north"))
        b.set(x + 1, 1, 19, S("lab_bench", facing="north"))
        b.set(x, 1, 16, S("lab_bench", facing="south"))
        b.set(x + 1, 1, 16, S("lab_bench", facing="south"))
        b.set(x, 2, 16, S("brewing_stand", has_bottle_0="true", has_bottle_1="false", has_bottle_2="true"))
    for x in range(19, 29, 2):
        b.set(x, 1, 19, S("specimen_tank"))
        b.set(x, 1, 14, S("cryo_pod", facing="south"))
    for z in (16, 17):
        b.set(29, 1, z, S("server_rack", facing="west"))
        b.set(29, 2, z, S("server_rack", facing="west"))
    b.set(24, 1, 17, S("office_chair", facing="east"))
    skeleton(b, 7, 1, 17, "south", "skeleton_curled")
    skeleton(b, 25, 1, 16, "south", "skeleton_lying")
    b.chest(1, 1, 12, "lab", "east")
    b.chest(1, 1, 14, "medical", "east", "barrel")
    b.set(18, 1, 12, S("toxic_barrel"))
    lights(b, 3, 3, 27, 18, 4, 4)
    # Stairs to the upper floor along the west wall.
    for k in range(0, 5):
        b.set(1, 1 + k, 4 + k, S("quartz_stairs", facing="south", half="bottom", shape="straight",
                                 waterlogged="false"))
        b.set(1, 5, 4 + k, AIR)
    # Upper floor: offices (glass partitions), break room, the director's room.
    partition_z(b, 10, 2, 29, 6, 9, S("white_stained_glass"))
    for x in (8, 16, 23):
        partition_x(b, x, 1, 9, 6, 9, S("white_stained_glass"))
        b.set(x, 6, 9, AIR)
        b.set(x, 7, 9, AIR)
    for x0 in (3, 10, 18, 25):
        b.set(x0, 6, 3, S("office_desk", facing="south"))
        b.set(x0 + 1, 6, 3, S("office_desk", facing="south"))
        b.set(x0, 6, 4, S("office_chair", facing="north"))
        b.set(x0 + 2, 6, 1, S("filing_cabinet", facing="south"))
        b.set(x0 + 1, 7, 3, S("data_terminal", facing="south", log=6)) if x0 == 25 else None
    for x in range(2, 12):
        b.set(x, 6, 19, S("smooth_stone"))
    b.set(3, 7, 19, S("cauldron"))
    b.set(5, 6, 18, S("vending_machine", facing="north"))
    long_table(b, 4, 9, 6, 15)
    for x in range(14, 29):
        b.set(x, 6, 19, S("bookshelf") if x % 3 else S("filing_cabinet", facing="north"))
    b.set(20, 6, 15, S("office_desk", facing="north"))
    b.set(21, 6, 15, S("office_desk", facing="north"))
    b.set(20, 6, 16, S("office_chair", facing="south"))
    skeleton(b, 21, 6, 16, "south", "skeleton_sitting")
    b.chest(28, 6, 14, "lab", "west")
    lights(b, 3, 3, 27, 18, 9, 4)
    # Roof plant: air handlers, the exhaust stacks of the BSL suite, an antenna.
    for (x0, z0) in ((4, 12), (20, 12)):
        for x in range(x0, x0 + 5):
            for z in range(z0, z0 + 4):
                b.set(x, 11, z, S("iron_block") if (x + z) % 2 else S("light_gray_concrete"))
                b.set(x, 12, z, S("iron_trapdoor", facing="north", half="bottom", open="false", powered="false",
                                  waterlogged="false"))
    for (x, z) in ((12, 16), (17, 16)):
        for y in range(11, 16):
            b.set(x, y, z, S("pipe", up="true", down="true", north="false", south="false", east="false",
                             west="false"))
    for y in range(11, 18):
        b.set(27, y, 4, bars())
    b.set(27, 18, 4, S("antenna_dish"))
    collapse(b, 0, 0, 30, 20, 6, 12, 0.08)
    b.decay(0.02, cracked={"minecraft:white_concrete": "calcite"})
    return b


# ---------------------------------------------------------------- field station
def research_station():
    """A field research station on stilts: living module (bunks, galley, mess table), laboratory (benches,
    microscopes, sample freezers), power module, a garage with the snow rover, linked by covered walkways; the
    weather mast, the comms dish, fuel bladders and a ring of flag markers in the snow."""
    b = Build("research_station", 1005)
    ground(b, -6, -6, 46, 30, lambda x, z: S("snow_block") if b.rnd.random() < 0.6 else S("packed_ice")
           if b.rnd.random() < 0.2 else S("gravel"))
    mods = {"living": (0, 0, 12, 7), "lab": (18, 0, 30, 7), "power": (18, 14, 26, 20), "garage": (0, 14, 12, 22)}
    for name, (x0, z0, x1, z1) in mods.items():
        stilt = name != "garage"
        y0 = 3 if stilt else 0
        if stilt:
            for x in (x0, (x0 + x1) // 2, x1):
                for z in (z0, z1):
                    for y in range(0, y0):
                        b.set(x, y, z, S("truss"))
        b.room(x0, y0, z0, x1, y0 + 5, z1, S("station_hull"), S("polished_andesite"))
        for x in range(x0 + 2, x1 - 1, 3):
            b.set(x, y0 + 3, z0, S("station_window"))
            b.set(x, y0 + 3, z1, S("station_window"))
        for x in range(x0, x1 + 1):
            b.set(x, y0 + 5, z0, S("red_concrete"))
            b.set(x, y0 + 5, z1, S("red_concrete"))
        lights(b, x0 + 2, z0 + 2, x1 - 2, z1 - 2, y0 + 4, 4, S("station_light"))
    # Walkways between living and lab (covered), lab and power.
    for x in range(12, 19):
        for z in (3, 4):
            b.set(x, 3, z, S("metal_grating"))
            b.set(x, 7, z, S("station_hull"))
            for y in (4, 5, 6):
                b.set(x, y, z, AIR)
        for y in range(4, 7):
            b.set(x, y, 2, S("station_window") if y == 5 else S("station_hull"))
            b.set(x, y, 5, S("station_window") if y == 5 else S("station_hull"))
    for z in range(7, 15):
        for x in (22, 23):
            b.set(x, 3, z, S("metal_grating"))
            b.set(x, 7, z, S("station_hull"))
            for y in (4, 5, 6):
                b.set(x, y, z, AIR)
        for y in range(4, 7):
            b.set(21, y, z, S("station_window") if y == 5 else S("station_hull"))
            b.set(24, y, z, S("station_window") if y == 5 else S("station_hull"))
    # Steps up to the living module.
    for k in range(0, 4):
        b.set(6, k, -1 - (3 - k), S("metal_grating"))
    door(b, 6, 4, 0)
    # Living: bunks, lockers, galley, mess table, a guitar (note block) and the expedition photo wall.
    for x in (1, 3, 5):
        b.set(x, 4, 6, S("bunk_bed", facing="north"))
        b.set(x, 4, 1, S("locker", facing="south")) if x != 5 else None
    for x in range(8, 12):
        b.set(x, 4, 6, S("smoker", facing="north", lit="false") if x == 8 else S("cauldron") if x == 9 else
              S("smooth_stone"))
    long_table(b, 8, 10, 4, 3)
    b.set(2, 4, 3, S("note_block", instrument="guitar", note=5, powered="false"))
    b.chest(11, 4, 1, "research_station", "south", "barrel")
    skeleton(b, 3, 5, 6, "north", "skeleton_curled")
    # Lab: benches, freezers, the core sample rack.
    for x in range(19, 30, 2):
        b.set(x, 4, 6, S("lab_bench", facing="north"))
        b.set(x + 1, 4, 6, S("specimen_tank")) if x < 28 else None
    for x in range(19, 25):
        b.set(x, 4, 1, S("lab_bench", facing="south"))
    b.set(20, 5, 1, S("brewing_stand", has_bottle_0="false", has_bottle_1="true", has_bottle_2="false"))
    b.set(29, 4, 3, S("data_terminal", facing="west", log=3))
    b.set(28, 4, 3, S("office_chair", facing="east"))
    b.chest(29, 4, 1, "lab", "west")
    for y in (4, 5):
        for x in (26, 27):
            b.set(x, y, 1, S("end_rod", facing="up"))
    skeleton(b, 28, 4, 4, "east", "skeleton_sitting")
    # Power module: generators, battery bank, fuel day tank.
    b.set(19, 4, 19, S("diesel_generator", facing="north"))
    b.set(21, 4, 19, S("diesel_generator", facing="north"))
    b.set(25, 4, 19, S("battery_bank", facing="north"))
    b.set(25, 4, 15, S("control_cabinet", facing="west"))
    b.set(19, 4, 15, S("toxic_barrel"))
    # Garage: the snow rover (tracks, cabin), tools, a sledge.
    for x in range(2, 9):
        for z in (16, 20):
            b.set(x, 1, z, S("black_concrete"))
        for z in range(16, 21):
            b.set(x, 2, z, S("orange_concrete"))
            if x > 4:
                b.set(x, 3, z, S("orange_concrete") if z in (16, 20) or x == 8 else AIR)
                b.set(x, 4, z, S("orange_concrete"))
    for z in (17, 18, 19):
        b.set(8, 3, z, pane())
    b.set(6, 3, 18, S("spruce_stairs", facing="west", half="bottom", shape="straight", waterlogged="false"))
    b.set(11, 1, 21, S("repair_station", facing="north"))
    b.set(10, 1, 21, S("anvil", facing="east"))
    for x in range(1, 5):
        b.set(x, 1, 21, S("spruce_slab", type="bottom", waterlogged="false"))
    for z in range(16, 21):
        b.set(12, 1, z, AIR)
        b.set(12, 2, z, AIR)
        b.set(12, 3, z, AIR)
    # Weather mast and comms dish, fuel bladders, flags.
    for y in range(0, 16):
        b.set(36, y, 4, bars() if y % 4 else S("iron_block"))
    b.set(36, 16, 4, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    for (dx, dz, f) in ((1, 0, "east"), (-1, 0, "west"), (0, 1, "south")):
        b.set(36 + dx, 14, 4 + dz, S("iron_trapdoor", facing=f, half="top", open="true", powered="false",
                                     waterlogged="false"))
    dish(b, 36, 1, 14, 3)
    for (x, z) in ((32, 22), (35, 22), (38, 22)):
        for dx in (0, 1):
            b.set(x + dx, 1, z, S("black_wool"))
    for k in range(0, 12):
        a = k * math.pi / 6
        x, z = int(15 + 26 * math.cos(a)), int(12 + 18 * math.sin(a))
        b.set(x, 1, z, S("spruce_fence", north="false", south="false", east="false", west="false",
                         waterlogged="false"))
        b.set(x, 2, z, S("red_wool") if k % 2 else S("orange_wool"))
    b.decay(0.02, keep=("minecraft:ladder", "intersolarsystem:truss"))
    return b


# ---------------------------------------------------------------- seed vault
def seed_bank():
    """The global seed vault: the wedge-shaped portal with its light-art panel, built into an earth-and-ice mound;
    a long tunnel with cable trays and lights; the service hall (control room, transformer room); three vault halls
    behind blast doors: one sealed, one opened and half emptied, one collapsed and iced over."""
    b = Build("seed_bank", 1006)
    G = S("gray_concrete")
    ground(b, -12, -8, 40, 70, lambda x, z: S("snow_block") if b.rnd.random() < 0.55 else S("packed_ice")
           if b.rnd.random() < 0.3 else S("gravel"))
    # The mound: an elongated hill over tunnel and halls.
    for x in range(-12, 41):
        for z in range(6, 70):
            h = 14 - ((x - 14) / 20.0) ** 2 * 10 - max(0, (12 - z)) * 1.4 - max(0, z - 62) * 1.6
            for y in range(1, int(h) + 1):
                b.set(x, y, z, S("snow_block") if y == int(h) else S("packed_ice") if b.rnd.random() < 0.15 else
                      S("dirt"))
    # Portal wedge.
    for i in range(0, 10):
        for x in range(10 - i // 3, 19 + i // 3):
            for y in range(0, i + 1):
                b.set(x, y, i, G if (x, y) not in () else G)
    for x in range(12, 17):
        for y in range(1, 6):
            for z in range(0, 10):
                b.set(x, y, z, AIR)
    for x in range(11, 18):
        for y in range(7, 10):
            b.set(x, y, 7, S("light_blue_stained_glass") if (x + y) % 2 else S("cyan_stained_glass"))
    iron_door(b, 14, 1, 2, "north", open_=True)
    door(b, 13, 1, 2)
    # Tunnel z 10..44.
    for z in range(10, 45):
        for x in range(11, 18):
            for y in range(0, 7):
                edge = x in (11, 17) or y in (0, 6)
                b.set(x, y, z, (S("polished_andesite") if y == 0 else G) if edge else AIR)
        if z % 6 == 0:
            b.set(14, 5, z, S("ceiling_light"))
        b.set(12, 4, z, S("cable", north="true", south="true", east="false", west="false", up="false",
                          down="false"))
        b.set(16, 4, z, S("pipe", north="true", south="true", east="false", west="false", up="false",
                          down="false"))
    for z in (20, 32):
        b.set(13, 1, z, S("shipping_crate"))
    b.set(16, 1, 26, S("leaking_pipe"))
    b.set(15, 0, 27, S("packed_ice"))
    skeleton(b, 13, 1, 38, "south", "skeleton_suited")
    # Service hall z 44..52 across x 2..26.
    b.room(2, 0, 44, 26, 7, 52, G, S("polished_andesite"))
    for x in range(12, 17):
        for y in range(1, 6):
            b.set(x, y, 44, AIR)
    partition_x(b, 9, 45, 51, 1, 6, G)
    partition_x(b, 19, 45, 51, 1, 6, G)
    door(b, 9, 1, 48)
    door(b, 19, 1, 48)
    for z in range(45, 52, 2):
        b.set(3, 1, z, S("server_rack", facing="east"))
    b.set(6, 1, 51, S("office_desk", facing="north"))
    b.set(7, 1, 51, S("data_terminal", facing="north", log=11))
    b.set(6, 1, 50, S("office_chair", facing="north"))
    b.set(4, 1, 45, S("filing_cabinet", facing="south"))
    for (x, z) in ((22, 46), (24, 46), (22, 50), (24, 50)):
        for y in (1, 2):
            b.set(x, y, z, S("iron_block") if y == 1 else S("station_radiator"))
    b.set(25, 1, 48, S("control_cabinet", facing="west"))
    lights(b, 5, 47, 24, 50, 6, 5)
    # Vault halls z 52..68: west sealed, middle opened, east collapsed.
    for (x0, state) in ((2, "sealed"), (11, "open"), (20, "collapsed")):
        b.room(x0, 0, 52, x0 + 7, 6, 68, G, S("polished_andesite"))
        b.set(x0 + 3, 1, 52, S("blast_door", facing="north") if state == "sealed" else AIR)
        b.set(x0 + 3, 2, 52, G if state == "sealed" else AIR)
        if state != "sealed":
            b.set(x0 + 4, 1, 52, AIR)
            b.set(x0 + 4, 2, 52, AIR)
        for z in range(54, 68, 2):
            for x in (x0 + 1, x0 + 6):
                for y in range(1, 5):
                    if state == "open" and b.rnd.random() < 0.4:
                        continue
                    b.set(x, y, z, S("barrel", facing="up", open="false") if y % 2 else S("shipping_crate"))
        lights(b, x0 + 3, 55, x0 + 4, 66, 5, 5)
    for x in (12, 18):
        b.chest(x, 1, 67, "seed_bank", "north")
    b.chest(15, 1, 67, "seed_bank", "north", "barrel")
    b.chest(4, 1, 66, "seed_bank", "north")
    skeleton(b, 15, 1, 60, "north", "skeleton_curled")
    for x in range(21, 27):
        for z in range(56, 68):
            for y in range(1, 6):
                if b.rnd.random() < 0.35 + (y < 3) * 0.4:
                    b.set(x, y, z, S("packed_ice") if b.rnd.random() < 0.6 else S("rubble"))
    b.decay(0.015, cracked=CONCRETE, top_bias=False)
    return b


# ---------------------------------------------------------------- terraformer
def terraformer():
    """A terraforming test site: the atmosphere processor (a ringed stack with intake fans and the catalyst
    chambers), a cracked biodome over a dead garden, condenser tanks, mirror arrays, the control annex, pipes to the
    poisoned overflow pond."""
    b = Build("terraformer", 1007)
    ground(b, -22, -22, 30, 24, lambda x, z: dead_ground(b))
    # Processor: base hall, the stack.
    for x in range(-5, 6):
        for z in range(-5, 6):
            for y in range(0, 7):
                edge = abs(x) == 5 or abs(z) == 5
                b.set(x, y, z, (S("hazard_concrete") if y == 0 else S("station_hull")) if edge or y in (0, 6)
                      else AIR)
    for y in range(7, 40):
        r = 3.0 if y < 30 else 2.2
        disc(b, 0, 0, y, r, S("station_hull") if y % 6 else S("station_foil"), hollow=True)
        if y % 6 == 0:
            disc(b, 0, 0, y, r + 1.4, S("metal_grating"), hollow=True)
    for y in range(40, 44):
        disc(b, 0, 0, y, 2.2, S("sooty_concrete"), hollow=True)
    for (dx, dz, f) in ((4, 0, "east"), (-4, 0, "west"), (0, 4, "south"), (0, -4, "north")):
        for y in (14, 15, 26, 27):
            b.set(dx, y, dz, S("iron_trapdoor", facing=f, half="bottom", open="true", powered="false",
                               waterlogged="false"))
    for y in range(1, 40):
        b.set(0, y, -2, S("ladder", facing="south"))
    for (x, z) in ((-3, -3), (3, -3), (-3, 3), (3, 3)):
        b.set(x, 1, z, S("reactor") if (x + z) == 0 else S("coolant_tank", facing="north", level=0))
    b.set(0, 1, 3, S("control_cabinet", facing="south"))
    b.set(2, 1, -3, S("sparking_panel"))
    door(b, 0, 1, 5, height=3)
    b.set(0, 5, 0, S("station_light"))
    # Biodome to the east: cracked glass over a dead garden with paths and benches.
    R = 9
    for x in range(-R, R + 1):
        for z in range(-R, R + 1):
            d = math.hypot(x, z)
            if d <= R:
                b.set(16 + x, 0, z, S("coarse_dirt") if b.rnd.random() < 0.6 else S("podzol", snowy="false"))
            for y in range(1, R + 1):
                dd = math.sqrt(x * x + y * y + z * z)
                if R - 0.6 <= dd <= R + 0.4:
                    st = S("green_stained_glass") if (x + y + z) % 5 else S("iron_block")
                    b.set(16 + x, y, z, st if b.rnd.random() > 0.2 else AIR)
                elif dd < R - 0.6:
                    b.set(16 + x, y, z, AIR)
    for x in range(-R + 1, R):
        b.set(16 + x, 0, 0, S("gravel"))
        b.set(16, 0, x, S("gravel"))
    for (x, z) in ((11, -4), (20, 4), (13, 5), (21, -3), (18, -6)):
        for y in range(1, 5):
            b.set(x, y, z, S("dead_log", axis="y"))
        b.set(x, 5, z, S("dead_log", axis="x"))
    b.set(14, 1, 2, S("park_bench", facing="east"))
    b.set(18, 1, -2, S("park_bench", facing="west"))
    for x in range(6, 8):
        for y in range(1, 3):
            b.set(x, y, 0, AIR)
    for x in range(5, 8):
        b.set(x, 3, 0, S("station_hull"))
        b.set(x, 1, -1, S("station_hull"))
        b.set(x, 1, 1, S("station_hull"))
    # Condenser tanks and mirror arrays.
    for (cx, cz) in ((-12, -8), (-12, 0), (-12, 8)):
        cylinder(b, cx, cz, 1, 8, 2.2, S("iron_block"))
        disc(b, cx, cz, 9, 2.2, S("light_gray_concrete"))
        b.set(cx + 3, 4, cz, S("pipe", east="true", west="true", north="false", south="false", up="false",
                               down="false"))
    for x in range(-9, -5):
        b.set(x, 4, 0, S("pipe", east="true", west="true", north="false", south="false", up="false", down="false"))
    for row in range(0, 3):
        for x in range(-18, 2, 3):
            z = -16 - row * 2
            b.set(x, 1, z, S("truss"))
            b.set(x, 2, z, S("solar_panel") if b.rnd.random() > 0.2 else S("glass"))
            b.set(x + 1, 2, z, S("solar_panel") if b.rnd.random() > 0.2 else AIR)
    # Control annex.
    b.room(-8, 0, 12, 0, 5, 20, S("white_concrete"), S("floor_tiles"))
    door(b, -4, 1, 12)
    windows_band(b, -8, 12, 0, 20, 3, 2, pane())
    b.set(-1, 1, 16, S("data_terminal", facing="west", log=1))
    b.set(-1, 1, 18, S("navigation_console", facing="west"))
    b.set(-2, 1, 16, S("office_chair", facing="east"))
    b.chest(-7, 1, 19, "lab", "east")
    b.set(-7, 1, 13, S("lab_bench", facing="east"))
    b.set(-7, 1, 15, S("specimen_tank"))
    skeleton(b, -3, 1, 18, "east", "skeleton_lying")
    b.set(-4, 4, 16, S("ceiling_light"))
    # The overflow pond: pipes running to a pool of acid mud, barrels.
    for z in range(6, 16):
        b.set(8, 1, z, S("pipe", north="true", south="true", east="false", west="false", up="false", down="false"))
    for x in range(4, 14):
        for z in range(15, 23):
            if math.hypot(x - 9, z - 19) < 4.5:
                b.set(x, 0, z, S("acidic_mud"))
    b.set(8, 1, 15, S("leaking_pipe"))
    for (x, z) in ((13, 13), (14, 14), (3, 21)):
        b.set(x, 1, z, S("toxic_barrel"))
    b.set(5, 1, 13, S("warning_sign", facing="south"))
    b.decay(0.02, keep=("minecraft:ladder", "intersolarsystem:station_hull"))
    return b


# ---------------------------------------------------------------- dam
def dam():
    """A gravity dam across a dry canyon: the curved wall with its stepped downstream face, the crest road with
    railings and lamps, spillway gates, two intake towers on the dry reservoir side, the valve house with the
    control room, and the powerhouse at the foot (turbine hall, transformers). The reservoir is a cracked silt
    plain; a pleasure boat sits where the water was."""
    b = Build("dam", 1009)
    L = 48
    for z in range(0, L):
        bow = int(round(3 * math.sin(math.pi * z / (L - 1))))
        for y in range(0, 26):
            thick = 4 + (26 - y) // 2
            for x in range(0, thick):
                face = x == thick - 1
                st = (S("light_gray_concrete") if (y + z) % 6 else S("gray_concrete")) if not face else \
                    (S("light_gray_concrete") if y % 3 else S("smooth_stone"))
                b.set(x - bow, y, z, st)
        # Crest road with railings, the downstream parapet.
        for x in range(-bow - 1, 5 - bow):
            b.set(x, 26, z, S("asphalt") if 0 <= x + bow <= 3 else S("smooth_stone"))
        b.set(-bow - 1, 27, z, bars(north=True, south=True))
        b.set(4 - bow, 27, z, bars(north=True, south=True))
        if z % 8 == 4:
            b.set(4 - bow, 28, z, S("street_lamp", facing="west"))
    # Spillway gates: openings in the crest with steel gates hanging half-raised, a scoured chute below.
    for z in range(18, 30):
        for y in range(21, 26):
            for x in range(-4, 8):
                b.set(x, y, z, AIR)
        if z % 4 == 2:
            for y in range(21, 27):
                b.set(1, y, z, S("gray_concrete"))
        else:
            b.set(1, 24, z, S("iron_block"))
            b.set(1, 25, z, S("iron_block"))
    for z in range(18, 30):
        for y in range(0, 21):
            b.set(4 + (26 - y) // 2 - 1, y, z, S("sooty_concrete") if y % 2 else S("light_gray_concrete"))
    # Intake towers on the upstream side.
    for zc in (8, 40):
        for y in range(0, 30):
            disc(b, -7, zc, y, 2.2, S("gray_concrete"), hollow=y > 0)
        disc(b, -7, zc, 30, 2.6, S("smooth_stone"))
        b.set(-7, 31, zc, S("station_light"))
        for x in range(-5, -2):
            b.set(x, 26, zc, S("metal_grating"))
    # Valve house on the crest.
    b.room(-1, 26, 4, 6, 31, 12, S("light_gray_concrete"), S("polished_andesite"))
    windows_band(b, -1, 4, 6, 12, 29, 2, pane())
    door(b, 2, 27, 4)
    b.set(5, 27, 9, S("data_terminal", facing="west", log=2))
    b.set(5, 27, 7, S("control_cabinet", facing="west"))
    b.set(5, 27, 11, S("navigation_console", facing="west"))
    b.set(4, 27, 9, S("office_chair", facing="east"))
    b.chest(0, 27, 11, "industrial", "east")
    b.set(0, 27, 5, S("filing_cabinet", facing="east"))
    skeleton(b, 3, 27, 7, "east", "skeleton_sitting")
    b.set(2, 30, 8, S("ceiling_light"))
    # Powerhouse at the foot of the dam: turbine hall with three generator housings, transformers outside.
    b.room(17, 0, 6, 30, 9, 42, S("light_gray_concrete"), S("polished_andesite"))
    windows_band(b, 17, 6, 30, 42, 6, 3, pane())
    for zc in (14, 24, 34):
        cylinder(b, 23, zc, 1, 3, 3.0, S("iron_block"))
        disc(b, 23, zc, 4, 2.0, S("yellow_concrete"))
        b.set(23, 5, zc, S("iron_block"))
        b.set(27, 1, zc, S("control_cabinet", facing="west"))
    for z in range(7, 42):
        b.set(28, 8, z, S("iron_block") if z % 5 == 0 else AIR)
    for z in range(8, 41):
        b.set(19, 8, z, bars(north=True, south=True))
    door(b, 30, 1, 24, height=3)
    b.chest(29, 1, 8, "industrial", "west", "barrel")
    lights(b, 21, 10, 27, 38, 8, 6)
    for z in range(10, 40, 6):
        for x in (32, 33, 34):
            b.set(x, 1, z, S("gray_concrete"))
            b.set(x, 2, z, S("iron_block"))
        b.set(33, 3, z, S("lightning_rod", facing="up", powered="false", waterlogged="false"))
    # Penstocks from the dam into the powerhouse.
    for zc in (14, 24, 34):
        for x in range(10, 18):
            disc(b, x, zc, 0, 0, S("rusted_panel"))
            b.set(x, 2, zc, S("rusted_panel"))
            b.set(x, 3, zc, S("rusted_panel"))
    # Dry reservoir: cracked silt, a stranded pleasure boat, the old water line.
    for x in range(-24, -3):
        for z in range(-2, L + 2):
            b.set(x, 0, z, S("dried_silt") if b.rnd.random() < 0.7 else S("mud"))
    for z in range(0, L):
        b.set(-1, 12, z, S("sooty_concrete")) if z % 2 else None
    bx, bz = -18, 20
    for i in range(0, 9):
        w = 1 if i in (0, 8) else 2
        for dz in range(-w, w + 1):
            b.set(bx + i, 1, bz + dz, S("white_concrete"))
            if abs(dz) == w:
                b.set(bx + i, 2, bz + dz, S("white_concrete"))
    for i in range(3, 6):
        for dz in (-1, 0, 1):
            b.set(bx + i, 3, bz + dz, S("white_concrete") if i != 4 else pane())
    b.set(bx + 4, 2, bz, S("spruce_stairs", facing="east", half="bottom", shape="straight", waterlogged="false"))
    b.decay(0.02, cracked=CONCRETE)
    return b


# ---------------------------------------------------------------- crash sites
def crater(b, cx, cz, r, depth=2, glass=True):
    """An impact crater: the floor scooped out (fused and scorched), a raised rim of ejecta, scattered clods."""
    for x in range(int(cx - r - 3), int(cx + r + 4)):
        for z in range(int(cz - r - 3), int(cz + r + 4)):
            d = math.hypot(x - cx, z - cz)
            if d <= r:
                dd = int(round(depth * (1 - (d / r) ** 2)))
                for y in range(-dd + 1, 1):
                    b.set(x, y, z, AIR)
                r_ = b.rnd.random()
                b.set(x, -dd, z, S("trinitite") if glass and r_ < 0.25 else S("coal_block") if r_ < 0.4 else
                      S("blackstone") if r_ < 0.75 else S("basalt", axis="y"))
            elif d <= r + 2.2:
                b.set(x, 0, z, S("coarse_dirt") if b.rnd.random() < 0.6 else S("gravel"))
                if d <= r + 1.2 and b.rnd.random() < 0.6:
                    b.set(x, 1, z, S("coarse_dirt") if b.rnd.random() < 0.7 else S("cinder_rock"))


def furrow(b, x0, x1, z, half, depth=1):
    """The skid mark a crashing object ploughed before it stopped (along x)."""
    for x in range(min(x0, x1), max(x0, x1) + 1):
        for dz in range(-half, half + 1):
            for y in range(-depth + 1, 1):
                b.set(x, y, z + dz, AIR)
            b.set(x, -depth, z + dz, S("coarse_dirt") if b.rnd.random() < 0.6 else S("blackstone"))
        for dz in (-half - 1, half + 1):
            b.set(x, 1, z + dz, S("coarse_dirt")) if b.rnd.random() < 0.5 else None
        if b.rnd.random() < 0.25:
            b.set(x, -depth + 1, z + b.rnd.randint(-half, half), S("scrap_pile"))


def cargo_capsule():
    """A resupply capsule that came down hard: the scorched heat shield, the conical pressure hull on its side with
    the hatch blown open, cargo racks inside; the furrow it ploughed, heat-shield fragments, the parachute spread
    out behind with its rigging lines, and the scavenger who did not make it back."""
    b = Build("cargo_capsule", 1016)
    crater(b, 0, 0, 5, 2)
    furrow(b, 6, 18, 0, 1)
    # Capsule: axis along x, the heat shield facing west (-x), lying on its side.
    for i in range(0, 8):
        r = 3.6 - i * 0.32
        for dy in range(-4, 5):
            for dz in range(-4, 5):
                d = math.hypot(dy, dz)
                if d <= r:
                    shell = d > r - 1.1 or i in (0, 7)
                    if i == 0:
                        st = S("coal_block") if (dy + dz) % 3 else S("blackstone")
                    elif shell:
                        st = S("station_foil") if i in (1, 4) else S("station_hull")
                    else:
                        st = AIR
                    b.set(-2 + i, 2 + dy, dz, st)
    for (dy, dz) in ((0, 0), (1, 0), (0, 1), (1, 1)):
        b.set(5, 2 + dy, -3 + dz, AIR)
    b.set(5, 1, -4, S("iron_trapdoor", facing="north", half="bottom", open="true", powered="false",
                        waterlogged="false"))
    # Inside: racks with crates, the cargo chest, a light still glowing.
    for i in (0, 2):
        for dz in (-2, 2):
            b.set(1 + i, 1, dz, S("shipping_crate"))
    b.chest(2, 0, 0, "cargo", "east")
    b.set(3, 3, 0, S("station_light"))
    b.set(1, 2, -2, S("sparking_panel"))
    # Docking ring at the nose.
    for (dy, dz) in ((-1, 0), (1, 0), (0, -1), (0, 1)):
        b.set(6, 2 + dy, dz, S("iron_block"))
    # Heat shield fragments along the furrow, the parachute and lines further east.
    for x in range(-12, -4, 2):
        b.set(x, 1, b.rnd.randint(-3, 3), S("coal_block") if b.rnd.random() < 0.5 else S("blackstone"))
    for x in range(20, 36):
        for z in range(-7, 8):
            if ((x - 28) / 8.0) ** 2 + (z / 7.0) ** 2 <= 1 and b.rnd.random() < 0.92:
                b.set(x, 1, z, S("white_carpet") if ((x + z) // 2) % 2 else S("orange_carpet"))
    for k in range(0, 6):
        for x in range(8, 20):
            z = int(round((k - 2.5) * (x - 8) / 12.0 * 2))
            if b.rnd.random() < 0.85:
                b.set(x, 1, z, S("tripwire", attached="false", disarmed="false", east="true", west="true",
                                 north="false", south="false", powered="false"))
    skeleton(b, 9, 1, 3, "west", "skeleton_lying")
    b.set(10, 1, 4, S("barrel", facing="up", open="true"))
    return b


def crashed_satellite():
    """A big communications satellite brought down by the dying orbit: the gold-foiled bus split open, one solar wing
    torn off and lying folded at the end of the debris line, the other buckled into the ground, the main dish
    thrown aside, thruster pods, glassed sand round the impact."""
    b = Build("crashed_satellite", 1017)
    crater(b, 0, 0, 4, 2)
    furrow(b, -16, -5, 0, 1)
    # Bus: a 4x4x5 box, tilted (one corner dug in), gold foil, radiators, a thruster pod.
    for x in range(-2, 3):
        for z in range(-2, 2):
            for y in range(0, 5):
                yy = y + (1 if x > 0 else 0) - 1
                edge = x in (-2, 2) or z in (-2, 1) or y in (0, 4)
                b.set(x, yy, z, (S("station_foil") if (x + y) % 3 else S("station_radiator")) if edge else
                      (S("sparking_panel") if (x, y, z) == (0, 2, 0) else S("server_rack", facing="north")
                       if (x, y, z) == (-1, 1, -1) else AIR))
    for y in range(0, 3):
        b.set(2, y, 1, AIR)
        b.set(2, y, 0, AIR)
    b.chest(1, 0, -1, "space_debris", "east", "barrel")
    for (x, z) in ((-3, -2), (3, -2)):
        b.set(x, 2, z, S("lander_engine"))
    # The buckled wing: panels zig-zagging into the ground on the south side.
    for k in range(0, 9):
        y = 2 - k // 3
        for x in (-1, 0, 1):
            b.set(x, max(-1, y), 2 + k, S("solar_panel"))
        if k % 3 == 2:
            b.set(0, max(-1, y) + 1, 2 + k, S("truss"))
    # The torn wing at the end of the furrow, folded in three.
    for k in range(0, 3):
        for x in range(-22 + k * 4, -19 + k * 4):
            for z in (-1, 0, 1):
                b.set(x, 1 + (k == 1), z, S("solar_panel") if b.rnd.random() > 0.15 else S("glass"))
    # The dish, thrown aside on its rim.
    for dx in range(-3, 4):
        for dy in range(-3, 4):
            d = math.hypot(dx, dy)
            if d <= 3.3:
                b.set(7 + int(round(d * d / 6)), 3 + dy, -6 + dx, WHITE if d < 2.6 else LGREY)
    b.set(9, 3, -6, bars())
    for _ in range(14):
        x, z = b.rnd.randint(-10, 10), b.rnd.randint(-8, 8)
        if b.get(x, 1, z) in (None, AIR) and math.hypot(x, z) > 5:
            b.set(x, 1, z, S("scrap_pile") if b.rnd.random() < 0.6 else S("station_foil"))
    return b


def rocket_stage():
    """A spent first stage that fell back: thirty blocks of booster nose-down at a slant, its five engines in the air,
    an interstage ring and a fairing half lying nearby, a burnt fuel stain; an access hatch leads into the
    intertank, where the avionics bay still holds its boxes."""
    b = Build("rocket_stage", 1018)
    crater(b, 0, 0, 5, 3, glass=False)
    # The booster: from (0, -2, 0) nose-down, rising east at about 30 degrees.
    L = 30
    for i in range(0, L):
        cxv = i * 0.87
        cyv = -2 + i * 0.5
        for dy in range(-4, 5):
            for dz in range(-4, 5):
                d = math.hypot(dy, dz)
                if d <= 3.3:
                    x, y, z = int(round(cxv)), int(round(cyv + dy)), dz
                    shell = d > 2.3
                    st = AIR
                    if shell:
                        st = S("black_concrete") if i % 10 == 9 else S("station_foil") if 12 <= i <= 14 else \
                            S("station_hull")
                    if b.get(x, y, z) is None or b.get(x, y, z) == AIR or shell:
                        b.set(x, y, z, st)
    # Engines at the top end.
    ex, ey = int(round((L - 1) * 0.87)) + 1, int(round(-2 + (L - 1) * 0.5))
    for (dy, dz) in ((0, 0), (-2, 0), (2, 0), (0, -2), (0, 2)):
        b.set(ex, ey + dy, dz, S("lander_engine"))
        b.set(ex + 1, ey + dy, dz, S("sooty_concrete"))
    for (dy, dz) in ((-2, -2), (2, 2), (-2, 2), (2, -2)):
        for k in (1, 2):
            b.set(ex - 1 + k, ey + dy, dz + (k if dz > 0 else -k), S("station_hull"))
    # Hatch into the intertank (i ~ 13), the avionics bay.
    hx = int(round(13 * 0.87))
    hy = int(round(-2 + 13 * 0.5))
    b.set(hx, hy, -3, AIR)
    b.set(hx, hy + 1, -3, AIR)
    b.set(hx, hy, -4, S("iron_trapdoor", facing="north", half="bottom", open="true", powered="false",
                        waterlogged="false"))
    b.chest(hx, hy - 1, -1, "space_debris", "north", "barrel")
    b.set(hx + 1, hy - 1, 1, S("server_rack", facing="north"))
    b.set(hx - 1, hy - 1, 1, S("sparking_panel"))
    # Interstage ring and a fairing half on the ground.
    disc(b, -14, 10, 1, 3.3, S("station_hull"), hollow=True)
    for y in (2,):
        disc(b, -14, 10, y, 3.3, S("station_foil"), hollow=True)
    for k in range(0, 10):
        for dz in range(-2, 3):
            b.set(-12 + k, 1 + int(abs(dz) == 2), -12 + dz, WHITE)
    # Fuel stain and fragments.
    for _ in range(40):
        x, z = b.rnd.randint(-10, 14), b.rnd.randint(-10, 10)
        if b.get(x, 1, z) in (None, AIR) and b.get(x, 0, z) in (None,):
            b.set(x, 1, z, S("tar_puddle") if b.rnd.random() < 0.6 else S("scrap_pile"))
    return b


# ---------------------------------------------------------------- automated mine (underground, surface at y = 12)
def drone_site():
    """An automated mine the drones kept working long after the people left: headframe with its winding wheel over
    the shaft, the drone hangar with charging pads, ore conveyors to the spoil heaps, a control container; down the
    shaft a gallery with rails, ore carts and drones frozen mid-task."""
    SUR = 12
    b = Build("drone_site", 1019)
    ground(b, -6, -6, 26, 22, lambda x, z: S("gravel") if b.rnd.random() < 0.6 else S("coarse_dirt"))
    for x in range(-6, 27):
        for z in range(-6, 23):
            b.set(x, SUR, z, b.get(x, 0, z))
            b.blocks.pop((x, 0, z), None)
    # Shaft (x 4..7, z 4..7) from the surface down to the gallery.
    for y in range(1, SUR + 1):
        for x in range(3, 9):
            for z in range(3, 9):
                edge = x in (3, 8) or z in (3, 8)
                b.set(x, y, z, S("cobbled_deepslate") if edge else AIR)
    for y in range(1, SUR + 1):
        b.set(4, y, 4, S("ladder", facing="south"))
    for y in range(2, SUR):
        b.set(6, y, 6, CHAIN_Y)
    b.set(6, 1, 6, S("iron_block"))
    # Headframe: lattice legs and the wheel at the top.
    for y in range(SUR + 1, SUR + 14):
        for (x, z) in ((3, 3), (8, 3), (3, 8), (8, 8)):
            k = (y - SUR) // 5
            b.set(x + (k if x < 5 else -k), y, z + (k if z < 5 else -k), S("truss") if y % 4 else S("iron_block"))
    for (dx, dy) in ((0, 2), (0, -2), (2, 0), (-2, 0), (1, 1), (-1, 1), (1, -1), (-1, -1)):
        b.set(6 + dx, SUR + 14 + dy, 5, S("iron_block"))
    for y in range(SUR + 1, SUR + 13):
        b.set(6, y, 6, CHAIN_Y)
    # Gallery at y 0..5 running east and south from the shaft.
    for x in range(-2, 24):
        for z in range(4, 8):
            for y in range(0, 6):
                edge = z in (4, 7) or y in (0, 5)
                b.set(x, y, z, S("cobbled_deepslate") if edge and b.rnd.random() < 0.85 else
                      S("deepslate_titanium_ore") if edge else AIR)
        if x % 5 == 0:
            b.set(x, 4, 5, S("dark_oak_planks"))
            b.set(x, 4, 6, S("dark_oak_planks"))
        b.set(x, 1, 6, S("rail", shape="east_west", waterlogged="false"))
    for x in (10, 16):
        b.set(x, 1, 5, S("mining_drone", facing="east"))
    b.chest(20, 1, 5, "industrial", "north")
    b.set(22, 1, 6, S("barrel", facing="up", open="false"))
    for x in range(20, 24):
        for z in (4, 7):
            for y in (1, 2, 3):
                b.set(x, y, z, S("deepslate_titanium_ore") if b.rnd.random() < 0.4 else S("deepslate_bauxite_ore")
                      if b.rnd.random() < 0.3 else S("cobbled_deepslate"))
    b.set(12, 4, 6, S("lantern", hanging="true", waterlogged="false"))
    b.set(18, 4, 6, S("lantern", hanging="true", waterlogged="false"))
    # Drone hangar on the surface: a carport roof, charging pads, two drones parked.
    for x in range(12, 24):
        for z in range(-4, 3):
            b.set(x, SUR, z, S("hazard_concrete") if (x + z) % 5 == 0 else S("gray_concrete"))
            b.set(x, SUR + 6, z, S("smooth_stone_slab", type="bottom", waterlogged="false"))
    for x in (12, 18, 23):
        for z in (-4, 2):
            for y in range(SUR + 1, SUR + 6):
                b.set(x, y, z, bars())
    for x in (14, 17, 20):
        b.set(x, SUR + 1, -1, S("mining_drone", facing="south") if x != 17 else S("battery_bank", facing="south"))
        b.set(x, SUR + 1, -3, S("cable", east="true", west="true", north="false", south="false", up="false",
                                down="false"))
    b.set(21, SUR + 1, -3, S("diesel_generator", facing="west"))
    # Conveyor from the headframe to the spoil heaps.
    for x in range(9, 24):
        b.set(x, SUR + 3, 6, S("iron_trapdoor", facing="east", half="top", open="false", powered="false",
                               waterlogged="false"))
        if x % 4 == 1:
            for y in range(SUR + 1, SUR + 3):
                b.set(x, y, 6, bars())
    for (cx, cz, r) in ((24, 7, 3.5), (20, 14, 2.8)):
        for x in range(int(cx - r), int(cx + r + 1)):
            for z in range(int(cz - r), int(cz + r + 1)):
                d = math.hypot(x - cx, z - cz)
                for y in range(1, int(r - d) + 2):
                    b.set(x, SUR + y, z, S("gravel") if b.rnd.random() < 0.7 else S("cobbled_deepslate"))
    # Control container.
    b.room(-5, SUR, 12, 3, SUR + 4, 17, S("rusted_panel"), S("dead_planks"))
    door(b, 3, SUR + 1, 14)
    b.set(-4, SUR + 1, 16, S("control_cabinet", facing="east"))
    b.set(-4, SUR + 1, 13, S("server_rack", facing="east"))
    b.set(1, SUR + 1, 16, S("office_desk", facing="north"))
    b.set(1, SUR + 1, 15, S("office_chair", facing="north"))
    b.chest(-1, SUR + 1, 16, "industrial", "north", "barrel")
    b.set(-1, SUR + 3, 14, S("ceiling_light"))
    b.set(0, SUR + 1, 12, AIR)
    b.decay(0.02, keep=("minecraft:ladder", "minecraft:rail", "minecraft:iron_chain"), top_bias=False)
    return b


# ---------------------------------------------------------------- colonies
def colony(fate):
    """A failed settlement of the exodus years: the central dome (commons with the mess, a hydroponic garden ring,
    the meeting circle), five prefab modules on tube corridors (dormitory, clinic, workshop, comms, storage), a
    landing pad with a shuttle wreck, a rover, solar arrays. The fate decides the dressing: overgrown by the garden
    that outlived them, burnt out, or frozen."""
    b = Build("colony_" + fate, {"overgrown": 1020, "burnt": 1021, "frozen": 1022}[fate])
    floor = {"overgrown": S("moss_block"), "burnt": S("blackstone"), "frozen": S("packed_ice")}[fate]
    shell = {"overgrown": S("station_glass"), "burnt": S("tinted_glass"), "frozen": S("station_glass")}[fate]
    ground(b, -30, -30, 30, 30, lambda x, z: floor if b.rnd.random() < 0.3 else dead_ground(b) if fate == "burnt"
           else S("snow_block") if fate == "frozen" else S("coarse_dirt") if b.rnd.random() < 0.5 else S("moss_block"))
    R = 11
    for x in range(-R, R + 1):
        for z in range(-R, R + 1):
            if x * x + z * z <= R * R:
                b.set(x, 0, z, S("polished_andesite") if x * x + z * z < 30 else S("living_soil") if fate ==
                      "overgrown" else S("farmland", moisture=0))
            for y in range(1, R):
                d = math.sqrt(x * x + y * y * 1.3 + z * z)
                if R - 0.7 <= d <= R + 0.3:
                    rib = x % 4 == 0 or z % 4 == 0
                    b.set(x, y, z, S("station_hull") if rib else shell if b.rnd.random() > 0.15 else AIR)
                elif d < R - 0.7:
                    b.set(x, y, z, AIR)
    # Commons: a round table, benches, the noticeboard terminal.
    disc(b, 0, 0, 1, 1.5, S("smooth_stone_slab", type="top", waterlogged="false"))
    for (dx, dz, f) in ((0, -3, "south"), (0, 3, "north"), (-3, 0, "east"), (3, 0, "west")):
        b.set(dx, 1, dz, S("park_bench", facing=f))
    b.set(2, 1, -2, S("data_terminal", facing="south", log=4))
    b.set(0, 8, 0, S("station_light"))
    # Hydroponic ring between radius 6 and 9.
    crops = [S("wheat", age=7), S("carrots", age=7), S("potatoes", age=7), S("beetroots", age=3)]
    for x in range(-R, R + 1):
        for z in range(-R, R + 1):
            d = math.hypot(x, z)
            if 6 <= d <= 9 and b.get(x, 1, z) == AIR:
                if fate == "overgrown":
                    b.set(x, 1, z, crops[(x + z) % 4] if b.rnd.random() < 0.6 else S("azalea"))
                elif fate == "burnt":
                    b.set(x, 1, z, S("dead_bush") if b.rnd.random() < 0.3 else AIR)
                else:
                    b.set(x, 1, z, S("snow", layers=2) if b.rnd.random() < 0.5 else S("dead_bush"))
    # Modules on tube corridors.
    mods = [("dormitory", 0, -20), ("clinic", 19, -7), ("workshop", 12, 16), ("comms", -12, 16), ("storage", -19, -7)]
    for name, mx, mz in mods:
        b.room(mx - 4, 0, mz - 3, mx + 4, 5, mz + 3, S("station_hull"), S("polished_andesite"))
        for x in range(mx - 3, mx + 4, 2):
            b.set(x, 3, mz - 3, S("station_window"))
            b.set(x, 3, mz + 3, S("station_window"))
        b.set(mx, 4, mz, S("station_light"))
        # Corridor towards the dome: a 4-connected path of tube from the module to the dome shell.
        x, z = mx, mz
        while x * x + z * z >= (R - 1) ** 2:
            if abs(x) >= abs(z):
                x -= 1 if x > 0 else -1
            else:
                z -= 1 if z > 0 else -1
            if abs(x - mx) <= 4 and abs(z - mz) <= 3 and not (abs(x - mx) == 4 or abs(z - mz) == 3):
                continue
            b.set(x, 0, z, S("metal_grating"))
            b.set(x, 1, z, AIR)
            b.set(x, 2, z, AIR)
            b.set(x, 3, z, S("station_hull"))
            for (dx, dz) in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                for y in (1, 2):
                    if b.get(x + dx, y, z + dz) is None:
                        b.set(x + dx, y, z + dz, S("station_window") if y == 2 else S("station_hull"))
                if b.get(x + dx, 3, z + dz) is None:
                    b.set(x + dx, 3, z + dz, S("station_hull"))
        if name == "dormitory":
            for x in range(mx - 3, mx + 4, 2):
                b.set(x, 1, mz - 2, S("bunk_bed", facing="south"))
            b.chest(mx + 3, 1, mz + 2, "colony", "west")
            b.set(mx - 3, 1, mz + 2, S("locker", facing="east"))
            skeleton(b, mx - 1, 2, mz - 2, "south", "skeleton_curled")
        elif name == "clinic":
            b.set(mx - 2, 1, mz - 2, S("hospital_bed", facing="south"))
            b.set(mx + 1, 1, mz - 2, S("hospital_bed", facing="south"))
            b.set(mx - 1, 1, mz - 2, S("iv_stand"))
            b.set(mx + 3, 1, mz, S("medical_cabinet", facing="west"))
            b.chest(mx + 3, 1, mz + 2, "medical", "west")
            skeleton(b, mx + 1, 2, mz - 2, "south", "skeleton_lying")
        elif name == "workshop":
            b.set(mx - 3, 1, mz + 2, S("repair_station", facing="north"))
            b.set(mx - 1, 1, mz + 2, S("anvil", facing="east"))
            b.set(mx + 1, 1, mz + 2, S("crafting_table"))
            b.set(mx + 3, 1, mz, S("diesel_generator", facing="west"))
            b.chest(mx + 3, 1, mz - 2, "colony", "west", "barrel")
        elif name == "comms":
            b.set(mx - 3, 1, mz + 2, S("radio_set", facing="north"))
            b.set(mx - 1, 1, mz + 2, S("server_rack", facing="north"))
            b.set(mx + 1, 1, mz + 2, S("data_terminal", facing="north", log=4))
            b.set(mx + 1, 1, mz + 1, S("office_chair", facing="north"))
            skeleton(b, mx, 1, mz + 1, "north", "skeleton_sitting")
            for y in range(6, 14):
                b.set(mx + 3, y, mz - 2, bars())
            b.set(mx + 3, 14, mz - 2, S("antenna_dish"))
        else:
            for x in range(mx - 3, mx + 4):
                b.set(x, 1, mz + 2, S("shipping_crate") if x % 2 else S("barrel", facing="up", open="false"))
                b.set(x, 2, mz + 2, S("shipping_crate") if x % 3 == 0 else AIR)
            b.chest(mx - 3, 1, mz - 2, "colony", "east")
    # Landing pad with a shuttle wreck, a rover, solar arrays.
    disc(b, 22, 18, 0, 5, S("hazard_concrete"))
    disc(b, 22, 18, 0, 3.6, S("gray_concrete"))
    for i in range(0, 9):
        w = 1 if i in (0, 8) else 2
        for dz in range(-w, w + 1):
            for y in (1, 2, 3):
                if abs(dz) == w or y in (1, 3):
                    b.set(18 + i, y, 18 + dz, S("station_hull") if (i + y) % 4 else S("station_foil"))
                else:
                    b.set(18 + i, y, 18 + dz, AIR)
    b.set(26, 2, 18, S("ship_window"))
    for dz in (-3, 3):
        for k in range(0, 4):
            b.set(19 + k, 2, 18 + dz, S("ship_plating_slab", type="bottom", waterlogged="false"))
    b.set(17, 2, 18, S("lander_engine"))
    for (x, z) in ((-22, 20), (-19, 20), (-22, 23), (-19, 23)):
        b.set(x, 1, z, S("rover_wheel", facing="east"))
    for x in range(-22, -18):
        for z in range(21, 23):
            b.set(x, 2, z, S("white_concrete"))
    b.set(-21, 3, 21, S("spruce_stairs", facing="west", half="bottom", shape="straight", waterlogged="false"))
    for row in range(0, 3):
        for x in range(-26, -10, 2):
            z = -24 + row * 3
            b.set(x, 1, z, S("truss"))
            b.set(x, 2, z, S("solar_panel") if b.rnd.random() > 0.2 else AIR)
            b.set(x + 1, 2, z, S("solar_panel") if b.rnd.random() > 0.2 else AIR)
    # Fate.
    if fate == "overgrown":
        for _ in range(140):
            x, z = b.rnd.randint(-28, 28), b.rnd.randint(-28, 28)
            if b.get(x, 1, z) in (None, AIR) and b.get(x, 0, z) not in (None, AIR):
                b.set(x, 1, z, S("azalea") if b.rnd.random() < 0.25 else S("short_grass") if b.rnd.random() < 0.6
                      else S("fern"))
        for (x, z) in ((3, 4), (-4, -5), (6, -3)):
            for y in range(1, 6):
                b.set(x, y, z, S("oak_log", axis="y"))
            for dx in range(-2, 3):
                for dz in range(-2, 3):
                    for y in (5, 6, 7):
                        if abs(dx) + abs(dz) + (y - 6) * 1 <= 3 and b.get(x + dx, y, z + dz) in (None, AIR):
                            b.set(x + dx, y, z + dz, S("oak_leaves", distance=1, persistent="true",
                                                       waterlogged="false"))
        for (x, y, z), st in list(b.blocks.items()):
            if st[0].endswith("station_hull") and b.rnd.random() < 0.12 and b.get(x, y, z - 1) in (None, AIR):
                b.set(x, y, z - 1, S("vine", north="false", south="true", east="false", west="false", up="false"))
    elif fate == "burnt":
        for _ in range(80):
            x, z = b.rnd.randint(-28, 28), b.rnd.randint(-28, 28)
            if b.get(x, 1, z) in (None, AIR) and b.get(x, 0, z) not in (None, AIR):
                b.set(x, 1, z, S("rubble") if b.rnd.random() < 0.5 else S("coal_block"))
        for (x, y, z), st in list(b.blocks.items()):
            if st[0].endswith("station_hull") and b.rnd.random() < 0.3:
                b.set(x, y, z, S("sooty_concrete"))
        b.decay(0.05, keep=("intersolarsystem:data_terminal",))
    else:
        for _ in range(160):
            x, z = b.rnd.randint(-28, 28), b.rnd.randint(-28, 28)
            if b.get(x, 1, z) in (None, AIR) and b.get(x, 0, z) not in (None, AIR):
                b.set(x, 1, z, S("snow", layers=b.rnd.randint(1, 4)))
        for (x, y, z), st in list(b.blocks.items()):
            if st == AIR and y == 1 and b.rnd.random() < 0.1:
                b.set(x, y, z, S("snow", layers=3))
        b.decay(0.015)
    return b


# ---------------------------------------------------------------- wrecks
def dry_shipwreck():
    """A container freighter stranded on the old sea floor, listing to port: a shaped hull (raked bow, round stern,
    rudder and propeller in the air), the deck stacked with containers (some fallen), the superstructure with the
    bridge, the mess and cabins, and the engine room below with its great diesel."""
    b = Build("dry_shipwreck", 1023)
    crater(b, 26, 0, 6, 1, glass=False)
    L = 56
    for x in range(0, L):
        if x < 10:
            half = 1 + int(x * 0.55)
        elif x > L - 8:
            half = max(2, 6 - (x - (L - 8)) // 2)
        else:
            half = 6
        keel = 0 if 3 < x < L - 3 else 1
        for z in range(-half, half + 1):
            for y in range(keel, 10):
                tilt = (z + half) // 5   # listing: the starboard side sits higher
                edge = abs(z) == half or y == keel or (y == 9)
                yy = y + tilt - 1
                if edge:
                    if y == 9:
                        st = S("rusted_panel") if (x + z) % 7 else S("iron_block")
                    else:
                        st = S("red_concrete") if y < 3 else S("rusted_panel") if (x + y) % 5 else \
                            S("black_concrete")
                    if b.rnd.random() > 0.06:
                        b.set(x, yy, z, st)
                else:
                    b.set(x, yy, z, AIR)
    # Rudder and propeller at the stern (x = L).
    for y in range(0, 7):
        b.set(L, y, 0, S("iron_block"))
    for (dy, dz) in ((0, 1), (0, -1), (1, 0), (-1, 0)):
        b.set(L - 1, 3 + dy, dz, S("waxed_copper_block"))
    # Containers on deck (some fallen to the ground on the port side).
    colours = ("red", "blue", "green", "orange", "white", "cyan")
    for i, x in enumerate(range(12, 38, 5)):
        for row in (-4, 0):
            for level in (0, 1):
                if b.rnd.random() < 0.25:
                    continue
                c = S(colours[(i + row + level) % 6] + "_terracotta")
                for dx in range(0, 4):
                    for dz in range(0, 3):
                        for dy in (0, 1):
                            b.set(x + dx, 10 + level * 2 + dy + (row + 6) // 5, row + dz, c)
    for x in range(14, 24):
        for z in (-12, -11, -10):
            b.set(x, 1, z, S("blue_terracotta") if x < 18 else S("orange_terracotta"))
            b.set(x, 2, z, S("blue_terracotta") if x < 18 else S("orange_terracotta"))
    b.chest(15, 1, -9, "cargo", "north", "barrel")
    # Superstructure aft (x 42..51): three decks, the bridge on top with wings.
    for y in range(10, 22):
        for x in range(42, 52):
            for z in range(-5, 6):
                edge = x in (42, 51) or z in (-5, 5)
                deck = (y - 10) % 4 == 0
                yy = y + 1
                if deck:
                    b.set(x, yy, z, S("smooth_stone"))
                elif edge:
                    win = (y - 10) % 4 == 2 and x % 2 == 0
                    b.set(x, yy, z, pane() if win else WHITE)
                else:
                    b.set(x, yy, z, AIR)
    for z in range(-8, 9):
        b.set(43, 23, z, S("smooth_stone"))
        b.set(43, 24, z, bars(north=True, south=True)) if abs(z) > 5 else None
    for z in range(-4, 5):
        b.set(42, 24, z, pane())
    b.set(44, 23, 0, S("navigation_console", facing="west"))
    b.set(44, 23, -2, S("flight_panel", facing="west"))
    b.set(44, 23, 2, S("radio_set", facing="west"))
    b.set(46, 23, 0, S("office_chair", facing="west"))
    skeleton(b, 45, 23, 1, "west", "skeleton_sitting")
    b.chest(50, 23, 4, "wreck", "west")
    for x in range(44, 51, 3):
        b.set(x, 15, -4, S("bunk_bed", facing="south"))
        b.set(x + 1, 15, -4, S("locker", facing="south"))
    long_table(b, 45, 49, 19, 1)
    b.set(50, 19, -3, S("smoker", facing="west", lit="false"))
    b.chest(50, 19, -4, "bunker_food", "west", "barrel")
    for y in range(11, 24):
        b.set(51, y, 0, S("ladder", facing="west"))
    for y in range(24, 30):
        disc(b, 48, 0, y, 1.2, S("black_concrete") if y > 27 else S("red_concrete"))
    # Engine room: the main diesel in the hull below the superstructure.
    for x in range(42, 51):
        for z in (-1, 0, 1):
            for y in (1, 2, 3):
                b.set(x, y, z, S("iron_block") if (x + y) % 3 else S("gray_concrete"))
    for x in range(43, 50, 2):
        b.set(x, 4, 0, S("pipe", up="true", down="true", north="false", south="false", east="false",
                         west="false"))
    b.set(45, 1, -3, S("control_cabinet", facing="south"))
    b.chest(48, 1, 3, "wreck", "north")
    b.set(47, 1, -3, S("leaking_pipe"))
    b.decay(0.02, keep=("minecraft:chest", "minecraft:barrel", "minecraft:ladder"))
    return b


def whale_skeleton():
    """A blue whale stranded when the sea left: the great skull with its jaw bones, the spine arching out of the silt,
    ribs, the flipper bones, the tail vertebrae sinking into the ground."""
    b = Build("whale_skeleton", 1024)
    crater(b, 14, 0, 7, 1, glass=False)
    for x in range(-2, 34):
        for z in range(-6, 7):
            if b.get(x, 0, z) is None:
                b.set(x, 0, z, S("dried_silt") if b.rnd.random() < 0.7 else S("salt_crust"))
    # Spine: an arch from the skull (x 0) to the tail (x 34), vertebrae with dorsal processes.
    for x in range(4, 34):
        y = int(round(4 - ((x - 16) / 9.0) ** 2)) if x < 26 else max(0, 3 - (x - 26) // 2)
        b.set(x, max(0, y) + 1, 0, S("bone_block", axis="x"))
        if x % 2 == 0 and x < 28:
            b.set(x, max(0, y) + 2, 0, S("bone_block", axis="y"))
        if x % 3 == 0 and x < 28:
            b.set(x, max(0, y) + 1, -1, S("bone_block", axis="z"))
            b.set(x, max(0, y) + 1, 1, S("bone_block", axis="z"))
    # Ribs: arcs down to the ground on both sides.
    for x in range(8, 22, 2):
        top = int(round(4 - ((x - 16) / 9.0) ** 2)) + 1
        for dz in range(1, 6):
            y = int(round(top - (dz ** 2) / 4.5))
            if y >= 0:
                b.set(x, y, -dz, S("bone_block", axis="y"))
                b.set(x, y, dz, S("bone_block", axis="y"))
    # Skull: a long rostrum with jaw bones splayed outwards.
    for x in range(-6, 5):
        w = 2 if x > -3 else 1
        for dz in range(-w, w + 1):
            b.set(x, 1, dz, S("bone_block", axis="x"))
            if x > -1:
                b.set(x, 2, dz, S("bone_block", axis="x"))
    for x in range(-8, 4):
        spread = 3 + (4 - x) // 4
        b.set(x, 1, -spread, S("bone_block", axis="x"))
        b.set(x, 1, spread, S("bone_block", axis="x"))
    # Flippers.
    for k in range(0, 5):
        b.set(10 + k, 1, -6 - k // 2, S("bone_block", axis="x"))
        b.set(10 + k, 1, 6 + k // 2, S("bone_block", axis="x"))
    # Tail flukes in the silt.
    for dz in range(-4, 5):
        b.set(34 + abs(dz) // 2, 1, dz, S("bone_block", axis="z"))
    for _ in range(10):
        x, z = b.rnd.randint(-6, 36), b.rnd.randint(-8, 8)
        if b.get(x, 1, z) in (None, AIR):
            b.set(x, 1, z, S("dead_bush") if b.rnd.random() < 0.5 else S("bone_block", axis="x"))
    return b

BUILDERS = {
    "bunker_small": bunker_small,
    "bunker_large": bunker_large,
    "farmhouse": farmhouse,
    "radio_tower": radio_tower,
    "barracks": barracks,
    "oil_pumpjack": oil_pumpjack,
    "solar_farm": solar_farm,
    "space_center": space_center,
    "research_lab": research_lab,
    "research_station": research_station,
    "seed_bank": seed_bank,
    "terraformer": terraformer,
    "dam": dam,
    "cargo_capsule": cargo_capsule,
    "crashed_satellite": crashed_satellite,
    "rocket_stage": rocket_stage,
    "drone_site": drone_site,
    "colony_overgrown": lambda: colony("overgrown"),
    "colony_burnt": lambda: colony("burnt"),
    "colony_frozen": lambda: colony("frozen"),
    "dry_shipwreck": dry_shipwreck,
    "whale_skeleton": whale_skeleton,
}
