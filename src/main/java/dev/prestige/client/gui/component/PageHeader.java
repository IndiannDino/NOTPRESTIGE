package dev.prestige.client.gui.component;

import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/** Serif page title with an italic accent word, e.g. "<i>Visual</i> Modules", plus a subtitle. */
public final class PageHeader {
    private PageHeader() {
    }

    public static void render(DrawContext context, TextRenderer font, int x, int y,
                              String accentWord, String rest, String subtitle, int accent) {
        int width = Typography.draw(context, font, Font.DISPLAY_ITALIC, accentWord, x, y, accent);
        if (!rest.isEmpty()) {
            Typography.draw(context, font, Font.DISPLAY, " " + rest, x + width, y, Theme.TEXT_PRIMARY);
        }
        Typography.draw(context, font, Font.CAPTION, subtitle, x, y + 17, Theme.TEXT_SECONDARY);
    }
}
