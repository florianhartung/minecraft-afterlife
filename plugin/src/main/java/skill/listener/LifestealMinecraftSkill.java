package skill.listener;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import skill.generic.PlayerMinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import java.util.Objects;

/**
 * This class represents an skill, that whenever a player damages another entity, they get healed by a fixed amount
 */
@Configurable("lifesteal")
public class LifestealMinecraftSkill extends PlayerMinecraftSkill {

    /**
     * The chance that a player affected by this skill gets healed when they attack another entity
     */
    @ConfigValue("heal-chance")
    private static double HEAL_CHANCE;

    /**
     * The amount of healing the player receives
     */
    @ConfigValue("heal-amount")
    private static double HEAL_AMOUNT;


    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player && isActiveFor(player)) {
            double rand = Math.random();
            if (rand < HEAL_CHANCE) {
                if (healPlayer(player)) {
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SOUL_SAND_BREAK, SoundCategory.PLAYERS, 1, 1);
                    player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0.3, 0, 0), 7, 0.5, 0.7, 0.5, 0.1);
                }
            }
        }
    }

    /**
     * Tries to heal the given player for a fixed amount (see HEAL_AMOUNT)
     *
     * @param player The player to heal
     * @return true if the player was healed, otherwise false
     */
    private boolean healPlayer(Player player) {
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
        if (player.getHealth() < maxHealth) {
            player.setHealth(Math.min(maxHealth, player.getHealth() + HEAL_AMOUNT));
            return true;
        }
        return false;
    }
}
