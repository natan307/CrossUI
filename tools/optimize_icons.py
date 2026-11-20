#!/usr/bin/env python3
"""Shrink the pictures in icons/ (96x96, 96-colour PNG). Small files load much faster in Bedrock forms.

  python3 tools/optimize_icons.py icons
"""
import glob, os, sys
from PIL import Image

folder = sys.argv[1] if len(sys.argv) > 1 else "icons"
before = after = 0
for f in sorted(glob.glob(os.path.join(folder, "*.png"))):
    before += os.path.getsize(f)
    im = Image.open(f).convert("RGBA")
    if im.size != (96, 96):
        im = im.resize((96, 96), Image.BOX)
    im.quantize(colors=96, method=Image.Quantize.FASTOCTREE, dither=Image.Dither.NONE).save(f, "PNG", optimize=True)
    after += os.path.getsize(f)
print(f"{before // 1024} KB -> {after // 1024} KB")
