package dev.prestige.client.module.impl.combat;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.setting.BooleanSetting;
import dev.prestige.client.module.setting.ColorSetting;
import dev.prestige.client.module.setting.NumberSetting;

public final class HitMarkers extends Module {
    private final ColorSetting color = add(new ColorSetting("Color", "Marker color", 0xFFFFFFFF));
    private final NumberSetting size = add(new NumberSetting("Size", "Marker size in pixels", 5, 2, 12, 1, "px"));
    private final NumberSetting duration = add(new NumberSetting("Duration", "How long the marker stays", 0.4, 0.1, 2.0, 0.1, "s"));
    private final BooleanSetting sound = add(new BooleanSetting("Sound", "Play a click when a hit lands", false));

    public HitMarkers() {
        super("HitMarkers", "Shows a crosshair marker when your attacks land.", Category.COMBAT);
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
