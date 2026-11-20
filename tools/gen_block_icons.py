#!/usr/bin/env python3
"""Render transparent isometric cube icons for full blocks (planks, stone, logs, ores, ...).

  python3 tools/gen_block_icons.py --bedrock <bedrock-samples checkout> \
      --table crossui-core/src/main/resources/bedrock-icons.txt --out icons

Adds icons/<java_id>.png and appends the ids to crossui-core/src/main/resources/bedrock-shaped.txt.
Only blocks whose texture is fully opaque (or leaves) are rendered; plants, doors, glass panes,
chests and other non-cube blocks keep the picture from the Bedrock client.
"""
import argparse, importlib.util, os, re, sys
from PIL import Image

here = os.path.dirname(os.path.abspath(__file__))
spec = importlib.util.spec_from_file_location("shaped", os.path.join(here, "gen_shaped_icons.py"))
shaped = importlib.util.module_from_spec(spec); spec.loader.exec_module(shaped)

SKIP = re.compile(r"(door|bed|chest|sign|banner|head|skull|rail|torch|lantern|anvil|cauldron|hopper|campfire|enchant|brewing|lectern|bell|"
                  r"sapling|flower|tulip|fern|grass$|vine|pane|glass|lever|button|plate|slab|stairs|fence|wall|trapdoor|carpet|candle|"
                  r"coral|kelp|seagrass|lily|mushroom$|roots|sprout|bamboo$|scaffolding|ladder|chain|cake|pot|frame|conduit|beacon|"
                  r"spawner|portal|end_rod|lightning|amethyst_cluster|bud|cluster|pickle|egg|dripstone|cobweb|web|dust|powder$|redstone$|tripwire|comparator|repeater)")
TINT = [(re.compile(r"grass_block"), (0x7C, 0xBD, 0x6B)), (re.compile(r"leaves|azalea_leaves"), (0x59, 0xAE, 0x30)),
        (re.compile(r"^(grass_block|moss)"), None)]

def tex_path(bed, name):
    return os.path.join(bed, "resource_pack", "textures", "blocks", name + ".png")

def load(bed, name):
    p = tex_path(bed, name)
    if not os.path.exists(p):
        return None
    im = Image.open(p).convert("RGBA")
    if im.size[0] != im.size[1]:
        im = im.crop((0, 0, im.size[0], im.size[0]))
    return im.resize((16, 16), Image.NEAREST) if im.size[0] != 16 else im

def tint(im, rgb):
    r, g, b, a = im.split()
    r = r.point(lambda v: v * rgb[0] // 255); g = g.point(lambda v: v * rgb[1] // 255); b = b.point(lambda v: v * rgb[2] // 255)
    return Image.merge("RGBA", (r, g, b, a))

def find_top(bed, tname):
    base = tname.replace("_carried", "")
    for c in (base.replace("_side", "_top"), base + "_top", re.sub(r"_side.*$", "_top", base), base.replace("_side", "")):
        if c != tname and load(bed, c) is not None:
            return c
    return None

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
        if k in existing or not v.startswith("blocks/") or SKIP.search(k):
            continue
        tname = v[len("blocks/"):]
        side = load(a.bedrock, tname)
        if side is None:
            continue
        leaves = "leaves" in k
        if not leaves and min(side.getchannel("A").getextrema()) < 255:
            continue
        topn = find_top(a.bedrock, tname)
        top = load(a.bedrock, topn) if topn else side
        if k == "grass_block":
            side = load(a.bedrock, "grass_side_carried") or side
            top = tint(load(a.bedrock, "grass_carried") or load(a.bedrock, "grass_top") or top, (0x7C, 0xBD, 0x6B))
        elif leaves:
            side = tint(side, (0x59, 0xAE, 0x30)); top = side
            side.putalpha(255)
        elif "grass_top" in (topn or "") or tname.startswith("grass"):
            top = tint(top, (0x7C, 0xBD, 0x6B))
        shaped.render(side, [(0, 0, 0, 16, 16, 16)], top).save(os.path.join(a.out, k + ".png"), optimize=True)
        done.append(k)
    allids = sorted(existing | set(done))
    open(a.list, "w", encoding="utf-8").write("\n".join(allids) + "\n")
    print("rendered", len(done), "total", len(allids))
    if a.sheet:
        import random
        random.seed(1); pick = random.sample(done, min(48, len(done)))
        sh = Image.new("RGBA", (128 * 8, 128 * 6), (60, 60, 60, 255))
        for i, n in enumerate(pick):
            sh.alpha_composite(Image.open(os.path.join(a.out, n + ".png")), ((i % 8) * 128, (i // 8) * 128))
        sh.save(a.sheet); print(pick)

if __name__ == "__main__":
    main()
