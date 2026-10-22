package dev.prestige.client.gui.render;

/** Frame-rate independent easing helpers. */
public final class Animation {
    private static boolean enabled = true;

    private Animation() {
    }

    /** When disabled, every animation snaps straight to its target. */
    public static void setEnabled(boolean enabled) {
        Animation.enabled = enabled;
    }

    /**
     * Exponentially approaches {@code target}. {@code speed} is roughly "fraction of the
     * remaining distance covered per second", so results look the same at any FPS.
     */
    public static float approach(float current, float target, float deltaSeconds, float speed) {
        if (!enabled) {
            return target;
        }
        float next = current + (target - current) * (1f - (float) Math.exp(-speed * deltaSeconds));
        return Math.abs(target - next) < 0.001f ? target : next;
    }
}
