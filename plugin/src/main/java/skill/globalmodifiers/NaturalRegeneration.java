package skill.globalmodifiers;

import config.Config;
import config.ConfigReloadListener;
import config.ConfigType;
import main.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

import java.util.Comparator;
import java.util.List;

import static main.Util.unsafeListCast;

@Configurable("global.regeneration")
public class NaturalRegeneration extends MinecraftSkill {

    @ConfigValue("skill-block-aura-distance")
    private double SKILL_BLOCK_AURA_DISTANCE;
    @ConfigValue("regen-chance.day")
    private double REGEN_CHANCE_DAY;
    @ConfigValue("regen-chance.night")
    private double REGEN_CHANCE_NIGHT;
    @InjectPlugin(postInject = "startParticleTimer")
    private Plugin plugin;

    private List<Location> skillBlockLocations;

    public NaturalRegeneration() {
        ConfigReloadListener loadSkillBlocksFromConfig = fileConfiguration -> this.skillBlockLocations = unsafeListCast(fileConfiguration.getList("blocks"));
        Config.registerListener(ConfigType.SKILL_BLOCKS, loadSkillBlocksFromConfig);
        loadSkillBlocksFromConfig.onReload(Config.get(ConfigType.SKILL_BLOCKS));
    }

    @SuppressWarnings("unused")
    private void startParticleTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::spawnParticles, 0, 6);
    }

    private void spawnParticles() {
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> distanceToNearestSkillBlock(player.getLocation()) < SKILL_BLOCK_AURA_DISTANCE)
                .forEach(player -> player.getWorld().spawnParticle(Particle.SPELL_MOB_AMBIENT, player.getLocation(), 0, 1, 0, 0, 1));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRegen(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        if (distanceToNearestSkillBlock(e.getEntity().getLocation()) < SKILL_BLOCK_AURA_DISTANCE) {
            return;
        }

        boolean isNighttime = Util.isNighttime(player.getWorld().getTime());
        double regenChance = switch (e.getRegainReason()) {
            case SATIATED, EATING -> isNighttime ? REGEN_CHANCE_NIGHT : REGEN_CHANCE_DAY;
            default -> 1;
        };
        if (Math.random() > regenChance) {
            e.setCancelled(true);
        }
    }

    private double distanceToNearestSkillBlock(Location location) {
        return skillBlockLocations.stream()
                .min(Comparator.comparingDouble(l -> l.distanceSquared(location)))
                .map(location::distance)
                .orElse(Double.MAX_VALUE);
    }
}
