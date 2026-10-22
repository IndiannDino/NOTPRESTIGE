package dev.prestige.client.gui.render;

import dev.prestige.client.PrestigeClient;
import dev.prestige.client.module.Category;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

/** White alpha-mask icons, tinted at draw time. */
public final class Icons {
    public static final Identifier COMBAT = icon("combat");
    public static final Identifier MOVEMENT = icon("movement");
    public static final Identifier RENDER = icon("render");
    public static final Identifier WORLD = icon("world");
    public static final Identifier SEARCH = icon("search");
    public static final Identifier CHEVRON_DOWN = icon("chevron_down");
    public static final Identifier CHEVRON_RIGHT = icon("chevron_right");
    public static final Identifier SETTINGS = icon("settings");
    public static final Identifier THEME = icon("theme");
    public static final Identifier CONFIGS = icon("configs");
    public static final Identifier KEYBINDS = icon("keybinds");
    public static final Identifier LIST = icon("list");
    public static final Identifier GRID = icon("grid");
    public static final Identifier PLUS = icon("plus");

    private Icons() {
    }

    private static Identifier icon(String name) {
        return Identifier.of(PrestigeClient.MOD_ID, "textures/gui/icon/" + name + ".png");
    }

    public static Identifier forCategory(Category category) {
        return switch (category) {
            case COMBAT -> COMBAT;
            case MOVEMENT -> MOVEMENT;
            case RENDER -> RENDER;
            case WORLD -> WORLD;
        };
    }

    public static void draw(DrawContext context, Identifier icon, int x, int y, int size, int color) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, icon, x, y, 0f, 0f, size, size, size, size, color);
    }
}
