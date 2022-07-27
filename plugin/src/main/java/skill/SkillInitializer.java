package skill;

import data.Skill;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.injection.SkillInjector;
import skill.skills.factory.FastFactory;
import skill.skills.factory.NinjaFactory;
import skill.skills.factory.StackableSkillFactory;
import skill.skills.factory.TankFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillInitializer {

    private final SkillInjector skillInjector;

    public SkillInitializer(Plugin plugin, FileConfiguration skillsConfiguration) {
        this.skillInjector = new SkillInjector(plugin, skillsConfiguration);
    }

    public Pair<Map<Skill, ? extends MinecraftSkill>, List<? extends MinecraftSkill>> initializeSkills(Map<Skill, Class<? extends MinecraftSkill>> minecraftSkillClasses, List<Class<? extends MinecraftSkill>> globalModifierClasses) throws SkillInitializeException {
        Map<Skill, MinecraftSkill> instances = new HashMap<>();
        List<MinecraftSkill> globalModifiers = new ArrayList<>();

        for (Map.Entry<Skill, Class<? extends MinecraftSkill>> entry : minecraftSkillClasses.entrySet()) {
            Skill skill = entry.getKey();
            Class<? extends MinecraftSkill> minecraftSkillClass = entry.getValue();

            MinecraftSkill instance = getInstanceFromClass(minecraftSkillClass);
            instances.put(skill, instance);
        }

        for (Class<? extends MinecraftSkill> clazz : globalModifierClasses) {
            MinecraftSkill instance = getInstanceFromClass(clazz);
            globalModifiers.add(instance);
        }

        List<MinecraftSkill> allInstances = new ArrayList<>(globalModifiers);
        allInstances.addAll(instances.values());
        skillInjector.setSkillInstances(allInstances);

        allInstances.forEach(skillInjector::inject);

        instances.putAll(getAllStackableSkills());

        return Pair.of(instances, globalModifiers);
    }

    private Map<Skill, MinecraftSkill> getAllStackableSkills() {
        Map<Skill, MinecraftSkill> skillInstances = new HashMap<>();
        skillInstances.putAll(getStackableSkills(List.of(Skill.TANK1, Skill.TANK2, Skill.TANK3, Skill.TANK4, Skill.TANK5), new TankFactory()));
        skillInstances.putAll(getStackableSkills(List.of(Skill.NINJA1, Skill.NINJA2, Skill.NINJA3, Skill.NINJA4, Skill.NINJA5), new NinjaFactory()));
        skillInstances.putAll(getStackableSkills(List.of(Skill.FAST1, Skill.FAST2, Skill.FAST3), new FastFactory()));
        return skillInstances;
    }


    private Map<Skill, MinecraftSkill> getStackableSkills(List<Skill> skills, StackableSkillFactory factory) {
        skillInjector.inject(factory);

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

    static class SkillInitializeException extends Exception {
        public SkillInitializeException(Throwable cause) {
            super("Could not initialize minecraft skill", cause);
        }
    }
}
