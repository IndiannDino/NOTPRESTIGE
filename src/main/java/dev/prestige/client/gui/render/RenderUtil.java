package dev.prestige.client.gui.render;

import net.minecraft.client.gui.DrawContext;

/** Small drawing primitives on top of {@link DrawContext}. All colors are ARGB. */
public final class RenderUtil {
    private RenderUtil() {
    }

    public static void rect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);
    }

    /** Rectangle with 1px chamfered corners, which reads as softly rounded at GUI scale. */
    public static void roundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x + 1, y, x + width - 1, y + height, color);
        context.fill(x, y + 1, x + 1, y + height - 1, color);
        context.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    /** Rounded only along the top edge, for panel headers that sit flush on a body. */
    public static void roundedTopRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x + 1, y, x + width - 1, y + 1, color);
        context.fill(x, y + 1, x + width, y + height, color);
    }

    /** Soft drop shadow built from stacked translucent outlines. */
    public static void shadow(DrawContext context, int x, int y, int width, int height, int color, int spread) {
        for (int i = 1; i <= spread; i++) {
            int alpha = (color >>> 24) * (spread - i + 1) / spread;
            int layer = (alpha << 24) | (color & 0xFFFFFF);
            context.fill(x - i, y - i + 1, x + width + i, y + height + i, layer);
        }
    }

    public static int lerpColor(int from, int to, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int a = lerpChannel(from >>> 24, to >>> 24, t);
        int r = lerpChannel((from >> 16) & 0xFF, (to >> 16) & 0xFF, t);
        int g = lerpChannel((from >> 8) & 0xFF, (to >> 8) & 0xFF, t);
        int b = lerpChannel(from & 0xFF, to & 0xFF, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** Scales a color's existing alpha by {@code factor} (0..1). */
    public static int withAlpha(int color, float factor) {
        int alpha = Math.round((color >>> 24) * Math.max(0f, Math.min(1f, factor)));
        return (alpha << 24) | (color & 0xFFFFFF);
    }

    private static int lerpChannel(int from, int to, float t) {
        return Math.round(from + (to - from) * t);
    }
}
