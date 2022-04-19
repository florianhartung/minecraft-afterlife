package main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
        RestService.setConfig(getConfig());
        ChatHelper.setConfig(getConfig());

        SkillManager.init(this, getSkillsConfiguration("skills.yml"));
        SkillManager.startUpdater();

        getServer().getPluginManager().registerEvents(new SkillpageOpener(getSkillsConfiguration("skillblocks.yml"), this::saveConfig), this);
        getServer().getPluginManager().registerEvents(new AdvancementListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerRegistrationListener(), this);
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("skills").setExecutor(new SkillsExecutor());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        SkillManager.stopUpdater();
    }

    private FileConfiguration getSkillsConfiguration(String filename) {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder(), filename));
    }

    @EventHandler
    public void itemHold(PlayerItemHeldEvent e) {
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        System.out.println("a");
        System.out.println("item.getItemMeta().getDisplayName()=" + item.getItemMeta().getDisplayName());
        if (item.getType() == Material.COMPASS && item.getItemMeta() != null && item.getItemMeta().getDisplayName().contains("Custom Compass")) {
            System.out.println("b");
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
}