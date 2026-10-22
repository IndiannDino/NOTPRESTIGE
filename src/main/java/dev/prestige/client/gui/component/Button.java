package dev.prestige.client.gui.component;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.Icons;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Small rounded button. Primary buttons are filled with the accent color; secondary ones use the
 * input surface. Remembers where it was last drawn so clicks can be hit-tested.
 */
public final class Button {
    public static final int HEIGHT = 15;
    private static final int PADDING_X = 8;

    private final boolean primary;
    @Nullable
    private final Identifier icon;
    private float hover;
    private Bounds bounds = new Bounds(0, 0, 0, 0);

    public Button(boolean primary, @Nullable Identifier icon) {
        this.primary = primary;
        this.icon = icon;
    }

    public static int width(TextRenderer font, String label, boolean withIcon) {
        return PADDING_X * 2 + Typography.width(font, Font.BOLD, label) + (withIcon ? Theme.ICON_SIZE + 4 : 0);
    }

    public void render(DrawContext context, TextRenderer font, String label, int x, int y, int width,
                       int mouseX, int mouseY, int accent, float deltaSeconds) {
        bounds = new Bounds(x, y, width, HEIGHT);
        hover = Animation.approach(hover, bounds.contains(mouseX, mouseY) ? 1f : 0f, deltaSeconds, Theme.HOVER_SPEED);

        if (primary) {
            int fill = RenderUtil.lerpColor(accent, RenderUtil.lerpColor(accent, 0xFFFFFFFF, 0.15f), hover);
            RenderUtil.roundedRect(context, x, y, width, HEIGHT, 4, fill);
        } else {
            RenderUtil.roundedRect(context, x, y, width, HEIGHT, 4, Theme.INPUT_BORDER);
            RenderUtil.roundedRect(context, x + 1, y + 1, width - 2, HEIGHT - 2, 3,
                    RenderUtil.lerpColor(Theme.INPUT_BACKGROUND, Theme.CARD_HOVER, hover));
        }

        int contentWidth = Typography.width(font, Font.BOLD, label) + (icon != null ? Theme.ICON_SIZE + 4 : 0);
        int cx = x + (width - contentWidth) / 2;
        int textColor = primary ? Theme.TEXT_PRIMARY : Theme.TEXT_BODY;
        if (icon != null) {
            Icons.draw(context, icon, cx, y + (HEIGHT - Theme.ICON_SIZE) / 2, Theme.ICON_SIZE, textColor);
            cx += Theme.ICON_SIZE + 4;
        }
        Typography.draw(context, font, Font.BOLD, label, cx, Typography.centerY(Font.BOLD, y, HEIGHT), textColor);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY);
    }
}
