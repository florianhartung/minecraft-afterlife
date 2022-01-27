package skill;

import data.Skill;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.listener.factory.FastFactory;
import skill.listener.factory.NinjaFactory;
import skill.listener.factory.StackableSkillFactory;
import skill.listener.factory.TankFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillInitializer {

    private final Plugin plugin;
    private final ConfigurationSection skillsConfiguration;

    public SkillInitializer(Plugin plugin, FileConfiguration skillsConfiguration) {
        this.plugin = plugin;
        this.skillsConfiguration = skillsConfiguration;
    }

    public Map<Skill, ? extends MinecraftSkill> initializeSkills(Map<Skill, Class<? extends MinecraftSkill>> minecraftSkillClasses) throws SkillInitializeException {
        Map<Skill, MinecraftSkill> instances = new HashMap<>();

        for (Map.Entry<Skill, Class<? extends MinecraftSkill>> entry : minecraftSkillClasses.entrySet()) {
            Skill skill = entry.getKey();
            Class<? extends MinecraftSkill> minecraftSkillClass = entry.getValue();
            System.out.println("initializeSkill:" + skill);

            MinecraftSkill instance = getInstanceFromClass(minecraftSkillClass);

            populate(instance);

            instances.put(skill, instance);
        }

        instances.putAll(getAllStackableSkills());

        return instances;
    }

    private static Map<Skill, MinecraftSkill> getAllStackableSkills() {
        Map<Skill, MinecraftSkill> skillInstances = new HashMap<>();
        skillInstances.putAll(getStackableSkills(List.of(Skill.TANK1, Skill.TANK2, Skill.TANK3, Skill.TANK4, Skill.TANK5), new TankFactory()));
        skillInstances.putAll(getStackableSkills(List.of(Skill.NINJA1, Skill.NINJA2, Skill.NINJA3, Skill.NINJA4, Skill.NINJA5), new NinjaFactory()));
        skillInstances.putAll(getStackableSkills(List.of(Skill.FAST1, Skill.FAST2, Skill.FAST3), new FastFactory()));
        return skillInstances;
    }


    private static Map<Skill, MinecraftSkill> getStackableSkills(List<Skill> skills, StackableSkillFactory factory) {
        Map<Skill, MinecraftSkill> skillInstances = new HashMap<>();
        for (int i = 0; i < skills.size(); i++) {
            skillInstances.put(skills.get(i), factory.get(i + 1));
        }
        return skillInstances;
    }

    private MinecraftSkill getInstanceFromClass(Class<? extends MinecraftSkill> clazz) throws SkillInitializeException {
        try {
            return clazz.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new SkillInitializeException(e);
        }
    }

    private void populate(MinecraftSkill instance) {
        if (instance instanceof Configurable configurable) {
            String skillConfigPath = configurable.getConfigPath();
            ConfigurationSection skillConfigurationSection = skillsConfiguration.getConfigurationSection(skillConfigPath);
            configurable.setConfig(skillConfigurationSection);
        }
        if (instance instanceof PluginConsumer pluginConsumer) {
            pluginConsumer.setPlugin(plugin);
        }
    }

    static class SkillInitializeException extends Exception {
        public SkillInitializeException(Throwable cause) {
            super("Could not initialize minecraft skill", cause);
        }
    }
}
