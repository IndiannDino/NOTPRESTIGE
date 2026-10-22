package dev.prestige.client.module.impl.world;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.setting.NumberSetting;

public final class TimeChanger extends Module {
    private final NumberSetting time = add(new NumberSetting("Time", "Time of day in ticks (6000 = noon)", 6000, 0, 24000, 500));

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
