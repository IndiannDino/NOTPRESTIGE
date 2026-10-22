package dev.prestige.client.gui.page;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.GuiContext;
import dev.prestige.client.gui.component.Card;
import dev.prestige.client.gui.component.KeyChip;
import dev.prestige.client.gui.component.PageHeader;
import dev.prestige.client.gui.component.ScrollArea;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import dev.prestige.client.module.Module;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;

/** Every keybind in one list. Click a key to rebind it. */
public final class KeybindsPage implements Page {
    private static final int ROW_HEIGHT = 22;

    private final GuiContext context;
    private final ScrollArea scroll = new ScrollArea();
    private final Map<Module, Bounds> chips = new HashMap<>();
    private Bounds viewport = new Bounds(0, 0, 0, 0);

    public KeybindsPage(GuiContext context) {
        this.context = context;
    }

    @Override
    public void render(DrawContext draw, TextRenderer font, Bounds area, int mouseX, int mouseY, float deltaSeconds) {
        int accent = context.accent().color();
        int pad = Theme.CONTENT_PADDING;
        int left = area.x() + pad;
        int right = area.right() - pad;
        PageHeader.render(draw, font, left, area.y() + pad, "Keybinds", "",
                "Click a key to rebind · ESC cancels · DEL clears", accent);
        RenderUtil.rect(draw, left, area.y() + Theme.CONTENT_TOP - 6, right - left, 1, Theme.DIVIDER);

        viewport = new Bounds(left, area.y() + Theme.CONTENT_TOP, right - left, area.bottom() - pad - (area.y() + Theme.CONTENT_TOP));
        List<Module> modules = context.modules().getModules();
        int contentHeight = (modules.size() + 1) * (ROW_HEIGHT + Theme.CARD_GAP) - Theme.CARD_GAP;
        scroll.update(contentHeight, viewport.height(), deltaSeconds);

        chips.clear();
        draw.enableScissor(viewport.x(), viewport.y(), viewport.right(), viewport.bottom());
        int width = viewport.width() - 6;
        int y = viewport.y() - scroll.getOffset();

        // The menu key itself is a vanilla keybinding, so it is changed from Controls instead.
        Card.render(draw, viewport.x(), y, width, ROW_HEIGHT, 0f);
        int nameWidth = Typography.draw(draw, font, Font.BOLD, "Open menu", viewport.x() + 10,
                Typography.centerY(Font.BOLD, y, ROW_HEIGHT), Theme.TEXT_PRIMARY);
        Typography.draw(draw, font, Font.CAPTION, "Change in Options › Controls", viewport.x() + 16 + nameWidth,
                Typography.centerY(Font.CAPTION, y, ROW_HEIGHT), Theme.TEXT_MUTED);
        String menuKey = context.openMenuKey().getBoundKeyLocalizedText().getString().toUpperCase(Locale.ROOT);
        KeyChip.render(draw, font, menuKey, viewport.x() + width - 8, y + (ROW_HEIGHT - KeyChip.HEIGHT) / 2,
                false, true, accent);
        y += ROW_HEIGHT + Theme.CARD_GAP;

        for (Module module : modules) {
            Bounds row = new Bounds(viewport.x(), y, width, ROW_HEIGHT);
            y += ROW_HEIGHT + Theme.CARD_GAP;
            if (row.bottom() < viewport.y() || row.y() > viewport.bottom()) {
                continue;
            }
            int moduleAccent = context.accent().color(module.getCategory());
            Card.render(draw, row.x(), row.y(), row.width(), row.height(), 0f);
            int textY = Typography.centerY(Font.BOLD, row.y(), ROW_HEIGHT);
            int nameEnd = Typography.draw(draw, font, Font.BOLD, module.getName(), row.x() + 10, textY, Theme.TEXT_PRIMARY);
            Typography.draw(draw, font, Font.CAPTION, module.getCategory().getDisplayName(), row.x() + 16 + nameEnd,
                    Typography.centerY(Font.CAPTION, row.y(), ROW_HEIGHT), moduleAccent);
            Bounds chip = KeyChip.render(draw, font, KeyChip.keyName(module.getKeybind()), row.right() - 8,
                    row.y() + (ROW_HEIGHT - KeyChip.HEIGHT) / 2, context.isListening(module), !module.hasKeybind(),
                    moduleAccent);
            chips.put(module, chip);
        }
        draw.disableScissor();
        scroll.renderScrollbar(draw, viewport.right() - 2, viewport.y(), viewport.height(), accent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        context.cancelListening();
        if (!viewport.contains(mouseX, mouseY)) {
            return false;
        }
        for (Map.Entry<Module, Bounds> chip : chips.entrySet()) {
            if (chip.getValue().contains(mouseX, mouseY)) {
                context.startListening(chip.getKey());
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!viewport.contains(mouseX, mouseY)) {
            return false;
        }
        scroll.scroll(amount);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return context.handleKeyCapture(input);
    }
}
