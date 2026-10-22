package dev.prestige.client.module.impl.render;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;

public final class BlockOutline extends Module {
    public BlockOutline() {
        super("BlockOutline", "Customizes the outline of the targeted block.", Category.RENDER);
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
