package dev.prestige.client.gui.hud;

import dev.prestige.client.config.GuiSettings;
import dev.prestige.client.gui.component.ToggleSwitch;
import dev.prestige.client.gui.render.GuiScale;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Accent;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import dev.prestige.client.module.Module;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.util.Util;

/**
 * Slide-in cards in the top-right corner when a module is toggled in game. Toggles made from the
 * menu or while loading a config don't show a toast.
 */
public final class ToastOverlay implements HudElement {
    private static final int WIDTH = 124;
    private static final int HEIGHT = 26;
    private static final int GAP = 4;
    private static final int MARGIN = 8;
    private static final int MAX_TOASTS = 4;
    private static final long SLIDE_MS = 220;
    private static final long VISIBLE_MS = 2000;

    private final Supplier<GuiSettings> settings;
    private final Deque<Toast> toasts = new ArrayDeque<>();

    private static final class Toast {
        final Module module;
        boolean enabled;
        long shownAt;

        Toast(Module module, boolean enabled, long shownAt) {
            this.module = module;
            this.enabled = enabled;
            this.shownAt = shownAt;
        }
    }

    public ToastOverlay(Supplier<GuiSettings> settings) {
        this.settings = settings;
    }

    /** Module toggle listener. */
    public void onToggle(Module module) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!settings.get().toasts || client.player == null || client.currentScreen != null) {
            return;
        }
        long now = Util.getMeasuringTimeMs();
        for (Toast toast : toasts) {
            if (toast.module == module) {
                // Re-toggled while still visible: update in place instead of stacking a duplicate.
                toast.enabled = module.isEnabled();
                toast.shownAt = now - SLIDE_MS;
                return;
            }
        }
        toasts.addFirst(new Toast(module, module.isEnabled(), now));
        while (toasts.size() > MAX_TOASTS) {
            toasts.removeLast();
        }
    }

    @Override
    public void render(DrawContext draw, RenderTickCounter tickCounter) {
        if (toasts.isEmpty()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Window window = client.getWindow();
        float scale = GuiScale.toGuiScale(window);
        RenderUtil.setPixelScale(GuiScale.pixelScale(window));
        int screenWidth = Math.round(draw.getScaledWindowWidth() / scale);
        Accent accent = Accent.fromName(settings.get().accent);
        long now = Util.getMeasuringTimeMs();

        draw.getMatrices().pushMatrix();
        draw.getMatrices().scale(scale, scale);
        int y = MARGIN;
        Iterator<Toast> iterator = toasts.iterator();
        while (iterator.hasNext()) {
            Toast toast = iterator.next();
            long age = now - toast.shownAt;
            if (age > VISIBLE_MS + SLIDE_MS * 2) {
                iterator.remove();
                continue;
            }
            float slide = age < SLIDE_MS ? easeOut(age / (float) SLIDE_MS)
                    : age > VISIBLE_MS + SLIDE_MS ? 1 - easeIn((age - VISIBLE_MS - SLIDE_MS) / (float) SLIDE_MS) : 1;
            int x = Math.round(screenWidth - (WIDTH + MARGIN) * slide);
            renderToast(draw, client.textRenderer, toast, x, y, accent.color(toast.module.getCategory()));
            y += Math.round((HEIGHT + GAP) * slide);
        }
        draw.getMatrices().popMatrix();
    }

    private void renderToast(DrawContext draw, TextRenderer font, Toast toast, int x, int y, int accent) {
        RenderUtil.shadow(draw, x, y, WIDTH, HEIGHT, 6, Theme.SHADOW, 3);
        RenderUtil.roundedRect(draw, x - 1, y - 1, WIDTH + 2, HEIGHT + 2, 6, Theme.WINDOW_BORDER);
        RenderUtil.roundedRect(draw, x, y, WIDTH, HEIGHT, 5, Theme.WINDOW_BACKGROUND);
        RenderUtil.roundedRect(draw, x + 5, y + 6, 2, HEIGHT - 12, 1, toast.enabled ? accent : Theme.TEXT_MUTED);

        int textX = x + 13;
        int textWidth = WIDTH - 13 - 10 - Theme.SWITCH_WIDTH - 6;
        Typography.draw(draw, font, Font.BOLD, Typography.trim(font, Font.BOLD, toast.module.getName(), textWidth),
                textX, y + 5, Theme.TEXT_PRIMARY);
        Typography.draw(draw, font, Font.CAPTION, toast.enabled ? "Enabled" : "Disabled", textX, y + 15,
                toast.enabled ? accent : Theme.TEXT_SECONDARY);
        ToggleSwitch.render(draw, x + WIDTH - 10 - Theme.SWITCH_WIDTH, y + (HEIGHT - Theme.SWITCH_HEIGHT) / 2,
                toast.enabled ? 1f : 0f, accent);
    }

    private static float easeOut(float t) {
        float inverse = 1 - t;
        return 1 - inverse * inverse * inverse;
    }

    private static float easeIn(float t) {
        return t * t * t;
    }
}
