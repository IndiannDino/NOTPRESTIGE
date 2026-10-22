package dev.prestige.client.module.impl.combat;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.setting.ColorSetting;
import dev.prestige.client.module.setting.ModeSetting;
import dev.prestige.client.module.setting.NumberSetting;

public final class CooldownIndicator extends Module {
    private final ModeSetting style = add(new ModeSetting("Style", "How the cooldown is drawn", "Bar", "Bar", "Circle", "Text"));
    private final NumberSetting width = add(new NumberSetting("Width", "Bar width", 20, 10, 60, 1, "px")
            .visibleWhen(() -> style.is("Bar")));
    private final ColorSetting color = add(new ColorSetting("Color", "Indicator color", 0xFF7C6CF6));

    public CooldownIndicator() {
        super("CooldownIndicator", "Displays attack cooldown progress near the crosshair.", Category.COMBAT);
    }

    @Override
    protected void onEnable() {
        // TODO: feature setup
    }

    @Override
    protected void onDisable() {
        // TODO: feature teardown
    }

    @Override
    public void onTick() {
        // TODO: per-tick feature logic
    }
}
