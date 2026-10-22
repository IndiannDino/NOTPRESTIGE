package dev.prestige.client.gui.page;

import dev.prestige.client.gui.Bounds;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

/** One screen of content shown to the right of the sidebar. */
public interface Page {
    void render(DrawContext context, TextRenderer font, Bounds area, int mouseX, int mouseY, float deltaSeconds);

    /** Called whenever the page becomes visible. */
    default void onOpen() {
    }

    default boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    default boolean keyPressed(KeyInput input) {
        return false;
    }

    default boolean charTyped(CharInput input) {
        return false;
    }
}
