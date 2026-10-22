package dev.prestige.client.module.impl.world;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;

public final class TimeChanger extends Module {
    public TimeChanger() {
        super("TimeChanger", "Overrides the client-side time of day.", Category.WORLD);
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
