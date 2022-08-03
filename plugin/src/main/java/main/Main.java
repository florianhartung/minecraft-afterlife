package main;

import advancements.afterlife.AdvancementManager;
import advancements.cancelable.AdvancementCancellingListener;
import config.Config;
import config.ConfigType;
import hud.HudManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import skill.SkillManager;
import skillspage.SkillBlockManager;

import java.io.File;
import java.io.IOException;

/**
 * The plugin entry point
 */
public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Config.init(this);
        HudManager.init(this);
        RestService.setConfig(Config.get(ConfigType.DEFAULT));
        ChatHelper.setConfig(Config.get(ConfigType.DEFAULT));

        SkillManager.init(this);
        SkillManager.startUpdater();

        AdvancementManager.init(this);

        SkillBlockManager skillBlockManager = new SkillBlockManager();

        register(skillBlockManager);
        register(new SkillPointAdvancementListener());
        register(new PlayerRegistrationListener());
        register(new AdvancementCancellingListener());

        // TODO: Afterlife command
        getCommand("skillblock").setExecutor(skillBlockManager);
        getCommand("afterlife").setExecutor(new SkillsExecutor());
        getCommand("hud").setExecutor(HudManager.getInstance());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        SkillManager.stopUpdater();
    }

    public void saveConfig(FileConfiguration config, String filename) {
        try {
            config.save(new File(getDataFolder(), filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void register(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }
}