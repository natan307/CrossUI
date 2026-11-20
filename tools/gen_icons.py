#!/usr/bin/env python3
"""Builds bedrock-icons.json: Java item id -> vanilla Bedrock texture path.

Inputs (read only, never copied into the project):
  --bedrock  checkout of Mojang/bedrock-samples (resource_pack/ part)
  --java     items.json of PrismarineJS/minecraft-data for the target Java version
Only the *names* of textures are written; no image data is shipped.
"""
import argparse, json, os, re, sys

def load_json(path):
    with open(path, encoding="utf-8") as f:
        return json.loads(re.sub(r"(?m)^\s*//.*$", "", f.read()))

# Java id -> candidate Bedrock names, for ids that differ between the editions.
ALIASES = {
    "grass_block": ["grass"], "dirt_path": ["grass_path"], "cobweb": ["web"], "spawner": ["mob_spawner"],
    "lily_pad": ["waterlily"], "bricks": ["brick_block", "brick"], "terracotta": ["hardened_clay"],
    "nether_bricks": ["nether_brick"], "red_nether_bricks": ["red_nether_brick"],
    "end_stone_bricks": ["end_bricks"], "snow_block": ["snow"], "jack_o_lantern": ["lit_pumpkin"],
    "iron_chain": ["chain"], "sugar_cane": ["reeds"], "redstone": ["redstone_dust"],
    "lapis_lazuli": ["dye_powder_blue"], "wheat_seeds": ["seeds_wheat"], "pumpkin_seeds": ["seeds_pumpkin"],
    "melon_seeds": ["seeds_melon"], "beetroot_seeds": ["seeds_beetroot"], "turtle_scute": ["turtle_shell_piece"],
    "slime_block": ["slime"], "magma_block": ["magma"], "rooted_dirt": ["dirt_with_roots"],
    "nether_quartz_ore": ["quartz_ore"], "dead_bush": ["deadbush"], "note_block": ["noteblock"],
    "powered_rail": ["golden_rail"], "minecart": ["minecart_normal"], "chest_minecart": ["minecart_chest"],
    "furnace_minecart": ["minecart_furnace"], "tnt_minecart": ["minecart_tnt"], "hopper_minecart": ["minecart_hopper"],
    "bow": ["bow_standby"], "crossbow": ["crossbow_standby"], "oak_door": ["wooden_door"],
    "oak_boat": ["boat"], "oak_trapdoor": ["trapdoor"], "oak_button": ["planks_oak"],
    "oak_pressure_plate": ["planks_oak"], "oak_fence_gate": ["planks_oak"], "light": ["light_block_15"],
}
ALIASES.update({
    "porkchop": ["porkchop_raw"], "cooked_porkchop": ["porkchop_cooked"], "beef": ["beef_raw"],
    "cooked_beef": ["beef_cooked"], "chicken": ["chicken_raw"], "cooked_chicken": ["chicken_cooked"],
    "mutton": ["mutton_raw"], "cooked_mutton": ["mutton_cooked"], "rabbit": ["rabbit_raw"],
    "cooked_rabbit": ["rabbit_cooked"], "cod": ["fish_raw"], "cooked_cod": ["fish_cooked"],
    "salmon": ["fish_salmon_raw"], "cooked_salmon": ["fish_salmon_cooked"], "pufferfish": ["fish_pufferfish_raw"],
    "tropical_fish": ["fish_clownfish_raw"], "golden_apple": ["apple_golden"], "enchanted_golden_apple": ["apple_golden"],
    "golden_carrot": ["carrot_golden"], "book": ["book_normal"], "writable_book": ["book_writable"],
    "written_book": ["book_written"], "enchanted_book": ["book_enchanted"], "knowledge_book": ["book_enchanted"],
    "compass": ["compass_item"], "clock": ["clock_item"], "slime_ball": ["slimeball"], "melon_slice": ["melon"],
    "glistering_melon_slice": ["melon_speckled"], "fire_charge": ["fireball"], "firework_rocket": ["fireworks"],
    "firework_star": ["fireworks_charge"], "map": ["map_empty"], "filled_map": ["map_filled"],
    "glass_bottle": ["potion_bottle_empty"], "potion": ["potion_bottle_drinkable"],
    "splash_potion": ["potion_bottle_splash"], "lingering_potion": ["potion_bottle_lingering"],
    "totem_of_undying": ["totem"], "command_block_minecart": ["minecart_command_block"],
    "ink_sac": ["dye_powder_black"], "glow_ink_sac": ["dye_powder_glow"], "bone_meal": ["dye_powder_white"],
    "cocoa_beans": ["dye_powder_brown"], "water_bucket": ["bucket_water"], "lava_bucket": ["bucket_lava"],
    "milk_bucket": ["bucket_milk"], "powder_snow_bucket": ["bucket_powder_snow"], "cod_bucket": ["bucket_cod"],
    "salmon_bucket": ["bucket_salmon"], "pufferfish_bucket": ["bucket_pufferfish"],
    "tropical_fish_bucket": ["bucket_tropical"], "axolotl_bucket": ["bucket_axolotl"], "tadpole_bucket": ["bucket_tadpole"],
    "crimson_planks": ["crimson_planks"], "warped_planks": ["warped_planks"], "popped_chorus_fruit": ["chorus_fruit_popped"],
    "poisonous_potato": ["potato_poisonous"], "baked_potato": ["potato_baked"], "spectral_arrow": ["spectral_arrow"],
    "debug_stick": ["stick"], "shield": ["shield"], "flowering_azalea_leaves": ["azalea_leaves_flowered"],
    "small_dripleaf": ["small_dripleaf_top"], "cobblestone_stairs": ["cobblestone"],
    "cobbled_deepslate_stairs": ["cobbled_deepslate"], "end_stone_brick_stairs": ["end_bricks"],
})
EXCLUDE = {"player_head", "test_block", "test_instance_block"}
PREFIX_RULES = [  # (java prefix, bedrock prefix)
    ("wooden_", "wood_"), ("golden_", "gold_"), ("stone_", "stone_"), ("copper_", "copper_"),
]

