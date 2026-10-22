package dev.prestige.client.module.impl.render;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.setting.ModeSetting;
import dev.prestige.client.module.setting.NumberSetting;

public final class Fullbright extends Module {
    private final ModeSetting mode = add(new ModeSetting("Mode", "How brightness is applied", "Gamma", "Gamma", "Night Vision"));
    private final NumberSetting brightness = add(new NumberSetting("Brightness", "Gamma level", 10, 1, 15, 0.5, "x")
            .visibleWhen(() -> mode.is("Gamma")));

    public Fullbright() {
        super("Fullbright", "Brightens dark areas of the world.", Category.RENDER);
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
