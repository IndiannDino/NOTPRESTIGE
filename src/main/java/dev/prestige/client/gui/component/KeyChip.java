package dev.prestige.client.gui.component;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import dev.prestige.client.module.Module;
import java.util.Locale;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

/** Right-aligned pill showing a keybind; highlighted while waiting for a key press. */
public final class KeyChip {
    public static final int HEIGHT = 13;

    private KeyChip() {
    }

    public static String keyName(int keyCode) {
        if (keyCode == Module.KEY_NONE) {
            return "None";
        }
        return InputUtil.Type.KEYSYM.createFromCode(keyCode).getLocalizedText().getString().toUpperCase(Locale.ROOT);
    }

    /** Draws the chip with its right edge at {@code right} and returns its bounds. */
    public static Bounds render(DrawContext context, TextRenderer font, String label, int right, int y,
                                boolean listening, boolean dimmed, int accent) {
        String text = listening ? "Press a key…" : label;
        int width = Typography.width(font, Font.BOLD, text) + 12;
        int x = right - width;
        int border = listening ? accent : Theme.INPUT_BORDER;
        RenderUtil.roundedRect(context, x, y, width, HEIGHT, 3, border);
        RenderUtil.roundedRect(context, x + 1, y + 1, width - 2, HEIGHT - 2, 2,
                listening ? blend(accent) : Theme.INPUT_BACKGROUND);
        int color = listening ? Theme.TEXT_PRIMARY : dimmed ? Theme.TEXT_MUTED : Theme.TEXT_BODY;
        Typography.draw(context, font, Font.BOLD, text, x + 6, Typography.centerY(Font.BOLD, y, HEIGHT), color);
        return new Bounds(x, y, width, HEIGHT);
    }

    /** Opaque mix of the accent over the input surface. */
    private static int blend(int accent) {
        return RenderUtil.lerpColor(Theme.INPUT_BACKGROUND, accent, 0.25f);
    }
}
