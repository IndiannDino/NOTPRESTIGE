package dev.prestige.client.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** A number between {@code min} and {@code max}, snapped to {@code step}. */
public final class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;
    private final String suffix;

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double step) {
        this(name, description, defaultValue, min, max, step, "");
    }

    public NumberSetting(String name, String description, double defaultValue, double min, double max, double step,
                         String suffix) {
        super(name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
        this.suffix = suffix;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }

    public int intValue() {
        return (int) Math.round(get());
    }

    public float floatValue() {
        return get().floatValue();
    }

    /** Position of the value between min and max, 0..1. */
    public double getProgress() {
        return (get() - min) / (max - min);
    }

    public void setProgress(double progress) {
        set(min + (max - min) * progress);
    }

    /** The value formatted with as many decimals as the step needs, plus the suffix. */
    public String format() {
        int decimals = step >= 1 ? 0 : (int) Math.ceil(-Math.log10(step));
        return String.format(java.util.Locale.ROOT, "%." + decimals + "f", get()) + suffix;
    }

    @Override
    protected Double sanitize(Double value) {
        double snapped = min + Math.round((value - min) / step) * step;
        return Math.max(min, Math.min(max, snapped));
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(get());
    }

    @Override
    public void fromJson(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            set(json.getAsDouble());
        }
    }
}
