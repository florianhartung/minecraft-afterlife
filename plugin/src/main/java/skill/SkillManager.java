package skill;

import data.Skill;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import skill.generic.MinecraftSkill;

import java.util.Map;
import java.util.logging.Level;


public class SkillManager {
    private static final int UPDATE_INTERVAL = 5 * 20; // in ticks

    private static Plugin plugin;
    private static int updaterTaskId = -1;

    public static void init(Plugin plugin, FileConfiguration skillsConfiguration) {
        SkillManager.plugin = plugin;
        populateSkillHolder(plugin, skillsConfiguration);

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        SkillHolder.getAllMinecraftSkills()
                .forEach(minecraftSkill -> pluginManager.registerEvents(minecraftSkill, plugin));
    }

    private static void populateSkillHolder(Plugin plugin, FileConfiguration skillsConfiguration) {
        try {
            SkillInitializer skillInitializer = new SkillInitializer(plugin, skillsConfiguration);
            Map<Skill, ? extends MinecraftSkill> skillInstances = skillInitializer.initializeSkills(SkillMapper.getMinecraftSkillClasses());

            SkillHolder.addSkills(skillInstances);
            Bukkit.getLogger().log(Level.INFO, "[Minecraft-Afterlife] Successfully loaded " + skillInstances.size() + " skills!");
        } catch (SkillInitializer.SkillInitializeException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not initialize minecraft skills", e);
        }

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
        Bukkit.getServer().getOnlinePlayers()
                .forEach(SkillUpdater::reloadSkillsForPlayer);
    }
}
