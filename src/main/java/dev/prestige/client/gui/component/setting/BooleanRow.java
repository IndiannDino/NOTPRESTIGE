package dev.prestige.client.gui.component.setting;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.component.ToggleSwitch;
import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import dev.prestige.client.module.setting.BooleanSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

final class BooleanRow extends SettingRow<BooleanSetting> {
    private float progress;

    BooleanRow(BooleanSetting setting) {
        super(setting);
        progress = setting.isOn() ? 1f : 0f;
    }

    @Override
    public int height() {
        return 20;
    }

    @Override
    public void render(DrawContext draw, TextRenderer font, int x, int y, int width, int mouseX, int mouseY,
                       int accent, float deltaSeconds) {
        bounds = new Bounds(x, y, width, height());
        progress = Animation.approach(progress, setting.isOn() ? 1f : 0f, deltaSeconds, Theme.TOGGLE_SPEED);
        Typography.draw(draw, font, Font.BODY, setting.getName(), x, Typography.centerY(Font.BODY, y, height()),
                Theme.TEXT_SECONDARY);
        ToggleSwitch.render(draw, x + width - Theme.SWITCH_WIDTH, y + (height() - Theme.SWITCH_HEIGHT) / 2, progress, accent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        setting.toggle();
        return true;
    }
}
