package skill.skills;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import javax.annotation.Nullable;

@Configurable("no-mercy")
public class NoMercyMinecraftSkill extends MinecraftSkill {

    @ConfigValue("execution-threshold")
    private static double EXECUTION_THRESHOLD;

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player) || !(e.getEntity() instanceof LivingEntity target) || e.isCancelled()) {
            return;
        }

        if (!isActiveFor(player)) {
            return;
        }

        double finalTargetHealth = target.getHealth() - e.getFinalDamage();

        if (finalTargetHealth < EXECUTION_THRESHOLD) {
            e.setDamage(9999);

            float lightningVolume = target instanceof Player ? 0.5F : 0.2F;
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.PLAYERS, lightningVolume, 0.4F);
            target.getWorld().playSound(target.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, lightningVolume, 0.4F);

            particle(target, Particle.EXPLOSION_NORMAL, 2, null);
            particle(target, Particle.REDSTONE, 20, new Particle.DustOptions(Color.ORANGE, 1.5F));
            particle(target, Particle.VILLAGER_ANGRY, 20, null);
        }
    }

    private void particle(LivingEntity target, Particle type, int count, @Nullable Object data) {
        target.getWorld().spawnParticle(type, target.getLocation().add(0, 0.7, 0), count, 0.15, 0.4, 0.15, data);
    }
}
