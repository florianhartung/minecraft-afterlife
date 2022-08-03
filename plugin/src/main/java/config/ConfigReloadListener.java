package config;

import org.bukkit.configuration.file.FileConfiguration;

@FunctionalInterface
public interface ConfigReloadListener {
    void onReload(FileConfiguration config);
}
