package skill.skills;

import main.Util;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

@Configurable("masochistic")
public class MasochisticMinecraftSkill extends MinecraftSkill {

    @ConfigValue("damage-multipliers.max-health")
    private static double DAMAGE_MULTIPLIERS_MAX_HEALTH;
    @ConfigValue("damage-multipliers.zero-health")
    private static double DAMAGE_MULTIPLIERS_ZERO_HEALTH;
    @InjectPlugin(postInject = "startParticleTimer")
    private Plugin plugin;

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player) || !isActiveFor(player) || e.isCancelled()) {
            return;
        }

        double damageMultiplier = mapHealthToMultiplier(Util.healthPercentage(player));
        e.setDamage(e.getDamage() * damageMultiplier);
    }

    @SuppressWarnings("unused")
    private void startParticleTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::showParticles, 0, 2);
    }

    private void showParticles() {
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(this::isActiveFor)
                .forEach(player -> {
                    double healthPercentage = Util.healthPercentage(player);
                    if (healthPercentage == 1.0d) {
                        return;
                    }

                    double multiplier = mapHealthToMultiplier(healthPercentage);
                    int particleCount = (int) (multiplier / DAMAGE_MULTIPLIERS_ZERO_HEALTH * 30);
                    float dustSize = (float) (multiplier / DAMAGE_MULTIPLIERS_ZERO_HEALTH * 1.6);
                    player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, player.getLocation(), particleCount, 0.1, 0, 0.1, new Particle.DustTransition(Color.fromRGB(150, 0, 0), Color.fromRGB(70, 0, 0), dustSize));
                });
    }


    private double mapHealthToMultiplier(double healthPercentage) {
        return (DAMAGE_MULTIPLIERS_ZERO_HEALTH - DAMAGE_MULTIPLIERS_MAX_HEALTH) * Math.pow(1.0d - healthPercentage, 2.0d) + DAMAGE_MULTIPLIERS_MAX_HEALTH;
    }
}
