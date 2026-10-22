package dev.prestige.client.gui.component.setting;

import dev.prestige.client.module.setting.BooleanSetting;
import dev.prestige.client.module.setting.ColorSetting;
import dev.prestige.client.module.setting.ModeSetting;
import dev.prestige.client.module.setting.NumberSetting;
import dev.prestige.client.module.setting.Setting;
import java.util.List;

/** Creates the menu row that matches each setting type. */
public final class SettingRows {
    private SettingRows() {
    }

    public static SettingRow<?> create(Setting<?> setting) {
        if (setting instanceof BooleanSetting bool) {
            return new BooleanRow(bool);
        }
        if (setting instanceof NumberSetting number) {
            return new NumberRow(number);
        }
        if (setting instanceof ModeSetting mode) {
            return new ModeRow(mode);
        }
        if (setting instanceof ColorSetting color) {
            return new ColorRow(color);
        }
        throw new IllegalArgumentException("No menu row for " + setting.getClass().getName());
    }

    public static List<SettingRow<?>> createAll(List<Setting<?>> settings) {
        return settings.stream().<SettingRow<?>>map(SettingRows::create).toList();
    }
}
