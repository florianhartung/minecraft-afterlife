package skill.skills;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import java.util.Random;

@Configurable("karma")
public class KarmaMinecraftSkill extends MinecraftSkill {

    @ConfigValue("damage-reflection-chance")
    private static double DAMAGE_REFLECTION_CHANCE;

    private final Random random = new Random();

    @EventHandler
    public void onBlockDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player player) || !(e.getDamager() instanceof LivingEntity damager)) {
            return;
        }

        if (!isActiveFor(player)) {
            return;
        }

        if (e.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) == 0) {
            return;
        }

        if (random.nextDouble() < DAMAGE_REFLECTION_CHANCE) {
            damager.damage(e.getDamage(), player);
            if (damager instanceof Player damagerPlayer) {
                damagerPlayer.playSound(player, Sound.ENTITY_WITCH_CELEBRATE, SoundCategory.PLAYERS, 0.1f, 1.8f);
            }
        }
    }

}
