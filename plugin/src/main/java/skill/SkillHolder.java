package skill;

import data.Skill;
import skill.generic.MinecraftSkill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillHolder {


    private static final Map<Skill, MinecraftSkill> skillInstances;
    private static final List<MinecraftSkill> gameplayModifiers;

    static {
        skillInstances = new HashMap<>();
        gameplayModifiers = new ArrayList<>();
    }

    public static void addSkills(Map<Skill, ? extends MinecraftSkill> skills) {
        skillInstances.putAll(skills);
    }

    public static void addGlobalModifiers(List<? extends MinecraftSkill> globalModifiers) {
        gameplayModifiers.addAll(globalModifiers);
    }

    public static MinecraftSkill getMinecraftSkill(Skill dataSkill) {
        return skillInstances.get(dataSkill);
    }

    public static List<MinecraftSkill> getAllMinecraftSkills() {
        List<MinecraftSkill> allInstances = new ArrayList<>(gameplayModifiers);
        allInstances.addAll(skillInstances.values());

        return allInstances;
    }

}
