# Troubleshooting

| Problem | Solution |
| --- | --- |
| Bedrock players still see chest menus | Check the startup log for `Bedrock bridge: none`. Install Floodgate or Geyser-Spigot. Make sure `bedrock.convert-chest-guis` is `true` and the menu title is not in `title-blacklist`. |
| A converted menu flickers or reopens constantly | Add its title to `bedrock.title-blacklist`. |
| Buttons have no picture, or the pictures load slowly | The pictures are loaded from `bedrock.icon-pack-url`. Check that the `icons/` folder is reachable, for example open `https://raw.githubusercontent.com/natan307/CrossUI/main/icons/oak_slab.png` in a browser. Try the jsDelivr address from the [Configuration](Configuration) page, or set `bedrock.icon-pack-url: ""` for instant plain textures. An existing `config.yml` is not updated, so delete it or add the new keys by hand. |
| A player head shows no skin | Check `bedrock.head-url-template` and that the address returns a picture of the head. Heads without a skin show the plain head picture. |
| Items are missing from a converted menu | Make sure `bedrock.include-unnamed-items` is `true`. An existing `config.yml` is not overwritten on update, so set it by hand or delete the file to regenerate it, then run `/crossui reload`. |
| Button text shows a raw key such as `block.minecraft.stone` | Update to 1.1.0 or newer, which turns translation keys into readable names. |
| A menu appears empty for Bedrock | The menu has no usable buttons. Leave `bedrock.fallback-to-chest-when-empty` on so it is shown normally. |
| Java players do not see native dialogs | Both the server and the client must be 1.21.6 or newer. Check `java.native-dialogs`. |
| Your plugin throws `NoClassDefFoundError` when CrossUI is missing | Use the bridge isolation pattern on the [Developer API](Developer-API) page. |
| Need more detail | Set `debug: true` and run `/crossui reload`. |

## Known limitations

- Native Java dialogs have no plain "closed" event. `onClose` and `onCancel` fire only when the player uses a Close or Cancel button, not when the dialog is dismissed with Escape.
- Legacy text input uses chat, because there is no way to type into a chest.
- Conversion covers chest containers only (9x1 to 9x6). Other container types are shown normally.
- Automatic Fake-Anvil to Java Dialog interception is not implemented yet.

Found a bug? Open an issue at https://github.com/natan307/CrossUI/issues and include your server software, Minecraft version, the startup log line from CrossUI and the plugin that owns the menu.
