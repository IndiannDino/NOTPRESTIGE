package dev.prestige.client.gui.component;

import dev.prestige.client.config.PanelState;
import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * A draggable, collapsible panel listing every module in one category.
 *
 * <p>Header: left-drag moves the panel, right-click collapses it. Rows: left-click toggles the
 * module, middle-click asks the screen to start listening for a new keybind.
 */
public final class CategoryPanel {
    private final Category category;
    private final PanelState state;
    private final List<ModuleButton> buttons;
    private final Consumer<Module> bindRequest;

    private float expandProgress;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public CategoryPanel(Category category, List<Module> modules, PanelState state, Consumer<Module> bindRequest) {
        this.category = category;
        this.state = state;
        this.buttons = modules.stream().map(ModuleButton::new).toList();
        this.bindRequest = bindRequest;
        this.expandProgress = state.expanded ? 1f : 0f;
    }

    public Category getCategory() {
        return category;
    }

    public PanelState getState() {
        return state;
    }

    public int getWidth() {
        return Theme.PANEL_WIDTH;
    }

    private int bodyHeight() {
        int full = buttons.size() * Theme.ROW_HEIGHT + Theme.PANEL_BOTTOM_PADDING;
        return Math.round(full * expandProgress);
    }

    public int getHeight() {
        return Theme.HEADER_HEIGHT + bodyHeight();
    }

    public void render(DrawContext context, TextRenderer font, int mouseX, int mouseY,
                       float deltaSeconds, @Nullable Module listening) {
        expandProgress = Animation.approach(expandProgress, state.expanded ? 1f : 0f, deltaSeconds, Theme.EXPAND_SPEED);

        int x = state.x;
        int y = state.y;
        int width = getWidth();
        int body = bodyHeight();
        int accent = category.getAccentColor();

        RenderUtil.shadow(context, x, y, width, getHeight(), Theme.SHADOW, 3);
        RenderUtil.roundedRect(context, x, y, width, getHeight(), Theme.PANEL_BACKGROUND);
        renderHeader(context, font, x, y, width, accent);

        if (body <= 0) {
            return;
        }
        int bodyTop = y + Theme.HEADER_HEIGHT;
        context.enableScissor(x, bodyTop, x + width, bodyTop + body);
        for (int i = 0; i < buttons.size(); i++) {
            int rowY = bodyTop + i * Theme.ROW_HEIGHT;
            if (rowY >= bodyTop + body) {
                break;
            }
            ModuleButton button = buttons.get(i);
            boolean hovered = button == rowAt(mouseX, mouseY);
            button.render(context, font, x, rowY, width, accent, hovered, button.getModule() == listening, deltaSeconds);
        }
        context.disableScissor();
    }

    private void renderHeader(DrawContext context, TextRenderer font, int x, int y, int width, int accent) {
        RenderUtil.roundedTopRect(context, x, y, width, Theme.HEADER_HEIGHT, Theme.HEADER_BACKGROUND);
        RenderUtil.rect(context, x, y + Theme.HEADER_HEIGHT - 1, width, 1, accent);

        int textY = Typography.centerY(font, y, Theme.HEADER_HEIGHT - 1);
        Typography.heading(context, font, category.getDisplayName().toUpperCase(Locale.ROOT),
                x + Theme.PADDING, textY, Theme.TEXT_PRIMARY);

        long active = buttons.stream().filter(b -> b.getModule().isEnabled()).count();
        String counter = active + "/" + buttons.size();
        Typography.caption(context, font, counter, x + width - Theme.PADDING - font.getWidth(counter), textY,
                active > 0 ? accent : Theme.TEXT_MUTED);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= state.x && mouseX < state.x + getWidth()
                && mouseY >= state.y && mouseY < state.y + getHeight();
    }

    private boolean isOverHeader(double mouseX, double mouseY) {
        return mouseX >= state.x && mouseX < state.x + getWidth()
                && mouseY >= state.y && mouseY < state.y + Theme.HEADER_HEIGHT;
    }

    /** The row under the cursor, if it is fully visible (rows ignore clicks mid-animation). */
    @Nullable
    private ModuleButton rowAt(double mouseX, double mouseY) {
        if (mouseX < state.x || mouseX >= state.x + getWidth()) {
            return null;
        }
        int bodyTop = state.y + Theme.HEADER_HEIGHT;
        if (mouseY < bodyTop) {
            return null;
        }
        int index = (int) ((mouseY - bodyTop) / Theme.ROW_HEIGHT);
        if (index >= buttons.size()) {
            return null;
        }
        int rowBottom = bodyTop + (index + 1) * Theme.ROW_HEIGHT;
        return rowBottom <= bodyTop + bodyHeight() ? buttons.get(index) : null;
    }

    @Nullable
    public Module getHoveredModule(double mouseX, double mouseY) {
        ModuleButton row = rowAt(mouseX, mouseY);
        return row == null ? null : row.getModule();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isOverHeader(mouseX, mouseY)) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                dragging = true;
                dragOffsetX = (int) mouseX - state.x;
                dragOffsetY = (int) mouseY - state.y;
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                state.expanded = !state.expanded;
            }
            return true;
        }

        ModuleButton row = rowAt(mouseX, mouseY);
        if (row != null) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                row.getModule().toggle();
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                bindRequest.accept(row.getModule());
            }
            return true;
        }
        return isMouseOver(mouseX, mouseY);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        if (!dragging) {
            return false;
        }
        state.x = (int) mouseX - dragOffsetX;
        state.y = (int) mouseY - dragOffsetY;
        state.placed = true;
        clampTo(screenWidth, screenHeight);
        return true;
    }

    public boolean mouseReleased(int button) {
        if (dragging && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            dragging = false;
            return true;
        }
        return false;
    }

    public void cancelDrag() {
        dragging = false;
    }

    /** Keeps the header reachable after drags and window resizes. */
    public void clampTo(int screenWidth, int screenHeight) {
        int maxY = screenHeight - Theme.FOOTER_HEIGHT - Theme.HEADER_HEIGHT;
        state.x = Math.max(0, Math.min(state.x, screenWidth - getWidth()));
        state.y = Math.max(0, Math.min(state.y, maxY));
    }
}
