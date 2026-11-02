"""Renders a block model JSON (elements + textures) as an isometric preview PNG, like the inventory view.

Development helper only: lets models be checked without starting the game. Supports element rotation, face UVs,
face rotation, "#texture" references and parents inside this mod plus a few vanilla templates (cube_all, cube).
Animated textures (taller than wide) use their first frame.

Usage: python3 tools/art/render_model.py <model name, e.g. block/repair_station> <out.png> [facing]
"""
import json
import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/intersolarsystem"

VANILLA = {
    "block/cube_all": {"parent": "block/cube", "textures": {"particle": "#all", "down": "#all", "up": "#all",
                                                           "north": "#all", "south": "#all", "west": "#all",
                                                           "east": "#all"}},
    "block/cube": {"elements": [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": {
        d: {"texture": "#" + d, "cullface": d} for d in ("down", "up", "north", "south", "west", "east")}}]},
    "block/block": {},
}


def load_model(name):
    name = name.split(":")[-1]
    if name in VANILLA:
        return VANILLA[name]
    return json.loads((ASSETS / "models" / (name + ".json")).read_text())


def resolve(name):
    model = load_model(name)
    textures, elements = {}, None
    chain = [model]
    while "parent" in chain[-1]:
        chain.append(load_model(chain[-1]["parent"]))
    for m in reversed(chain):
        textures.update(m.get("textures", {}))
        if "elements" in m:
            elements = m["elements"]
    return textures, elements or []


def texture(textures, ref):
    seen = 0
    while ref.startswith("#") and seen < 10:
        ref = textures.get(ref[1:], "missing")
        seen += 1
    path = ASSETS / "textures" / (ref.split(":")[-1] + ".png")
    if not path.exists():
        img = Image.new("RGBA", (16, 16), (255, 0, 255, 255))
        return img
    img = Image.open(path).convert("RGBA")
    if img.height > img.width:
        img = img.crop((0, 0, img.width, img.width))
    return img


def rot(p, axis, angle, origin):
    a = math.radians(angle)
    x, y, z = (p[i] - origin[i] for i in range(3))
    c, s = math.cos(a), math.sin(a)
    if axis == "x":
        y, z = y * c - z * s, y * s + z * c
    elif axis == "y":
        x, z = x * c + z * s, -x * s + z * c
    else:
        x, y = x * c - y * s, x * s + y * c
    return (x + origin[0], y + origin[1], z + origin[2])


# Corners of each face, listed as (top-left, top-right, bottom-left) of the texture when looking at the face.
def face_corners(f, t, d):
    x0, y0, z0 = f
    x1, y1, z1 = t
    return {
        "north": ((x1, y1, z0), (x0, y1, z0), (x1, y0, z0)),
        "south": ((x0, y1, z1), (x1, y1, z1), (x0, y0, z1)),
        "west": ((x0, y1, z0), (x0, y1, z1), (x0, y0, z0)),
        "east": ((x1, y1, z1), (x1, y1, z0), (x1, y0, z1)),
        "up": ((x0, y1, z0), (x1, y1, z0), (x0, y1, z1)),
        "down": ((x0, y0, z1), (x1, y0, z1), (x0, y0, z0)),
    }[d]


def default_uv(f, t, d):
    x0, y0, z0 = f
    x1, y1, z1 = t
    return {
        "north": (16 - x1, 16 - y1, 16 - x0, 16 - y0), "south": (x0, 16 - y1, x1, 16 - y0),
        "west": (z0, 16 - y1, z1, 16 - y0), "east": (16 - z1, 16 - y1, 16 - z0, 16 - y0),
        "up": (x0, z0, x1, z1), "down": (x0, 16 - z1, x1, 16 - z0),
    }[d]


NORMALS = {"north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0), "east": (1, 0, 0), "up": (0, 1, 0),
           "down": (0, -1, 0)}

SHADE = {"up": 1.0, "down": 0.5, "north": 0.8, "south": 0.8, "west": 0.6, "east": 0.6}


