package skill;

import config.Config;
import config.ConfigType;
import data.Skill;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import skill.generic.MinecraftSkill;
import skill.injection.Command;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;


public class SkillManager {
    private static final int UPDATE_INTERVAL = 5 * 20; // in ticks

    private static Plugin plugin;
    private static int updaterTaskId = -1;

    public static void init(Plugin plugin) {
        SkillManager.plugin = plugin;
        populateSkillHolder(plugin, Config.get(ConfigType.SKILLS));

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        SkillHolder.getAllMinecraftSkills()
                .forEach(minecraftSkill -> {
                    pluginManager.registerEvents(minecraftSkill, plugin);

                    tryRegisterAsCommandExecutor(minecraftSkill);
                });
    }

    private static void tryRegisterAsCommandExecutor(MinecraftSkill minecraftSkill) {
        Class<? extends MinecraftSkill> clazz = minecraftSkill.getClass();
        Command commandAnnotation = clazz.getAnnotation(Command.class);
        if (commandAnnotation == null) {
            return;
        }

        String command = commandAnnotation.value();
        if (!(minecraftSkill instanceof CommandExecutor commandExecutor)) {
            Bukkit.getLogger().log(Level.SEVERE, "[Afterlife] MinecraftSkill " + clazz.getName() + " is requesting the command " + command + " via the @Command annotation, but is not implementing the CommandExecutor interface!");
            return;
        }

        ((JavaPlugin) plugin).getCommand(command).setExecutor(commandExecutor);
    }

    private static void populateSkillHolder(Plugin plugin, FileConfiguration skillsConfiguration) {
        try {
            SkillInitializer skillInitializer = new SkillInitializer(plugin, skillsConfiguration);
            Pair<Map<Skill, ? extends MinecraftSkill>, List<? extends MinecraftSkill>> instancesPair = skillInitializer.initializeSkills(SkillMapper.getMinecraftSkillClasses(), SkillMapper.getGlobalModifiers());

            SkillHolder.addSkills(instancesPair.getLeft());
            SkillHolder.addGlobalModifiers(instancesPair.getRight());
            Bukkit.getLogger().log(Level.INFO, "[Afterlife] Successfully loaded " + instancesPair.getLeft().size() + " skills!");
        } catch (SkillInitializer.SkillInitializeException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[Afterlife] Could not initialize minecraft skills", e);
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
