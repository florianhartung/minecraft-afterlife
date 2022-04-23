package config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class Config {

    private static Plugin plugin;

    public static void init(Plugin plugin) {
        Config.plugin = plugin;
    }

    public static FileConfiguration get(ConfigType configType) {
        return YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), configType.getFilename()));
    }
}
