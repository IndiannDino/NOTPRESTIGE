package dev.prestige.client.gui.render;

/** Frame-rate independent easing helpers. */
public final class Animation {
    private Animation() {
    }

    /**
     * Exponentially approaches {@code target}. {@code speed} is roughly "fraction of the
     * remaining distance covered per second", so results look the same at any FPS.
     */
    public static float approach(float current, float target, float deltaSeconds, float speed) {
        float next = current + (target - current) * (1f - (float) Math.exp(-speed * deltaSeconds));
        return Math.abs(target - next) < 0.001f ? target : next;
    }
}
