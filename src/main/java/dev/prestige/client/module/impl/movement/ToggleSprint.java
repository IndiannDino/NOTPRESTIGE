package dev.prestige.client.module.impl.movement;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;

public final class ToggleSprint extends Module {
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
