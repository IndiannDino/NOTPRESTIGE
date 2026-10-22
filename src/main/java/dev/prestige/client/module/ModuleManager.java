package dev.prestige.client.module;

import dev.prestige.client.module.impl.combat.CooldownIndicator;
import dev.prestige.client.module.impl.combat.HitMarkers;
import dev.prestige.client.module.impl.movement.ToggleSneak;
import dev.prestige.client.module.impl.movement.ToggleSprint;
import dev.prestige.client.module.impl.render.BlockOutline;
import dev.prestige.client.module.impl.render.Fullbright;
import dev.prestige.client.module.impl.world.TimeChanger;
import dev.prestige.client.module.impl.world.WeatherToggle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Central registry for all modules. Owns registration order, lookup and the per-tick dispatch. */
public final class ModuleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Prestige/ModuleManager");

    private final Map<String, Module> byName = new LinkedHashMap<>();
    private final Map<Class<? extends Module>, Module> byType = new HashMap<>();
    private final Map<Category, List<Module>> byCategory = new EnumMap<>(Category.class);
    private final List<Consumer<Module>> toggleListeners = new CopyOnWriteArrayList<>();

    public ModuleManager() {
        for (Category category : Category.values()) {
            byCategory.put(category, new ArrayList<>());
        }
    }

    /** Registers the built-in modules. Panel order in the ClickGUI follows registration order. */
    public void registerBuiltins() {
        register(new HitMarkers());
        register(new CooldownIndicator());

        register(new ToggleSprint());
        register(new ToggleSneak());

        register(new Fullbright());
        register(new BlockOutline());

        register(new TimeChanger());
        register(new WeatherToggle());
    }

    public <T extends Module> T register(T module) {
        String key = module.getName().toLowerCase(Locale.ROOT);
        if (byName.containsKey(key)) {
            throw new IllegalStateException("Duplicate module name: " + module.getName());
        }
        byName.put(key, module);
        byType.put(module.getClass(), module);
        byCategory.get(module.getCategory()).add(module);
        module.setToggleListener(this::fireToggle);
        return module;
    }

    /** Called after any module is switched on or off, including from config loads. */
    public void addToggleListener(Consumer<Module> listener) {
        toggleListeners.add(listener);
    }

    private void fireToggle(Module module) {
        for (Consumer<Module> listener : toggleListeners) {
            listener.accept(module);
        }
    }

    public Optional<Module> get(String name) {
        return Optional.ofNullable(byName.get(name.toLowerCase(Locale.ROOT)));
    }

    public <T extends Module> T get(Class<T> type) {
        Module module = byType.get(type);
        if (module == null) {
            throw new IllegalArgumentException("Module not registered: " + type.getName());
        }
        return type.cast(module);
    }

    public List<Module> getModules() {
        return List.copyOf(byName.values());
    }

    public List<Module> getModules(Category category) {
        return Collections.unmodifiableList(byCategory.get(category));
    }

    /** Modules whose name contains {@code query}, ignoring case. */
    public List<Module> search(String query) {
        String needle = query.toLowerCase(Locale.ROOT).replace(" ", "");
        return byName.values().stream()
                .filter(module -> module.getName().toLowerCase(Locale.ROOT).contains(needle))
                .toList();
    }

    /** Ticks every enabled module. A module that throws is disabled so it can't spam the log. */
    public void onTick() {
        for (Module module : byName.values()) {
            if (!module.isEnabled()) {
                continue;
            }
            try {
                module.onTick();
            } catch (RuntimeException e) {
                LOGGER.error("Module '{}' threw in onTick and was disabled", module.getName(), e);
                module.setEnabled(false);
            }
        }
    }
}
