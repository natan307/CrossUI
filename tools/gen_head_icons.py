#!/usr/bin/env python3
"""Render isometric mob head icons (zombie, skeleton, creeper, ...) from the Bedrock entity skins.

  python3 tools/gen_head_icons.py --bedrock <bedrock-samples checkout> --out icons
"""
import argparse, importlib.util, os
from PIL import Image

here = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("shaped", os.path.join(here, "gen_shaped_icons.py"))
shaped = importlib.util.module_from_spec(spec); spec.loader.exec_module(shaped)

HEADS = {
    "zombie_head": "zombie/zombie.png",
    "skeleton_skull": "skeleton/skeleton.png",
    "wither_skeleton_skull": "skeleton/wither_skeleton.png",
    "creeper_head": "creeper/creeper.png",
    "piglin_head": "piglin/piglin.png",
    "player_head": "steve.png",
}

def face(skin, box):
    s = skin.size[0] // 64  # skins are 64 wide; scale for hi-res packs
    f = skin.crop(tuple(v * s for v in box)).resize((16, 16), Image.NEAREST)
    return f

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--bedrock", required=True); ap.add_argument("--out", required=True)
    ap.add_argument("--list", default="crossui-core/src/main/resources/bedrock-shaped.txt")
    a = ap.parse_args()
    os.makedirs(a.out, exist_ok=True)
    ids = set(open(a.list).read().split()) if os.path.exists(a.list) else set()
    for name, rel in HEADS.items():
        path = os.path.join(a.bedrock, "resource_pack", "textures", "entity", rel)
        if not os.path.exists(path):
            print("missing", path); continue
        skin = Image.open(path).convert("RGBA")
        top, front, left = face(skin, (8, 0, 16, 8)), face(skin, (8, 8, 16, 16)), face(skin, (16, 8, 24, 16))
        for f in (top, front, left):
            f.putalpha(255)
        shaped.render(front, [(0, 0, 0, 16, 16, 16)], top, left).save(os.path.join(a.out, name + ".png"), optimize=True)
        ids.add(name)
    open(a.list, "w", encoding="utf-8").write("\n".join(sorted(ids)) + "\n")
    print("done", len(ids))

if __name__ == "__main__":
    main()
