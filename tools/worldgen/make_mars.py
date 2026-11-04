"""Mars as a world: dimension type (butterscotch sky, ordinary day and night), the terrain (broad basins and
plateaus with cratered highlands and canyons), three biomes (rusty plains, dune seas, cratered highlands) placed by
climate noise, the surface dressing and the craters. The polar caps are laid by the surface feature itself (by
latitude, see MarsSurfaceFeature).

    python3 tools/worldgen/make_mars.py
"""
import copy
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data"
NS = "intersolarsystem:"

NO_SPAWNS = {"argument": {"spawn_costs": {}, "spawns_by_category": {
    c: [] for c in ("ambient", "axolotls", "creature", "misc", "monster", "underground_water_creature",
                    "water_ambient", "water_creature")}}, "modifier": "overlay"}


def write(rel, data):
    path = DATA / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


def dimension():
    moon = json.loads((DATA / "intersolarsystem/dimension_type/moon.json").read_text())
    mars = copy.deepcopy(moon)
    mars["ambient_light"] = 0.0
    mars["attributes"]["minecraft:visual/ambient_light_color"] = "#0d0806"
    mars["attributes"]["minecraft:visual/sky_color"] = "#d9a57a"
    mars["attributes"]["minecraft:visual/fog_color"] = "#c48c62"
    mars["attributes"]["minecraft:visual/cloud_color"] = "#00ffffff"
    mars["timelines"] = "#intersolarsystem:on_mars"
    write("intersolarsystem/dimension_type/mars.json", mars)
    write("intersolarsystem/tags/timeline/on_mars.json", {"values": ["#minecraft:universal", "minecraft:day"]})
    biomes = [
        ("mars_plains", [-1.0, 0.2], [-1.0, 0.1]),
        ("mars_dunes", [0.2, 1.0], [-1.0, 0.1]),
        ("mars_highlands", [-1.0, 1.0], [0.1, 1.0]),
    ]
    entries = [{"biome": NS + b, "parameters": {"temperature": t, "humidity": h, "continentalness": 0.0,
                                                 "erosion": 0.0, "weirdness": 0.0, "depth": 0.0, "offset": 0.0}}
               for (b, t, h) in biomes]
    write("intersolarsystem/dimension/mars.json", {
        "type": NS + "mars",
        "generator": {"type": "minecraft:noise", "biome_source": {"type": "minecraft:multi_noise", "biomes": entries},
                      "settings": NS + "mars"}})


def noise_settings():
    moon = json.loads((DATA / "intersolarsystem/worldgen/noise_settings/moon.json").read_text())
    mars = copy.deepcopy(moon)
    mars["default_block"] = NS + "mars_rock"
    mars["material_rule"] = NS + "mars"
    router = mars["noise_router"]
    router["temperature"] = {"type": "minecraft:noise", "noise": "minecraft:temperature", "xz_scale": 0.35,
                             "y_scale": 0.0}
    router["vegetation"] = {"type": "minecraft:noise", "noise": "minecraft:vegetation", "xz_scale": 0.35,
                            "y_scale": 0.0}
    # Broad basins and plateaus (a slow noise), rolling hills on top, a gentle overall slope with height.
    router["final_density"] = {"type": "minecraft:squeeze", "input": {
        "type": "minecraft:interpolated", "cell_size_xz": 8, "cell_size_y": 4, "input": {
            "type": "minecraft:add",
            "left": {"type": "minecraft:gradient", "axis": "y", "from_coordinate": 44, "from_value": 1.3,
                     "to_coordinate": 140, "to_value": -1.3},
            "right": {"type": "minecraft:add",
                      "left": {"type": "minecraft:mul", "left": {
                          "type": "minecraft:old_blended_noise", "smear_scale_multiplier": 8.0, "xz_factor": 110.0,
                          "xz_scale": 0.25, "y_factor": 80.0, "y_scale": 0.125}, "right": 0.5},
                      "right": {"type": "minecraft:mul", "left": {
                          "type": "minecraft:noise", "noise": "minecraft:erosion", "xz_scale": 0.12, "y_scale": 0.0},
                          "right": 0.85}}}}}
    write("intersolarsystem/worldgen/noise_settings/mars.json", mars)
    write("intersolarsystem/worldgen/material_rule/mars.json",
          {"type": "minecraft:block", "result_state": NS + "mars_rock"})


