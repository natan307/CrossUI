# Installation

**Supported servers:** Spigot, Paper, Purpur, Folia (Minecraft 1.21.x up to 26.3). Java 21 or newer.

## Steps

1. Put `CrossUI.jar` in the `plugins/` folder and restart the server. No other plugin is required, because PacketEvents is bundled.
2. For Bedrock players install **Floodgate** (recommended) or **Geyser-Spigot**. Geyser without Floodgate is also supported.
3. Check the startup log. It lists the detected Bedrock bridge and whether native dialogs are available:

   ```
   [CrossUI] Enabled on Paper | Bedrock bridge: Floodgate | Native dialogs: available
   ```

4. Join and run `/crossui testgui` to see the best UI for your client.

Native Java dialogs need a server on **1.21.6 or newer**. On older servers, Java players get the legacy chest renderer.

`/version CrossUI` shows the plugin version and a link to this wiki.

## Optional plugins

| Plugin | Purpose |
| --- | --- |
| Floodgate | Detects Bedrock players and shows forms (recommended) |
| Geyser-Spigot | Fallback for servers that only have Geyser |
| ViaVersion | Lets older Java clients join; they get the legacy renderer |

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/crossui` | `crossui.admin` | Shows usage and the wiki link |
| `/crossui reload` | `crossui.admin` | Reloads `config.yml` |
| `/crossui testgui` | `crossui.admin` | Opens a test menu using the best UI for your client |

Alias: `/cui`. The `crossui.admin` permission defaults to operators.

## Updating

Replace the jar and restart. Your existing `config.yml` is never overwritten, so new options are not added to it automatically. To get the latest defaults, rename the old file and let CrossUI generate a new one, then copy your changes over.
