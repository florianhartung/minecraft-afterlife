package config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Config {

    private static Plugin plugin;

    private static final Map<ConfigType, List<ConfigReloadListener>> configReloadListeners = new HashMap<>();

    public static void init(Plugin plugin) {
        Config.plugin = plugin;
    }

    public static FileConfiguration get(ConfigType configType) {
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), configType.getFilename()));
    }

    public static void save(ConfigType configType, FileConfiguration config) {
        try {
            config.save(new File(plugin.getDataFolder(), configType.getFilename()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Optional.ofNullable(configReloadListeners.get(configType))
                .ifPresent(listeners -> listeners.forEach(listener -> listener.onReload(config)));
    }

    public static void registerListener(ConfigType configType, ConfigReloadListener configReloadListener) {
        configReloadListeners.computeIfAbsent(configType, type -> new ArrayList<>());
        configReloadListeners.get(configType).add(configReloadListener);
    }
}
