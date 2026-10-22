package dev.prestige.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Locale;

/** An ARGB color, saved as a {@code #AARRGGBB} hex string. */
public final class ColorSetting extends Setting<Integer> {
    public ColorSetting(String name, String description, int defaultArgb) {
        super(name, description, defaultArgb);
    }

    public int argb() {
        return get();
    }

    public String toHex() {
        return String.format(Locale.ROOT, "#%08X", get());
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(toHex());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (!json.isJsonPrimitive() || !json.getAsJsonPrimitive().isString()) {
            return;
        }
        String hex = json.getAsString();
        if (hex.matches("#[0-9A-Fa-f]{8}")) {
            set((int) Long.parseLong(hex.substring(1), 16));
        }
    }
}
