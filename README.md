# Prestige Client

A modular Fabric utility framework with a dark, high-contrast ClickGUI.

- **Minecraft:** 1.21.11
- **Fabric Loader:** 0.18.4+
- **Fabric API:** 0.141.6+1.21.11
- **Java:** 21

## Building

```
./gradlew build
```

The mod jar is written to `build/libs/prestige-client-<version>.jar`. Drop it into your
`mods` folder alongside Fabric API.

To launch a development client: `./gradlew runClient`.

## Using the menu

Press **Right Shift** in game to open it. You can rebind this under Options → Controls → Prestige Client.

The menu is one window with a sidebar:

- **Modules:** Combat, Movement, Render and World. Each page lists module cards with a
  description and an on/off switch.
- **General:**
  - **Settings:** background blur, animations, module descriptions, grid view.
  - **Theme:** accent color (Lavender, Blue, Rose, Emerald, Amber, or one color per category).
  - **Configs:** save the current module setup under a name, then apply or delete it later.
  - **Keybinds:** every module keybind in one list.

| Action | Effect |
| --- | --- |
| Click a module card | Toggle it |
| Click the `>` chevron (or right-click a card) | Show its keybind row |
| Click a key chip, or middle-click a card | Rebind: press a key, **ESC** cancels, **DEL**/**Backspace** clears |
| Type anywhere on a module page | Search all modules (**ESC** clears) |
| List / grid icons | Switch between one and two columns |

Module keybinds only work when no screen (chat, inventory, the menu) is open.

Settings are saved to `.minecraft/config/prestige_config.json` when the menu closes and when the
game exits. Saved configs live in `.minecraft/config/prestige/profiles/`.

The menu picks its own scale from the window size, so it looks the same at any GUI scale.

## Project layout

```
dev.prestige.client
├── PrestigeClient           Entrypoint: wires subsystems, registers events and the menu key
├── module
│   ├── Category             Combat / Movement / Render / World + accent colors
│   ├── Module               Base class: name, description, toggle state, keybind, lifecycle hooks
│   ├── ModuleManager        Registration, lookup, search and per-tick dispatch
│   └── impl/<category>      Feature modules (currently placeholders)
├── config
│   ├── ConfigManager        Gson load/save of prestige_config.json and named profiles
│   ├── GuiSettings          Persisted menu preferences
│   └── Profile              A saved config's name and timestamp
├── input
│   └── KeybindHandler       Edge-triggered module keybinds
└── gui
    ├── ClickGuiScreen       Window, scaling, sidebar navigation, input routing
    ├── GuiContext           State shared by pages (settings, accent, keybind capture)
    ├── page                 ModulesPage, SettingsPage, ThemePage, ConfigsPage, KeybindsPage
    ├── component            Sidebar, Card, ToggleSwitch, TextField, Button, KeyChip, ScrollArea…
    ├── render               RenderUtil (anti-aliased rounded shapes), Icons, Animation
    └── theme                Theme (colors, metrics), Accent, Typography (fonts)
```

Fonts: [Inter](https://rsms.me/inter/) for body text and
[Instrument Serif](https://fonts.google.com/specimen/Instrument+Serif) for headings, both under the
SIL Open Font License (see `src/main/resources/licenses/`).

## Adding a module

1. Create a class in `module/impl/<category>/` extending `Module`.
2. Call `super(name, description, Category.X)` (optionally pass a default GLFW key code).
3. Put your logic in `onEnable()`, `onDisable()` and `onTick()`.
4. Register it in `ModuleManager.registerBuiltins()`.

```java
public final class Example extends Module {
    public Example() {
        super("Example", "What this module does.", Category.WORLD);
    }

    @Override
    protected void onEnable() {
        // runs once when switched on (mc.player may be null here)
    }

    @Override
    protected void onDisable() {
        // runs once when switched off
    }

    @Override
    public void onTick() {
        // runs every client tick while enabled and in a world
    }
}
```

`onTick()` only runs while a world is loaded. If a module throws in `onTick()`, the error is
logged and that module is disabled.
