package dev.prestige.client.gui.page;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.GuiContext;
import dev.prestige.client.gui.component.Card;
import dev.prestige.client.gui.component.PageHeader;
import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Accent;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import dev.prestige.client.module.Category;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/** Accent color picker. */
public final class ThemePage implements Page {
    private static final int SWATCH_WIDTH = 72;
    private static final int SWATCH_HEIGHT = 44;

    private final GuiContext context;
    private final Map<Accent, Bounds> hits = new EnumMap<>(Accent.class);
    private final Map<Accent, Float> hover = new EnumMap<>(Accent.class);

    public ThemePage(GuiContext context) {
        this.context = context;
    }

    @Override
    public void render(DrawContext draw, TextRenderer font, Bounds area, int mouseX, int mouseY, float deltaSeconds) {
        Accent selected = context.accent();
        int pad = Theme.CONTENT_PADDING;
        int left = area.x() + pad;
        int width = area.width() - pad * 2;
        PageHeader.render(draw, font, left, area.y() + pad, "Theme", "", "Pick the accent used across the menu", selected.color());
        RenderUtil.rect(draw, left, area.y() + Theme.CONTENT_TOP - 6, width, 1, Theme.DIVIDER);
        Typography.draw(draw, font, Font.CAPTION, "ACCENT COLOR", left, area.y() + Theme.CONTENT_TOP, Theme.TEXT_MUTED);

        int x = left;
        int y = area.y() + Theme.CONTENT_TOP + 12;
        for (Accent accent : Accent.values()) {
            if (x + SWATCH_WIDTH > left + width) {
                x = left;
                y += SWATCH_HEIGHT + Theme.CARD_GAP;
            }
            Bounds bounds = new Bounds(x, y, SWATCH_WIDTH, SWATCH_HEIGHT);
            float h = Animation.approach(hover.getOrDefault(accent, 0f), bounds.contains(mouseX, mouseY) ? 1f : 0f,
                    deltaSeconds, Theme.HOVER_SPEED);
            hover.put(accent, h);
            renderSwatch(draw, font, accent, bounds, accent == selected, h);
            hits.put(accent, bounds);
            x += SWATCH_WIDTH + Theme.CARD_GAP;
        }
    }

    private void renderSwatch(DrawContext draw, TextRenderer font, Accent accent, Bounds b, boolean selected, float hover) {
        if (selected) {
            RenderUtil.roundedRect(draw, b.x() - 1, b.y() - 1, b.width() + 2, b.height() + 2, Theme.CARD_RADIUS + 1, accent.color());
        }
        Card.render(draw, b.x(), b.y(), b.width(), b.height(), hover);

        int previewX = b.x() + 8;
        int previewY = b.y() + 8;
        int previewWidth = b.width() - 16;
        if (accent == Accent.PER_CATEGORY) {
            Category[] categories = Category.values();
            int segment = previewWidth / categories.length;
            for (int i = 0; i < categories.length; i++) {
                RenderUtil.roundedRect(draw, previewX + i * segment, previewY, segment - 2, 14, 3,
                        categories[i].getAccentColor());
            }
        } else {
            RenderUtil.roundedRect(draw, previewX, previewY, previewWidth, 14, 3, accent.color());
        }
        int labelWidth = Typography.width(font, Font.BOLD, accent.getDisplayName());
        Typography.draw(draw, font, Font.BOLD, accent.getDisplayName(), b.x() + (b.width() - labelWidth) / 2,
                b.y() + 28, selected ? Theme.TEXT_PRIMARY : Theme.TEXT_BODY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        for (Map.Entry<Accent, Bounds> hit : hits.entrySet()) {
            if (hit.getValue().contains(mouseX, mouseY)) {
                context.settings().accent = hit.getKey().name();
                return true;
            }
        }
        return false;
    }
}
