#!/usr/bin/env python3
"""Render transparent 128x128 flat icons (texture scaled up, with margin) for items and non-cube blocks.

  python3 tools/gen_flat_icons.py --bedrock <bedrock-samples checkout> \
      --table crossui-core/src/main/resources/bedrock-icons.txt --out icons

Run after gen_shaped_icons.py and gen_block_icons.py; ids that already have a picture are skipped.
"""
import argparse, os, random
from PIL import Image

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--bedrock", required=True); ap.add_argument("--table", required=True); ap.add_argument("--out", required=True)
    ap.add_argument("--list", default="crossui-core/src/main/resources/bedrock-shaped.txt")
    ap.add_argument("--sheet", default=None)
    a = ap.parse_args()
    os.makedirs(a.out, exist_ok=True)
    existing = set(open(a.list).read().split()) if os.path.exists(a.list) else set()
    done = []
    for line in open(a.table, encoding="utf-8"):
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        k, v = line.split("=", 1)
        if k in existing:
            continue
        p = os.path.join(a.bedrock, "resource_pack", "textures", v + ".png")
        if not os.path.exists(p):
            continue
        im = Image.open(p).convert("RGBA")
        if im.size[0] != im.size[1]:
            im = im.crop((0, 0, im.size[0], im.size[0]))
        im = im.resize((96, 96), Image.NEAREST)
        out = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
        out.alpha_composite(im, (16, 16))
        out.save(os.path.join(a.out, k + ".png"), optimize=True)
        done.append(k)
    allids = sorted(existing | set(done))
    open(a.list, "w", encoding="utf-8").write("\n".join(allids) + "\n")
    print("rendered", len(done), "total", len(allids))
    if a.sheet:
        random.seed(2); pick = random.sample(done, min(48, len(done)))
        sh = Image.new("RGBA", (128 * 8, 128 * 6), (60, 60, 60, 255))
        for i, n in enumerate(pick):
            sh.alpha_composite(Image.open(os.path.join(a.out, n + ".png")), ((i % 8) * 128, (i // 8) * 128))
        sh.save(a.sheet)


if __name__ == "__main__":
    main()
