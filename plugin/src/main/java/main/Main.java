package main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import skill.SkillManager;

import java.io.File;

/**
 * The plugin entry point
 */
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        RestService.setConfig(getConfig());

        SkillManager.init(this, getSkillsConfiguration());
        SkillManager.startUpdater();

        getServer().getPluginManager().registerEvents(new AdvancementListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerRegistrationListener(), this);
        getCommand("skills").setExecutor(new SkillsExecutor());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        SkillManager.stopUpdater();
    }

    private FileConfiguration getSkillsConfiguration() {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder(), "skills.yml"));
    }
}