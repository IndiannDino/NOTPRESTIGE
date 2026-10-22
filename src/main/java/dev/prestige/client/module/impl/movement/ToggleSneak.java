package dev.prestige.client.module.impl.movement;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;

public final class ToggleSneak extends Module {
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
