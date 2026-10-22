package dev.prestige.client.gui.theme;

/** Colors (ARGB) and metrics shared by every ClickGUI component. */
public final class Theme {
    // Surfaces
    public static final int SCREEN_OVERLAY = 0x60000000;
    public static final int SCREEN_OVERLAY_NO_BLUR = 0xA0000000;
    public static final int WINDOW_BACKGROUND = 0xFF0C0C13;
    public static final int WINDOW_BORDER = 0x1CFFFFFF;
    public static final int SIDEBAR_BACKGROUND = 0xFF0F0F18;
    public static final int DIVIDER = 0x12FFFFFF;
    public static final int CARD_BACKGROUND = 0xFF14141E;
    public static final int CARD_HOVER = 0xFF1A1A27;
    public static final int CARD_BORDER = 0xFF1D1D2A;
    public static final int INPUT_BACKGROUND = 0xFF12121B;
    public static final int INPUT_BORDER = 0xFF23232F;
    public static final int ITEM_HOVER = 0x0CFFFFFF;
    public static final int SHADOW = 0x60000000;

    // Text
    public static final int TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int TEXT_BODY = 0xFFD9DAE3;
    public static final int TEXT_SECONDARY = 0xFF8A8B9C;
    public static final int TEXT_MUTED = 0xFF5B5C6E;

    // Toggle switch
    public static final int SWITCH_TRACK_OFF = 0xFF2A2A38;
    public static final int SWITCH_KNOB_OFF = 0xFF9A9BAA;
    public static final int SWITCH_KNOB_ON = 0xFFFFFFFF;
    public static final int SWITCH_WIDTH = 16;
    public static final int SWITCH_HEIGHT = 9;

    // Window
    public static final int WINDOW_MAX_WIDTH = 600;
    public static final int WINDOW_MAX_HEIGHT = 360;
    public static final int WINDOW_MIN_WIDTH = 360;
    public static final int WINDOW_MIN_HEIGHT = 230;
    public static final int WINDOW_MARGIN = 20;
    public static final int WINDOW_RADIUS = 8;
    public static final int SIDEBAR_WIDTH = 108;

    // Content
    public static final int CONTENT_PADDING = 14;
    public static final int CONTENT_TOP = 52;
    public static final int CARD_RADIUS = 5;
    public static final int CARD_GAP = 5;
    public static final int ICON_SIZE = 8;

    // Animation speeds (higher is snappier)
    public static final float HOVER_SPEED = 18f;
    public static final float TOGGLE_SPEED = 14f;
    public static final float EXPAND_SPEED = 14f;
    public static final float SCROLL_SPEED = 16f;

    private Theme() {
    }
}
