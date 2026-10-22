package dev.prestige.client.gui;

import dev.prestige.client.config.ConfigManager;
import dev.prestige.client.gui.component.Sidebar;
import dev.prestige.client.gui.page.ConfigsPage;
import dev.prestige.client.gui.page.KeybindsPage;
import dev.prestige.client.gui.page.ModulesPage;
import dev.prestige.client.gui.page.Page;
import dev.prestige.client.gui.page.SettingsPage;
import dev.prestige.client.gui.page.ThemePage;
import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.Icons;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.module.Category;
import dev.prestige.client.module.ModuleManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

/**
 * The Prestige menu: one centered window with a navigation sidebar on the left and the selected
 * page on the right. Settings are saved whenever the screen closes.
 */
public final class ClickGuiScreen extends Screen {
    private static final String CATEGORY_PREFIX = "category:";

    private final GuiContext context;
    private final ConfigManager config;
    private final ModulesPage modulesPage;
    private final Map<String, Page> generalPages = new LinkedHashMap<>();
    private final Sidebar sidebar;
    private Page currentPage;
    private Bounds window = new Bounds(0, 0, 0, 0);
    private long lastFrameMs;
    /** Drawing units to GUI pixels; lets the menu keep the same size at any GUI scale. */
    private float menuScale = 1f;
    private float pixelScale = 2f;

    public ClickGuiScreen(ModuleManager modules, ConfigManager config, KeyBinding openMenuKey) {
        super(Text.literal("Prestige"));
        this.config = config;
        this.context = new GuiContext(modules, config, openMenuKey);
        this.modulesPage = new ModulesPage(context);
        generalPages.put("settings", new SettingsPage(context));
        generalPages.put("theme", new ThemePage(context));
        generalPages.put("configs", new ConfigsPage(context));
        generalPages.put("keybinds", new KeybindsPage(context));

        List<Sidebar.Entry> categoryEntries = new ArrayList<>();
        for (Category category : Category.values()) {
            categoryEntries.add(new Sidebar.Entry(CATEGORY_PREFIX + category.name(), category.getDisplayName(),
                    Icons.forCategory(category), () -> String.valueOf(modules.getModules(category).size())));
        }
        List<Sidebar.Entry> generalEntries = List.of(
                new Sidebar.Entry("settings", "Settings", Icons.SETTINGS, () -> ""),
                new Sidebar.Entry("theme", "Theme", Icons.THEME, () -> ""),
                new Sidebar.Entry("configs", "Configs", Icons.CONFIGS, () -> ""),
                new Sidebar.Entry("keybinds", "Keybinds", Icons.KEYBINDS, () -> ""));
        this.sidebar = new Sidebar(
                List.of(new Sidebar.Section("MODULES", categoryEntries), new Sidebar.Section("GENERAL", generalEntries)),
                () -> openMenuKey.getBoundKeyLocalizedText().getString(),
                CATEGORY_PREFIX + Category.COMBAT.name());
        this.currentPage = modulesPage;
    }

    private void select(String id) {
        context.cancelListening();
        if (id.startsWith(CATEGORY_PREFIX)) {
            modulesPage.setCategory(Category.valueOf(id.substring(CATEGORY_PREFIX.length())));
            currentPage = modulesPage;
        } else {
            currentPage = generalPages.get(id);
        }
        sidebar.setSelected(id);
        currentPage.onOpen();
    }

    @Override
    protected void init() {
        updateScale();
        int unitsWide = Math.round(width / menuScale);
        int unitsHigh = Math.round(height / menuScale);
        int windowWidth = clamp(unitsWide - Theme.WINDOW_MARGIN * 2, Theme.WINDOW_MIN_WIDTH, Theme.WINDOW_MAX_WIDTH);
        int windowHeight = clamp(unitsHigh - Theme.WINDOW_MARGIN * 2, Theme.WINDOW_MIN_HEIGHT, Theme.WINDOW_MAX_HEIGHT);
        window = new Bounds((unitsWide - windowWidth) / 2, (unitsHigh - windowHeight) / 2, windowWidth, windowHeight);
        lastFrameMs = 0;
        currentPage.onOpen();
    }

