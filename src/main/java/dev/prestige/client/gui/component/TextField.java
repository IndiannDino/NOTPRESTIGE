package dev.prestige.client.gui.component;

import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.render.Icons;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/** Minimal single-line text input with placeholder, optional leading icon and blinking caret. */
public final class TextField {
    public static final int HEIGHT = 15;

    private final String placeholder;
    private final int maxLength;
    @Nullable
    private final Identifier icon;
    private String text = "";
    private boolean focused;
    private Bounds bounds = new Bounds(0, 0, 0, 0);

    public TextField(String placeholder, int maxLength, @Nullable Identifier icon) {
        this.placeholder = placeholder;
        this.maxLength = maxLength;
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public void render(DrawContext context, TextRenderer font, int x, int y, int width, int accent) {
        bounds = new Bounds(x, y, width, HEIGHT);
        int border = focused ? RenderUtil.withAlpha(accent, 0.7f) : Theme.INPUT_BORDER;
        RenderUtil.roundedRect(context, x, y, width, HEIGHT, 4, border);
        RenderUtil.roundedRect(context, x + 1, y + 1, width - 2, HEIGHT - 2, 3, Theme.INPUT_BACKGROUND);

        int textX = x + 6;
        if (icon != null) {
            Icons.draw(context, icon, textX, y + (HEIGHT - 7) / 2, 7, Theme.TEXT_SECONDARY);
            textX += 11;
        }
        int textY = Typography.centerY(Font.BODY, y, HEIGHT);
        int available = x + width - 6 - textX;
        if (text.isEmpty()) {
            Typography.draw(context, font, Font.BODY, Typography.trim(font, Font.BODY, placeholder, available),
                    textX, textY, Theme.TEXT_MUTED);
        }
        String visible = text;
        while (!visible.isEmpty() && Typography.width(font, Font.BODY, visible) > available - 3) {
            visible = visible.substring(1);
        }
        int textWidth = Typography.draw(context, font, Font.BODY, visible, textX, textY, Theme.TEXT_PRIMARY);
        if (focused && (Util.getMeasuringTimeMs() / 500) % 2 == 0) {
            int caretX = textX + (visible.isEmpty() ? 0 : textWidth + 1);
            RenderUtil.rect(context, caretX, y + 4, 1, HEIGHT - 8, Theme.TEXT_PRIMARY);
        }
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY);
    }

    /** Appends a typed character. Returns true if it was accepted. */
    public boolean charTyped(CharInput input) {
        if (!focused || !input.isValidChar()) {
            return false;
        }
        if (text.length() < maxLength) {
            text += input.asString();
        }
        return true;
    }

    /** Handles editing keys. Ctrl+Backspace clears the field. */
    public boolean keyPressed(KeyInput input) {
        if (!focused || input.key() != GLFW.GLFW_KEY_BACKSPACE) {
            return false;
        }
        if ((input.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
            text = "";
        } else if (!text.isEmpty()) {
            text = text.substring(0, text.offsetByCodePoints(text.length(), -1));
        }
        return true;
    }
}
