#!/usr/bin/env python3
"""Render transparent 128x128 isometric icons for shaped blocks (slab, stairs, fence, ...).

  python3 tools/gen_shaped_icons.py --bedrock <bedrock-samples checkout> \
      --table crossui-core/src/main/resources/bedrock-icons.txt --out icons

Writes icons/<java_id>.png and crossui-core/src/main/resources/bedrock-shaped.txt (list of ids).
"""
import argparse, os, re
from PIL import Image

SUFFIX = re.compile(r"_(slab|stairs|fence_gate|fence|wall|pressure_plate|button|trapdoor)$")
A = 2.8          # px per unit (canvas 128, a cube is ~90 px wide)
YA = 3.0
SS = 4           # supersampling
SIZE = 128

SHAPES = {
    "slab": [(0, 0, 0, 16, 8, 16)],
    "stairs": [(0, 0, 0, 16, 8, 16), (0, 8, 0, 16, 16, 8)],
    "fence": [(0, 0, 6, 4, 16, 10), (12, 0, 6, 16, 16, 10), (4, 12, 7, 12, 15, 9), (4, 5, 7, 12, 8, 9)],
    "fence_gate": [(0, 0, 7, 3, 16, 9), (13, 0, 7, 16, 16, 9), (3, 6, 7, 13, 9, 9), (3, 11, 7, 13, 14, 9), (6, 9, 7, 10, 11, 9)],
    "wall": [(4, 0, 4, 12, 16, 12), (0, 0, 5, 4, 14, 11), (12, 0, 5, 16, 14, 11)],
    "pressure_plate": [(1, 0, 1, 15, 2, 15)],
    "button": [(4, 0, 5, 12, 4, 11)],
    "trapdoor": [(0, 0, 0, 16, 3, 16)],
}

def proj(x, y, z):
    return ((x - z) * A * SS, ((x + z) * A / 2 - y * YA) * SS)

def face(canvas, tex, pts, uv, shade):
    """pts: 3 screen points (origin, +u end, +v end); uv: (u0,v0,u1,v1) crop of the 16x16 texture."""
    u0, v0, u1, v1 = uv
    if u1 <= u0 or v1 <= v0:
        return
    t = tex.crop((u0, v0, u1, v1)).convert("RGBA")
    w, h = t.size
    (ox, oy), (ux, uy), (vx, vy) = pts
    # screen = o + s*(u-axis)+ t*(v-axis); invert to sample texture
    ax, ay = (ux - ox), (uy - oy)
    bx, by = (vx - ox), (vy - oy)
    det = ax * by - ay * bx
    if abs(det) < 1e-6:
        return
    # inverse affine: (s,t) = M^-1 (p - o); texture coords = (s*w, t*h)
    ia, ib = by / det, -bx / det
    ic, id_ = -ay / det, ax / det
    # PIL wants coefficients mapping output (x,y) -> input: (a x + b y + c, d x + e y + f)
    a_ = ia * w; b_ = ib * w; c_ = -(ia * ox + ib * oy) * w
    d_ = ic * h; e_ = id_ * h; f_ = -(ic * ox + id_ * oy) * h
    layer = t.transform(canvas.size, Image.AFFINE, (a_, b_, c_, d_, e_, f_), Image.NEAREST)
    # mask: only the parallelogram area
    mask = Image.new("L", canvas.size, 0)
    from PIL import ImageDraw
    ImageDraw.Draw(mask).polygon([(ox, oy), (ox + ax, oy + ay), (ox + ax + bx, oy + ay + by), (ox + bx, oy + by)], fill=255)
    r, g, b, al = layer.split()
    r = r.point(lambda v: int(v * shade)); g = g.point(lambda v: int(v * shade)); b = b.point(lambda v: int(v * shade))
    from PIL import ImageChops
    al = ImageChops.multiply(al, mask)
    canvas.alpha_composite(Image.merge("RGBA", (r, g, b, al)))

def render(tex, boxes, top=None, right=None):
    top = top or tex
    right = right or tex
    canvas = Image.new("RGBA", (SIZE * SS, SIZE * SS), (0, 0, 0, 0))
    ox, oy = SIZE * SS / 2, SIZE * SS * 0.5
    # centre the full cube
    cy = ((0 + 16) * A / 2 - 8 * YA) * SS  # middle of cube on screen
    def P(x, y, z):
        px, py = proj(x, y, z)
        return (px + ox, py + oy - cy)
    for (x0, y0, z0, x1, y1, z1) in sorted(boxes, key=lambda b: (b[0] + b[3] + b[2] + b[5] + b[1] + b[4])):
        # top (y1): u along +x, v along +z
        face(canvas, top, (P(x0, y1, z0), P(x1, y1, z0), P(x0, y1, z1)), (x0, z0, x1, z1), 1.0)
        # front-left (+z side at z1): u along +x, v downwards
        face(canvas, tex, (P(x0, y1, z1), P(x1, y1, z1), P(x0, y0, z1)), (x0, 16 - y1, x1, 16 - y0), 0.8)
        # front-right (+x side at x1): u runs from z1 towards z0, v downwards
        face(canvas, right, (P(x1, y1, z1), P(x1, y1, z0), P(x1, y0, z1)), (16 - z1, 16 - y1, 16 - z0, 16 - y0), 0.6)
    return canvas.resize((SIZE, SIZE), Image.BOX)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--bedrock", required=True)
    ap.add_argument("--table", required=True)
    ap.add_argument("--out", required=True)
    ap.add_argument("--list", default="crossui-core/src/main/resources/bedrock-shaped.txt")
    a = ap.parse_args()
    os.makedirs(a.out, exist_ok=True)
    done = []
    for line in open(a.table, encoding="utf-8"):
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        m = SUFFIX.search(k)
        if not m or not v.startswith("blocks/"):
            continue
        path = os.path.join(a.bedrock, "resource_pack", "textures", v + ".png")
        if not os.path.exists(path):
            print("missing texture", k, v)
            continue
        tex = Image.open(path).convert("RGBA")
        if tex.size[0] != tex.size[1]:
            tex = tex.crop((0, 0, tex.size[0], tex.size[0]))
        tex = tex.resize((16, 16), Image.NEAREST) if tex.size[0] != 16 else tex
        # opaque base for translucent/odd textures is not needed for these blocks
        render(tex, SHAPES[m.group(1)]).save(os.path.join(a.out, k + ".png"), optimize=True)
        done.append(k)
    done.sort()
    open(a.list, "w", encoding="utf-8").write("\n".join(done) + "\n")
    print("rendered", len(done))

if __name__ == "__main__":
    main()
