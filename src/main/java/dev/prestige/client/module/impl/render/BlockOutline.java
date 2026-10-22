package dev.prestige.client.module.impl.render;

import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.setting.BooleanSetting;
import dev.prestige.client.module.setting.ColorSetting;
import dev.prestige.client.module.setting.NumberSetting;

public final class BlockOutline extends Module {
    private final ColorSetting color = add(new ColorSetting("Color", "Outline color", 0xFF7C6CF6));
    private final NumberSetting thickness = add(new NumberSetting("Thickness", "Line width", 2, 1, 6, 0.5, "px"));
    private final BooleanSetting fill = add(new BooleanSetting("Fill", "Tint the block faces too", false));
    private final ColorSetting fillColor = add(new ColorSetting("Fill color", "Face tint color", 0x407C6CF6)
            .visibleWhen(fill::isOn));

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
