package main;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import skill.HealOnAttackSkill;
import skill.HealthSkill;
import skill.MovementSpeedSkill;
import skill.Skill;

import java.util.List;

/**
 * The plugin entry point
 */
public class Main extends JavaPlugin {
    /**
     * Some temporary skills that get applied or removed to a player when they type in chat
     *
     * @see SkillApplier
     */
    private static final List<Skill> SKILLS = List.of(new HealthSkill(1.0d), new MovementSpeedSkill(0.001d), new HealOnAttackSkill());

    @Override
    public void onEnable() {
        PluginManager pluginManager = getServer().getPluginManager();
        SKILLS.forEach(skill -> pluginManager.registerEvents(skill, this));

        SkillApplier skillApplier = new SkillApplier(SKILLS);
        pluginManager.registerEvents(skillApplier, this);
        pluginManager.registerEvents(new AdvancementListener(), this);
        getCommand("skills").setExecutor(new SkillsExecutor());
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}