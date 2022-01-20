package main;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import skill.SkillManager;

/**
 * The plugin entry point
 */
public class Main extends JavaPlugin {

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        SkillManager.init(this);
        SkillManager.startUpdater();


        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new AdvancementListener(), this);
        getCommand("skills").setExecutor(new SkillsExecutor());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        SkillManager.stopUpdater();
    }
}