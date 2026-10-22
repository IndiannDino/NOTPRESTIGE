package dev.prestige.client.module.impl.combat;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;

public final class CooldownIndicator extends Module {
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
