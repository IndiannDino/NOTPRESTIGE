package dev.prestige.client.gui.render;

import dev.prestige.client.gui.theme.Theme;
import net.minecraft.client.util.Window;

/**
 * Prestige UI scale, independent of Minecraft's GUI scale: framebuffer pixels per UI unit, picked
 * so a full-size menu window covers about three quarters of the screen.
 */
public final class GuiScale {
    private GuiScale() {
    }

    public static float pixelScale(Window window) {
        float fit = Math.min(window.getFramebufferWidth() * 0.75f / Theme.WINDOW_MAX_WIDTH,
                window.getFramebufferHeight() * 0.8f / Theme.WINDOW_MAX_HEIGHT);
        return Math.max(1.5f, Math.min(fit, 3f));
    }

    /** Factor from UI units to Minecraft GUI pixels. */
    public static float toGuiScale(Window window) {
        return pixelScale(window) / window.getScaleFactor();
    }
}
