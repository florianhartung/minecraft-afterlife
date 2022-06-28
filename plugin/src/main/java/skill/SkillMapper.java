package skill;

import data.Skill;
import lombok.Getter;
import skill.generic.MinecraftSkill;
import skill.skills.*;
import skill.skills.spiderqueen.SpiderQueenMinecraftSkill;

import java.util.HashMap;
import java.util.Map;

public class SkillMapper {
    @Getter
    private static final Map<Skill, Class<? extends MinecraftSkill>> minecraftSkillClasses;

    static {
        minecraftSkillClasses = new HashMap<>();

        put(Skill.ADRENALINE, AdrenalineMinecraftSkill.class);
        put(Skill.BACKSTAB, BackstabMinecraftSkill.class);
        put(Skill.BARRIER, BarrierMinecraftSkill.class);
        put(Skill.BLOODLUST, BloodlustMinecraftSkill.class);
        put(Skill.CRYSTAL_KING, CrystalKingMinecraftSkill.class);
        put(Skill.DIMENSION_JUMPER, DimensionJumperMinecraftSkill.class);
        put(Skill.GHOST_SOUL, GhostSoulMinecraftSkill.class);
        put(Skill.HITMAN, HitmanMinecraftSkill.class);
        put(Skill.LIFESTEAL, LifestealMinecraftSkill.class);
        put(Skill.VIRUS, VirusMinecraftSkill.class);
        put(Skill.SPIDER_QUEEN, SpiderQueenMinecraftSkill.class);
        put(Skill.DWARF, DwarfMinecraftSkill.class);
        put(Skill.ENDURANCE, EnduranceMinecraftSkill.class);
        put(Skill.RADAR, RadarMinecraftSkill.class);
    }

    private static void put(Skill skill, Class<? extends MinecraftSkill> minecraftSkillClass) {
        minecraftSkillClasses.put(skill, minecraftSkillClass);
    }
}
