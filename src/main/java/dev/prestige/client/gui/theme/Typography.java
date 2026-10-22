package dev.prestige.client.gui.theme;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * The only place the ClickGUI draws text, so every label uses the same small set of styles.
 * Headings get a drop shadow for weight; body and captions stay flat for a cleaner look.
 */
public final class Typography {
    private Typography() {
    }

    public static void heading(DrawContext context, TextRenderer font, String text, int x, int y, int color) {
        context.drawText(font, text, x, y, color, true);
    }

    public static void body(DrawContext context, TextRenderer font, String text, int x, int y, int color) {
        context.drawText(font, text, x, y, color, false);
    }

    public static void caption(DrawContext context, TextRenderer font, String text, int x, int y, int color) {
        context.drawText(font, text, x, y, color, false);
    }

    /** Y offset that vertically centers a line of text inside a box of the given height. */
    public static int centerY(TextRenderer font, int top, int height) {
        return top + (height - font.fontHeight) / 2 + 1;
    }
}
