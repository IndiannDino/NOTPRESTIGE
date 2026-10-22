package dev.prestige.client.config;

/** Persisted layout of one ClickGUI panel. */
public final class PanelState {
    public int x;
    public int y;
    public boolean expanded = true;
    /** False until the user drags the panel; unplaced panels use the default layout. */
    public boolean placed;
}
