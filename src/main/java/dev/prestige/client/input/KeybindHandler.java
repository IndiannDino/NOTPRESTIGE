package dev.prestige.client.input;

import dev.prestige.client.module.Module;
import dev.prestige.client.module.ModuleManager;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;

/**
 * Toggles modules from their keybinds. Polls once per tick and fires on the press edge only,
 * so holding a key never re-toggles. Nothing fires while any screen (chat, inventory, the
 * ClickGUI itself) is open, but held state is still tracked so closing a screen with the key
 * down doesn't trigger a toggle.
 */
public final class KeybindHandler {
    private final ModuleManager modules;
    private Set<Integer> heldKeys = new HashSet<>();

    public KeybindHandler(ModuleManager modules) {
        this.modules = modules;
    }

    public void tick(MinecraftClient client) {
        Window window = client.getWindow();
        Set<Integer> downKeys = new HashSet<>();
        for (Module module : modules.getModules()) {
            int key = module.getKeybind();
            if (key != Module.KEY_NONE && InputUtil.isKeyPressed(window, key)) {
                downKeys.add(key);
            }
        }

        if (client.currentScreen == null) {
            for (Module module : modules.getModules()) {
                int key = module.getKeybind();
                if (downKeys.contains(key) && !heldKeys.contains(key)) {
                    module.toggle();
                }
            }
        }
        heldKeys = downKeys;
    }
}
