package dev.prestige.client.module.setting;

import com.google.gson.JsonElement;
import java.util.function.BooleanSupplier;

/**
 * A single configurable value on a module. Settings are declared in the module's constructor
 * through {@code Module.add(...)}, shown in the module's card and saved with its config.
 */
public abstract class Setting<T> {
    private final String name;
    private final String description;
    private final T defaultValue;
    private T value;
    private BooleanSupplier visibility = () -> true;

    protected Setting(String name, String description, T defaultValue) {
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = sanitize(value);
    }

    public T getDefault() {
        return defaultValue;
    }

    public void reset() {
        value = defaultValue;
    }

    /** Hides the setting in the menu unless {@code condition} holds, e.g. only for one mode. */
    @SuppressWarnings("unchecked")
    public <S extends Setting<T>> S visibleWhen(BooleanSupplier condition) {
        this.visibility = condition;
        return (S) this;
    }

    public boolean isVisible() {
        return visibility.getAsBoolean();
    }

    /** Clamps or otherwise corrects an incoming value. */
    protected T sanitize(T value) {
        return value;
    }

    public abstract JsonElement toJson();

    /** Applies a saved value; malformed input leaves the current value untouched. */
    public abstract void fromJson(JsonElement json);
}
