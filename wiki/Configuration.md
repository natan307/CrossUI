# Configuration

File: `plugins/CrossUI/config.yml`. Run `/crossui reload` after editing.

## Options

| Key | Default | Description |
| --- | --- | --- |
| `debug` | `false` | Prints extra console output about intercepted menus. |
| `bedrock.convert-chest-guis` | `true` | Converts chest GUIs of other plugins into Bedrock forms. |
| `bedrock.title-blacklist` | `Roulette`, `Spinning`, `Crate Opening` | Menus whose title matches are never converted. Plain entries are a case-insensitive "contains" match. Prefix with `regex:` for a regular expression. |
| `bedrock.whitelist-mode` | `false` | Converts only menus that match `title-whitelist`. |
| `bedrock.title-whitelist` | `[]` | Titles to convert when whitelist mode is on. |
| `bedrock.decoration-materials` | glass panes, iron bars, chain, barrier | Items of these materials with a blank name count as decoration and are removed. `*` is a wildcard. |
| `bedrock.include-unnamed-items` | `true` | Turns items without a custom name into buttons, using the material name. |
| `bedrock.show-lore-in-button` | `true` | Shows the first lore line under the button text. |
| `bedrock.lore-max-length` | `40` | Maximum length of that lore line. |
| `bedrock.keep-colors` | `false` | Keeps color codes in button text. RGB colors are reduced to the nearest basic color. |
| `bedrock.form-content` | `""` | Text shown above the buttons of a converted menu. |
| `bedrock.dual-click.left-keywords` | `left-click`, `left click`, `lmb` | Lore keywords that mark a left-click action. |
| `bedrock.dual-click.right-keywords` | `right-click`, `right click`, `rmb` | Lore keywords that mark a right-click action. When both are present on one item, the player is asked which action to run. |
| `bedrock.refresh-delay-ticks` | `4` | Delay before the form is rebuilt after a click. |
| `bedrock.fallback-to-chest-when-empty` | `true` | Shows the menu normally when it has no usable buttons. |
| `java.native-dialogs` | `true` | Uses native dialogs for Java 1.21.6+ clients. |
| `messages.*` | | Every player-facing text, including the `/crossui testgui` strings. |

## Tips

- Put fast or animated menus (crates, roulettes, loading screens) in `title-blacklist`. Converting a menu that changes many times per second makes the form flicker.
- Set `include-unnamed-items` to `false` only if you want plain vanilla items without a custom name to be hidden from converted menus.
- Use `debug: true` together with `/crossui reload` when you need to see why a menu was or was not converted.
