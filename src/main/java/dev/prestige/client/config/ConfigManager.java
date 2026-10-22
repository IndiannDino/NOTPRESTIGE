package dev.prestige.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.prestige.client.module.Category;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.ModuleManager;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists module state and ClickGUI layout to {@code prestige_config.json}.
 *
 * <p>Modules are keyed by name, so entries for removed modules are ignored and new modules
 * fall back to their defaults. Writes go through a temp file and an atomic move, so a crash
 * mid-save never leaves a truncated config behind.
 */
public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Prestige/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FORMAT_VERSION = 1;

    private final Path file;
    private final ModuleManager modules;
    private final Map<Category, PanelState> panels = new EnumMap<>(Category.class);

    public ConfigManager(Path file, ModuleManager modules) {
        this.file = file;
        this.modules = modules;
        for (Category category : Category.values()) {
            panels.put(category, new PanelState());
        }
    }

    public PanelState getPanelState(Category category) {
        return panels.get(category);
    }

    public void load() {
        if (!Files.exists(file)) {
            LOGGER.info("No config at {}, using defaults", file);
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                throw new JsonParseException("Root is not a JSON object");
            }
            readModules(root.getAsJsonObject());
            readPanels(root.getAsJsonObject());
            LOGGER.info("Loaded config from {}", file);
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Failed to read {}, keeping defaults", file, e);
            backupCorruptFile();
        }
    }

    public void save() {
        JsonObject root = new JsonObject();
        root.addProperty("version", FORMAT_VERSION);
        root.add("modules", writeModules());
        root.add("panels", writePanels());

        try {
            Files.createDirectories(file.getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save {}", file, e);
        }
    }

    private void readModules(JsonObject root) {
        JsonObject section = getObject(root, "modules");
        if (section == null) {
            return;
        }
        for (Module module : modules.getModules()) {
            JsonObject entry = getObject(section, module.getName());
            if (entry == null) {
                continue;
            }
            if (isNumber(entry, "keybind")) {
                module.setKeybind(entry.get("keybind").getAsInt());
            }
            if (isBoolean(entry, "enabled")) {
                module.setEnabled(entry.get("enabled").getAsBoolean());
            }
        }
    }

    private JsonObject writeModules() {
        JsonObject section = new JsonObject();
        for (Module module : modules.getModules()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("enabled", module.isEnabled());
            entry.addProperty("keybind", module.getKeybind());
            section.add(module.getName(), entry);
        }
        return section;
    }

    private void readPanels(JsonObject root) {
        JsonObject section = getObject(root, "panels");
        if (section == null) {
            return;
        }
        for (Map.Entry<Category, PanelState> panel : panels.entrySet()) {
            JsonObject entry = getObject(section, panel.getKey().name());
            if (entry == null) {
                continue;
            }
            PanelState state = panel.getValue();
            if (isBoolean(entry, "expanded")) {
                state.expanded = entry.get("expanded").getAsBoolean();
            }
            if (isNumber(entry, "x") && isNumber(entry, "y")) {
                state.x = entry.get("x").getAsInt();
                state.y = entry.get("y").getAsInt();
                state.placed = true;
            }
        }
    }

    private JsonObject writePanels() {
        JsonObject section = new JsonObject();
        for (Map.Entry<Category, PanelState> panel : panels.entrySet()) {
            PanelState state = panel.getValue();
            JsonObject entry = new JsonObject();
            entry.addProperty("expanded", state.expanded);
            if (state.placed) {
                entry.addProperty("x", state.x);
                entry.addProperty("y", state.y);
            }
            section.add(panel.getKey().name(), entry);
        }
        return section;
    }

    private void backupCorruptFile() {
        try {
            Path backup = file.resolveSibling(file.getFileName() + ".corrupt");
            Files.copy(file, backup, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.warn("Backed up unreadable config to {}", backup);
        } catch (IOException e) {
            LOGGER.error("Failed to back up unreadable config", e);
        }
    }

    private static JsonObject getObject(JsonObject parent, String key) {
        JsonElement element = parent.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static boolean isNumber(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
    }

    private static boolean isBoolean(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
    }
}
