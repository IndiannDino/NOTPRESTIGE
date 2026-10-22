package dev.prestige.client.module.impl.movement;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.setting.BooleanSetting;
import dev.prestige.client.module.setting.ModeSetting;

public final class ToggleSprint extends Module {
    private final ModeSetting mode = add(new ModeSetting("Mode", "When to sprint", "Toggle", "Toggle", "Always"));
    private final BooleanSetting showStatus = add(new BooleanSetting("Show status", "Show [Sprinting] on the HUD", true));

    public ToggleSprint() {
        super("ToggleSprint", "Keeps sprinting without holding the sprint key.", Category.MOVEMENT);
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
