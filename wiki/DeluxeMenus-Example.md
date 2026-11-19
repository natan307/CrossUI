# DeluxeMenus Example

A ready-made menu for testing CrossUI with [DeluxeMenus](https://github.com/HelpChat/DeluxeMenus). Java players see the normal chest menu, and Bedrock players (through Geyser) see it as a form.

## Setup

1. Save the file below as `plugins/DeluxeMenus/gui_menus/crossui_demo.yml`.
2. In `plugins/DeluxeMenus/config.yml`, register it:

   ```yaml
   gui_menus:
     crossui_demo:
       file: crossui_demo.yml
   ```

3. Run `/dm reload`, then `/crossuidemo`.

## What it shows

- Glass panes are removed and never become form buttons.
- Item names are readable, for example "Grass Block" instead of a translation key.
- The first lore line is shown under the button name.
- The Diamond item has both a left-click and a right-click line in its lore, so Bedrock players are asked which action to run.

## Menu file

```yaml
# Example DeluxeMenus menu for testing CrossUI.
# Open it with /crossuidemo. Java players see the normal chest menu,
# Bedrock players (through Geyser) see it as a form with item pictures.

menu_title: '&8Example Shop'
open_command: crossuidemo
size: 27

items:
  # Decoration: CrossUI removes these panes, so they never become form buttons.
  'filler':
    material: GRAY_STAINED_GLASS_PANE
    display_name: ' '
    slots:
      - 0
      - 1
      - 2
      - 6
      - 7
      - 8
      - 9
      - 17
      - 18
      - 19
      - 20
      - 24
      - 25
      - 26

  # Normal button: name, first lore line shown under the name, one action.
  'grass':
    material: GRASS_BLOCK
    slot: 3
    display_name: '&aGrass Block'
    lore:
      - '&7Buy Price: &e$10'
    left_click_commands:
      - '[message] &aYou bought 1 Grass Block.'
      - '[close]'

  'soul_sand':
    material: SOUL_SAND
    slot: 4
    display_name: '&6Soul Sand'
    lore:
      - '&7Buy Price: &e$25'
    left_click_commands:
      - '[message] &aYou bought 1 Soul Sand.'
      - '[close]'

  'blue_ice':
    material: BLUE_ICE
    slot: 5
    display_name: '&bBlue Ice'
    lore:
      - '&7Buy Price: &e$40'
    left_click_commands:
      - '[message] &aYou bought 1 Blue Ice.'
      - '[close]'

  # Dual-click button: the lore mentions left-click and right-click,
  # so Bedrock players are asked which action to run.
  'diamond':
    material: DIAMOND
    slot: 12
    display_name: '&bDiamond'
    lore:
      - '&7Buy Price: &e$100'
      - '&eLeft-click &7to buy 1'
      - '&eRight-click &7to buy 16'
    left_click_commands:
      - '[message] &aYou bought 1 Diamond.'
      - '[close]'
    right_click_commands:
      - '[message] &aYou bought 16 Diamonds.'
      - '[close]'

  # Custom name and lore do not change the picture: it follows the item type.
  'sword':
    material: DIAMOND_SWORD
    slot: 13
    display_name: '&c&lBlade of Testing'
    lore:
      - '&7A custom named item.'
    left_click_commands:
      - '[message] &aYou received the Blade of Testing.'
      - '[close]'

  'apple':
    material: GOLDEN_APPLE
    slot: 14
    display_name: '&6Golden Apple'
    lore:
      - '&7Buy Price: &e$60'
    left_click_commands:
      - '[message] &aYou bought 1 Golden Apple.'
      - '[close]'

  'close':
    material: BARRIER
    slot: 22
    display_name: '&cClose'
    left_click_commands:
      - '[close]'
```
