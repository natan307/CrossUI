# Configuration

`plugins/CrossUI/config.yml`. Run `/crossui reload` after editing.

| Key | Default | Description |
| --- | --- | --- |
| `debug` | `false` | Extra console output about intercepted menus. |
| `bedrock.convert-chest-guis` | `true` | Convert chest GUIs of other plugins into Bedrock forms. |
| `bedrock.title-blacklist` | `Roulette`, `Spinning`, `Crate Opening` | Menus whose title matches are never converted. Prefix with `regex:` for a regular expression. |
| `bedrock.whitelist-mode` / `title-whitelist` | `false` / empty | Convert only menus that match the whitelist. |
| `bedrock.decoration-materials` | glass panes, iron bars, ... | Blank-named items of these materials are treated as decoration. `*` is a wildcard. |
| `bedrock.include-unnamed-items` | `false` | Turn items without a custom name into buttons. |
| `bedrock.show-lore-in-button` | `true` | Show the first lore line under the button text. |
| `bedrock.dual-click.*` | left/right keywords | Lore keywords that mark two different click actions. |
| `bedrock.refresh-delay-ticks` | `4` | Delay before the form is rebuilt after a click. |
| `java.native-dialogs` | `true` | Use native dialogs for Java 1.21.6+ clients. |
| `messages.*` | | All player-facing texts. |
