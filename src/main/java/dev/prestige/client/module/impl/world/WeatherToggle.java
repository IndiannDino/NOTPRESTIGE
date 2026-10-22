package dev.prestige.client.module.impl.world;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.setting.BooleanSetting;

public final class WeatherToggle extends Module {
    private final BooleanSetting hideRain = add(new BooleanSetting("Hide rain", "Remove rain and its sound", true));
    private final BooleanSetting hideSnow = add(new BooleanSetting("Hide snow", "Remove snowfall", true));
    private final BooleanSetting hideThunder = add(new BooleanSetting("Hide thunder", "Remove lightning flashes", false));

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
