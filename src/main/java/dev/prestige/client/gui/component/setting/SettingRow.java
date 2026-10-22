package dev.prestige.client.gui.component.setting;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.module.setting.Setting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Menu row for one module setting, drawn inside an expanded module card. */
public abstract class SettingRow<S extends Setting<?>> {
    protected final S setting;
    protected Bounds bounds = new Bounds(0, 0, 0, 0);

    protected SettingRow(S setting) {
        this.setting = setting;
    }

    public S getSetting() {
        return setting;
    }

    public abstract int height();

    /** Draws the row; {@code bounds} must be updated so clicks can be hit-tested. */
    public abstract void render(DrawContext draw, TextRenderer font, int x, int y, int width, int mouseX, int mouseY,
                                int accent, float deltaSeconds);

    public boolean isMouseOver(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY);
    }

    /** Returns true if the click was used. */
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);

    /** True while this row owns an in-progress drag. */
    public boolean isDragging() {
        return false;
    }

    public void mouseDragged(double mouseX, double mouseY) {
    }

    public void mouseReleased() {
    }
}
