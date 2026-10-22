package dev.prestige.client.module.impl.movement;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.setting.BooleanSetting;

public final class ToggleSneak extends Module {
    private final BooleanSetting showStatus = add(new BooleanSetting("Show status", "Show [Sneaking] on the HUD", true));

    public ToggleSneak() {
        super("ToggleSneak", "Keeps sneaking without holding the sneak key.", Category.MOVEMENT);
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
