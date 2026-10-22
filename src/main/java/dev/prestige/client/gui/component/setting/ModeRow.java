package dev.prestige.client.gui.component.setting;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.render.Icons;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import dev.prestige.client.module.setting.ModeSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/** Mode selector: left-click for the next mode, right-click for the previous one. */
final class ModeRow extends SettingRow<ModeSetting> {
    private static final int CHIP_HEIGHT = 13;

    ModeRow(ModeSetting setting) {
        super(setting);
    }

    @Override
    public int height() {
        return 20;
    }

    @Override
    public void render(DrawContext draw, TextRenderer font, int x, int y, int width, int mouseX, int mouseY,
                       int accent, float deltaSeconds) {
        bounds = new Bounds(x, y, width, height());
        Typography.draw(draw, font, Font.BODY, setting.getName(), x, Typography.centerY(Font.BODY, y, height()),
                Theme.TEXT_SECONDARY);

        int textWidth = Typography.width(font, Font.BOLD, setting.get());
        int chipWidth = textWidth + 22;
        int chipX = x + width - chipWidth;
        int chipY = y + (height() - CHIP_HEIGHT) / 2;
        boolean hovered = bounds.contains(mouseX, mouseY);
        RenderUtil.roundedRect(draw, chipX, chipY, chipWidth, CHIP_HEIGHT, 3,
                hovered ? RenderUtil.withAlpha(accent, 0.8f) : Theme.INPUT_BORDER);
        RenderUtil.roundedRect(draw, chipX + 1, chipY + 1, chipWidth - 2, CHIP_HEIGHT - 2, 2, Theme.INPUT_BACKGROUND);
        Typography.draw(draw, font, Font.BOLD, setting.get(), chipX + 7,
                Typography.centerY(Font.BOLD, chipY, CHIP_HEIGHT), Theme.TEXT_PRIMARY);
        Icons.draw(draw, Icons.CHEVRON_RIGHT, chipX + chipWidth - 11, chipY + (CHIP_HEIGHT - 6) / 2, 6,
                hovered ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            setting.cycle(1);
            return true;
        }
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setting.cycle(-1);
            return true;
        }
        return false;
    }
}
