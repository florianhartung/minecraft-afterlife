package skill.listener.factory;

import skill.generic.MinecraftSkill;

public interface StackableSkillFactory {
    MinecraftSkill get(int i);
}
