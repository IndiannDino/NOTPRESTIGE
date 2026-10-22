package dev.prestige.client.gui.component;

import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import net.minecraft.client.gui.DrawContext;

/** Smooth vertical scrolling for a fixed-height viewport, with a thin scrollbar. */
public final class ScrollArea {
    private static final float WHEEL_STEP = 24f;

    private float target;
    private float offset;
    private int maxScroll;

    /** Updates the scroll limits and animation. Call once per frame before drawing content. */
    public void update(int contentHeight, int viewportHeight, float deltaSeconds) {
        maxScroll = Math.max(0, contentHeight - viewportHeight);
        target = clamp(target);
        offset = clamp(Animation.approach(offset, target, deltaSeconds, Theme.SCROLL_SPEED));
    }

    public int getOffset() {
        return Math.round(offset);
    }

    public void scroll(double amount) {
        target = clamp(target - (float) amount * WHEEL_STEP);
    }

    public void reset() {
        target = 0;
        offset = 0;
    }

    public void renderScrollbar(DrawContext context, int x, int y, int viewportHeight, int accent) {
        if (maxScroll <= 0) {
            return;
        }
        int contentHeight = viewportHeight + maxScroll;
        int thumbHeight = Math.max(16, viewportHeight * viewportHeight / contentHeight);
        int thumbY = y + Math.round((viewportHeight - thumbHeight) * (offset / maxScroll));
        RenderUtil.roundedRect(context, x, y, 2, viewportHeight, 1, Theme.DIVIDER);
        RenderUtil.roundedRect(context, x, thumbY, 2, thumbHeight, 1, RenderUtil.withAlpha(accent, 0.8f));
    }

    private float clamp(float value) {
        return Math.max(0, Math.min(value, maxScroll));
    }
}
