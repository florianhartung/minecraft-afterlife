package main;


import skill.HealOnAttackSkill;
import skill.HealthSkill;
import skill.MovementSpeedSkill;

import data.Skill;

public class SkillMapper {
    public static skill.Skill map(Skill dataSkill) {
        if(dataSkill == Skill.HEALTH1) {
            return new HealthSkill(2.0d);
        } else if (dataSkill == Skill.HEALTH2) {
            return new HealthSkill(4.0d);
        }  else if (dataSkill == Skill.HEALTH3) {
            return new HealthSkill(6.0d);
        } else if (dataSkill == Skill.LIFESTEAL) {
            return new HealOnAttackSkill();
        } else if (dataSkill == Skill.MOVEMENT_SPEED) {
            return new MovementSpeedSkill(0.05d);
        } else {
            return null;
        }
        /*switch (dataSkill) {
            case HEALTH1: return new HealthSkill(2.0d);
            case HEALTH2: return new HealthSkill(4.0d);
            case HEALTH3: return new HealthSkill(6.0d);
            case MOVEMENT_SPEED: return new MovementSpeedSkill(0.02d);
            case LIFESTEAL: return new HealOnAttackSkill();
        };
        return null;*/
    }
}
