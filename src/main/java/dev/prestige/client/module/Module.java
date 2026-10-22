package dev.prestige.client.module;

import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for every Prestige feature.
 *
 * <p>Subclasses only describe themselves through the constructor and put their behaviour in the
 * lifecycle hooks. State changes always go through {@link #setEnabled(boolean)}, which guarantees
 * {@link #onEnable()} / {@link #onDisable()} fire exactly once per transition.
 */
public abstract class Module {
    /** Keybind value meaning "not bound". Matches {@code GLFW_KEY_UNKNOWN}. */
    public static final int KEY_NONE = -1;

    private static final Logger LOGGER = LoggerFactory.getLogger("Prestige/Module");

    protected final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private final int defaultKeybind;
    private boolean enabled;
    private int keybind;

    protected Module(String name, String description, Category category) {
        this(name, description, category, KEY_NONE);
    }

    protected Module(String name, String description, Category category, int defaultKeybind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.defaultKeybind = defaultKeybind;
        this.keybind = defaultKeybind;
    }

    /**
     * Called when the module is switched on. May run before a world is loaded (for example when
     * the config is restored at startup), so guard against a null {@code mc.player}.
     */
    protected void onEnable() {
    }

    /** Called when the module is switched off. Same world caveat as {@link #onEnable()}. */
    protected void onDisable() {
    }

    /** Called at the end of every client tick while enabled and a world is loaded. */
    public void onTick() {
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        try {
            if (enabled) {
                onEnable();
            } else {
                onDisable();
            }
        } catch (RuntimeException e) {
            LOGGER.error("Module '{}' threw in {}", name, enabled ? "onEnable" : "onDisable", e);
        }
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final Category getCategory() {
        return category;
    }

    public final int getKeybind() {
        return keybind;
    }

    public final void setKeybind(int keybind) {
        this.keybind = keybind;
    }

    public final int getDefaultKeybind() {
        return defaultKeybind;
    }

    public final boolean hasKeybind() {
        return keybind != KEY_NONE;
    }
}
