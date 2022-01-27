package skill;

import data.Skill;
import skill.generic.MinecraftSkill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillHolder {


    private static final Map<Skill, MinecraftSkill> skillInstances;

    static {
        skillInstances = new HashMap<>();
    }

    public static void addSkills(Map<Skill, ? extends MinecraftSkill> skills) {
        skillInstances.putAll(skills);
    }

    public static MinecraftSkill getMinecraftSkill(Skill dataSkill) {
        return skillInstances.get(dataSkill);
    }

    public static List<MinecraftSkill> getAllMinecraftSkills() {
        return skillInstances.values()
                .stream()
                .toList();
    }

}
