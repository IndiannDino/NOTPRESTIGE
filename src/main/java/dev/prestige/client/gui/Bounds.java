package dev.prestige.client.gui;

/** Axis-aligned rectangle in GUI coordinates. */
public record Bounds(int x, int y, int width, int height) {
    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    public boolean contains(double px, double py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }
}
