package skill.skills;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectTimer;

import java.util.Optional;
import java.util.logging.Level;

@Configurable("dimension-jumper")
public class DimensionJumperMinecraftSkill extends MinecraftSkill {
    @ConfigValue("invincibility-duration")
    private int INVINCIBILITY_DURATION;
    @InjectTimer(durationField = "INVINCIBILITY_DURATION")
    private MinecraftSkillTimer invincibilityTimer;
    @InjectTimer(durationField = "INVINCIBILITY_DURATION")
    private MinecraftSkillTimer deathTimer;

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
    public void onPlayerDeath(PlayerDeathEvent e) {
        invincibilityTimer.cancel(e.getEntity());
        if (willRespawningInDifferentWorld(e.getEntity())) {
            deathTimer.start(e.getEntity());
        }
    }

    @EventHandler
    public void onDimensionChange(PlayerChangedWorldEvent e) {
        if (deathTimer.isActive(e.getPlayer())) {
            deathTimer.cancel(e.getPlayer());
            return;
        }

        invincibilityTimer.start(e.getPlayer());
    }

    public boolean willRespawningInDifferentWorld(Player player) {
        World spawnpointWorld = Optional.ofNullable(player.getBedSpawnLocation()).map(Location::getWorld).orElse(null);
        if (spawnpointWorld == null) {
            spawnpointWorld = Bukkit.getServer().getWorld("world");
            if (spawnpointWorld == null) {
                Bukkit.getLogger().log(Level.WARNING, "[Minecraft-Afterlife] Dimension Jumper could not find the default 'world'. It may not properly work, when players do not have a custom spawnpoint set!");
                return false;
            }
        }

        return !player.getWorld().equals(spawnpointWorld);
    }
}
