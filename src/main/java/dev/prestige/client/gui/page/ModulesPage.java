package dev.prestige.client.gui.page;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.GuiContext;
import dev.prestige.client.gui.component.Card;
import dev.prestige.client.gui.component.KeyChip;
import dev.prestige.client.gui.component.PageHeader;
import dev.prestige.client.gui.component.ScrollArea;
import dev.prestige.client.gui.component.TextField;
import dev.prestige.client.gui.component.ToggleSwitch;
import dev.prestige.client.gui.component.setting.SettingRow;
import dev.prestige.client.gui.component.setting.SettingRows;
import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.Icons;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * Module cards for one category, or search results across all of them.
 *
 * <p>Left-click a card to toggle the module, click the chevron (or right-click) to expand its
 * keybind row, middle-click to rebind straight away. Typing anywhere searches.
 */
public final class ModulesPage implements Page {
    private static final int SEARCH_WIDTH = 132;
    private static final int CARD_HEIGHT = 28;
    private static final int CARD_HEIGHT_COMPACT = 20;
    private static final int KEYBIND_ROW_HEIGHT = 22;
    private static final int EXPANDED_PADDING = 4;
    private static final int VIEW_TOGGLE_SIZE = 13;

    private final GuiContext context;
    private final TextField search = new TextField("Search modules…", 32, Icons.SEARCH);
    private final ScrollArea scroll = new ScrollArea();
    private final Map<Module, CardState> states = new HashMap<>();
    private final List<CardHit> hits = new ArrayList<>();
    private Category category = Category.COMBAT;
    private Bounds viewport = new Bounds(0, 0, 0, 0);
    private Bounds listToggle = new Bounds(0, 0, 0, 0);
    private Bounds gridToggle = new Bounds(0, 0, 0, 0);

    private static final class CardState {
        List<SettingRow<?>> rows;
        float hover;
        float enabled;
        float expand;
        boolean expanded;
    }

    @Nullable
    private SettingRow<?> draggingRow;

    private record CardHit(Module module, Bounds card, int headerBottom, Bounds chevron, @Nullable Bounds keyChip,
                           boolean interactive) {
    }

    public ModulesPage(GuiContext context) {
        this.context = context;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        search.setText("");
        search.setFocused(false);
        scroll.reset();
    }

    private String query() {
        return search.getText().trim();
    }

    private List<Module> visibleModules() {
        String query = query();
        return query.isEmpty() ? context.modules().getModules(category) : context.modules().search(query);
    }

    private CardState state(Module module) {
        return states.computeIfAbsent(module, m -> {
            CardState state = new CardState();
            state.enabled = m.isEnabled() ? 1f : 0f;
            state.rows = SettingRows.createAll(m.getSettings());
            return state;
        });
    }

    @Override
    public void render(DrawContext draw, TextRenderer font, Bounds area, int mouseX, int mouseY, float deltaSeconds) {
        int accent = context.accent().color();
        int pad = Theme.CONTENT_PADDING;
        int left = area.x() + pad;
        int right = area.right() - pad;
        int top = area.y() + pad;
        List<Module> modules = visibleModules();
        long enabled = modules.stream().filter(Module::isEnabled).count();

        if (query().isEmpty()) {
            PageHeader.render(draw, font, left, top, category.getDisplayName(), "Modules",
                    modules.size() + " modules · " + enabled + " enabled", context.accent().color(category));
        } else {
            PageHeader.render(draw, font, left, top, "Search", "Results",
                    modules.size() + (modules.size() == 1 ? " match" : " matches"), accent);
        }

        search.render(draw, font, right - SEARCH_WIDTH, top, SEARCH_WIDTH, accent);
        int toggleY = top + TextField.HEIGHT + 5;
        gridToggle = new Bounds(right - VIEW_TOGGLE_SIZE, toggleY, VIEW_TOGGLE_SIZE, VIEW_TOGGLE_SIZE);
        listToggle = new Bounds(gridToggle.x() - VIEW_TOGGLE_SIZE - 3, toggleY, VIEW_TOGGLE_SIZE, VIEW_TOGGLE_SIZE);
        boolean grid = context.settings().gridView;
        renderViewToggle(draw, listToggle, Icons.LIST, !grid);
        renderViewToggle(draw, gridToggle, Icons.GRID, grid);

        viewport = new Bounds(left, area.y() + Theme.CONTENT_TOP, right - left, area.bottom() - pad - (area.y() + Theme.CONTENT_TOP));
        RenderUtil.rect(draw, left, viewport.y() - 6, right - left, 1, Theme.DIVIDER);

        if (modules.isEmpty()) {
            String message = "No modules match \"" + query() + "\"";
            int width = Typography.width(font, Font.BODY, message);
            Typography.draw(draw, font, Font.BODY, message, viewport.x() + (viewport.width() - width) / 2,
                    viewport.y() + 20, Theme.TEXT_SECONDARY);
            hits.clear();
            return;
        }
        renderCards(draw, font, modules, grid ? 2 : 1, mouseX, mouseY, deltaSeconds);
        scroll.renderScrollbar(draw, viewport.right() - 2, viewport.y(), viewport.height(), accent);
    }

