package dev.prestige.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.List;

/** One choice out of a fixed list of named modes. */
public final class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String description, String defaultValue, String... modes) {
        super(name, description, defaultValue);
        this.modes = List.of(modes);
        if (!this.modes.contains(defaultValue)) {
            throw new IllegalArgumentException("Default mode '" + defaultValue + "' is not one of " + this.modes);
        }
    }

    public List<String> getModes() {
        return modes;
    }

    public boolean is(String mode) {
        return get().equals(mode);
    }

    /** Moves to the next mode (or previous, for a negative direction), wrapping around. */
    public void cycle(int direction) {
        int index = modes.indexOf(get());
        set(modes.get(Math.floorMod(index + direction, modes.size())));
    }

    @Override
    protected String sanitize(String value) {
        return modes.contains(value) ? value : get();
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
            set(json.getAsString());
        }
    }
}
