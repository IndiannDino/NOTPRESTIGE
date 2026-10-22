package dev.prestige.client.gui.component;

import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import net.minecraft.client.gui.DrawContext;

/** The bordered, rounded surface behind every list item. */
public final class Card {
    private Card() {
    }

    public static void render(DrawContext context, int x, int y, int width, int height, float hover) {
        RenderUtil.roundedRect(context, x, y, width, height, Theme.CARD_RADIUS, Theme.CARD_BORDER);
        RenderUtil.roundedRect(context, x + 1, y + 1, width - 2, height - 2, Theme.CARD_RADIUS - 1,
                RenderUtil.lerpColor(Theme.CARD_BACKGROUND, Theme.CARD_HOVER, hover));
    }
}
