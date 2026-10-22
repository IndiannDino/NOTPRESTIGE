package dev.prestige.client.gui.page;

import dev.prestige.client.config.GuiSettings;
import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.GuiContext;
import dev.prestige.client.gui.component.Card;
import dev.prestige.client.gui.component.PageHeader;
import dev.prestige.client.gui.component.ScrollArea;
import dev.prestige.client.gui.component.ToggleSwitch;
import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/** Toggles for the menu's own behaviour. */
public final class SettingsPage implements Page {
    private static final int CARD_HEIGHT = 28;

    private final GuiContext context;
    private final List<Setting> settings = List.of(
            new Setting("Background blur", "Blur the world behind the menu",
                    s -> s.blur, s -> s.blur = !s.blur),
            new Setting("Animations", "Smooth transitions throughout the menu",
                    s -> s.animations, s -> s.animations = !s.animations),
            new Setting("Module descriptions", "Show a short description on every module card",
                    s -> s.descriptions, s -> s.descriptions = !s.descriptions),
            new Setting("Grid view", "Show modules in two columns",
                    s -> s.gridView, s -> s.gridView = !s.gridView),
            new Setting("Toast notifications", "Show a card in the corner when a module is toggled in game",
                    s -> s.toasts, s -> s.toasts = !s.toasts),
            new Setting("Menu sounds", "Play a soft click when using the menu",
                    s -> s.sounds, s -> s.sounds = !s.sounds));
    private final ScrollArea scroll = new ScrollArea();
    private Bounds viewport = new Bounds(0, 0, 0, 0);
    private final List<Bounds> hits = new ArrayList<>();

    private static final class Setting {
        final String title;
        final String description;
        final Predicate<GuiSettings> getter;
        final Consumer<GuiSettings> toggler;
        float hover;
        float progress = -1f;

        Setting(String title, String description, Predicate<GuiSettings> getter, Consumer<GuiSettings> toggler) {
            this.title = title;
            this.description = description;
            this.getter = getter;
            this.toggler = toggler;
        }
    }

    public SettingsPage(GuiContext context) {
        this.context = context;
    }

    @Override
    public void render(DrawContext draw, TextRenderer font, Bounds area, int mouseX, int mouseY, float deltaSeconds) {
        int accent = context.accent().color();
        int pad = Theme.CONTENT_PADDING;
        int left = area.x() + pad;
        int width = area.width() - pad * 2;
        PageHeader.render(draw, font, left, area.y() + pad, "Settings", "", "Customize how the menu looks and behaves", accent);
        RenderUtil.rect(draw, left, area.y() + Theme.CONTENT_TOP - 6, width, 1, Theme.DIVIDER);

        hits.clear();
        viewport = new Bounds(left, area.y() + Theme.CONTENT_TOP, width, area.bottom() - pad - (area.y() + Theme.CONTENT_TOP));
        int contentHeight = settings.size() * (CARD_HEIGHT + Theme.CARD_GAP) - Theme.CARD_GAP;
        scroll.update(contentHeight, viewport.height(), deltaSeconds);
        int cardWidth = contentHeight > viewport.height() ? width - 6 : width;
        draw.enableScissor(viewport.x(), viewport.y(), viewport.right(), viewport.bottom());
        int y = viewport.y() - scroll.getOffset();
        for (Setting setting : settings) {
            Bounds card = new Bounds(left, y, cardWidth, CARD_HEIGHT);
            boolean on = setting.getter.test(context.settings());
            if (setting.progress < 0) {
                setting.progress = on ? 1f : 0f;
            }
            boolean hovered = viewport.contains(mouseX, mouseY) && card.contains(mouseX, mouseY);
            setting.hover = Animation.approach(setting.hover, hovered ? 1f : 0f, deltaSeconds, Theme.HOVER_SPEED);
            setting.progress = Animation.approach(setting.progress, on ? 1f : 0f, deltaSeconds, Theme.TOGGLE_SPEED);

            Card.render(draw, card.x(), card.y(), card.width(), card.height(), setting.hover);
            Typography.draw(draw, font, Font.BOLD, setting.title, card.x() + 10, card.y() + 6, Theme.TEXT_PRIMARY);
            Typography.draw(draw, font, Font.CAPTION, setting.description, card.x() + 10, card.y() + 17, Theme.TEXT_SECONDARY);
            ToggleSwitch.render(draw, card.right() - 10 - Theme.SWITCH_WIDTH,
                    card.y() + (CARD_HEIGHT - Theme.SWITCH_HEIGHT) / 2, setting.progress, accent);
            hits.add(card);
            y += CARD_HEIGHT + Theme.CARD_GAP;
        }
        draw.disableScissor();
        scroll.renderScrollbar(draw, viewport.right() - 2, viewport.y(), viewport.height(), accent);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !viewport.contains(mouseX, mouseY)) {
            return false;
        }
        for (int i = 0; i < hits.size(); i++) {
            if (hits.get(i).contains(mouseX, mouseY)) {
                settings.get(i).toggler.accept(context.settings());
                return true;
            }
        }
        return false;
    }
}
