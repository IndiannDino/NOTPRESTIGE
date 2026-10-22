package dev.prestige.client.gui;

import dev.prestige.client.config.ConfigManager;
import dev.prestige.client.gui.component.CategoryPanel;
import dev.prestige.client.gui.component.ModuleButton;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.ModuleManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * The Prestige dashboard. Owns panel z-order, keybind capture and the info footer; all row and
 * header behaviour lives in {@link CategoryPanel}. Config is saved whenever the screen closes.
 */
public final class ClickGuiScreen extends Screen {
    private static final String HINT =
            "LMB toggle  ·  MMB bind  ·  RMB header collapse  ·  Drag header to move";

    private final ConfigManager config;
    /** Back-to-front draw order; the last panel is on top and gets clicks first. */
    private final List<CategoryPanel> panels = new ArrayList<>();

    @Nullable
    private Module listening;
    private long lastFrameMs;

    public ClickGuiScreen(ModuleManager modules, ConfigManager config) {
        super(Text.literal("Prestige"));
        this.config = config;
        for (Category category : Category.values()) {
            panels.add(new CategoryPanel(category, modules.getModules(category),
                    config.getPanelState(category), module -> listening = module));
        }
    }

    @Override
    protected void init() {
        lastFrameMs = 0;
        layoutUnplacedPanels();
        for (CategoryPanel panel : panels) {
            panel.clampTo(width, height);
        }
    }

    /** Lays panels the user hasn't moved yet left-to-right, wrapping on narrow windows. */
    private void layoutUnplacedPanels() {
        int x = Theme.SCREEN_MARGIN;
        int y = Theme.SCREEN_MARGIN;
        int rowHeight = 0;
        for (Category category : Category.values()) {
            CategoryPanel panel = panelFor(category);
            if (x + panel.getWidth() > width - Theme.SCREEN_MARGIN && x > Theme.SCREEN_MARGIN) {
                x = Theme.SCREEN_MARGIN;
                y += rowHeight + Theme.PANEL_GAP;
                rowHeight = 0;
            }
            if (!panel.getState().placed) {
                panel.getState().x = x;
                panel.getState().y = y;
            }
            x += panel.getWidth() + Theme.PANEL_GAP;
            rowHeight = Math.max(rowHeight, panel.getHeight());
        }
    }

    private CategoryPanel panelFor(Category category) {
        return panels.stream()
                .filter(panel -> panel.getCategory() == category)
                .findFirst()
                .orElseThrow();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, Theme.SCREEN_OVERLAY);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        long now = Util.getMeasuringTimeMs();
        float deltaSeconds = lastFrameMs == 0 ? 0f : Math.min((now - lastFrameMs) / 1000f, 0.1f);
        lastFrameMs = now;

        for (CategoryPanel panel : panels) {
            panel.render(context, textRenderer, mouseX, mouseY, deltaSeconds, listening);
        }
        renderFooter(context, mouseX, mouseY);
    }

    private void renderFooter(DrawContext context, int mouseX, int mouseY) {
        int top = height - Theme.FOOTER_HEIGHT;
        RenderUtil.rect(context, 0, top, width, Theme.FOOTER_HEIGHT, Theme.FOOTER_BACKGROUND);
        RenderUtil.rect(context, 0, top, width, 1, Theme.DIVIDER);

        int textY = Typography.centerY(textRenderer, top, Theme.FOOTER_HEIGHT);
        int x = Theme.PADDING + 2;
        Typography.heading(context, textRenderer, "PRESTIGE", x, textY, Theme.TEXT_PRIMARY);
        x += textRenderer.getWidth("PRESTIGE") + 3;
        Typography.caption(context, textRenderer, "CLIENT", x, textY, Theme.TEXT_MUTED);
        x += textRenderer.getWidth("CLIENT") + Theme.PADDING * 2;

        int infoColor = Theme.TEXT_SECONDARY;
        String info;
        if (listening != null) {
            info = "Press a key to bind " + listening.getName() + "  ·  ESC cancel  ·  DEL unbind";
            infoColor = listening.getCategory().getAccentColor();
        } else {
            Module hovered = hoveredModule(mouseX, mouseY);
            if (hovered != null) {
                String bind = hovered.hasKeybind() ? ModuleButton.keyName(hovered.getKeybind()) : "None";
                info = hovered.getName() + "  ·  " + hovered.getDescription() + "  ·  Bind: " + bind;
                infoColor = Theme.TEXT_PRIMARY;
            } else {
                info = HINT;
            }
        }
        info = textRenderer.trimToWidth(info, width - x - Theme.PADDING);
        Typography.body(context, textRenderer, info, x, textY, infoColor);
    }

    @Nullable
    private Module hoveredModule(double mouseX, double mouseY) {
        CategoryPanel top = topPanelAt(mouseX, mouseY);
        return top == null ? null : top.getHoveredModule(mouseX, mouseY);
    }

    @Nullable
    private CategoryPanel topPanelAt(double mouseX, double mouseY) {
        for (int i = panels.size() - 1; i >= 0; i--) {
            if (panels.get(i).isMouseOver(mouseX, mouseY)) {
                return panels.get(i);
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        // Any click while waiting for a key cancels the capture.
        listening = null;
        CategoryPanel panel = topPanelAt(click.x(), click.y());
        if (panel != null && panel.mouseClicked(click.x(), click.y(), click.button())) {
            panels.remove(panel);
            panels.add(panel);
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        for (CategoryPanel panel : panels) {
            if (panel.mouseDragged(click.x(), click.y(), width, height)) {
                return true;
            }
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        boolean handled = false;
        for (CategoryPanel panel : panels) {
            handled |= panel.mouseReleased(click.button());
        }
        return handled || super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (listening == null) {
            return super.keyPressed(input);
        }
        int key = input.key();
        if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
            listening.setKeybind(Module.KEY_NONE);
        } else if (key != GLFW.GLFW_KEY_ESCAPE && key != GLFW.GLFW_KEY_UNKNOWN) {
            listening.setKeybind(key);
        }
        listening = null;
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        listening = null;
        for (CategoryPanel panel : panels) {
            panel.cancelDrag();
        }
        config.save();
    }
}
