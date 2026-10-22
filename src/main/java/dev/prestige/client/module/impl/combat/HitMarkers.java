package dev.prestige.client.module.impl.combat;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;

public final class HitMarkers extends Module {
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
