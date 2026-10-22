package dev.prestige.client;

import dev.prestige.client.config.ConfigManager;
import dev.prestige.client.gui.ClickGuiScreen;
import dev.prestige.client.input.KeybindHandler;
import dev.prestige.client.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Client entrypoint. Wires the subsystems together and drives them from Fabric events. */
public final class PrestigeClient implements ClientModInitializer {
    public static final String MOD_ID = "prestige";
    public static final Logger LOGGER = LoggerFactory.getLogger("Prestige");

    private static PrestigeClient instance;

    private ModuleManager moduleManager;
    private ConfigManager configManager;
    private KeybindHandler keybindHandler;
    private KeyBinding openGuiKey;
    private ClickGuiScreen clickGui;

    @Override
    public void onInitializeClient() {
        instance = this;

        moduleManager = new ModuleManager();
        moduleManager.registerBuiltins();

        configManager = new ConfigManager(
                FabricLoader.getInstance().getConfigDir().resolve("prestige_config.json"), moduleManager);
        configManager.load();

        keybindHandler = new KeybindHandler(moduleManager);
        openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.prestige.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of(MOD_ID, "main"))));

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> configManager.save());

        LOGGER.info("Prestige initialized with {} modules", moduleManager.getModules().size());
    }

    private void onEndTick(MinecraftClient client) {
        while (openGuiKey.wasPressed()) {
            if (client.currentScreen == null) {
                client.setScreen(getClickGui());
            }
        }
        keybindHandler.tick(client);
        if (client.player != null && client.world != null) {
            moduleManager.onTick();
        }
    }

    private ClickGuiScreen getClickGui() {
        if (clickGui == null) {
            clickGui = new ClickGuiScreen(moduleManager, configManager);
        }
        return clickGui;
    }

    public static PrestigeClient getInstance() {
        return instance;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
