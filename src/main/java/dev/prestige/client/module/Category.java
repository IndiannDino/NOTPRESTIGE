package dev.prestige.client.module;

/**
 * Top-level grouping for modules. Each category is rendered as its own ClickGUI panel
 * and carries the accent color used for its header and active-state indicators.
 */
public enum Category {
    COMBAT("Combat", 0xFFE5484D),
    MOVEMENT("Movement", 0xFF3E9BFF),
    RENDER("Render", 0xFFB46CFF),
    WORLD("World", 0xFF3DD68C);

    private final String displayName;
    private final int accentColor;

    Category(String displayName, int accentColor) {
        this.displayName = displayName;
        this.accentColor = accentColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** ARGB accent color for this category. */
    public int getAccentColor() {
        return accentColor;
    }
}
