"""World generation data of the dead Earth (phase 7).

Writes the world presets (only the Overworld: no Nether, no End), the noise settings (vanilla terrain shape with the
seas dried out), the surface rule, the twelve biomes, their climate placement, the canyon carver, and the features
and placed features (ores, debris, dead trees, craters, acid pools...).

    python3 tools/worldgen/make_worldgen.py
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data"
NS = "intersolarsystem:"
MC = "minecraft:"


def write(rel, data):
    path = DATA / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


def ns(x):
    return x if ":" in x else NS + x


# ---------------------------------------------------------------- climate placement
FULL = [-1.0, 1.0]
OCEAN = [-1.2, -0.19]
LAND = [-0.19, 1.0]

# (biome, temperature, humidity, continentalness, erosion, weirdness, offset). Points between the boxes go to the
# nearest box, so these only have to cover the climate space roughly.
CLIMATE = [
    ("dried_seabed", FULL, FULL, OCEAN, FULL, FULL, 0.0),
    ("deep_canyons", [-0.45, 1.0], [-1.0, 0.3], [-0.11, 1.0], [-0.4, 0.6], [-0.07, 0.07], 0.0),
    ("frozen_megalopolis", [-1.0, -0.45], FULL, LAND, FULL, FULL, 0.0),
    ("salt_flats", [0.2, 1.0], [-1.0, -0.35], LAND, [0.3, 1.0], FULL, 0.0),
    ("ash_desert", [0.2, 1.0], [-0.35, -0.1], LAND, [-0.38, 1.0], FULL, 0.0),
    ("ash_desert", [0.2, 1.0], [-1.0, -0.35], LAND, [-0.38, 0.3], FULL, 0.0),
    ("oil_field", [0.55, 1.0], [-0.1, 0.45], LAND, [-0.2, 1.0], FULL, 0.0),
    ("acid_swamp", [-0.15, 0.55], [0.3, 1.0], [-0.19, 0.3], [0.2, 1.0], FULL, 0.0),
    ("petrified_forest", [-0.45, 0.2], [0.1, 1.0], LAND, [-0.38, 1.0], FULL, 0.0),
    ("abandoned_farmland", [-0.45, 0.2], [-1.0, 0.1], LAND, [-0.2, 1.0], FULL, 0.0),
    ("abandoned_farmland", [0.2, 0.55], [-0.1, 0.3], [-0.11, 0.3], [0.2, 1.0], FULL, 0.0),
    ("junk_mountains", [-0.45, 1.0], FULL, [0.03, 1.0], [-1.0, -0.38], FULL, 0.0),
    ("irradiated_crater", [-0.15, 1.0], [-0.35, 0.3], [0.03, 1.0], [-0.38, 1.0], [0.72, 1.0], 0.0),
    # Hot and humid land is mostly acid swamp (with a penalty, so the narrow oasis box below wins inside itself).
    ("acid_swamp", [0.55, 1.0], [0.45, 1.0], LAND, FULL, FULL, 0.12),
    # The oasis is small and rare: a narrow corner of the climate space.
    ("oasis", [0.7, 1.0], [0.6, 1.0], [0.0, 1.0], [0.2, 1.0], [-0.45, -0.32], 0.0),
]


def biome_source():
    entries = []
    for (b, t, h, c, e, w, off) in CLIMATE:
        entries.append({"biome": NS + b, "parameters": {
            "temperature": t, "humidity": h, "continentalness": c, "erosion": e, "weirdness": w, "depth": 0.0,
            "offset": off}})
    return {"type": "minecraft:multi_noise", "biomes": entries}


def presets():
    overworld = {"type": "minecraft:overworld", "generator": {
        "type": "minecraft:noise", "biome_source": biome_source(), "settings": NS + "dead_earth"}}
    for preset in ("normal", "large_biomes", "amplified"):
        write(f"minecraft/worldgen/world_preset/{preset}.json", {"dimensions": {"minecraft:overworld": overworld}})


# ---------------------------------------------------------------- noise settings
def noise_settings():
    src = json.loads((Path(__file__).parent / "vanilla_overworld_noise.json").read_text())
    src["material_rule"] = NS + "dead_earth"
    # The seas are gone: below this level aquifers would still flood, everything above is dry.
    src["sea_level"] = -60
    write("intersolarsystem/worldgen/noise_settings/dead_earth.json", src)


# ---------------------------------------------------------------- surface rule
def block(b):
    return {"type": "minecraft:block", "result_state": ns(b) if ":" in b or b in OURS else MC + b}


OURS = {"ash", "cinder_rock", "dried_silt", "salt_crust", "salt_block", "irradiated_soil", "trinitite", "acidic_mud",
        "compacted_junk", "tar_sand", "oil_shale", "silica_sand", "lithium_brine_crust", "oasis_grass", "living_soil"}


def patches(default, spots):
    """Top material with noise patches: spots = [(min, max, block)] on the vanilla surface noise."""
    seq = [{"type": "minecraft:condition",
            "if_true": {"type": "minecraft:noise_threshold", "noise": "minecraft:surface", "min_threshold": lo,
                        "max_threshold": hi},
            "then_run": block(b)} for (lo, hi, b) in spots]
    seq.append(block(default))
    return {"type": "minecraft:sequence", "sequence": seq}


SURFACE = {
    # biome: (top, [(noise min, noise max, patch block)], under)
    "ash_desert": ("ash", [(0.35, 0.55, "cinder_rock"), (-0.9, -0.7, "silica_sand")], "ash"),
    "dried_seabed": ("dried_silt", [(0.3, 0.6, "sand"), (-0.6, -0.4, "gravel"), (0.8, 1.2, "salt_crust")], "sand"),
    "petrified_forest": ("coarse_dirt", [(0.2, 0.5, "gravel"), (-0.5, -0.3, "ash")], "coarse_dirt"),
    "irradiated_crater": ("irradiated_soil", [(0.4, 0.6, "gravel"), (-0.4, -0.3, "trinitite")], "gravel"),
    "frozen_megalopolis": ("snow_block", [(0.4, 0.7, "packed_ice"), (-0.6, -0.4, "gravel")], "gravel"),
    "acid_swamp": ("acidic_mud", [(0.3, 0.5, "mud"), (-0.5, -0.35, "coarse_dirt")], "mud"),
    "junk_mountains": ("compacted_junk", [(0.2, 0.45, "gravel"), (-0.45, -0.25, "coarse_dirt")], "compacted_junk"),
    "salt_flats": ("salt_crust", [(0.55, 0.7, "lithium_brine_crust")], "salt_block"),
    "deep_canyons": ("red_sand", [(0.3, 0.6, "orange_terracotta")], "orange_terracotta"),
    "oasis": ("oasis_grass", [(0.5, 0.8, "sand")], "living_soil"),
    "abandoned_farmland": ("grass_block", [(0.25, 0.55, "coarse_dirt"), (-0.55, -0.4, "rooted_dirt")], "dirt"),
    "oil_field": ("tar_sand", [(0.3, 0.55, "oil_shale"), (-0.5, -0.35, "coarse_dirt")], "oil_shale"),
}


def material_rule():
    biomes = []
    for b, (top, spots, under) in SURFACE.items():
        biomes.append({"type": "minecraft:condition",
                       "if_true": {"type": "minecraft:biome", "biome_is": [NS + b]},
                       "then_run": {"type": "minecraft:sequence", "sequence": [
                           {"type": "minecraft:condition", "if_true": "minecraft:on_floor",
                            "then_run": patches(top, spots)},
                           {"type": "minecraft:condition", "if_true": "minecraft:under_floor",
                            "then_run": block(under)}]}})
    write("intersolarsystem/worldgen/material_rule/dead_earth.json", {
        "type": "minecraft:sequence", "sequence": [
            "minecraft:bedrock_floor",
            {"type": "minecraft:condition", "if_true": {"type": "minecraft:above_preliminary_surface"},
             "then_run": {"type": "minecraft:sequence", "sequence": biomes}},
            "minecraft:overworld/underground"]})


# ---------------------------------------------------------------- carver
def carver():
    write("intersolarsystem/worldgen/carver/deep_canyon.json", {
        "type": "minecraft:canyon",
        "probability": 0.09,
        "shape": {
            "distance_factor": {"type": "minecraft:uniform", "min_inclusive": 0.85, "max_exclusive": 1.0},
            "horizontal_radius_factor": {"type": "minecraft:uniform", "min_inclusive": 1.2, "max_exclusive": 1.8},
            "thickness": {"type": "minecraft:trapezoid", "min": 2.0, "max": 7.0, "plateau": 2.0},
            "vertical_radius_center_factor": 0.0,
            "vertical_radius_default_factor": 1.0,
            "width_smoothness": 3,
            "y_scale": 7.0},
        "vertical_rotation": {"type": "minecraft:uniform", "min_inclusive": -0.08, "max_exclusive": 0.08},
        "y": {"type": "minecraft:uniform", "min_inclusive": {"absolute": 30}, "max_inclusive": {"absolute": 70}}})


# ---------------------------------------------------------------- features
def feature(name, data):
    write(f"intersolarsystem/worldgen/feature/{name}.json", data)


def placed(name, feat, placement):
    write(f"intersolarsystem/worldgen/placed_feature/{name}.json", {"feature": ns(feat), "placement": placement})


def surface_scatter(count=None, rarity=None, heightmap="WORLD_SURFACE_WG", air=True):
    p = []
    if rarity:
        p.append({"type": "minecraft:rarity_filter", "chance": rarity})
    if count:
        p.append({"type": "minecraft:count", "count": count})
    p += [{"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": heightmap},
          {"type": "minecraft:biome"}]
    if air:
        p.append({"type": "minecraft:block_predicate_filter",
                  "predicate": {"type": "minecraft:matching_block_tag", "tag": "minecraft:air"}})
    return p


def state(b, props=None):
    s = {"id": ns(b) if b in OURS or b in PROPS else (b if ":" in b else MC + b)}
    if props:
        s["properties"] = props
    return s


PROPS = {"scrap_pile", "rubble", "toxic_barrel", "mining_drone", "tar_puddle", "dead_log", "petrified_log",
         "car_wreck_red", "car_wreck_blue", "car_wreck_white", "taxi_wreck", "police_car_wreck", "skeleton_lying",
         "skeleton_curled", "street_lamp", "warning_sign", "shipping_crate"}
CARS = ("car_wreck_red", "car_wreck_blue", "car_wreck_white", "taxi_wreck", "police_car_wreck")


def simple(name, b, props=None):
    feature(name, {"type": "minecraft:simple_block", "to_place": state(b, props)})


def blob(name, b, on):
    feature(name, {"type": "minecraft:block_blob", "state": ns(b) if b in OURS else MC + b,
                   "can_place_on": {"type": "minecraft:matching_blocks", "blocks": [ns(x) if x in OURS else MC + x
                                                                                   for x in on]}})


def disk(name, b, targets, lo, hi):
    feature(name, {"type": "minecraft:disk", "half_height": 1,
                   "radius": {"type": "minecraft:uniform", "min_inclusive": lo, "max_inclusive": hi},
                   "state_provider": {"type": "minecraft:rule_based", "fallback": state(b), "rules": []},
                   "target": {"type": "minecraft:matching_blocks",
                              "blocks": [ns(x) if x in OURS else MC + x for x in targets]}})


def ore(name, b, deep, size, air=0.0):
    targets = [{"state": ns(b), "target": {"predicate_type": "minecraft:tag_match",
                                            "tag": "minecraft:stone_ore_replaceables"}}]
    if deep:
        targets.append({"state": ns(deep), "target": {"predicate_type": "minecraft:tag_match",
                                                       "tag": "minecraft:deepslate_ore_replaceables"}})
    feature(name, {"type": "minecraft:ore", "discard_chance_on_air_exposure": air, "size": size, "targets": targets})


def ore_placed(name, feat, count, lo, hi, shape="uniform"):
    height = {"type": "minecraft:" + shape, "min_inclusive": {"absolute": lo}, "max_inclusive": {"absolute": hi}}
    placed(name, feat, [{"type": "minecraft:count", "count": count}, {"type": "minecraft:in_square"},
                        {"type": "minecraft:height_range", "height": height}, {"type": "minecraft:biome"}])


def features():
    # Ores of the space programme.
    ore("ore_bauxite", "bauxite_ore", "deepslate_bauxite_ore", 10)
    ore_placed("ore_bauxite", "ore_bauxite", 10, 0, 140)
    ore("ore_titanium", "titanium_ore", "deepslate_titanium_ore", 7, 0.3)
    ore_placed("ore_titanium", "ore_titanium", 6, -40, 70, "trapezoid")
    ore("ore_uraninite", "uraninite_ore", "deepslate_uraninite_ore", 5, 0.5)
    ore_placed("ore_uraninite", "ore_uraninite", 3, -64, 30, "trapezoid")
    ore_placed("ore_uraninite_fallout", "ore_uraninite", 9, -20, 90)
    ore("ore_spodumene", "spodumene_ore", "deepslate_spodumene_ore", 6)
    ore_placed("ore_spodumene", "ore_spodumene", 6, -16, 96)
    ore("ore_monazite", "monazite_ore", "deepslate_monazite_ore", 5, 0.5)
    ore_placed("ore_monazite", "ore_monazite", 4, -64, 20, "trapezoid")
    ore("ore_oil_shale", "oil_shale", None, 28)
    ore_placed("ore_oil_shale", "ore_oil_shale", 2, 0, 80)
    ore_placed("ore_oil_shale_field", "ore_oil_shale", 14, 10, 100)

    # Scattered debris and props.
    simple("scrap_pile", "scrap_pile")
    simple("rubble", "rubble")
    simple("toxic_barrel", "toxic_barrel")
    simple("tar_puddle", "tar_puddle")
    for f in ("north", "east", "south", "west"):
        simple("mining_drone_" + f, "mining_drone", {"facing": f})
    simple("hay_bale", "hay_block", {"axis": "y"})
    for coral in ("brain", "tube", "fire", "horn", "bubble"):
        simple(f"dead_{coral}_coral", f"dead_{coral}_coral_block")
    placed("scrap_pile_sparse", "scrap_pile", surface_scatter(rarity=3))
    placed("scrap_pile_dense", "scrap_pile", surface_scatter(count=4))
    placed("rubble_sparse", "rubble", surface_scatter(rarity=2))
    placed("rubble_dense", "rubble", surface_scatter(count=3))
    placed("toxic_barrel_rare", "toxic_barrel", surface_scatter(rarity=6))
    placed("toxic_barrel_common", "toxic_barrel", surface_scatter(rarity=2))
    placed("tar_puddle", "tar_puddle", surface_scatter(count=5))
    for f in ("north", "east", "south", "west"):
        placed("mining_drone_" + f, "mining_drone_" + f, surface_scatter(rarity=40))
    placed("hay_bale", "hay_bale", surface_scatter(rarity=4))
    for coral in ("brain", "tube", "fire", "horn", "bubble"):
        placed(f"dead_{coral}_coral", f"dead_{coral}_coral", surface_scatter(rarity=2))

    # The remains of the old world: abandoned cars along vanished roads, the dead where they fell, cargo crates.
    for car in CARS:
        for f in ("north", "east", "south", "west"):
            simple(f"{car}_{f}", car, {"facing": f})
            placed(f"{car}_{f}", f"{car}_{f}", surface_scatter(rarity=90))
    for pose in ("skeleton_lying", "skeleton_curled"):
        for f in ("north", "east", "south", "west"):
            simple(f"{pose}_{f}", pose, {"facing": f})
            placed(f"{pose}_{f}", f"{pose}_{f}", surface_scatter(rarity=160))
    simple("shipping_crate", "shipping_crate")
    placed("shipping_crate", "shipping_crate", surface_scatter(rarity=40))

    # Boulders and blobs.
    blob("cinder_boulder", "cinder_rock", ["ash", "silica_sand", "cinder_rock"])
    placed("cinder_boulder", "cinder_boulder", surface_scatter(rarity=3, heightmap="MOTION_BLOCKING", air=False))
    blob("junk_heap", "compacted_junk", ["compacted_junk", "gravel", "coarse_dirt"])
    placed("junk_heap", "junk_heap", surface_scatter(count=2, heightmap="MOTION_BLOCKING", air=False))
    blob("salt_boulder", "salt_block", ["salt_crust", "lithium_brine_crust"])
    placed("salt_boulder", "salt_boulder", surface_scatter(rarity=5, heightmap="MOTION_BLOCKING", air=False))
    blob("bone_heap", "bone_block", ["dried_silt", "sand", "gravel", "salt_crust"])
    placed("bone_heap", "bone_heap", surface_scatter(rarity=12, heightmap="MOTION_BLOCKING", air=False))
    blob("ice_boulder", "packed_ice", ["snow_block", "packed_ice", "gravel"])
    placed("ice_boulder", "ice_boulder", surface_scatter(rarity=4, heightmap="MOTION_BLOCKING", air=False))
    blob("trinitite_blob", "trinitite", ["irradiated_soil", "gravel", "trinitite"])
    placed("trinitite_blob", "trinitite_blob", surface_scatter(rarity=3, heightmap="MOTION_BLOCKING", air=False))

    # Disks: lithium salts in the flats, salt crust on the old sea floor, oil shale outcrops.
    disk("lithium_disk", "lithium_brine_crust", ["salt_crust"], 2, 5)
    placed("lithium_disk", "lithium_disk", surface_scatter(count=2, air=False))
    disk("salt_disk", "salt_crust", ["dried_silt", "sand"], 3, 6)
    placed("salt_disk", "salt_disk", surface_scatter(rarity=2, air=False))
    disk("oil_shale_disk", "oil_shale", ["tar_sand", "coarse_dirt"], 2, 4)
    placed("oil_shale_disk", "oil_shale_disk", surface_scatter(count=1, air=False))

    # Dead trees.
    for kind in ("petrified", "dead", "stump"):
        feature(f"{kind}_tree", {"type": NS + "dead_tree", "kind": kind})
    placed("petrified_trees", "petrified_tree", surface_scatter(count=3, heightmap="MOTION_BLOCKING"))
    placed("dead_trees", "dead_tree", surface_scatter(count=1, heightmap="MOTION_BLOCKING"))
    placed("dead_trees_sparse", "dead_tree", surface_scatter(rarity=3, heightmap="MOTION_BLOCKING"))
    placed("stumps", "stump_tree", surface_scatter(rarity=2, heightmap="MOTION_BLOCKING"))

    # Fallout craters, canyon strata.
    feature("fallout_crater", {"type": NS + "fallout_crater", "min_radius": 4, "max_radius": 9})
    placed("fallout_crater", "fallout_crater", surface_scatter(rarity=3, air=False))
    feature("canyon_strata", {"type": NS + "strata"})
    placed("canyon_strata", "canyon_strata", [{"type": "minecraft:biome"}])

    # Pools: acid in the swamp, the last clean water in the oasis.
    for name, barrier in (("acid_pool", "acidic_mud"), ("oasis_pond", "sand")):
        feature(name, {
            "type": "minecraft:lake", "barrier": state(barrier),
            "can_place_feature": {"type": "minecraft:true"},
            "can_replace_with_air_or_fluid": {"type": "minecraft:not", "predicate": {
                "type": "minecraft:matching_block_tag", "tag": "minecraft:features_cannot_replace"}},
            "can_replace_with_barrier": {"type": "minecraft:not", "predicate": {
                "type": "minecraft:matching_block_tag", "tag": "minecraft:lava_pool_stone_cannot_replace"}},
            "fluid": {"id": "minecraft:water", "properties": {"level": "0"}}})
    placed("acid_pool", "acid_pool", surface_scatter(count=1, air=False))
    placed("oasis_pond", "oasis_pond", surface_scatter(count=1, air=False))


# ---------------------------------------------------------------- biomes
ROCK_VARIETY = ["minecraft:ore_gravel", "minecraft:ore_granite_upper", "minecraft:ore_granite_lower",
                "minecraft:ore_diorite_upper", "minecraft:ore_diorite_lower", "minecraft:ore_andesite_upper",
                "minecraft:ore_andesite_lower", "minecraft:ore_tuff"]
SPACE_ORES = [NS + "ore_bauxite", NS + "ore_titanium", NS + "ore_uraninite", NS + "ore_spodumene",
              NS + "ore_monazite", NS + "ore_oil_shale"]
NO_SPAWNS = {"argument": {"spawn_costs": {}, "spawns_by_category": {
    c: [] for c in ("ambient", "axolotls", "creature", "misc", "monster", "underground_water_creature",
                    "water_ambient", "water_creature")}}, "modifier": "overlay"}

BIOMES = {
    # name: dict(temp, down, precip, sky, fog, water, water_fog, grass, foliage, particle, lakes, local, deco, top)
    "ash_desert": dict(t=1.6, sky="#a59a8c", fog="#9a8f80", grass="#8a7d5e", particle=("minecraft:white_ash", 0.02),
                       deco=["minecraft:patch_dead_bush", "stumps", "cinder_boulder", "rubble_sparse",
                             "scrap_pile_sparse"]),
    "dried_seabed": dict(fossils=["minecraft:fossil_upper", "minecraft:fossil_lower"], particle=("minecraft:white_ash", 0.003), t=1.4, sky="#a8b4b8", fog="#b9c2c0",
                         deco=["salt_disk", "dead_brain_coral", "dead_tube_coral", "dead_fire_coral",
                               "dead_horn_coral", "dead_bubble_coral", "bone_heap", "scrap_pile_sparse"]),
    "petrified_forest": dict(fossils=["minecraft:fossil_upper"], particle=("minecraft:white_ash", 0.002), t=0.8, sky="#9ba1a6", fog="#a7a59b", grass="#7d7a5a",
                             deco=["petrified_trees", "rubble_sparse", "minecraft:patch_dead_bush"]),
    "irradiated_crater": dict(t=1.0, sky="#8ea36f", fog="#7f9a5a", water="#6fd13a", water_fog="#3a6a14",
                              grass="#6e8a3a", particle=("minecraft:warped_spore", 0.012),
                              local=["fallout_crater"], ores=[NS + "ore_uraninite_fallout"],
                              deco=["trinitite_blob", "toxic_barrel_common", "scrap_pile_sparse"]),
    "frozen_megalopolis": dict(particle=("minecraft:snowflake", 0.01), t=-0.7, down=0.5, precip=True, sky="#b9c4d0", fog="#d2dae2",
                               deco=["ice_boulder", "rubble_dense", "scrap_pile_sparse", "minecraft:ice_spike"],
                               top=["minecraft:freeze_top_layer"]),
    "acid_swamp": dict(t=0.9, down=0.9, precip=True, sky="#93a07a", fog="#8a9a62", water="#8fd13a",
                       water_fog="#4c7a1e", grass="#6b7a35", foliage="#5f6d2c", particle=("minecraft:spore_blossom_air", 0.004),
                       lakes=["acid_pool"], deco=["dead_trees", "toxic_barrel_common", "rubble_sparse"]),
    "junk_mountains": dict(t=1.1, sky="#a39a8f", fog="#958b7d", particle=("minecraft:ash", 0.01),
                           deco=["junk_heap", "scrap_pile_dense", "toxic_barrel_rare", "mining_drone_north",
                                 "mining_drone_east", "mining_drone_south", "mining_drone_west"]),
    "salt_flats": dict(particle=("minecraft:white_ash", 0.002), t=1.8, sky="#c3ccd2", fog="#e2e3de", deco=["lithium_disk", "salt_boulder"]),
    "deep_canyons": dict(particle=("minecraft:ash", 0.003), t=1.5, sky="#b0a08a", fog="#c09a72", grass="#90814d", carvers=[NS + "deep_canyon"],
                         local=["canyon_strata"], deco=["minecraft:patch_dead_bush", "rubble_sparse"]),
    "oasis": dict(particle=("minecraft:firefly", 0.003), t=0.9, down=0.8, precip=True, sky="#7fa8d8", fog="#c0d8ff", water="#3f76e4", water_fog="#050533",
                  grass="#6aa84f", foliage="#5a9a40", lakes=["oasis_pond"],
                  deco=["minecraft:trees_plains", "minecraft:flower_plains", "minecraft:patch_grass_plain",
                        "minecraft:patch_sugar_cane", "minecraft:patch_pumpkin"]),
    "abandoned_farmland": dict(particle=("minecraft:white_ash", 0.0015), t=0.9, down=0.3, sky="#a9aeb0", fog="#b8b4a4", grass="#a3955a", foliage="#8a7a45",
                               deco=["minecraft:patch_dry_grass_badlands", "minecraft:patch_dead_bush", "hay_bale",
                                     "dead_trees_sparse", "scrap_pile_sparse"]),
    "oil_field": dict(t=1.5, sky="#9a8f86", fog="#857a70", water="#2a2620", water_fog="#0d0b09",
                      particle=("minecraft:ash", 0.006), ores=[NS + "ore_oil_shale_field"],
                      deco=["oil_shale_disk", "tar_puddle", "toxic_barrel_rare", "scrap_pile_sparse"]),
}


def remains(name):
    """Cars, the dead and lost cargo, scattered over the biomes people lived in."""
    out = []
    if name in ("frozen_megalopolis", "ash_desert", "abandoned_farmland", "salt_flats", "oil_field", "dried_seabed"):
        out += [f"{car}_{f}" for car in CARS for f in ("north", "east", "south", "west")]
    if name != "oasis":
        out += [f"{p}_{f}" for p in ("skeleton_lying", "skeleton_curled") for f in ("north", "east", "south", "west")]
        out.append("shipping_crate")
    return out


# How often a one-shot ambience sound plays (per tick), from tools/audio/make_ambience.py.
AMBIENT_CHANCE = {"ash_desert": 0.004, "dried_seabed": 0.003, "petrified_forest": 0.003, "irradiated_crater": 0.006,
                  "frozen_megalopolis": 0.005, "acid_swamp": 0.006, "junk_mountains": 0.005, "salt_flats": 0.003,
                  "deep_canyons": 0.003, "oasis": 0.008, "abandoned_farmland": 0.003, "oil_field": 0.004}


def biomes():
    for name, b in BIOMES.items():
        attrs = {"minecraft:gameplay/natural_mob_spawns": NO_SPAWNS,
                 "minecraft:visual/sky_color": b["sky"], "minecraft:visual/fog_color": b["fog"]}
        if "water_fog" in b:
            attrs["minecraft:visual/water_fog_color"] = b["water_fog"]
        attrs["minecraft:audio/ambient_sounds"] = {
            "loop": NS + f"ambient.{name}.loop",
            "additions": {"sound": NS + f"ambient.{name}.additions", "tick_chance": AMBIENT_CHANCE.get(name, 0.004)}}
        if "particle" in b:
            attrs["minecraft:visual/ambient_particles"] = {
                "argument": [{"particle": {"type": b["particle"][0]}, "probability": b["particle"][1]}],
                "modifier": "append"}
        effects = {"water_color": b.get("water", "#5a6a62")}
        if "grass" in b:
            effects["grass_color"] = b["grass"]
        if "foliage" in b:
            effects["foliage_color"] = b["foliage"]
        steps = [[] for _ in range(11)]
        steps[1] = [NS + x for x in b.get("lakes", [])] + ["minecraft:lake_lava_underground"]
        steps[2] = [NS + x for x in b.get("local", [])]
        steps[3] = b.get("fossils", [])
        steps[6] = ROCK_VARIETY + SPACE_ORES + b.get("ores", [])
        steps[8] = ["minecraft:spring_lava"]
        steps[9] = [x if ":" in x else NS + x for x in b.get("deco", []) + remains(name)]
        steps[10] = b.get("top", [])
        # Features of one step must keep the same relative order in every biome (else "Feature order cycle").
        steps = [sorted(step) for step in steps]
        write(f"intersolarsystem/worldgen/biome/{name}.json", {
            "attributes": attrs,
            "carvers": ["minecraft:cave", "minecraft:cave_extra_underground", "minecraft:canyon"]
                       + b.get("carvers", []),
            "downfall": b.get("down", 0.0),
            "effects": effects,
            "features": steps,
            "has_precipitation": b.get("precip", False),
            "temperature": b["t"]})
    # Tags the structures use (has_structure/...) are written by make_structures.py.
    write("intersolarsystem/tags/worldgen/biome/dead_earth.json", {"values": [NS + n for n in BIOMES]})
    write("minecraft/tags/worldgen/biome/is_overworld.json", {"replace": False, "values": [NS + n for n in BIOMES]})


if __name__ == "__main__":
    presets()
    noise_settings()
    material_rule()
    carver()
    features()
    biomes()
    print("worldgen written")
