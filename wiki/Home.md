# CrossUI Wiki

CrossUI is a UI translation middleware and cross-platform API for Minecraft servers. It turns one UI description into a **Bedrock Form**, a **Java Native Dialog** or a **classic chest GUI**, depending on the player's client.

**For server admins:** drop the jar into `plugins/`. Chest menus of other plugins (shops, warps, crates, ...) are shown to Bedrock players as native forms, with no configuration.

**For plugin developers:** describe a menu, an input or a confirmation once and CrossUI shows the best UI for each player.

## Pages

- [Installation](Installation): requirements, setup and commands
- [Configuration](Configuration): every `config.yml` option
- [Developer API](Developer-API): builders, ServicesManager and safe soft-depend
- [DeluxeMenus Example](DeluxeMenus-Example): a ready-made test menu
- [Troubleshooting](Troubleshooting): common problems and fixes

## Quick facts

| | |
| --- | --- |
| Servers | Spigot, Paper, Purpur, Folia |
| Minecraft | 1.21.x up to 26.3 |
| Java | 21 or newer |
| Extra plugins needed | None (PacketEvents is bundled). Floodgate or Geyser-Spigot is optional for Bedrock players |
| License | Plugin: GPL-3.0. API: MIT |

Source code and issues: https://github.com/natan307/CrossUI