    private void renderViewToggle(DrawContext draw, Bounds bounds, Identifier icon, boolean active) {
        if (active) {
            RenderUtil.roundedRect(draw, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 3, Theme.INPUT_BORDER);
        }
        Icons.draw(draw, icon, bounds.x() + 3, bounds.y() + 3, 7, active ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED);
    }

    private void renderCards(DrawContext draw, TextRenderer font, List<Module> modules, int columns,
                             int mouseX, int mouseY, float deltaSeconds) {
        boolean descriptions = context.settings().descriptions;
        int baseHeight = descriptions ? CARD_HEIGHT : CARD_HEIGHT_COMPACT;
        int scrollbarRoom = 6;
        int columnWidth = (viewport.width() - scrollbarRoom - (columns - 1) * Theme.CARD_GAP) / columns;

        // Advance animations first so the layout uses this frame's heights.
        for (Module module : modules) {
            CardState state = state(module);
            state.expand = Animation.approach(state.expand, state.expanded ? 1f : 0f, deltaSeconds, Theme.EXPAND_SPEED);
            state.enabled = Animation.approach(state.enabled, module.isEnabled() ? 1f : 0f, deltaSeconds, Theme.TOGGLE_SPEED);
        }

        int contentHeight = 0;
        List<int[]> positions = new ArrayList<>();
        for (int row = 0; row * columns < modules.size(); row++) {
            int rowHeight = 0;
            for (int col = 0; col < columns; col++) {
                int index = row * columns + col;
                if (index >= modules.size()) {
                    break;
                }
                int height = baseHeight + Math.round(expandedHeight(modules.get(index)) * state(modules.get(index)).expand);
                positions.add(new int[] {col * (columnWidth + Theme.CARD_GAP), contentHeight, height});
                rowHeight = Math.max(rowHeight, height);
            }
            contentHeight += rowHeight + Theme.CARD_GAP;
        }
        scroll.update(contentHeight - Theme.CARD_GAP, viewport.height(), deltaSeconds);

        hits.clear();
        draw.enableScissor(viewport.x(), viewport.y(), viewport.right(), viewport.bottom());
        for (int i = 0; i < modules.size(); i++) {
            int[] position = positions.get(i);
            Bounds card = new Bounds(viewport.x() + position[0], viewport.y() + position[1] - scroll.getOffset(),
                    columnWidth, position[2]);
            if (card.bottom() < viewport.y() || card.y() > viewport.bottom()) {
                continue;
            }
            boolean hovered = viewport.contains(mouseX, mouseY) && card.contains(mouseX, mouseY);
            renderCard(draw, font, modules.get(i), card, baseHeight, hovered, mouseX, mouseY, deltaSeconds);
        }
        draw.disableScissor();
    }

    /** Height of the keybind row plus every visible setting row. */
    private int expandedHeight(Module module) {
        int height = KEYBIND_ROW_HEIGHT + EXPANDED_PADDING;
        for (SettingRow<?> row : state(module).rows) {
            if (row.getSetting().isVisible()) {
                height += row.height();
            }
        }
        return height;
    }

