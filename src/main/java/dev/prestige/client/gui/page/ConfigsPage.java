package dev.prestige.client.gui.page;

import dev.prestige.client.config.ConfigManager;
import dev.prestige.client.config.Profile;
import dev.prestige.client.gui.Bounds;
import dev.prestige.client.gui.GuiContext;
import dev.prestige.client.gui.component.Button;
import dev.prestige.client.gui.component.Card;
import dev.prestige.client.gui.component.PageHeader;
import dev.prestige.client.gui.component.ScrollArea;
import dev.prestige.client.gui.component.TextField;
import dev.prestige.client.gui.render.Animation;
import dev.prestige.client.gui.render.Icons;
import dev.prestige.client.gui.render.RenderUtil;
import dev.prestige.client.gui.theme.Theme;
import dev.prestige.client.gui.theme.Typography;
import dev.prestige.client.gui.theme.Typography.Font;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/** Save, apply and delete named module setups. */
public final class ConfigsPage implements Page {
    private static final int CARD_HEIGHT = 28;
    private static final long STATUS_MS = 3000;
    private static final long CONFIRM_MS = 3000;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final GuiContext context;
    private final TextField nameField = new TextField("Config name…", 32, null);
    private final Button saveButton = new Button(true, Icons.PLUS);
    private final Button folderButton = new Button(false, Icons.CONFIGS);
    private final ScrollArea scroll = new ScrollArea();
    private final Map<String, ProfileRow> rows = new HashMap<>();
    private List<Profile> profiles = List.of();
    private final List<ProfileRow> visibleRows = new ArrayList<>();
    private Bounds viewport = new Bounds(0, 0, 0, 0);

    private String status = "";
    private int statusColor = Theme.TEXT_SECONDARY;
    private long statusUntil;
    @Nullable
    private String pendingDelete;
    private long pendingDeleteUntil;

    private static final class ProfileRow {
        final Button apply = new Button(true, null);
        final Button delete = new Button(false, null);
        Profile profile;
        float hover;
    }

    public ConfigsPage(GuiContext context) {
        this.context = context;
    }

    @Override
    public void onOpen() {
        refresh();
    }

    private void refresh() {
        profiles = context.config().listProfiles();
    }

    private void setStatus(String message, boolean error) {
        status = message;
        statusColor = error ? 0xFFEF5350 : Theme.TEXT_SECONDARY;
        statusUntil = Util.getMeasuringTimeMs() + STATUS_MS;
    }

