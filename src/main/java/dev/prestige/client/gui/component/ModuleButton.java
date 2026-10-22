package dev.prestige.client.gui.component;

import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.module.Module;
import java.util.Locale;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;

/** One module row: name, keybind label and an animated on/off switch. */
public final class ModuleButton {
    private final Module module;
    private float hoverProgress;
    private float enabledProgress;

    public ModuleButton(Module module) {
        this.module = module;
        this.enabledProgress = module.isEnabled() ? 1f : 0f;
    }

    public Module getModule() {
        return module;
    }

    public void render(DrawContext context, TextRenderer font, int x, int y, int width, int accent,
                       boolean hovered, boolean listening, float deltaSeconds) {
        hoverProgress = Animation.approach(hoverProgress, hovered ? 1f : 0f, deltaSeconds, Theme.HOVER_SPEED);
        enabledProgress = Animation.approach(enabledProgress, module.isEnabled() ? 1f : 0f, deltaSeconds, Theme.TOGGLE_SPEED);

        int height = Theme.ROW_HEIGHT;
        RenderUtil.rect(context, x, y, width, height,
                RenderUtil.lerpColor(Theme.ROW_BACKGROUND, Theme.ROW_HOVER, hoverProgress));

        // Active indicator: accent bar on the left edge that fades in with the enabled state.
        if (enabledProgress > 0f) {
            RenderUtil.rect(context, x, y + 3, Theme.ACCENT_BAR_WIDTH, height - 6,
                    RenderUtil.withAlpha(accent, enabledProgress));
        }

        int switchX = x + width - Theme.PADDING - Theme.SWITCH_WIDTH;
        int switchY = y + (height - Theme.SWITCH_HEIGHT) / 2;
        renderSwitch(context, switchX, switchY, accent);

        int textY = Typography.centerY(font, y, height);
        int labelRight = switchX - 4;
        String label = keyLabel(listening);
        if (label != null) {
            int labelX = labelRight - font.getWidth(label);
            Typography.caption(context, font, label, labelX, textY, listening ? accent : Theme.TEXT_MUTED);
            labelRight = labelX - 4;
        }

        int nameX = x + Theme.PADDING + Theme.ACCENT_BAR_WIDTH;
        String name = font.trimToWidth(module.getName(), labelRight - nameX);
        Typography.body(context, font, name, nameX, textY,
                RenderUtil.lerpColor(Theme.TEXT_SECONDARY, Theme.TEXT_PRIMARY, enabledProgress));
    }

    private void renderSwitch(DrawContext context, int x, int y, int accent) {
        RenderUtil.roundedRect(context, x, y, Theme.SWITCH_WIDTH, Theme.SWITCH_HEIGHT,
                RenderUtil.lerpColor(Theme.SWITCH_TRACK_OFF, accent, enabledProgress));

        int knobSize = Theme.SWITCH_HEIGHT - 2;
        int travel = Theme.SWITCH_WIDTH - 2 - knobSize;
        int knobX = x + 1 + Math.round(travel * enabledProgress);
        RenderUtil.roundedRect(context, knobX, y + 1, knobSize, knobSize,
                RenderUtil.lerpColor(Theme.SWITCH_KNOB_OFF, Theme.SWITCH_KNOB_ON, enabledProgress));
    }

    private String keyLabel(boolean listening) {
        if (listening) {
            return "...";
        }
        if (!module.hasKeybind()) {
            return null;
        }
        return keyName(module.getKeybind());
    }

    public static String keyName(int keyCode) {
        return InputUtil.Type.KEYSYM.createFromCode(keyCode).getLocalizedText().getString().toUpperCase(Locale.ROOT);
    }
}
