package skill.skills;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

@Configurable("bloodlust")
public class BloodlustMinecraftSkill extends MinecraftSkill {
    @ConfigValue("speed-duration")
    private static int SPEED_DURATION; // in ticks
    @ConfigValue("speed-amplifier")
    private static int SPEED_AMPLIFIER;

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onKill(EntityDeathEvent e) {
        EntityDamageEvent lastDamage = e.getEntity().getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent lastDamageByEntity) {
            if (lastDamageByEntity.getDamager() instanceof Player player && isActiveFor(player)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, SPEED_DURATION, SPEED_AMPLIFIER, true));
                e.getEntity().getWorld().spawnParticle(Particle.BLOCK_CRACK, e.getEntity().getLocation().add(0, 1, 0), 10, 0.3, 0.5, 0.3, new MaterialData(Material.REDSTONE_BLOCK));
            }
        }
    }
}
