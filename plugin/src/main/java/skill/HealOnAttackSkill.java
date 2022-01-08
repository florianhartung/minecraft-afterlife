package skill;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * This class represents an skill, that whenever a player damages another entity, they get healed by a fixed amount
 */
public class HealOnAttackSkill extends Skill {

    /**
     * The chance that a player affected by this skill gets healed when they attack another entity
     */
    private static final double HEAL_CHANCE = 1.0 / 8.0;

    /**
     * The amount of healing the player receives
     */
    private static final double HEAL_AMOUNT = 6.0d;

    /**
     * Contains the uuids of all players who are currently affected by this skill
     */
    List<UUID> playersAffected = new ArrayList<>();

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player) {
            double rand = Math.random();
            if (rand < HEAL_CHANCE) {
                if (healPlayer(player)) {
                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SOUL_SAND_BREAK, SoundCategory.PLAYERS, 1, 1);
                    player.getWorld().spawnParticle(Particle.SOUL, player.getLocation().add(0.3, 0, 0), 7, 0.5, 0.7, 0.5, 0.1);
                }
            }
        }
    }

    @Override
    public void apply(Player player) {
        if (!playersAffected.contains(player.getUniqueId())) {
            playersAffected.add(player.getUniqueId());
        }
    }

    @Override
    public void remove(Player player) {
        playersAffected.remove(player.getUniqueId());
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
