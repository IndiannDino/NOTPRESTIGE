package dev.prestige.client.gui.component.setting;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.render.ColorUtil;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import dev.prestige.client.module.setting.ColorSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/**
 * Color swatch that opens an inline picker with hue, saturation, brightness and opacity sliders.
 * Keeps its own HSV state so the hue survives while saturation or brightness is zero.
 */
final class ColorRow extends SettingRow<ColorSetting> {
    private static final int HEADER_HEIGHT = 20;
    private static final int SLIDER_HEIGHT = 13;
    private static final int GRADIENT_STEPS = 32;
    private static final String[] LABELS = {"Hue", "Saturation", "Brightness", "Opacity"};

    private final Bounds[] sliders = new Bounds[LABELS.length];
    private Bounds swatch = new Bounds(0, 0, 0, 0);
    private boolean open;
    private int dragging = -1;
    private float hue;
    private float saturation;
    private float value;
    private int lastSynced;

    ColorRow(ColorSetting setting) {
        super(setting);
        syncFromSetting();
    }

    private void syncFromSetting() {
        float[] hsv = ColorUtil.rgbToHsv(setting.argb());
        if (hsv[1] > 0 && hsv[2] > 0) {
            hue = hsv[0];
        }
        saturation = hsv[1];
        value = hsv[2];
        lastSynced = setting.argb();
    }

    private int alpha() {
        return setting.argb() >>> 24;
    }

    @Override
    public int height() {
        return open ? HEADER_HEIGHT + LABELS.length * SLIDER_HEIGHT + 4 : HEADER_HEIGHT;
    }

    @Override
    public void render(DrawContext draw, TextRenderer font, int x, int y, int width, int mouseX, int mouseY,
                       int accent, float deltaSeconds) {
        if (setting.argb() != lastSynced) {
            // Changed elsewhere, e.g. a config was applied.
            syncFromSetting();
        }
        bounds = new Bounds(x, y, width, height());
        Typography.draw(draw, font, Font.BODY, setting.getName(), x, Typography.centerY(Font.BODY, y, HEADER_HEIGHT),
                Theme.TEXT_SECONDARY);

        int swatchWidth = 22;
        int swatchHeight = 11;
        swatch = new Bounds(x + width - swatchWidth, y + (HEADER_HEIGHT - swatchHeight) / 2, swatchWidth, swatchHeight);
        RenderUtil.roundedRect(draw, swatch.x() - 1, swatch.y() - 1, swatch.width() + 2, swatch.height() + 2, 3,
                open ? accent : Theme.INPUT_BORDER);
        renderChecker(draw, swatch);
        RenderUtil.roundedRect(draw, swatch.x(), swatch.y(), swatch.width(), swatch.height(), 2, setting.argb());
        String hex = setting.toHex();
        Typography.draw(draw, font, Font.CAPTION, hex, swatch.x() - 5 - Typography.width(font, Font.CAPTION, hex),
                Typography.centerY(Font.CAPTION, y, HEADER_HEIGHT), Theme.TEXT_MUTED);

        if (!open) {
            return;
        }
        int labelWidth = 52;
        int sliderX = x + labelWidth;
        int sliderWidth = width - labelWidth;
        for (int i = 0; i < LABELS.length; i++) {
            int rowY = y + HEADER_HEIGHT + i * SLIDER_HEIGHT;
            Typography.draw(draw, font, Font.CAPTION, LABELS[i], x + 6, Typography.centerY(Font.CAPTION, rowY, SLIDER_HEIGHT),
                    Theme.TEXT_MUTED);
            Bounds slider = new Bounds(sliderX, rowY + 3, sliderWidth, SLIDER_HEIGHT - 6);
            sliders[i] = slider;
            renderGradient(draw, i, slider);
            float position = switch (i) {
                case 0 -> hue;
                case 1 -> saturation;
                case 2 -> value;
                default -> alpha() / 255f;
            };
            int knobX = Math.round(slider.x() + position * slider.width()) - 1;
            RenderUtil.rect(draw, knobX - 1, slider.y() - 2, 4, slider.height() + 4, 0xFF000000);
            RenderUtil.rect(draw, knobX, slider.y() - 1, 2, slider.height() + 2, 0xFFFFFFFF);
        }
    }

    private void renderGradient(DrawContext draw, int channel, Bounds slider) {
        if (channel == 3) {
            renderChecker(draw, slider);
        }
        for (int step = 0; step < GRADIENT_STEPS; step++) {
            float t = step / (float) (GRADIENT_STEPS - 1);
            int color = switch (channel) {
                case 0 -> ColorUtil.hsvToRgb(t, 1, 1, 255);
                case 1 -> ColorUtil.hsvToRgb(hue, t, value, 255);
                case 2 -> ColorUtil.hsvToRgb(hue, saturation, t, 255);
                default -> ColorUtil.hsvToRgb(hue, saturation, value, Math.round(t * 255));
            };
            int x1 = slider.x() + slider.width() * step / GRADIENT_STEPS;
            int x2 = slider.x() + slider.width() * (step + 1) / GRADIENT_STEPS;
            draw.fill(x1, slider.y(), x2, slider.bottom(), color);
        }
    }

    private static void renderChecker(DrawContext draw, Bounds area) {
        int cell = 3;
        for (int cx = 0; cx < area.width(); cx += cell) {
            for (int cy = 0; cy < area.height(); cy += cell) {
                int color = ((cx / cell + cy / cell) % 2 == 0) ? 0xFF3A3A44 : 0xFF24242C;
                draw.fill(area.x() + cx, area.y() + cy, Math.min(area.x() + cx + cell, area.right()),
                        Math.min(area.y() + cy + cell, area.bottom()), color);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (mouseY < bounds.y() + HEADER_HEIGHT) {
            open = !open;
            return true;
        }
        for (int i = 0; i < LABELS.length && open; i++) {
            Bounds slider = sliders[i];
            if (slider != null && mouseY >= slider.y() - 3 && mouseY < slider.bottom() + 3
                    && mouseX >= slider.x() - 3 && mouseX < slider.right() + 3) {
                dragging = i;
                mouseDragged(mouseX, mouseY);
                return true;
            }
        }
        return true;
    }

    @Override
    public boolean isDragging() {
        return dragging >= 0;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY) {
        if (dragging < 0) {
            return;
        }
        Bounds slider = sliders[dragging];
        float t = (float) Math.max(0, Math.min(1, (mouseX - slider.x()) / slider.width()));
        int alpha = alpha();
        switch (dragging) {
            case 0 -> hue = Math.min(t, 0.999f);
            case 1 -> saturation = t;
            case 2 -> value = t;
            default -> alpha = Math.round(t * 255);
        }
        setting.set(ColorUtil.hsvToRgb(hue, saturation, value, alpha));
        lastSynced = setting.argb();
    }

    @Override
    public void mouseReleased() {
        dragging = -1;
    }
}
