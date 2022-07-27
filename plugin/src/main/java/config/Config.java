package config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;

public class Config {

    private static Plugin plugin;

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
    }
}
