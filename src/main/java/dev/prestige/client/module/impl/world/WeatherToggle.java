package dev.prestige.client.module.impl.world;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;

public final class WeatherToggle extends Module {
    public WeatherToggle() {
        super("WeatherToggle", "Hides client-side rain and snow.", Category.WORLD);
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
