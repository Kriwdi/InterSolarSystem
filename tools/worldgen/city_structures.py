"""The dead megacity: a jigsaw city of wide avenues and big buildings.

Start: a four-way crossing with traffic lights. Streets (9 wide: sidewalks, two lanes) branch from it; their sides
carry building lots. Buildings: glass skyscrapers (up to ~90 m), office towers, apartment blocks, a police station,
the natural history museum with its Tyrannosaurus, shop rows, a park and a collapsed tower. Every building's
connector sits in the middle of its street-facing wall (z = d - 1, facing south).
"""
import math

from make_structures import AIR, CONCRETE, GLASS, LGREY, S, WHITE, Build, jigsaw
from big_structures import (SKELETON_POSES, asphalt, collapse, door, facade_building, skeleton, stair_core)

SIDEWALK = S("smooth_stone")
STREET_LEN = 32
STREET_W = 9


def street_connectors(b, length, lots=True):
    st, tag = jigsaw("street", "street", "city/streets", "north_up", "intersolarsystem:asphalt")
    b.set(4, 0, 0, st, tag)
    st, tag = jigsaw("street", "street", "city/streets", "south_up", "intersolarsystem:asphalt")
    b.set(4, 0, length - 1, st, tag)
    if lots:
        for z in (length // 4, 3 * length // 4):
            st, tag = jigsaw("lot", "lot", "city/buildings", "west_up", "minecraft:smooth_stone")
            b.set(0, 0, z, st, tag)
            st, tag = jigsaw("lot", "lot", "city/buildings", "east_up", "minecraft:smooth_stone")
            b.set(8, 0, z, st, tag)


def street_base(b, length):
    for z in range(length):
        for x in range(STREET_W):
            if x in (0, 1, 7, 8):
                b.set(x, 0, z, SIDEWALK if b.rnd.random() > 0.15 else S("cracked_concrete"))
            else:
                st = asphalt(b, 0.18)
                if x == 4 and z % 6 < 3:
                    st = S("road_marking", facing="north")
                b.set(x, 0, z, st)
        b.set(1, 1, z, AIR)
    for z in range(4, length, 10):
        b.set(1, 1, z, S("street_lamp", facing="east"))
        b.set(7, 1, z + 5 if z + 5 < length else z, S("street_lamp", facing="west"))


def street(variant, seed):
    b = Build("city/street_" + variant, seed)
    L = STREET_LEN
    street_base(b, L)
    cars = ("car_wreck_red", "car_wreck_blue", "car_wreck_white", "taxi_wreck")
    if variant in ("straight", "jam"):
        n = 2 if variant == "straight" else 6
        for i in range(n):
            z = 3 + i * (L - 6) // max(1, n - 1) if n > 1 else L // 2
            lane = 3 if i % 2 == 0 else 5
            b.set(lane, 1, min(L - 3, z), S(b.rnd.choice(cars), facing="north" if lane == 5 else "south"))
        b.set(1, 1, 9, S("fire_hydrant", facing="east"))
        b.set(7, 1, 20, S("park_bench", facing="west"))
        if variant == "jam":
            b.set(3, 1, 14, S("skeleton_lying", facing="east"))
            b.chest(7, 1, 26, "civic", "west", "barrel")
    elif variant == "checkpoint":
        # A military checkpoint: sandbag walls with a gap, barriers, a police car, the dead who tried to pass.
        for x in range(STREET_W):
            if x not in (4,):
                b.set(x, 1, 15, S("sandbags", facing="north"))
                b.set(x, 2, 15, S("sandbags", facing="north") if x in (0, 1, 7, 8) else AIR)
        b.set(4, 1, 13, S("quarantine_barrier", facing="north"))
        b.set(3, 1, 18, S("police_car_wreck", facing="north"))
        for x in (0, 8):
            b.set(x, 1, 13, S("warning_sign", facing="north"))
        for x in range(STREET_W):
            b.set(x, 1, 11, S("barbed_wire", facing="north") if x not in (4,) else AIR)
        for (x, z) in ((2, 8), (5, 10), (4, 6), (6, 9)):
            skeleton(b, x, 1, z, pose=b.rnd.choice(("skeleton_lying", "skeleton_curled")))
        b.set(7, 1, 20, S("poster_quarantine", facing="west"))
        b.chest(1, 1, 18, "military", "east")
    elif variant == "collapsed":
        # A building came down across the road: a rubble ridge with concrete slabs and rebar.
        for z in range(10, 22):
            h = int(4 - abs(z - 16) * 0.6)
            for x in range(0, STREET_W):
                for y in range(1, max(1, h + (x % 3 == 0)) + 1):
                    if b.rnd.random() < 0.85:
                        b.set(x, y, z, b.rnd.choice((S("rubble"), S("rebar_concrete"), S("cracked_concrete"), S("gravel"))))
        for x in range(3, 6):
            for y in range(1, 3):
                b.set(x, y, 16, AIR)  # a crawl gap people dug through
        b.set(2, 1, 6, S("taxi_wreck", facing="north"))
    street_connectors(b, L)
    return b


def crossing():
    """The start: a four-way crossing (25 x 25) with traffic lights, crossing stripes, a toppled bus shelter."""
    b = Build("city/crossing", 6001)
    N = 25
    for x in range(N):
        for z in range(N):
            road = 8 <= x <= 16 or 8 <= z <= 16
            corner = not road
            st = SIDEWALK if corner else asphalt(b, 0.15)
            if road and (x in (7, 17) and 8 <= z <= 16 or z in (7, 17) and 8 <= x <= 16):
                st = S("runway_marking", facing="north" if x in (7, 17) else "east")
            b.set(x, 0, z, st)
    for (x, z, f) in ((7, 7, "south"), (17, 7, "west"), (7, 17, "east"), (17, 17, "north")):
        b.set(x, 1, z, S("traffic_light", facing=f))
    b.set(10, 1, 12, S("car_wreck_blue", facing="east"))
    b.set(14, 1, 5, S("police_car_wreck", facing="north"))
    b.set(3, 1, 3, S("fire_hydrant", facing="south"))
    for x in range(2, 6):
        b.set(x, 1, 20, S("park_bench", facing="north"))
    b.set(4, 1, 21, S("poster_exodus", facing="north"))
    skeleton(b, 12, 1, 18, pose="skeleton_lying")
    for (x, z, o) in ((12, 0, "north_up"), (12, N - 1, "south_up"), (0, 12, "west_up"), (N - 1, 12, "east_up")):
        st, tag = jigsaw("street", "street", "city/streets", o, "intersolarsystem:asphalt")
        b.set(x, 0, z, st, tag)
    # Building lots at the four corners (diagonal), facing the avenue.
    for (x, z, o) in ((3, 0, "north_up"), (21, N - 1, "south_up"), (0, 21, "west_up"), (N - 1, 3, "east_up")):
        st, tag = jigsaw("lot", "lot", "city/buildings", o, "minecraft:smooth_stone")
        b.set(x, 0, z, st, tag)
    return b


def lot_connector(b, w, d):
    st, tag = jigsaw("lot", "lot", "city/buildings", "south_up", "minecraft:smooth_stone")
    b.set(w // 2, 0, d - 1, st, tag)


def office_floor(b, x0, z0, x1, z1, y, rich=True):
    for x in range(x0 + 2, x1 - 1, 3):
        for z in range(z0 + 2, z1 - 2, 4):
            if b.rnd.random() < 0.75:
                b.set(x, y, z, S("office_desk", facing="south"))
                b.set(x, y, z + 1, S("office_chair", facing="north") if b.rnd.random() < 0.7 else AIR)
    if rich:
        b.set(x1 - 1, y, z0 + 1, S("filing_cabinet", facing="west"))
        b.set(x0 + 1, y, z0 + 1, S("vending_machine", facing="east") if b.rnd.random() < 0.4 else S("filing_cabinet", facing="east"))
    if b.rnd.random() < 0.25:
        skeleton(b, x0 + 2 + b.rnd.randrange(max(1, x1 - x0 - 4)), y, z1 - 2, pose="skeleton_sitting", facing="north")


def skyscraper(name, seed, w, d, floors):
    """A glass tower: lobby with a reception and departure-style boards, office floors, a server floor, the top
    floors torn open, a mast on the roof."""
    b = Build("city/" + name, seed)
    storey = 4
    levels = facade_building(b, 0, 0, w - 1, d - 1, floors, storey, S("gray_concrete"), S("curtain_wall"),
                             S("office_carpet"), roof=S("gray_concrete"), pillars=3, window_rows=(1, 2, 3))
    top = floors * storey
    for (x, y, z), st in list(b.blocks.items()):
        if st == S("curtain_wall") and b.rnd.random() < 0.08 + 0.25 * y / top:
            b.set(x, y, z, S("broken_curtain_wall") if b.rnd.random() < 0.5 else AIR)
    # Lobby: double height entrance, marble floor, reception.
    for x in range(1, w - 1):
        for z in range(1, d - 1):
            b.set(x, 0, z, S("terminal_floor"))
            b.set(x, storey, z, AIR if 3 <= x <= w - 4 and 3 <= z <= d - 4 else S("office_carpet"))
    for x in range(w // 2 - 2, w // 2 + 3):
        door(b, x, 1, d - 1, 1, 3)
    for x in range(w // 2 - 4, w // 2 + 5):
        b.set(x, 1, 4, S("office_desk", facing="south") if x % 2 else S("smooth_stone"))
    b.set(w // 2, 1, 5, S("skeleton_sitting", facing="south"))
    b.set(w // 2 + 3, 3, d - 2, S("poster_exodus", facing="north"))
    stair_core(b, 1, 1, levels, top)
    b.chest(w - 2, 1, 1, "civic", "west")
    for f in range(2, floors):
        y = levels[f] + 1
        if f == floors // 2:
            for x in range(3, w - 3, 2):
                for z in range(3, d - 3, 3):
                    b.set(x, y, z, S("server_rack", facing="south"))
            b.set(w - 3, y, d - 3, S("data_terminal", facing="north", log=6))
            b.chest(2, y, d - 2, "lab", "east")
        else:
            office_floor(b, 0, 0, w - 1, d - 1, y)
        if f % 4 == 0:
            b.chest(w - 2, y, d - 2, "civic", "west", "barrel")
        b.set(w // 2, levels[f] + storey - 1, d // 2, S("ceiling_light"))
    for y in range(top + 1, top + 12):
        b.set(w // 2, y, d // 2, S("iron_bars") if y < top + 11 else S("antenna_dish"))
    b.set(w // 2 + 1, top + 1, d // 2, S("station_light"))
    collapse(b, 0, 0, w - 1, d - 1, int(top * 0.6), top + 12, 0.12)
    lot_connector(b, w, d)
    return b


def office_tower(seed):
    b = Build("city/office", seed)
    w, d, floors, storey = 16, 18, 10, 4
    levels = facade_building(b, 0, 0, w - 1, d - 1, floors, storey, S("sooty_concrete"), S("glass"),
                             S("office_carpet"), roof=S("gray_concrete"), pillars=2, window_rows=(2,))
    top = floors * storey
    door(b, w // 2 - 1, 1, d - 1, 2, 3)
    stair_core(b, 1, 1, levels, top)
    for f in range(floors):
        office_floor(b, 0, 0, w - 1, d - 1, levels[f] + 1, rich=f % 2 == 0)
        if f % 3 == 1:
            b.chest(w - 2, levels[f] + 1, d - 2, "civic", "west")
    collapse(b, 0, 0, w - 1, d - 1, top // 2, top, 0.1)
    lot_connector(b, w, d)
    return b


def apartments(seed):
    """A brick block of flats: two flats per floor off a central stair, balconies on the street side."""
    b = Build("city/apartments", seed)
    w, d, floors, storey = 26, 12, 8, 4
    levels = facade_building(b, 0, 0, w - 1, d - 1, floors, storey, S("bricks"), S("glass_pane", north="false", south="false",
                             east="true", west="true", waterlogged="false"), S("spruce_planks"), roof=S("gray_concrete"), pillars=3,
                             window_rows=(2,))
    top = floors * storey
    # Window panes on the x-facing walls need the other orientation.
    for (x, y, z), st in list(b.blocks.items()):
        if st[0] == "minecraft:glass_pane" and x in (0, w - 1):
            b.set(x, y, z, S("glass_pane", north="true", south="true", east="false", west="false", waterlogged="false"))
    door(b, w // 2, 1, d - 1, 1, 2)
    stair_core(b, w // 2, 1, levels, top)
    for f in range(floors):
        y = levels[f] + 1
        for x in range(1, w - 1):
            if x in (w // 2 - 1, w // 2 + 1):
                for z in range(1, d - 1):
                    if z != d // 2:
                        b.set(x, y, z, S("bricks"))
                        b.set(x, y + 1, z, S("bricks"))
        for side, x0 in ((-1, 2), (1, w // 2 + 3)):
            b.set(x0, y, 2, S("red_bed", facing="north", part="head", occupied="false"))
            b.set(x0, y, 3, S("red_bed", facing="north", part="foot", occupied="false"))
            b.set(x0 + 4, y, 2, S("oak_slab", type="bottom") if b.rnd.random() < 0.5 else S("crafting_table"))
            b.set(x0 + 6, y, d - 3, S("barrel", facing="up", open="false"))
            if b.rnd.random() < 0.3:
                skeleton(b, x0 + 1, y, 5, pose=b.rnd.choice(SKELETON_POSES))
            if b.rnd.random() < 0.15:
                b.set(x0 + 3, y, 6, S("dog_skeleton", facing="east"))
        if f % 2 == 0:
            b.chest(1, y, d - 2, "bunker_food", "east", "barrel")
        if f % 3 == 1:
            b.chest(w - 2, y, 1, "civic", "west")
        # Balconies on the street side.
        if f:
            for x in range(2, w - 2, 6):
                for dx in range(0, 3):
                    b.set(x + dx, levels[f], d, S("smooth_stone_slab", type="top"))
                    b.set(x + dx, levels[f] + 1, d + 1, S("iron_bars"))
    collapse(b, 0, 0, w - 1, d + 1, top // 2, top, 0.07)
    lot_connector(b, w, d + 2)  # in front of the balconies, on the outermost row
    return b


def police_station(seed):
    b = Build("city/police", seed)
    w, d, storey = 22, 16, 5
    levels = facade_building(b, 0, 0, w - 1, d - 1, 2, storey, S("stone_bricks"), S("iron_bars"), S("polished_andesite"),
                             roof=S("gray_concrete"), pillars=3, window_rows=(2, 3))
    door(b, w // 2 - 1, 1, d - 1, 2, 3)
    for x in range(w // 2 - 3, w // 2 + 3):
        b.set(x, storey - 1, d, S("blue_concrete"))
    stair_core(b, 1, 1, levels, 2 * storey)
    # Front desk, cells with bars, the armoury, lockers.
    for x in range(3, w - 3):
        b.set(x, 1, d - 6, S("office_desk", facing="south") if x % 3 == 0 else S("smooth_stone"))
    for cx in range(2, w - 2, 5):
        for x in range(cx, cx + 4):
            b.set(x, 1, 5, S("iron_bars"))
            b.set(x, 2, 5, S("iron_bars"))
            b.set(x, 3, 5, S("iron_bars"))
        b.set(cx + 1, 1, 2, S("white_bed", facing="east", part="head", occupied="false"))
        b.set(cx, 1, 2, S("white_bed", facing="east", part="foot", occupied="false"))
        if b.rnd.random() < 0.6:
            skeleton(b, cx + 2, 1, 3, pose=b.rnd.choice(("skeleton_curled", "skeleton_sitting")), facing="south")
    y = storey + 1
    for x in range(2, w - 2, 2):
        b.set(x, y, 1, S("locker", facing="south"))
    office_floor(b, 0, 0, w - 1, d - 1, y)
    b.chest(w - 2, y, d - 2, "military", "west")
    b.chest(w - 2, 1, 1, "military", "west", "barrel")
    b.set(2, y, d - 2, S("data_terminal", facing="north", log=8))
    for x in (2, w - 6):
        b.set(x, 1, d + 3, S("police_car_wreck", facing="north"))
    for x in range(0, w):
        b.set(x, 0, d, SIDEWALK)
        b.set(x, 0, d + 1, asphalt(b))
        b.set(x, 0, d + 2, asphalt(b))
        b.set(x, 0, d + 3, asphalt(b))
        b.set(x, 0, d + 4, SIDEWALK)
    collapse(b, 0, 0, w - 1, d - 1, storey, 2 * storey, 0.05)
    lot_connector(b, w, d + 5)
    return b


def museum(seed):
    """The natural history museum: a columned portico, the great hall with the Tyrannosaurus, side galleries."""
    b = Build("city/museum", seed)
    w, d, h = 34, 26, 14
    b.fill(0, 0, 0, w - 1, 0, d - 1, S("polished_andesite"))
    for y in range(1, h):
        for x in range(0, w):
            for z in range(0, d - 5):
                edge = x in (0, w - 1) or z in (0, d - 6)
                b.set(x, y, z, S("smooth_quartz") if edge else AIR)
    for x in range(0, w):
        for z in range(0, d - 5):
            b.set(x, h, z, S("smooth_quartz_slab", type="bottom"))
    # Glass roof light over the hall.
    for x in range(8, w - 8):
        for z in range(4, d - 9):
            b.set(x, h, z, S("glass") if b.rnd.random() > 0.3 else AIR)
    # Portico: steps and six columns, a pediment.
    for i, z in enumerate(range(d - 5, d)):
        for x in range(4, w - 4):
            b.set(x, 0, z, S("quartz_stairs", facing="north", half="bottom", shape="straight", waterlogged="false") if i == 4 else S("smooth_quartz"))
    for x in range(5, w - 4, 5):
        for y in range(1, h):
            b.set(x, y, d - 3, S("quartz_pillar", axis="y"))
    for x in range(3, w - 3):
        b.set(x, h, d - 3, S("smooth_quartz"))
        b.set(x, h + 1, d - 3, S("smooth_quartz_slab", type="bottom") if abs(x - w // 2) > (w // 2 - 3) - 2 else S("smooth_quartz"))
    for x in range(8, w - 8):
        if abs(x - w // 2) < 10:
            b.set(x, h + 2, d - 3, S("smooth_quartz_slab", type="bottom") if abs(x - w // 2) > 6 else S("smooth_quartz"))
    door(b, w // 2 - 1, 1, d - 6, 3, 4)
    # The Tyrannosaurus: skull on a plinth, neck, spine, ribs, tail, legs in bone blocks.
    cx, cz = w // 2, 9
    b.set(cx - 9, 1, cz, S("dino_skull", facing="west"))
    for i in range(-8, 12):
        y = 6 - abs(i) // 5 if i < 4 else 6 - (i - 4) // 3
        b.set(cx + i, max(2, y), cz, S("bone_block", axis="x"))
        if -3 <= i <= 4:
            for dy in range(1, 4):
                b.set(cx + i, max(2, y) - dy, cz - 1 - dy // 2, S("bone_block", axis="y") if i % 2 == 0 else AIR)
                b.set(cx + i, max(2, y) - dy, cz + 1 + dy // 2, S("bone_block", axis="y") if i % 2 == 0 else AIR)
    for (lx, lz) in ((cx + 2, cz - 2), (cx + 2, cz + 2)):
        for y in range(1, 5):
            b.set(lx, y, lz, S("bone_block", axis="y"))
        b.set(lx - 1, 1, lz, S("bone_block", axis="x"))
    for x in range(cx - 12, cx + 13):
        for z in (cz - 4, cz + 4):
            if b.get(x, 1, z) == AIR:
                b.set(x, 1, z, S("quarantine_barrier", facing="north") if x % 4 == 0 else AIR)
    # Side galleries: skeletons of extinct animals (ours, now), display cases, the gift shop.
    for (x, z, kind) in ((3, 4, "horse_skeleton"), (3, 12, "cow_skeleton"), (w - 4, 4, "dog_skeleton"), (w - 4, 12, "horse_skeleton")):
        b.set(x, 1, z, S("smooth_quartz"))
        b.set(x, 2, z, S(kind, facing="east" if x < w // 2 else "west"))
    for x in range(4, w - 4, 4):
        b.set(x, 1, 2, S("glass"))
        b.set(x, 2, 2, S("tinted_glass"))
    b.set(w - 3, 1, d - 8, S("vending_machine", facing="north"))
    b.set(2, 1, d - 7, S("poster_exodus", facing="south"))
    b.set(w // 2 + 4, 1, d - 8, S("data_terminal", facing="north", log=6))
    skeleton(b, w // 2 + 2, 1, 15, pose="skeleton_sitting", facing="north")
    b.chest(1, 1, 1, "civic", "east")
    b.chest(w - 2, 1, 1, "lab", "west", "barrel")
    collapse(b, 0, 0, w - 1, d - 1, 8, h + 2, 0.08)
    lot_connector(b, w, d)
    return b


def shops(seed):
    b = Build("city/shops", seed)
    w, d = 24, 12
    facade_building(b, 0, 0, w - 1, d - 1, 1, 5, S("stone_bricks"), S("glass"), S("smooth_stone"), roof=S("gray_concrete"),
                    pillars=6, window_rows=(1, 2, 3))
    for x in range(0, w):
        b.set(x, 4, d, S("red_concrete") if x % 6 < 3 else S("white_concrete"))  # awnings
    for sx in (0, 6, 12, 18):
        door(b, sx + 3, 1, d - 1, 1, 2)
        if sx:
            for z in range(1, d - 1):
                for y in range(1, 4):
                    b.set(sx, y, z, S("stone_bricks"))
            b.set(sx, 1, d // 2, AIR)
        for z in range(2, d - 3, 2):
            b.set(sx + 2, 1, z, S("barrel", facing="up", open="false"))
            b.set(sx + 4, 1, z, S("barrel", facing="up", open="false") if b.rnd.random() < 0.5 else AIR)
    b.set(9, 1, 2, S("vending_machine", facing="south"))
    b.set(15, 1, 2, S("medical_cabinet", facing="south"))
    b.chest(1, 1, 1, "civic", "east")
    b.chest(13, 1, 1, "bunker_food", "east", "barrel")
    b.chest(19, 1, 1, "medical", "east")
    skeleton(b, 20, 1, 8, pose="skeleton_curled")
    for x in range(0, w):
        b.set(x, 0, d, SIDEWALK)
    b.decay(0.04, cracked=CONCRETE)
    lot_connector(b, w, d + 1)
    return b


def park(seed):
    b = Build("city/park", seed)
    w, d = 22, 22
    for x in range(w):
        for z in range(d):
            path = abs(x - w // 2) <= 1 or abs(z - d // 2) <= 1
            b.set(x, 0, z, S("gravel") if path else S("coarse_dirt") if b.rnd.random() < 0.6 else S("ash"))
    for (x, z) in ((4, 4), (16, 5), (5, 16), (17, 17), (9, 3)):
        h = b.rnd.randint(4, 7)
        for y in range(1, h):
            b.set(x, y, z, S("dead_log", axis="y"))
        b.set(x + 1, h - 1, z, S("dead_log", axis="x"))
        b.set(x, h - 2, z + 1, S("dead_log", axis="z"))
    for (x, z, f) in ((w // 2 - 3, d // 2 - 2, "south"), (w // 2 + 3, d // 2 + 2, "north"), (w // 2 - 2, d // 2 + 3, "east")):
        b.set(x, 1, z, S("park_bench", facing=f))
    # A statue of the Aurora founder, toppled.
    b.fill(w // 2 - 1, 1, d // 2 - 1, w // 2 + 1, 1, d // 2 + 1, S("polished_andesite"))
    for i in range(5):
        b.set(w // 2 + 2 + i, 1, d // 2, S("calcite"))
    b.set(w // 2 + 7, 1, d // 2, S("chiseled_stone_bricks"))
    skeleton(b, w // 2 - 3, 1, d // 2 - 1, pose="skeleton_sitting", facing="south")
    b.set(w // 2 + 4, 1, d // 2 - 3, S("dog_skeleton", facing="west"))
    b.chest(2, 1, d - 2, "civic", "east", "barrel")
    lot_connector(b, w, d)
    return b


def collapsed_tower(seed):
    b = Build("city/collapsed", seed)
    w, d = 18, 18
    for x in range(w):
        for z in range(d):
            b.set(x, 0, z, S("cracked_concrete"))
            dist = math.hypot(x - w / 2, z - d / 2)
            h = int(max(0, 7 - dist * 0.7 + b.rnd.random() * 2))
            for y in range(1, h + 1):
                b.set(x, y, z, b.rnd.choice((S("rubble"), S("rebar_concrete"), S("cracked_concrete"), S("broken_curtain_wall"), S("gravel"))))
    # The stump of the frame still stands at one corner.
    for y in range(1, 24):
        for (x, z) in ((1, 1), (1, 6), (6, 1)):
            if y < 24 - (x + z) * 2:
                b.set(x, y, z, S("gray_concrete"))
        if y % 4 == 0:
            for x in range(1, 7 - y // 6):
                b.set(x, y, 1, S("gray_concrete"))
    b.chest(w // 2, 8, d // 2, "civic", "north", "barrel")
    skeleton(b, 3, 1, d - 3, pose="skeleton_lying")
    lot_connector(b, w, d)
    return b


def city_pieces():
    pieces = {"city/crossing": crossing()}
    for v, seed in (("straight", 6011), ("jam", 6012), ("checkpoint", 6013), ("collapsed", 6014)):
        pieces["city/street_" + v] = street(v, seed)
    pieces["city/skyscraper"] = skyscraper("skyscraper", 6101, 22, 22, 22)
    pieces["city/skyscraper_tall"] = skyscraper("skyscraper_tall", 6102, 18, 18, 28)
    pieces["city/office"] = office_tower(6103)
    pieces["city/apartments"] = apartments(6104)
    pieces["city/police"] = police_station(6105)
    pieces["city/museum"] = museum(6106)
    pieces["city/shops"] = shops(6107)
    pieces["city/park"] = park(6108)
    pieces["city/collapsed"] = collapsed_tower(6109)
    return pieces


POOLS = {
    "city/start": [("city/crossing", 1)],
    "city/streets": [("city/street_straight", 5), ("city/street_jam", 3), ("city/street_checkpoint", 1),
                     ("city/street_collapsed", 1), ("city/crossing", 2)],
    "city/buildings": [("city/skyscraper", 2), ("city/skyscraper_tall", 1), ("city/office", 3), ("city/apartments", 3),
                       ("city/police", 1), ("city/museum", 1), ("city/shops", 3), ("city/park", 2), ("city/collapsed", 2)],
}
