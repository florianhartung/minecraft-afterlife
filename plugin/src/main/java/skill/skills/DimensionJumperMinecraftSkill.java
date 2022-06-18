package skill.skills;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectTimer;

@Configurable("dimension-jumper")
public class DimensionJumperMinecraftSkill extends MinecraftSkill {
    @ConfigValue("invincibility-duration")
    private int INVINCIBILITY_DURATION;
    @InjectTimer(durationField = "INVINCIBILITY_DURATION")
    private MinecraftSkillTimer invincibilityTimer;

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player) || !isActiveFor(player)) {
            return;
        }

        if (e.getCause() == EntityDamageEvent.DamageCause.VOID) {
            return;
        }

        if (!invincibilityTimer.isActive(player)) {
            return;
        }

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

    @EventHandler
    public void onDimensionChange(PlayerChangedWorldEvent e) {
        invincibilityTimer.start(e.getPlayer());
    }
}
