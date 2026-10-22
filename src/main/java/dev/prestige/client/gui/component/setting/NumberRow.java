package dev.prestige.client.gui.component.setting;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import dev.prestige.client.module.setting.NumberSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/** Label and value on top, draggable slider underneath. */
final class NumberRow extends SettingRow<NumberSetting> {
    private static final int TRACK_HEIGHT = 3;
    private static final int KNOB = 7;

    private Bounds track = new Bounds(0, 0, 0, 0);
    private boolean dragging;
    private float shown;

    NumberRow(NumberSetting setting) {
        super(setting);
        shown = (float) setting.getProgress();
    }

    @Override
    public int height() {
        return 26;
    }

    @Override
    public void render(DrawContext draw, TextRenderer font, int x, int y, int width, int mouseX, int mouseY,
                       int accent, float deltaSeconds) {
        bounds = new Bounds(x, y, width, height());
        shown = dragging ? (float) setting.getProgress()
                : Animation.approach(shown, (float) setting.getProgress(), deltaSeconds, Theme.TOGGLE_SPEED);

        Typography.draw(draw, font, Font.BODY, setting.getName(), x, y + 3, Theme.TEXT_SECONDARY);
        String value = setting.format();
        Typography.draw(draw, font, Font.BOLD, value, x + width - Typography.width(font, Font.BOLD, value), y + 3,
                Theme.TEXT_PRIMARY);

        int trackY = y + 17;
        track = new Bounds(x, trackY - 4, width, TRACK_HEIGHT + 8);
        RenderUtil.roundedRect(draw, x, trackY, width, TRACK_HEIGHT, 1.5f, Theme.SWITCH_TRACK_OFF);
        float filled = width * shown;
        RenderUtil.roundedRect(draw, x, trackY, Math.max(filled, TRACK_HEIGHT), TRACK_HEIGHT, 1.5f, accent);
        float knobX = x + filled - KNOB / 2f;
        knobX = Math.max(x - 1, Math.min(knobX, x + width - KNOB + 1));
        boolean active = dragging || track.contains(mouseX, mouseY);
        int knobSize = active ? KNOB + 2 : KNOB;
        float offset = (knobSize - KNOB) / 2f;
        RenderUtil.roundedRect(draw, knobX - offset, trackY + TRACK_HEIGHT / 2f - knobSize / 2f, knobSize, knobSize,
                knobSize / 2f, Theme.SWITCH_KNOB_ON);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !track.contains(mouseX, mouseY)) {
            return false;
        }
        dragging = true;
        mouseDragged(mouseX, mouseY);
        return true;
    }

    @Override
    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY) {
        if (dragging && track.width() > 0) {
            setting.setProgress(Math.max(0, Math.min(1, (mouseX - track.x()) / track.width())));
        }
    }

    @Override
    public void mouseReleased() {
        dragging = false;
    }
}
