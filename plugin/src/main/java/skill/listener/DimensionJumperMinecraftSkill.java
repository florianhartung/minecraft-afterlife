package skill.listener;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import skill.Configurable;
import skill.generic.CooldownMinecraftSkill;

public class DimensionJumperMinecraftSkill extends CooldownMinecraftSkill implements Configurable {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player && isActiveFor(player)) {
            if (isOnCooldown(player)) {
                e.setCancelled(true);
                boolean playEffects = true;
                if (e.getCause() == EntityDamageEvent.DamageCause.LAVA) {
                    playEffects = Math.random() < 0.1d;
                }
                if (playEffects) {
                    player.getWorld().spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 20, 0.2d, 0.6d, 0.2d, 0.1d);
                    player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 0.1f, 2.0f);
                }

            }
        }
    }

    @EventHandler
    public void onDimensionChange(PlayerChangedWorldEvent e) {
        startCooldown(e.getPlayer());
    }

    @Override
    public void setConfig(ConfigurationSection config) {
        setCooldown(config.getInt("invincibility-duration"));
    }

    @Override
    public String getConfigPath() {
        return "dimension-jumper";
    }
}
