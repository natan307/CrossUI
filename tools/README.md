# tools

`gen_icons.py` rebuilds `crossui-core/src/main/resources/bedrock-icons.txt` (Java item id to Bedrock texture name).

```
git clone --depth 1 https://github.com/Mojang/bedrock-samples
python3 tools/gen_icons.py --bedrock bedrock-samples --java items.json --out bedrock-icons.txt
```

`items.json` is `data/pc/<version>/items.json` from PrismarineJS/minecraft-data. Unmapped items are listed in `<out>.missing.txt`.

`gen_shaped_icons.py` renders the transparent isometric pictures in `icons/` (needs Pillow):

```
python3 tools/gen_shaped_icons.py --bedrock bedrock-samples --table crossui-core/src/main/resources/bedrock-icons.txt --out icons
```

`gen_block_icons.py` (full blocks) and `gen_flat_icons.py` (items and the rest) fill `icons/` the same way. Run `gen_shaped_icons.py`, `gen_block_icons.py`, `gen_flat_icons.py` in that order.

`gen_head_icons.py` renders the mob head icons (zombie, skeleton, creeper, ...) from the Bedrock entity skins; it needs the `textures/entity` folder of bedrock-samples.

Run `optimize_icons.py icons` last: it shrinks every picture to 96x96 with 96 colours so the menus load faster.
