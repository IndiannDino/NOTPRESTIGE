package dev.prestige.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.prestige.client.module.Module;
import dev.prestige.client.module.ModuleManager;
import dev.prestige.client.module.setting.Setting;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persists module state and GUI settings to {@code prestige_config.json}, and manages named
 * profiles in {@code prestige/profiles/}.
 *
 * <p>Modules are keyed by name, so entries for removed modules are ignored and new modules
 * fall back to their defaults. Writes go through a temp file and an atomic move, so a crash
 * mid-save never leaves a truncated file behind.
 */
public final class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Prestige/Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int FORMAT_VERSION = 1;
    private static final Pattern PROFILE_NAME = Pattern.compile("[A-Za-z0-9 _-]{1,32}");
    private static final String PROFILE_EXTENSION = ".json";

    private final Path file;
    private final Path profileDir;
    private final ModuleManager modules;
    private final GuiSettings gui = new GuiSettings();

    public ConfigManager(Path configDir, ModuleManager modules) {
        this.file = configDir.resolve("prestige_config.json");
        this.profileDir = configDir.resolve("prestige").resolve("profiles");
        this.modules = modules;
    }

    public GuiSettings getGuiSettings() {
        return gui;
    }

    public Path getProfileDir() {
        return profileDir;
    }

    public void load() {
        if (!Files.exists(file)) {
            LOGGER.info("No config at {}, using defaults", file);
            return;
        }
        try {
            JsonObject root = readObject(file);
            readModules(root);
            readGui(root);
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
        root.add("gui", GSON.toJsonTree(gui));
        try {
            writeObject(file, root);
        } catch (IOException e) {
            LOGGER.error("Failed to save {}", file, e);
        }
    }

    public static boolean isValidProfileName(String name) {
        return PROFILE_NAME.matcher(name).matches() && !name.isBlank();
    }

    /** Saved profiles, most recently modified first. */
    public List<Profile> listProfiles() {
        List<Profile> profiles = new ArrayList<>();
        if (!Files.isDirectory(profileDir)) {
            return profiles;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(profileDir, "*" + PROFILE_EXTENSION)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                String name = fileName.substring(0, fileName.length() - PROFILE_EXTENSION.length());
                profiles.add(new Profile(name, Files.getLastModifiedTime(path).toInstant()));
            }
        } catch (IOException e) {
            LOGGER.error("Failed to list profiles in {}", profileDir, e);
        }
        profiles.sort(Comparator.comparing(Profile::lastModified).reversed());
        return profiles;
    }

    /** Saves the current module states as a profile, replacing any profile with the same name. */
    public boolean saveProfile(String name) {
        if (!isValidProfileName(name)) {
            return false;
        }
        JsonObject root = new JsonObject();
        root.addProperty("version", FORMAT_VERSION);
        root.add("modules", writeModules());
        try {
            writeObject(profilePath(name), root);
            return true;
        } catch (IOException e) {
            LOGGER.error("Failed to save profile '{}'", name, e);
            return false;
        }
    }

    /** Applies a profile's module states and keybinds. Modules it doesn't mention are disabled. */
    public boolean applyProfile(String name) {
        if (!isValidProfileName(name)) {
            return false;
        }
        try {
            JsonObject root = readObject(profilePath(name));
            for (Module module : modules.getModules()) {
                module.setEnabled(false);
            }
            readModules(root);
            save();
            return true;
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Failed to apply profile '{}'", name, e);
            return false;
        }
    }

    public boolean deleteProfile(String name) {
        if (!isValidProfileName(name)) {
            return false;
        }
        try {
            return Files.deleteIfExists(profilePath(name));
        } catch (IOException e) {
            LOGGER.error("Failed to delete profile '{}'", name, e);
            return false;
        }
    }

    private Path profilePath(String name) {
        return profileDir.resolve(name + PROFILE_EXTENSION);
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
            JsonObject settings = getObject(entry, "settings");
            if (settings != null) {
                for (Setting<?> setting : module.getSettings()) {
                    JsonElement value = settings.get(setting.getName());
                    if (value != null) {
                        setting.fromJson(value);
                    }
                }
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
            if (!module.getSettings().isEmpty()) {
                JsonObject settings = new JsonObject();
                for (Setting<?> setting : module.getSettings()) {
                    settings.add(setting.getName(), setting.toJson());
                }
                entry.add("settings", settings);
            }
            section.add(module.getName(), entry);
        }
        return section;
    }

    private void readGui(JsonObject root) {
        JsonObject section = getObject(root, "gui");
        if (section == null) {
            return;
        }
        GuiSettings loaded = GSON.fromJson(section, GuiSettings.class);
        if (loaded.accent != null) {
            gui.accent = loaded.accent;
        }
        gui.blur = loaded.blur;
        gui.animations = loaded.animations;
        gui.descriptions = loaded.descriptions;
        gui.gridView = loaded.gridView;
        gui.toasts = loaded.toasts;
        gui.sounds = loaded.sounds;
    }

    private static JsonObject readObject(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                throw new JsonParseException("Root of " + path + " is not a JSON object");
            }
            return root.getAsJsonObject();
        }
    }

    private static void writeObject(Path path, JsonObject root) throws IOException {
        Files.createDirectories(path.getParent());
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        }
        try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
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
