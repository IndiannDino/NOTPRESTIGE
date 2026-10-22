package dev.prestige.client.gui.component;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.Icons;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/** Brand header, sectioned navigation and the menu-key hint at the bottom. */
public final class Sidebar {
    private static final int ITEM_HEIGHT = 16;
    private static final int ITEM_GAP = 2;
    private static final int MENU_HINT_HEIGHT = 20;

    public record Entry(String id, String label, Identifier icon, Supplier<String> badge) {
    }

    public record Section(String title, List<Entry> entries) {
    }

    private final List<Section> sections;
    private final Supplier<String> menuKey;
    private final Map<String, Float> hover = new HashMap<>();
    private final List<Map.Entry<Bounds, String>> hits = new ArrayList<>();
    private String selected;

    public Sidebar(List<Section> sections, Supplier<String> menuKey, String selected) {
        this.sections = sections;
        this.menuKey = menuKey;
        this.selected = selected;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String id) {
        selected = id;
    }

    public void render(DrawContext draw, TextRenderer font, Bounds area, int mouseX, int mouseY, int accent,
                       float deltaSeconds) {
        renderBrand(draw, font, area.x() + 10, area.y() + 12, accent);

        hits.clear();
        int y = area.y() + 46;
        for (Section section : sections) {
            Typography.draw(draw, font, Font.CAPTION, section.title(), area.x() + 12, y, Theme.TEXT_MUTED);
            y += 11;
            for (Entry entry : section.entries()) {
                Bounds item = new Bounds(area.x() + 6, y, area.width() - 12, ITEM_HEIGHT);
                renderItem(draw, font, entry, item, mouseX, mouseY, accent, deltaSeconds);
                hits.add(Map.entry(item, entry.id()));
                y += ITEM_HEIGHT + ITEM_GAP;
            }
            y += 8;
        }
        if (y <= area.bottom() - 6 - MENU_HINT_HEIGHT) {
            renderMenuKeyHint(draw, font, area);
        }
    }

    private void renderBrand(DrawContext draw, TextRenderer font, int x, int y, int accent) {
        int logo = 16;
        RenderUtil.roundedRect(draw, x, y, logo, logo, 4, accent);
        int letterWidth = Typography.width(font, Font.BRAND_ITALIC, "P");
        Typography.draw(draw, font, Font.BRAND_ITALIC, "P", x + (logo - letterWidth) / 2, y + 4, Theme.TEXT_PRIMARY);

        int textX = x + logo + 6;
        int width = Typography.draw(draw, font, Font.BRAND, "Prestige", textX, y + 1, Theme.TEXT_PRIMARY);
        Typography.draw(draw, font, Font.BRAND_ITALIC, " Client", textX + width, y + 1, accent);
        Typography.draw(draw, font, Font.CAPTION, "Utility framework", textX, y + 11, Theme.TEXT_MUTED);
    }

    private void renderItem(DrawContext draw, TextRenderer font, Entry entry, Bounds item, int mouseX, int mouseY,
                            int accent, float deltaSeconds) {
        boolean active = entry.id().equals(selected);
        float h = Animation.approach(hover.getOrDefault(entry.id(), 0f), item.contains(mouseX, mouseY) ? 1f : 0f,
                deltaSeconds, Theme.HOVER_SPEED);
        hover.put(entry.id(), h);

        if (active) {
            RenderUtil.roundedRect(draw, item.x(), item.y(), item.width(), item.height(), 4,
                    RenderUtil.lerpColor(Theme.SIDEBAR_BACKGROUND, accent, 0.22f));
        } else if (h > 0f) {
            RenderUtil.roundedRect(draw, item.x(), item.y(), item.width(), item.height(), 4,
                    RenderUtil.withAlpha(Theme.ITEM_HOVER, h));
        }
        Icons.draw(draw, entry.icon(), item.x() + 6, item.y() + (ITEM_HEIGHT - Theme.ICON_SIZE) / 2, Theme.ICON_SIZE,
                active ? accent : Theme.TEXT_SECONDARY);
        Typography.draw(draw, font, Font.BODY, entry.label(), item.x() + 19,
                Typography.centerY(Font.BODY, item.y(), ITEM_HEIGHT), active ? Theme.TEXT_PRIMARY : Theme.TEXT_BODY);

        String badge = entry.badge().get();
        if (!badge.isEmpty()) {
            int width = Typography.width(font, Font.CAPTION, badge);
            Typography.draw(draw, font, Font.CAPTION, badge, item.right() - 6 - width,
                    Typography.centerY(Font.CAPTION, item.y(), ITEM_HEIGHT), active ? accent : Theme.TEXT_MUTED);
        }
    }

    private void renderMenuKeyHint(DrawContext draw, TextRenderer font, Bounds area) {
        int height = MENU_HINT_HEIGHT;
        Bounds box = new Bounds(area.x() + 6, area.bottom() - 6 - height, area.width() - 12, height);
        Card.render(draw, box.x(), box.y(), box.width(), box.height(), 0f);
        Typography.draw(draw, font, Font.CAPTION, "Menu", box.x() + 7,
                Typography.centerY(Font.CAPTION, box.y(), height), Theme.TEXT_SECONDARY);
        int labelWidth = Typography.width(font, Font.CAPTION, "Menu");
        String key = Typography.trim(font, Font.BOLD, menuKey.get(), box.width() - 21 - labelWidth);
        int width = Typography.width(font, Font.BOLD, key);
        Typography.draw(draw, font, Font.BOLD, key, box.right() - 7 - width,
                Typography.centerY(Font.BOLD, box.y(), height), Theme.TEXT_PRIMARY);
    }

    /** Returns the id of the clicked entry, if any. */
    @Nullable
    public String entryAt(double mouseX, double mouseY) {
        for (Map.Entry<Bounds, String> hit : hits) {
            if (hit.getKey().contains(mouseX, mouseY)) {
                return hit.getValue();
            }
        }
        return null;
    }
}
