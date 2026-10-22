package dev.prestige.client.gui.component;

import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import net.minecraft.client.gui.DrawContext;

/** Pill switch whose knob position and color follow an animated 0..1 progress. */
public final class ToggleSwitch {
    private ToggleSwitch() {
    }

    public static void render(DrawContext context, int x, int y, float progress, int accent) {
        float height = Theme.SWITCH_HEIGHT;
        RenderUtil.roundedRect(context, x, y, Theme.SWITCH_WIDTH, height, height / 2f,
                RenderUtil.lerpColor(Theme.SWITCH_TRACK_OFF, accent, progress));
        float knob = height - 2;
        float travel = Theme.SWITCH_WIDTH - 2 - knob;
        RenderUtil.roundedRect(context, x + 1 + travel * progress, y + 1, knob, knob, knob / 2f,
                RenderUtil.lerpColor(Theme.SWITCH_KNOB_OFF, Theme.SWITCH_KNOB_ON, progress));
    }
}
