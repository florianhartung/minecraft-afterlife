package main;

import org.bukkit.plugin.java.JavaPlugin;
import skill.SkillManager;

/**
 * The plugin entry point
 */
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        RestService.setConfig(getConfig());

        SkillManager.init(this);
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
}