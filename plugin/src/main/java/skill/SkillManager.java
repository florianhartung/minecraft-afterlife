package skill;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;


public class SkillManager {
    private static final int UPDATE_INTERVAL = 5 * 20; // in ticks

    private static Plugin plugin;
    private static int updaterTaskId = -1;

    public static void init(Plugin plugin) {
        SkillManager.plugin = plugin;
        SkillHolder.init(plugin);

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        SkillHolder.getAllMinecraftSkills()
                .forEach(minecraftSkill -> pluginManager.registerEvents(minecraftSkill, plugin));
    }

    public static void startUpdater() {
        if (updaterTaskId != -1) {
            throw new IllegalStateException("Skill updater is already running");
        }

        updaterTaskId = plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(plugin, SkillManager::reloadSkills, 0, UPDATE_INTERVAL).getTaskId();
    }

    public static void stopUpdater() {
        plugin.getServer().getScheduler().cancelTask(updaterTaskId);
        updaterTaskId = -1;
    }

    public static void reloadSkills() {
        for (int i = 0; i < 20; i++) {
            Bukkit.getServer().getOnlinePlayers()
                    .forEach(SkillUpdater::reloadSkillsForPlayer);
        }
    }
}