def block_texture(bid, blocks, terrain):
    entry = blocks.get(bid)
    if not entry:
        return None
    tex = entry.get("textures")
    if isinstance(tex, dict):
        tex = tex.get("side") or tex.get("up") or next(iter(tex.values()), None)
    if not isinstance(tex, str):
        return None
    t = terrain.get(tex)
    if not t:
        return None
    path = t.get("textures")
    if isinstance(path, list):
        path = path[0] if path else None
    if isinstance(path, dict):
        path = path.get("path")
    return path if isinstance(path, str) else None

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--bedrock", required=True)
    ap.add_argument("--java", required=True)
    ap.add_argument("--out", required=True)
    a = ap.parse_args()
    rp = os.path.join(a.bedrock, "resource_pack")
    blocks = load_json(os.path.join(rp, "blocks.json"))
    terrain = load_json(os.path.join(rp, "textures/terrain_texture.json"))["texture_data"]
    itemtex = load_json(os.path.join(rp, "textures/item_texture.json"))["texture_data"]
    have = set()
    for folder in ("items", "blocks"):
        base = os.path.join(rp, "textures", folder)
        for root, _dirs, files in os.walk(base):
            for f in files:
                if f.endswith((".png", ".tga")):
                    rel = os.path.relpath(os.path.join(root, f.rsplit(".", 1)[0]), os.path.join(rp, "textures"))
                    have.add("textures/" + rel.replace(os.sep, "/"))

    def terrain_path(name):
        t = terrain.get(name)
        if not t:
            return None
        p = t.get("textures")
        if isinstance(p, list):
            p = p[0] if p else None
        if isinstance(p, dict):
            p = p.get("path")
        return p if isinstance(p, str) and p in have else None

    def item_path(name):
        t = itemtex.get(name)
        if t:
            p = t.get("textures")
            if isinstance(p, list):
                p = p[0] if p else None
            if isinstance(p, str) and p in have:
                return p
        for folder in ("items", "blocks"):
            p = f"textures/{folder}/{name}"
            if p in have:
                return p
        return None

    def candidates(n):
        out = [n]
        out += ALIASES.get(n, [])
        for jp, bp in PREFIX_RULES:
            if n.startswith(jp):
                out.append(bp + n[len(jp):])
                out.append(n[len(jp):] + "_" + bp.strip("_"))
        m = re.match(r"^(.*)_(spawn_egg)$", n)
        if m:
            out += ["spawn_egg_" + m.group(1), "spawn_egg"]
        m = re.match(r"^(white|orange|magenta|light_blue|yellow|lime|pink|gray|light_gray|cyan|purple|blue|brown|green|red|black)_(harness|dye|bed|banner|carpet|wool|concrete|concrete_powder|stained_glass|stained_glass_pane|glazed_terracotta|terracotta|shulker_box|candle)$", n)
        if m:
            c, k = m.groups()
            out += [f"{k}_{c}", f"{c}_{k}"]
        m = re.match(r"^(.*)_(boat|chest_boat)$", n)
        if m:
            out += [f"boat_{m.group(1)}", f"{m.group(2)}"]
        m = re.match(r"^(.*)_(door)$", n)
        if m:
            out += [n, f"door_{m.group(1)}", f"{m.group(1)}_door"]
        # slabs, stairs, fences, ... look like the block they are made of
        m = re.match(r"^(.*?)_(slab|stairs|fence|fence_gate|wall|button|pressure_plate|sign|hanging_sign|trapdoor)$", n)
        if m:
            base = m.group(1)
            out += [base, base + "_planks", base + "_block", base + "s", base + "_bricks"]
            if base in ("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "crimson", "warped", "mangrove", "cherry", "bamboo", "pale_oak"):
                out += [base + "_planks"]
            if base.startswith(("cut_", "smooth_", "polished_", "chiseled_")):
                out += [base]
        m = re.match(r"^(stripped_)?(.*)_(stem|hyphae|log|wood)$", n)
        if m:
            pre, tree, kind = m.groups()
            out += [f"{pre or ''}{tree}_stem", f"{pre or ''}{tree}_log", f"stripped_{tree}_log" if pre else f"{tree}_log"]
        m = re.match(r"^(raw_|waxed_|exposed_|weathered_|oxidized_)+(.*)$", n)
        if m:
            out += [m.group(2), re.sub(r"^(waxed_)", "", n)]
        if n.startswith("deepslate_"):
            out += [n.replace("deepslate_", "deepslate_", 1)]
        m = re.match(r"^(.*)_banner$", n)
        if m:
            c = m.group(1)
            out += ["wool_colored_" + ("silver" if c == "light_gray" else c)]
        out = out + [x.replace("light_gray", "silver") for x in out if "light_gray" in x]
        m = re.match(r"^music_disc_(.*)$", n)
        if m:
            out += ["record_" + m.group(1), n]
        m = re.match(r"^(white|orange|magenta|light_blue|yellow|lime|pink|gray|light_gray|cyan|purple|blue|brown|green|red|black)_(dye|bundle|banner)$", n)
        if m:
            c, k = m.groups()
            c2 = "silver" if c == "light_gray" else c
            if k == "dye":
                out += [f"dye_powder_{c2}", f"dye_powder_{c2}_new"]
            elif k == "bundle":
                out += [f"bundle_{c}"]
        m = re.match(r"^(crimson|warped)_(sign)$", n)
        if m:
            out += [f"{m.group(1)}_sign_item", f"sign_{m.group(1)}"]
        m = re.match(r"^(.*)_hanging_sign$", n)
        if m:
            out += [n]
        m = re.match(r"^(cobbled_|polished_)?(deepslate)(_bricks|_tiles)?(_(slab|stairs|wall))?$", n)
        if m:
            pre, d, kind, _, _ = m.groups()
            out += [f"{pre or ''}{d}{kind or ''}", "deepslate"]
        m = re.match(r"^(.*)_(spear)$", n)
        if m:
            w = {"wooden": "wood", "golden": "gold"}.get(m.group(1), m.group(1))
            out += [f"{w}_spear"]
        m = re.match(r"^deepslate_(.*)_ore$", n)
        if m:
            out += [n]
        return out

    java = [i["name"] for i in json.load(open(a.java, encoding="utf-8"))]
    mapping, missing = {}, []
    for n in java:
        if n == "air" or n in EXCLUDE or n.endswith(("_head", "_skull")):
            missing.append(n)
            continue
        path = None
        for c in candidates(n):
            if c in blocks:
                p = block_texture(c, blocks, terrain)
                if p and p in have:
                    path = p
                    break
            p = terrain_path(c) or item_path(c)
            if p:
                path = p
                break
        if path and path + "_carried" in have:
            path += "_carried"  # untinted inventory version (grass, leaves, ...)
        if path:
            mapping[n] = path
        else:
            missing.append(n)

    with open(a.out, "w", encoding="utf-8", newline="\n") as f:
        f.write("# Generated by tools/gen_icons.py. Java item id = Bedrock texture path (without the textures/ prefix).\n")
        for k, v in sorted(mapping.items()):
            f.write(f"{k}={v[len('textures/'):]}\n")
    print(f"mapped {len(mapping)} / {len(java)}; missing {len(missing)}", file=sys.stderr)
    with open(a.out + ".missing.txt", "w") as f:
        f.write("\n".join(missing))

main()