def state(name):
    """A block state as the 26.3 codecs write it: just the id when it has no properties."""
    return name if ":" in name else NS + name


def features():
    def surface(name, top, alt, alt_chance, under, depth):
        write(f"intersolarsystem/worldgen/feature/{name}.json", {
            "type": NS + "mars_surface", "top": state(top), "alt": state(alt), "alt_chance": alt_chance,
            "under": state(under), "depth": depth})
        write(f"intersolarsystem/worldgen/placed_feature/{name}.json", {"feature": NS + name, "placement": []})
    surface("mars_surface_plains", "mars_regolith", "mars_dust", 0.18, "mars_regolith", 3)
    surface("mars_surface_dunes", "mars_dust", "mars_dust", 0.0, "mars_dust", 6)
    surface("mars_surface_highlands", "mars_regolith", "perchlorate_crust", 0.04, "mars_sandstone", 4)
    for (name, lo, hi, rarity) in (("mars_crater_large", 7, 9, 10), ("mars_crater_small", 3, 6, 3)):
        write(f"intersolarsystem/worldgen/feature/{name}.json", {
            "type": NS + "crater", "min_radius": lo, "max_radius": hi, "floor": state("mars_regolith"),
            "fresh": state("mars_dust"), "rim": state("mars_rock")})
        write(f"intersolarsystem/worldgen/placed_feature/{name}.json", {"feature": NS + name, "placement": [
            {"type": "minecraft:rarity_filter", "chance": rarity}, {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]})
    write("intersolarsystem/worldgen/feature/mars_boulder.json", {
        "type": "minecraft:block_blob", "state": state("mars_rock"),
        "can_place_on": {"type": "minecraft:matching_blocks",
                         "blocks": [NS + "mars_regolith", NS + "mars_dust", NS + "mars_rock"]}})
    write("intersolarsystem/worldgen/placed_feature/mars_boulder.json", {"feature": NS + "mars_boulder", "placement": [
        {"type": "minecraft:rarity_filter", "chance": 4}, {"type": "minecraft:in_square"},
        {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING"}, {"type": "minecraft:biome"}]})


SKY = "#d9a57a"
FOG = "#c48c62"


def biomes():
    spec = {
        "mars_plains": (["mars_surface_plains"], ["mars_crater_large", "mars_crater_small"], ["mars_boulder"],
                        ("minecraft:white_ash", 0.004)),
        "mars_dunes": (["mars_surface_dunes"], ["mars_crater_small"], [], ("minecraft:white_ash", 0.012)),
        "mars_highlands": (["mars_surface_highlands"], ["mars_crater_large", "mars_crater_small"], ["mars_boulder"],
                           ("minecraft:white_ash", 0.002)),
    }
    for name, (first, craters, deco, particle) in spec.items():
        steps = [[] for _ in range(11)]
        steps[0] = [NS + f for f in first]
        steps[3] = sorted(NS + f for f in craters)
        steps[9] = [NS + f for f in deco]
        write(f"intersolarsystem/worldgen/biome/{name}.json", {
            "attributes": {
                "minecraft:gameplay/natural_mob_spawns": NO_SPAWNS,
                "minecraft:visual/sky_color": SKY, "minecraft:visual/fog_color": FOG,
                "minecraft:visual/ambient_particles": {"argument": [{"particle": {"type": particle[0]},
                                                                     "probability": particle[1]}],
                                                       "modifier": "append"},
                "minecraft:audio/ambient_sounds": {
                    "loop": NS + "ambient.mars.loop",
                    "additions": {"sound": NS + "ambient.mars.additions", "tick_chance": 0.003}}},
            "carvers": [NS + "deep_canyon"] if name == "mars_highlands" else [],
            "downfall": 0.0,
            "effects": {"water_color": "#7a5a48"},
            "features": steps,
            "has_precipitation": False,
            "temperature": 0.2})


if __name__ == "__main__":
    dimension()
    noise_settings()
    features()
    biomes()
    print("mars worldgen written")
