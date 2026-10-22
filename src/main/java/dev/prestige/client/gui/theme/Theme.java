package dev.prestige.client.gui.theme;

/** Colors (ARGB) and metrics shared by every ClickGUI component. */
public final class Theme {
    // Surfaces
    public static final int SCREEN_OVERLAY = 0xB0050506;
    public static final int PANEL_BACKGROUND = 0xFF111114;
    public static final int HEADER_BACKGROUND = 0xFF09090B;
    public static final int ROW_BACKGROUND = 0xFF111114;
    public static final int ROW_HOVER = 0xFF1D1D23;
    public static final int FOOTER_BACKGROUND = 0xF009090B;
    public static final int DIVIDER = 0xFF24242B;
    public static final int SHADOW = 0x30000000;

    // Text
    public static final int TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFF8B8B96;
    public static final int TEXT_MUTED = 0xFF5C5C66;

    // Toggle switch
    public static final int SWITCH_TRACK_OFF = 0xFF2C2C34;
    public static final int SWITCH_KNOB_OFF = 0xFF8B8B96;
    public static final int SWITCH_KNOB_ON = 0xFFFFFFFF;

    // Metrics
    public static final int PANEL_WIDTH = 124;
    public static final int HEADER_HEIGHT = 20;
    public static final int ROW_HEIGHT = 16;
    public static final int PANEL_BOTTOM_PADDING = 3;
    public static final int PADDING = 6;
    public static final int PANEL_GAP = 10;
    public static final int SCREEN_MARGIN = 16;
    public static final int FOOTER_HEIGHT = 20;
    public static final int SWITCH_WIDTH = 14;
    public static final int SWITCH_HEIGHT = 8;
    public static final int ACCENT_BAR_WIDTH = 2;

    // Animation speeds (higher is snappier)
    public static final float HOVER_SPEED = 18f;
    public static final float TOGGLE_SPEED = 14f;
    public static final float EXPAND_SPEED = 12f;

    private Theme() {
    }
}
