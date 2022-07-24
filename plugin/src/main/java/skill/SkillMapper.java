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
        put(Skill.ENERGY_COOKIES, EnergyCookieMinecraftSkill.class);
        put(Skill.SATURATED, SaturatedMinecraftSkill.class);
        put(Skill.INDIAN, SnailMinecraftSkill.class);
        put(Skill.CARPENTER, CarpenterMinecraftSkill.class);
        put(Skill.SPRINT_BURST, SprintBurstMinecraftSkill.class);
        put(Skill.LIEUTENANT, LieutenantMinecraftSkill.class);
        put(Skill.POISON, PoisonMinecraftSkill.class);
        put(Skill.FOOL, FoolMinecraftSkill.class);
        put(Skill.INSECT, InsectMinecraftSkill.class);
        put(Skill.KARMA, KarmaMinecraftSkill.class);
        put(Skill.NO_MERCY, NoMercyMinecraftSkill.class);
        put(Skill.VIBING_CAT, VibingCatMinecraftSkill.class);
    }

    private static void put(Skill skill, Class<? extends MinecraftSkill> minecraftSkillClass) {
        minecraftSkillClasses.put(skill, minecraftSkillClass);
    }
}