    private void renderCard(DrawContext draw, TextRenderer font, Module module, Bounds card, int baseHeight,
                            boolean hovered, int mouseX, int mouseY, float deltaSeconds) {
        CardState state = state(module);
        state.hover = Animation.approach(state.hover, hovered ? 1f : 0f, deltaSeconds, Theme.HOVER_SPEED);
        int moduleAccent = context.accent().color(module.getCategory());

        // Only clip while the keybind row is animating; clipping at rest would shave the card's
        // edges, because the scissor rect is snapped to whole GUI pixels.
        boolean clip = state.expand > 0.01f && state.expand < 0.99f;
        if (clip) {
            draw.enableScissor(card.x() - 1, card.y() - 1, card.right() + 1, card.bottom() + 1);
        }
        Card.render(draw, card.x(), card.y(), card.width(), card.height(), state.hover);

        int switchX = card.right() - 10 - Theme.SWITCH_WIDTH;
        ToggleSwitch.render(draw, switchX, card.y() + (baseHeight - Theme.SWITCH_HEIGHT) / 2, state.enabled, moduleAccent);

        int chevronX = switchX - 15;
        Bounds chevron = new Bounds(chevronX - 3, card.y(), 13, baseHeight);
        Icons.draw(draw, state.expanded ? Icons.CHEVRON_DOWN : Icons.CHEVRON_RIGHT, chevronX,
                card.y() + (baseHeight - 7) / 2, 7, hovered ? Theme.TEXT_BODY : Theme.TEXT_MUTED);

        int textX = card.x() + 10;
        int textWidth = chevronX - 6 - textX;
        boolean descriptions = baseHeight == CARD_HEIGHT;
        int nameY = descriptions ? card.y() + 6 : Typography.centerY(Font.BOLD, card.y(), baseHeight);
        Typography.draw(draw, font, Font.BOLD, Typography.trim(font, Font.BOLD, module.getName(), textWidth),
                textX, nameY, Theme.TEXT_PRIMARY);
        if (descriptions) {
            Typography.draw(draw, font, Font.CAPTION,
                    Typography.trim(font, Font.CAPTION, module.getDescription(), textWidth),
                    textX, card.y() + 17, Theme.TEXT_SECONDARY);
        }

        Bounds keyChip = null;
        if (state.expand > 0.01f) {
            int rowY = card.y() + baseHeight;
            RenderUtil.rect(draw, card.x() + 10, rowY, card.width() - 20, 1, Theme.DIVIDER);
            Typography.draw(draw, font, Font.BODY, "Keybind", textX,
                    Typography.centerY(Font.BODY, rowY, KEYBIND_ROW_HEIGHT), Theme.TEXT_SECONDARY);
            Bounds chip = KeyChip.render(draw, font, KeyChip.keyName(module.getKeybind()), card.right() - 10,
                    rowY + (KEYBIND_ROW_HEIGHT - KeyChip.HEIGHT) / 2, context.isListening(module), !module.hasKeybind(),
                    moduleAccent);
            if (state.expand > 0.99f) {
                keyChip = chip;
            }
            rowY += KEYBIND_ROW_HEIGHT;
            for (SettingRow<?> row : state.rows) {
                if (!row.getSetting().isVisible()) {
                    continue;
                }
                row.render(draw, font, textX, rowY, card.width() - 20, mouseX, mouseY, moduleAccent, deltaSeconds);
                rowY += row.height();
            }
        }
        if (clip) {
            draw.disableScissor();
        }

        hits.add(new CardHit(module, card, card.y() + baseHeight, chevron, keyChip, state.expand > 0.99f));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        context.cancelListening();
        boolean overSearch = search.isMouseOver(mouseX, mouseY);
        search.setFocused(overSearch);
        if (overSearch) {
            return true;
        }
        if (listToggle.contains(mouseX, mouseY) || gridToggle.contains(mouseX, mouseY)) {
            context.settings().gridView = gridToggle.contains(mouseX, mouseY);
            scroll.reset();
            return true;
        }
        if (!viewport.contains(mouseX, mouseY)) {
            return false;
        }
        for (CardHit hit : hits) {
            if (!hit.card().contains(mouseX, mouseY)) {
                continue;
            }
            CardState state = state(hit.module());
            if (mouseY >= hit.headerBottom()) {
                // Expanded area: keybind chip and setting rows. Never toggles the module.
                if (!hit.interactive()) {
                    return true;
                }
                if (hit.keyChip() != null && hit.keyChip().contains(mouseX, mouseY)) {
                    context.startListening(hit.module());
                    return true;
                }
                for (SettingRow<?> row : state.rows) {
                    if (row.getSetting().isVisible() && row.isMouseOver(mouseX, mouseY)
                            && row.mouseClicked(mouseX, mouseY, button)) {
                        if (row.isDragging()) {
                            draggingRow = row;
                        }
                        return true;
                    }
                }
                return true;
            }
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                    || (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && hit.chevron().contains(mouseX, mouseY))) {
                state.expanded = !state.expanded;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                hit.module().toggle();
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                state.expanded = true;
                context.startListening(hit.module());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY) {
        if (draggingRow == null) {
            return false;
        }
        draggingRow.mouseDragged(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean mouseReleased() {
        if (draggingRow == null) {
            return false;
        }
        draggingRow.mouseReleased();
        draggingRow = null;
        return true;
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
        if (context.handleKeyCapture(input)) {
            return true;
        }
        if (input.key() == GLFW.GLFW_KEY_ESCAPE && !search.getText().isEmpty()) {
            search.setText("");
            search.setFocused(false);
            scroll.reset();
            return true;
        }
        return search.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (!input.isValidChar()) {
            return false;
        }
        // Typing anywhere on the page goes to the search box.
        search.setFocused(true);
        scroll.reset();
        return search.charTyped(input);
    }
}
