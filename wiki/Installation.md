# Installation

**Supported servers:** Spigot, Paper, Purpur, Folia (Minecraft 1.21.x - 26.3). Java 21 or newer.

1. Put `CrossUI.jar` in `plugins/` and restart. No other plugin is required (PacketEvents is bundled).
2. For Bedrock players install Floodgate (recommended) or Geyser-Spigot. Geyser without Floodgate is also supported.

Check that it works with `/crossui testgui`. The startup log lists the detected Bedrock bridge and whether native dialogs are available (they need server version 1.21.6 or newer).

`/version CrossUI` shows the plugin version and a link to this wiki.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/crossui reload` | `crossui.admin` | Reloads `config.yml`. |
| `/crossui testgui` | `crossui.admin` | Opens a test menu using the best UI for your client. |
