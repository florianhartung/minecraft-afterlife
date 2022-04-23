package main;

import advancements.afterlife.AdvancementManager;
import advancements.cancelable.AdvancementCancellingListener;
import config.Config;
import config.ConfigType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import skill.SkillManager;
import skillspage.SkillpageOpener;

import java.io.File;
import java.io.IOException;

/**
 * The plugin entry point
 */
public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Config.init(this);
        RestService.setConfig(Config.get(ConfigType.DEFAULT));
        ChatHelper.setConfig(Config.get(ConfigType.DEFAULT));

        SkillManager.init(this);
        SkillManager.startUpdater();

        AdvancementManager.init(this);

        register(new SkillpageOpener(this::saveConfig));
        register(new AdvancementListener());
        register(new PlayerRegistrationListener());
        register(new AdvancementCancellingListener());
        register(this);
        getCommand("skills").setExecutor(new SkillsExecutor());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        SkillManager.stopUpdater();
    }


    @EventHandler
    public void itemHold(PlayerItemHeldEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() == Material.COMPASS && item.getItemMeta() != null && item.getItemMeta().getDisplayName().contains("Custom Compass")) {
            // -55.49 59.00 -80.47
            Location location = e.getPlayer().getLocation();
            location.setX(-56);
            location.setY(59);
            location.setZ(-80);
            e.getPlayer().setCompassTarget(location);
        }
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