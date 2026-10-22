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

## Using the ClickGUI

Press **Right Shift** in game to open it. You can rebind this under Options → Controls → Prestige Client.

| Action | Effect |
| --- | --- |
| Left-click a module | Toggle it |
| Middle-click a module | Bind a key: press a key to set it, **ESC** cancels, **DEL**/**Backspace** unbinds |
| Left-drag a panel header | Move the panel |
| Right-click a panel header | Collapse / expand the panel |

The footer shows the description and keybind of the hovered module. Module keybinds only work
when no screen (chat, inventory, the GUI) is open.

Settings are saved to `.minecraft/config/prestige_config.json` when the GUI closes and when the
game exits.

## Project layout

```
dev.prestige.client
├── PrestigeClient           Entrypoint: wires subsystems, registers events and the GUI key
├── module
│   ├── Category             Combat / Movement / Render / World + accent colors
│   ├── Module               Base class: name, description, toggle state, keybind, lifecycle hooks
│   ├── ModuleManager        Registration, lookup and per-tick dispatch
│   └── impl/<category>      Feature modules (currently placeholders)
├── config
│   ├── ConfigManager        Gson load/save of prestige_config.json (atomic writes)
│   └── PanelState           Persisted panel position / collapsed state
├── input
│   └── KeybindHandler       Edge-triggered module keybinds
└── gui
    ├── ClickGuiScreen       Screen: panel z-order, keybind capture, footer
    ├── component            CategoryPanel, ModuleButton
    ├── render               RenderUtil (shapes, colors), Animation (easing)
    └── theme                Theme (colors, metrics), Typography (text styles)
```

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
