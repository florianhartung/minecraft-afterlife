package skill.listener;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.springframework.data.util.Pair;
import skill.generic.PlayerMinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import java.util.*;


@Configurable("virus")
public class VirusMinecraftSkill extends PlayerMinecraftSkill {

    /**
     * How many times the damage jumps to other entites
     */
    @ConfigValue("chain-jumps")
    private static int CHAIN_JUMPS;

    /**
     * The amount of damage falloff on each jump. In percent.<br>
     * A value of 0.5 would mean that the damage is halfed on every jump
     */
    @ConfigValue("damage-falloff")
    private static double DAMAGE_FALLOFF;

    /**
     * Maximum distance the virus can travel in one jump in blocks
     */
    @ConfigValue("max-jump-distance")
    private static double MAX_JUMP_DISTANCE;

    /**
     * Damage that still has to be dealth by virus in pairs of the damager and target.<br>
     * This prevents the event loopback (which leads to a stackoverflow) where when the virus damage is dealt the event itself is fired again.
     */
    private final List<Pair<UUID, UUID>> toBeDamaged = new ArrayList<>();

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (toBeDamaged.remove(Pair.of(e.getDamager().getUniqueId(), e.getEntity().getUniqueId()))) {
            return;
        }

        if (e.getDamager() instanceof Player damager && isActiveFor(damager) && e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            Entity targetEntity = e.getEntity();
            if (targetEntity instanceof LivingEntity target) {
                List<LivingEntity> jumpTargets = new ArrayList<>();
                jumpTargets.add(damager);
                jumpTargets.add(target);

                for (int i = 0; i < CHAIN_JUMPS; i++) {
                    Optional<LivingEntity> newTarget = findClosestLivingEntity(lastElementInList(jumpTargets).getLocation(), jumpTargets);
                    newTarget.ifPresent(jumpTargets::add);

                    if (newTarget.isEmpty()) {
                        break;
                    }
                }
                jumpTargets.remove(0); // remove player
                jumpTargets.remove(0); // remove initial target as this one's damage is handled by minecraft

                Location lastLoc = target.getEyeLocation();
                double lastDamage = e.getDamage();
                for (LivingEntity le : jumpTargets) {
                    toBeDamaged.add(Pair.of(damager.getUniqueId(), le.getUniqueId()));
                    lastDamage *= DAMAGE_FALLOFF;
                    le.damage(lastDamage, damager);

                    drawParticleLine(lastLoc, le.getEyeLocation(), 0.1);
                    drawHitParticle(le.getEyeLocation());

                    lastLoc = le.getLocation();
                }
                damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_ZOMBIE_INFECT, SoundCategory.NEUTRAL, 0.1f, 2f);
            }
        }
    }

    private Optional<LivingEntity> findClosestLivingEntity(Location from, List<LivingEntity> ignoredEntities) {
        World world = from.getWorld();
        if (world == null) {
            System.err.println("Virus: Could not find world of player using this skill.\nLocation:" + from);
            return Optional.empty();
        }
        Collection<Entity> nearbyEntities = world.getNearbyEntities(from,
                MAX_JUMP_DISTANCE,
                MAX_JUMP_DISTANCE,
                MAX_JUMP_DISTANCE, entity -> checkJumpDistance(from, entity.getLocation()) && entity instanceof LivingEntity && !ignoredEntities.contains(entity));

        return nearbyEntities.stream()
                .min((o1, o2) -> {
                    double d1 = from.distanceSquared(o1.getLocation());
                    double d2 = from.distanceSquared(o2.getLocation());

                    return Double.compare(d1, d2);
                })
                .map(entity -> (LivingEntity) entity);
    }

    private boolean checkJumpDistance(Location from, Location to) {
        return from.distance(to) < MAX_JUMP_DISTANCE;
    }

    private static <T> T lastElementInList(List<T> list) {
        return list.get(list.size() - 1);
    }

    @SuppressWarnings("SameParameterValue")
    private void drawParticleLine(Location fromLoc, Location toLoc, double spaceBetweenParticles) {
        World world = fromLoc.getWorld();
        if (world == null) {
            return;
        }

        Vector from = fromLoc.toVector();
        Vector to = toLoc.toVector();

        double distance = from.distance(to);
        double distanceCovered = 0;

        Vector step = to.subtract(from).normalize().multiply(spaceBetweenParticles);

        for (; distanceCovered < distance; from.add(step)) {
            world.spawnParticle(Particle.REDSTONE, from.getX(), from.getY(), from.getZ(), 1, new Particle.DustOptions(Color.fromRGB(20, 255, 20), 1f));
            distanceCovered += spaceBetweenParticles;
        }
    }

    private void drawHitParticle(Location loc) {
        World world = loc.getWorld();
        if (world == null) {
            return;
        }
        world.spawnParticle(Particle.CRIT, loc, 3, 0.2, 0.5, 0.2, 0.2);
    }

}
