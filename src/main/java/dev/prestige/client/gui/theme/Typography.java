package dev.prestige.client.gui.theme;

import dev.prestige.client.PrestigeClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * The only place the ClickGUI draws text. Body text uses the bundled Inter font, headings and
 * branding use Instrument Serif; both fall back to the vanilla font for missing glyphs.
 */
public final class Typography {
    private static final String ELLIPSIS = "…";

    public enum Font {
        BODY("body", 8),
        BOLD("body_bold", 8),
        CAPTION("caption", 7),
        DISPLAY("display", 12),
        DISPLAY_ITALIC("display_italic", 12),
        BRAND("brand", 8),
        BRAND_ITALIC("brand_italic", 8);

        private final Style style;
        /** Visual height of a line, used for vertical centering. */
        private final int lineHeight;

        Font(String path, int lineHeight) {
            this.style = Style.EMPTY.withFont(new StyleSpriteSource.Font(Identifier.of(PrestigeClient.MOD_ID, path)));
            this.lineHeight = lineHeight;
        }

        public Text text(String text) {
            return Text.literal(text).setStyle(style);
        }

        public int lineHeight() {
            return lineHeight;
        }
    }

    private Typography() {
    }

    /** Draws text and returns its width. */
    public static int draw(DrawContext context, TextRenderer renderer, Font font, String text, int x, int y, int color) {
        context.drawText(renderer, font.text(text), x, y, color, false);
        return width(renderer, font, text);
    }

    public static int width(TextRenderer renderer, Font font, String text) {
        return renderer.getWidth(font.text(text));
    }

    /** Shortens text with an ellipsis so it fits in {@code maxWidth}. */
    public static String trim(TextRenderer renderer, Font font, String text, int maxWidth) {
        if (width(renderer, font, text) <= maxWidth) {
            return text;
        }
        String trimmed = text;
        while (!trimmed.isEmpty() && width(renderer, font, trimmed + ELLIPSIS) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed + ELLIPSIS;
    }

    /** Y that vertically centers a line of the given font inside a box. */
    public static int centerY(Font font, int top, int height) {
        return top + (height - font.lineHeight()) / 2;
    }
}
