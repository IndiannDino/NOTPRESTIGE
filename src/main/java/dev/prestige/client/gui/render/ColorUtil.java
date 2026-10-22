package dev.prestige.client.gui.render;

/** HSV <-> ARGB conversion for the color picker. Hue, saturation and value are all 0..1. */
public final class ColorUtil {
    private ColorUtil() {
    }

    public static int hsvToRgb(float hue, float saturation, float value, int alpha) {
        float h = (hue - (float) Math.floor(hue)) * 6f;
        int sector = (int) h;
        float f = h - sector;
        float p = value * (1 - saturation);
        float q = value * (1 - saturation * f);
        float t = value * (1 - saturation * (1 - f));
        float r;
        float g;
        float b;
        switch (sector) {
            case 0 -> { r = value; g = t; b = p; }
            case 1 -> { r = q; g = value; b = p; }
            case 2 -> { r = p; g = value; b = t; }
            case 3 -> { r = p; g = q; b = value; }
            case 4 -> { r = t; g = p; b = value; }
            default -> { r = value; g = p; b = q; }
        }
        return (alpha << 24) | (Math.round(r * 255) << 16) | (Math.round(g * 255) << 8) | Math.round(b * 255);
    }

    /** Returns {hue, saturation, value}. */
    public static float[] rgbToHsv(int argb) {
        float r = ((argb >> 16) & 0xFF) / 255f;
        float g = ((argb >> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        float hue;
        if (delta == 0) {
            hue = 0;
        } else if (max == r) {
            hue = ((g - b) / delta) / 6f;
        } else if (max == g) {
            hue = ((b - r) / delta + 2) / 6f;
        } else {
            hue = ((r - g) / delta + 4) / 6f;
        }
        if (hue < 0) {
            hue += 1;
        }
        return new float[] {hue, max == 0 ? 0 : delta / max, max};
    }
}