def render(name, out, facing_y=0, size=512):
    textures, elements = resolve(name)
    yaw, pitch = math.radians(225 + facing_y), math.radians(30)
    scale = size / 16 / 1.75

    def camera_z(v):
        x, y, z = v
        x, z = x * math.cos(yaw) - z * math.sin(yaw), x * math.sin(yaw) + z * math.cos(yaw)
        y, z = y * math.cos(pitch) - z * math.sin(pitch), y * math.sin(pitch) + z * math.cos(pitch)
        return -z

    def project(p):
        x, y, z = p[0] - 8, p[1] - 8, p[2] - 8
        x, z = x * math.cos(yaw) - z * math.sin(yaw), x * math.sin(yaw) + z * math.cos(yaw)
        y, z = y * math.cos(pitch) - z * math.sin(pitch), y * math.sin(pitch) + z * math.cos(pitch)
        return (size / 2 + x * scale, size / 2 - y * scale, z)

    quads = []
    for e in elements:
        r = e.get("rotation")
        for d, face in e.get("faces", {}).items():
            corners = face_corners(e["from"], e["to"], d)
            if r:
                corners = tuple(rot(c, r["axis"], r["angle"], r["origin"]) for c in corners)
            tl, tr, bl = (project(c) for c in corners)
            # Back-face culling: rotate the face normal like the element and keep faces pointing at the camera.
            n = NORMALS[d]
            if r:
                n = rot(n, r["axis"], r["angle"], (0, 0, 0))
            if camera_z(n) >= -1e-6:
                continue
            depth = (tl[2] + tr[2] + bl[2] + (tr[2] + bl[2] - tl[2])) / 4
            quads.append((depth, d, face, tl, tr, bl))
    quads.sort(key=lambda q: q[0])

    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    for depth, d, face, tl, tr, bl in quads:
        tex = texture(textures, face["texture"])
        tex = tex.resize((tex.width * 4, tex.height * 4), Image.NEAREST)
        k = tex.width / 16
        u0, v0, u1, v1 = face["uv"]
        cx0, cy0 = int(min(u0, u1) * k), int(min(v0, v1) * k)
        crop = tex.crop((cx0, cy0, max(cx0 + 1, int(max(u0, u1) * k)), max(cy0 + 1, int(max(v0, v1) * k))))
        if u1 < u0:
            crop = crop.transpose(Image.FLIP_LEFT_RIGHT)
        if v1 < v0:
            crop = crop.transpose(Image.FLIP_TOP_BOTTOM)
        crop = crop.rotate(-face.get("rotation", 0), expand=True)
        w, h = crop.size
        # Affine map from screen to texture: solve tl + a*(tr-tl)/w*u + ...
        ax, ay = (tr[0] - tl[0]) / w, (tr[1] - tl[1]) / w
        bx, by = (bl[0] - tl[0]) / h, (bl[1] - tl[1]) / h
        det = ax * by - ay * bx
        if abs(det) < 1e-9:
            continue
        ia, ib, ic, id_ = by / det, -bx / det, -ay / det, ax / det
        coeffs = (ia, ib, -(ia * tl[0] + ib * tl[1]), ic, id_, -(ic * tl[0] + id_ * tl[1]))
        shade = SHADE[d] if not face.get("emissive") else 1.0
        px = crop.load()
        shaded = Image.new("RGBA", crop.size)
        sp = shaded.load()
        for i in range(w):
            for j in range(h):
                r_, g_, b_, a_ = px[i, j]
                sp[i, j] = (int(r_ * shade), int(g_ * shade), int(b_ * shade), a_)
        layer = shaded.transform((size, size), Image.AFFINE, coeffs, resample=Image.NEAREST)
        mask = Image.new("L", (size, size), 0)
        br = (tr[0] + bl[0] - tl[0], tr[1] + bl[1] - tl[1])
        ImageDraw.Draw(mask).polygon([tl[:2], tr[:2], br, bl[:2]], fill=255)
        alpha = Image.composite(layer.getchannel("A"), Image.new("L", (size, size), 0), mask)
        layer.putalpha(alpha)
        canvas.alpha_composite(layer)
    canvas.save(out)


if __name__ == "__main__":
    model = sys.argv[1]
    out = sys.argv[2]
    facing = int(sys.argv[3]) if len(sys.argv) > 3 else 0
    textures, elements = resolve(model)
    for e in elements:
        for d, face in e.get("faces", {}).items():
            face.setdefault("uv", list(default_uv(e["from"], e["to"], d)))
    # Re-render with filled UVs: patch resolve to return these elements.
    _resolve = resolve
    resolve = lambda n: (textures, elements)  # noqa: E731
    render(model, out, facing)
