package dev.prestige.client.gui.theme;

import dev.prestige.client.module.Category;

/** Selectable highlight color for toggles, selections and headings. */
public enum Accent {
    LAVENDER("Lavender", 0xFF7C6CF6),
    BLUE("Blue", 0xFF3B7CF5),
    ROSE("Rose", 0xFFE5487A),
    EMERALD("Emerald", 0xFF22B573),
    AMBER("Amber", 0xFFE0A030),
    PER_CATEGORY("Category", 0xFF7C6CF6);

    private final String displayName;
    private final int color;

    Accent(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Accent for UI chrome that isn't tied to a category. */
    public int color() {
        return color;
    }

    public int color(Category category) {
        return this == PER_CATEGORY ? category.getAccentColor() : color;
    }

    public static Accent fromName(String name) {
        for (Accent accent : values()) {
            if (accent.name().equalsIgnoreCase(name)) {
                return accent;
            }
        }
        return LAVENDER;
    }
}
