# Developer API

## Dependency

```kotlin
repositories { maven("https://jitpack.io") }
dependencies { compileOnly("com.github.natan307:CrossUI:2.0.0") }
```

Modrinth Maven alternative: `maven("https://api.modrinth.com/maven")` and `compileOnly("maven.modrinth:crossui:2.0.0")`.

Declare `depend: [CrossUI]` (hard) or `softdepend: [CrossUI]` (optional) in your `plugin.yml`.

The API only uses plain Java and Bukkit types (`Player`, `UUID`, `String`, `Consumer`, `List`). No PacketEvents, Geyser or Floodgate types ever appear in it.

## Static facade

```java
CrossUI.menu(player)
    .title("Warps")
    .button("Spawn", p -> p.performCommand("spawn"))
    .button("Market", "Buy and sell", p -> p.performCommand("market"))
    .open();

CrossUI.confirm(player)
    .title("Delete home")
    .body("Are you sure?")
    .onYes(p -> deleteHome(p))
    .open();

CrossUI.input(player)
    .title("Transfer Balance")
    .label("Enter the amount:")
    .onSubmit((p, text) -> bank.transfer(p, text))
    .open();
```

All callbacks run on the thread that owns the player (the main thread on Spigot/Paper, the player's region thread on Folia). Java native dialogs cannot report a plain "closed" event, so `onClose` / `onCancel` are only called where the platform supports it.

## What each builder shows

| Builder | Bedrock | Java 1.21.6+ | Older Java |
| --- | --- | --- | --- |
| `menu` | SimpleForm | Native multi-action dialog | Chest inventory |
| `input` | CustomForm with an input field | Native dialog with a text field and Submit / Cancel | Next chat message is the answer (`cancel` aborts) |
| `confirm` | ModalForm | Native confirmation dialog | Chest with Yes / No wool |

Detect the client with `CrossUI.platform(player)`, which returns `BEDROCK`, `JAVA_DIALOG` or `JAVA_LEGACY`. Text in all builders accepts `&` color codes.

## Button icons

A menu button can have an optional icon: `url:https://...` and `path:textures/...` are used by Bedrock forms. The legacy chest renderer reads a Bukkit material name, for example `"DIAMOND_SWORD"`.

```java
CrossUI.menu(player)
    .title("Warps")
    .button("Arena", "PvP", "url:https://example.com/arena.png", p -> p.performCommand("arena"))
    .open();
```

## ServicesManager

```java
RegisteredServiceProvider<CrossUIProvider> rsp =
        Bukkit.getServicesManager().getRegistration(CrossUIProvider.class);
if (rsp != null) {
    MenuBuilder menu = rsp.getProvider().createMenu(player);
}
```

## Soft-depend without NoClassDefFoundError

If a class that imports `CrossUI` is loaded while CrossUI is missing, the JVM can throw `NoClassDefFoundError`. Isolate CrossUI behind your own interface.

**1. Your interface**

```java
public interface UIBridge {
    void openAmountInput(Player player, String title, Consumer<String> onResult);
}
```

**2. CrossUI implementation** (only loaded when CrossUI is active)

```java
public final class CrossUIBridge implements UIBridge {
    @Override
    public void openAmountInput(Player player, String title, Consumer<String> onResult) {
        CrossUI.input(player)
                .title(title)
                .placeholder("Type an amount...")
                .onSubmit((p, text) -> onResult.accept(text))
                .open();
    }
}
```

**3. Fallback implementation**

```java
public final class LegacyChatBridge implements UIBridge {
    @Override
    public void openAmountInput(Player player, String title, Consumer<String> onResult) {
        player.sendMessage("§e[" + title + "] §fType the amount in chat:");
        // your own AsyncPlayerChatEvent handling
    }
}
```

**4. Safe initialisation in `onEnable()`**

```java
UIBridge uiBridge;
if (Bukkit.getPluginManager().isPluginEnabled("CrossUI")) {
    uiBridge = new CrossUIBridge();
} else {
    uiBridge = new LegacyChatBridge();
}
```

## Versioning

`crossui-api` follows Semantic Versioning.

- **Patch** (1.0.1): bug fixes in `crossui-core`, PacketEvents updates. No recompile needed.
- **Minor** (1.1.0): new builder methods. Existing methods are never removed or changed.
- **Major**: only for unavoidable breaking changes. The plugin and the API share one version number, so a plugin-only release (such as 2.0.0, which adds item pictures) can raise it without changing the API.
