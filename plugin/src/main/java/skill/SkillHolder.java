package skill;

import data.Skill;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.listener.HitmanSkill;
import skill.listener.LifestealMinecraftSkill;
import skill.listener.factory.FastFactory;
import skill.listener.factory.NinjaFactory;
import skill.listener.factory.StackableSkillFactory;
import skill.listener.factory.TankFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillHolder {

    private static Plugin plugin;


    private static final Map<Skill, MinecraftSkill> skills;

    static {
        skills = new HashMap<>();
    }

    public static void init(Plugin plugin) {
        SkillHolder.plugin = plugin;

        putStackableSkills(List.of(Skill.TANK1, Skill.TANK2, Skill.TANK3, Skill.TANK4, Skill.TANK5), new TankFactory());
        putStackableSkills(List.of(Skill.NINJA1, Skill.NINJA2, Skill.NINJA3, Skill.NINJA4, Skill.NINJA5), new NinjaFactory());
        putStackableSkills(List.of(Skill.FAST1, Skill.FAST2, Skill.FAST3), new FastFactory());

        skills.put(Skill.HITMAN, new HitmanSkill(plugin));
        skills.put(Skill.LIFESTEAL, new LifestealMinecraftSkill());
    }

    private static void putStackableSkills(List<Skill> skills, StackableSkillFactory factory) {
        for (int i = 0; i < skills.size(); i++) {
            SkillHolder.skills.put(skills.get(i), factory.get(i + 1));
        }
    }

    public static MinecraftSkill getMinecraftSkill(Skill dataSkill) {
        return skills.get(dataSkill);
    }

    public static List<MinecraftSkill> getAllMinecraftSkills() {
        return skills.values()
                .stream()
                .toList();
    }

}