    @Override
    public void render(DrawContext draw, TextRenderer font, Bounds area, int mouseX, int mouseY, float deltaSeconds) {
        int accent = context.accent().color();
        int pad = Theme.CONTENT_PADDING;
        int left = area.x() + pad;
        int right = area.right() - pad;
        int top = area.y() + pad;
        PageHeader.render(draw, font, left, top, "Configs", "",
                profiles.size() + (profiles.size() == 1 ? " saved config" : " saved configs"), accent);

        // Toolbar: name field, save, open folder.
        int folderWidth = Button.width(font, "Folder", true);
        int saveWidth = Button.width(font, "Save", true);
        folderButton.render(draw, font, "Folder", right - folderWidth, top, folderWidth, mouseX, mouseY, accent, deltaSeconds);
        int saveX = right - folderWidth - 4 - saveWidth;
        saveButton.render(draw, font, "Save", saveX, top, saveWidth, mouseX, mouseY, accent, deltaSeconds);
        int fieldWidth = Math.min(150, saveX - 4 - (left + 120));
        nameField.render(draw, font, saveX - 4 - fieldWidth, top, fieldWidth, accent);

        RenderUtil.rect(draw, left, area.y() + Theme.CONTENT_TOP - 6, right - left, 1, Theme.DIVIDER);
        if (Util.getMeasuringTimeMs() < statusUntil) {
            int width = Typography.width(font, Font.CAPTION, status);
            Typography.draw(draw, font, Font.CAPTION, status, right - width, top + TextField.HEIGHT + 6, statusColor);
        }

        viewport = new Bounds(left, area.y() + Theme.CONTENT_TOP, right - left, area.bottom() - pad - (area.y() + Theme.CONTENT_TOP));
        visibleRows.clear();
        if (profiles.isEmpty()) {
            String message = "No saved configs yet. Type a name above and press Save.";
            int width = Typography.width(font, Font.BODY, message);
            Typography.draw(draw, font, Font.BODY, message, viewport.x() + (viewport.width() - width) / 2,
                    viewport.y() + 20, Theme.TEXT_SECONDARY);
            return;
        }

        int contentHeight = profiles.size() * (CARD_HEIGHT + Theme.CARD_GAP) - Theme.CARD_GAP;
        scroll.update(contentHeight, viewport.height(), deltaSeconds);
        if (pendingDelete != null && Util.getMeasuringTimeMs() > pendingDeleteUntil) {
            pendingDelete = null;
        }

        draw.enableScissor(viewport.x(), viewport.y(), viewport.right(), viewport.bottom());
        int y = viewport.y() - scroll.getOffset();
        int cardWidth = viewport.width() - 6;
        for (Profile profile : profiles) {
            ProfileRow row = rows.computeIfAbsent(profile.name(), name -> new ProfileRow());
            row.profile = profile;
            Bounds card = new Bounds(viewport.x(), y, cardWidth, CARD_HEIGHT);
            y += CARD_HEIGHT + Theme.CARD_GAP;
            if (card.bottom() < viewport.y() || card.y() > viewport.bottom()) {
                continue;
            }
            boolean hovered = viewport.contains(mouseX, mouseY) && card.contains(mouseX, mouseY);
            row.hover = Animation.approach(row.hover, hovered ? 1f : 0f, deltaSeconds, Theme.HOVER_SPEED);
            Card.render(draw, card.x(), card.y(), card.width(), card.height(), row.hover);

            String deleteLabel = profile.name().equals(pendingDelete) ? "Confirm" : "Delete";
            int deleteWidth = Button.width(font, deleteLabel, false);
            int applyWidth = Button.width(font, "Apply", false);
            int buttonY = card.y() + (CARD_HEIGHT - Button.HEIGHT) / 2;
            int deleteX = card.right() - 8 - deleteWidth;
            int applyX = deleteX - 4 - applyWidth;
            row.delete.render(draw, font, deleteLabel, deleteX, buttonY, deleteWidth, mouseX, mouseY, accent, deltaSeconds);
            row.apply.render(draw, font, "Apply", applyX, buttonY, applyWidth, mouseX, mouseY, accent, deltaSeconds);

            int textWidth = applyX - 8 - (card.x() + 10);
            Typography.draw(draw, font, Font.BOLD, Typography.trim(font, Font.BOLD, profile.name(), textWidth),
                    card.x() + 10, card.y() + 6, Theme.TEXT_PRIMARY);
            Typography.draw(draw, font, Font.CAPTION, "Saved " + DATE_FORMAT.format(profile.lastModified()),
                    card.x() + 10, card.y() + 17, Theme.TEXT_SECONDARY);
            visibleRows.add(row);
        }
        draw.disableScissor();
        scroll.renderScrollbar(draw, viewport.right() - 2, viewport.y(), viewport.height(), accent);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean overField = nameField.isMouseOver(mouseX, mouseY);
        nameField.setFocused(overField);
        if (overField) {
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        if (saveButton.isMouseOver(mouseX, mouseY)) {
            saveProfile();
            return true;
        }
        if (folderButton.isMouseOver(mouseX, mouseY)) {
            openFolder();
            return true;
        }
        if (!viewport.contains(mouseX, mouseY)) {
            return false;
        }
        for (ProfileRow row : visibleRows) {
            String name = row.profile.name();
            if (row.apply.isMouseOver(mouseX, mouseY)) {
                boolean applied = context.config().applyProfile(name);
                setStatus(applied ? "Applied \"" + name + "\"" : "Couldn't apply \"" + name + "\"", !applied);
                return true;
            }
            if (row.delete.isMouseOver(mouseX, mouseY)) {
                if (name.equals(pendingDelete)) {
                    pendingDelete = null;
                    boolean deleted = context.config().deleteProfile(name);
                    setStatus(deleted ? "Deleted \"" + name + "\"" : "Couldn't delete \"" + name + "\"", !deleted);
                    refresh();
                } else {
                    pendingDelete = name;
                    pendingDeleteUntil = Util.getMeasuringTimeMs() + CONFIRM_MS;
                }
                return true;
            }
        }
        return false;
    }

    private void saveProfile() {
        String name = nameField.getText().trim();
        if (!ConfigManager.isValidProfileName(name)) {
            setStatus("Use 1-32 letters, numbers, spaces, - or _", true);
            return;
        }
        boolean saved = context.config().saveProfile(name);
        setStatus(saved ? "Saved \"" + name + "\"" : "Couldn't save \"" + name + "\"", !saved);
        if (saved) {
            nameField.setText("");
            nameField.setFocused(false);
            refresh();
        }
    }

    private void openFolder() {
        try {
            Files.createDirectories(context.config().getProfileDir());
            Util.getOperatingSystem().open(context.config().getProfileDir());
        } catch (IOException e) {
            setStatus("Couldn't open the configs folder", true);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (!viewport.contains(mouseX, mouseY)) {
            return false;
        }
        scroll.scroll(amount);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (nameField.isFocused() && (input.key() == GLFW.GLFW_KEY_ENTER || input.key() == GLFW.GLFW_KEY_KP_ENTER)) {
            saveProfile();
            return true;
        }
        if (nameField.isFocused() && input.key() == GLFW.GLFW_KEY_ESCAPE) {
            nameField.setFocused(false);
            return true;
        }
        return nameField.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        return nameField.charTyped(input);
    }
}
