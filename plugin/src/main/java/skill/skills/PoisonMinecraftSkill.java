package skill.skills;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import java.util.List;

@Configurable("poison")
public class PoisonMinecraftSkill extends MinecraftSkill {
    private static final List<Material> SWORDS = List.of(Material.WOODEN_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD, Material.STONE_SWORD);

    @ConfigValue("effect-duration")
    private static int EFFECT_DURATION;
    @ConfigValue("effect-amplifier")
    private static int EFFECT_AMPLIFIER;
    @ConfigValue("chance")
    private static double CHANCE;


    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player) || e.isCancelled()) {
            return;
        }

        if (!isActiveFor(player)) {
            return;
        }

        if (!(e.getEntity() instanceof LivingEntity target)) {
            return;
        }

        if (!SWORDS.contains(player.getInventory().getItemInMainHand().getType())) {
            return;
        }

        if (Math.random() < CHANCE) {
            applyPoison(target);
        }
    }


    private void applyPoison(LivingEntity e) {
        e.addPotionEffect(new PotionEffect(PotionEffectType.POISON, EFFECT_DURATION, EFFECT_AMPLIFIER, false, true));

        Location particleLocation = e.getLocation().add(0, 0.5, 0);
        e.getWorld().spawnParticle(Particle.WAX_OFF, particleLocation, 3, 0.3, 0.6, 0.3);
        e.getWorld().spawnParticle(Particle.REDSTONE, particleLocation, 25, 0.3, 0.6, 0.3, new Particle.DustOptions(Color.fromRGB(25, 75, 25), 1.8f));
    }
}
