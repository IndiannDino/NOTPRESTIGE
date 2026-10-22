package dev.prestige.client.gui.render;

import net.minecraft.client.gui.DrawContext;
import org.joml.Matrix3x2fStack;

/**
 * Drawing primitives on top of {@link DrawContext}. All colors are ARGB.
 *
 * <p>Rounded shapes are rasterized in real framebuffer pixels rather than GUI pixels, with a
 * partially transparent edge pixel per scanline, so corners stay smooth at every GUI scale.
 */
public final class RenderUtil {
    private static float pixelScale = 2f;

    private RenderUtil() {
    }

    /** Framebuffer pixels per drawing unit under the current transform. */
    public static void setPixelScale(float scale) {
        pixelScale = scale;
    }

    public static void rect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x, y, x + width, y + height, color);
    }

    public static void roundedRect(DrawContext context, float x, float y, float width, float height,
                                   float radius, int color) {
        if (width <= 0 || height <= 0 || (color >>> 24) == 0) {
            return;
        }
        float scale = pixelScale;
        int px = Math.round(x * scale);
        int py = Math.round(y * scale);
        int pw = Math.round(width * scale);
        int ph = Math.round(height * scale);
        int r = Math.min(Math.round(radius * scale), Math.min(pw, ph) / 2);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(1f / scale, 1f / scale);

        context.fill(px, py + r, px + pw, py + ph - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            double inset = r - Math.sqrt((double) r * r - dy * dy);
            int full = (int) Math.ceil(inset);
            float coverage = (float) (full - inset);
            int top = py + i;
            int bottom = py + ph - 1 - i;
            context.fill(px + full, top, px + pw - full, top + 1, color);
            context.fill(px + full, bottom, px + pw - full, bottom + 1, color);
            if (full > 0 && coverage > 0.02f) {
                int edge = withAlpha(color, coverage);
                context.fill(px + full - 1, top, px + full, top + 1, edge);
                context.fill(px + pw - full, top, px + pw - full + 1, top + 1, edge);
                context.fill(px + full - 1, bottom, px + full, bottom + 1, edge);
                context.fill(px + pw - full, bottom, px + pw - full + 1, bottom + 1, edge);
            }
        }
        matrices.popMatrix();
    }

    /** Soft drop shadow made of progressively larger, fainter rounded rects. */
    public static void shadow(DrawContext context, float x, float y, float width, float height,
                              float radius, int color, int spread) {
        int baseAlpha = color >>> 24;
        for (int i = spread; i >= 1; i--) {
            int alpha = baseAlpha / (i + 1);
            int layer = (alpha << 24) | (color & 0xFFFFFF);
            roundedRect(context, x - i, y - i + 1.5f, width + i * 2, height + i * 2, radius + i, layer);
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
