package dev.prestige.client.gui;

import dev.prestige.client.config.ConfigManager;
import dev.prestige.client.config.GuiSettings;
import dev.prestige.client.gui.theme.Accent;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.client.option.KeyBinding;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/** State shared by every page of the ClickGUI, including the active keybind capture. */
public final class GuiContext {
    private final ModuleManager modules;
    private final ConfigManager config;
    private final KeyBinding openMenuKey;
    @Nullable
    private Module listening;
    /** Set when a key press completed a capture, so its character event isn't typed anywhere. */
    private boolean swallowChar;

    public GuiContext(ModuleManager modules, ConfigManager config, KeyBinding openMenuKey) {
        this.modules = modules;
        this.config = config;
        this.openMenuKey = openMenuKey;
    }

    public ModuleManager modules() {
        return modules;
    }

    public ConfigManager config() {
        return config;
    }

    public GuiSettings settings() {
        return config.getGuiSettings();
    }

    public KeyBinding openMenuKey() {
        return openMenuKey;
    }

    public Accent accent() {
        return Accent.fromName(settings().accent);
    }

    /** Soft UI click, if menu sounds are enabled. */
    public void playClick() {
        if (settings().sounds) {
            MinecraftClient.getInstance().getSoundManager()
                    .play(PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK.value(), 1.6f, 0.18f));
        }
    }

    public boolean isListening(Module module) {
        return listening == module;
    }

    public boolean isListening() {
        return listening != null;
    }

    public void startListening(Module module) {
        listening = module;
    }

    public void cancelListening() {
        listening = null;
    }

    /**
     * Completes a keybind capture: ESC cancels, DELETE/BACKSPACE unbinds, anything else binds.
     * Returns false when no capture is active.
     */
    public boolean handleKeyCapture(KeyInput input) {
        if (listening == null) {
            return false;
        }
        int key = input.key();
        if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
            listening.setKeybind(Module.KEY_NONE);
        } else if (key != GLFW.GLFW_KEY_ESCAPE && key != GLFW.GLFW_KEY_UNKNOWN) {
            listening.setKeybind(key);
        }
        listening = null;
        swallowChar = true;
        return true;
    }

    /**
     * True once for the character event that follows a key press used by a capture. GLFW
     * delivers both in the same frame, so the flag is cleared again every frame.
     */
    public boolean consumeSwallowedChar() {
        boolean swallow = swallowChar;
        swallowChar = false;
        return swallow;
    }

    public void onFrame() {
        swallowChar = false;
    }
}