    /**
     * Picks a pixel scale so the full-size window covers about three quarters of the screen,
     * independent of Minecraft's GUI scale.
     */
    private void updateScale() {
        Window mcWindow = client.getWindow();
        float fit = Math.min(mcWindow.getFramebufferWidth() * 0.75f / Theme.WINDOW_MAX_WIDTH,
                mcWindow.getFramebufferHeight() * 0.8f / Theme.WINDOW_MAX_HEIGHT);
        pixelScale = Math.max(1.5f, Math.min(fit, 3f));
        menuScale = pixelScale / mcWindow.getScaleFactor();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    @Override
    public void renderBackground(DrawContext draw, int mouseX, int mouseY, float delta) {
        boolean blur = config.getGuiSettings().blur;
        if (blur || client == null || client.world == null) {
            super.renderBackground(draw, mouseX, mouseY, delta);
        }
        draw.fill(0, 0, width, height, blur ? Theme.SCREEN_OVERLAY : Theme.SCREEN_OVERLAY_NO_BLUR);
    }

    @Override
    public void render(DrawContext draw, int mouseX, int mouseY, float delta) {
        long now = Util.getMeasuringTimeMs();
        float deltaSeconds = lastFrameMs == 0 ? 0f : Math.min((now - lastFrameMs) / 1000f, 0.1f);
        lastFrameMs = now;
        context.onFrame();
        Animation.setEnabled(config.getGuiSettings().animations);
        RenderUtil.setPixelScale(pixelScale);
        int accent = context.accent().color();
        draw.getMatrices().pushMatrix();
        draw.getMatrices().scale(menuScale, menuScale);
        mouseX = Math.round(mouseX / menuScale);
        mouseY = Math.round(mouseY / menuScale);

        int x = window.x();
        int y = window.y();
        int radius = Theme.WINDOW_RADIUS;
        RenderUtil.shadow(draw, x, y, window.width(), window.height(), radius, Theme.SHADOW, 6);
        RenderUtil.roundedRect(draw, x - 1, y - 1, window.width() + 2, window.height() + 2, radius + 1, Theme.WINDOW_BORDER);
        RenderUtil.roundedRect(draw, x, y, window.width(), window.height(), radius, Theme.WINDOW_BACKGROUND);
        RenderUtil.roundedRect(draw, x, y, Theme.SIDEBAR_WIDTH + radius, window.height(), radius, Theme.SIDEBAR_BACKGROUND);
        RenderUtil.rect(draw, x + Theme.SIDEBAR_WIDTH, y, radius, window.height(), Theme.WINDOW_BACKGROUND);
        RenderUtil.rect(draw, x + Theme.SIDEBAR_WIDTH, y, 1, window.height(), Theme.DIVIDER);

        sidebar.render(draw, textRenderer, new Bounds(x, y, Theme.SIDEBAR_WIDTH, window.height()),
                mouseX, mouseY, accent, deltaSeconds);
        currentPage.render(draw, textRenderer, contentBounds(), mouseX, mouseY, deltaSeconds);
        draw.getMatrices().popMatrix();
    }

    private Bounds contentBounds() {
        return new Bounds(window.x() + Theme.SIDEBAR_WIDTH, window.y(), window.width() - Theme.SIDEBAR_WIDTH, window.height());
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double x = click.x() / menuScale;
        double y = click.y() / menuScale;
        String entry = sidebar.entryAt(x, y);
        if (entry != null) {
            select(entry);
            return true;
        }
        if (currentPage.mouseClicked(x, y, click.button())) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return currentPage.mouseScrolled(mouseX / menuScale, mouseY / menuScale, verticalAmount)
                || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return currentPage.keyPressed(input) || super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (context.consumeSwallowedChar()) {
            return true;
        }
        return currentPage.charTyped(input) || super.charTyped(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void removed() {
        context.cancelListening();
        config.save();
    }
}
