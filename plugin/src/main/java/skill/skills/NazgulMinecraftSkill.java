package skill.skills;

import main.Util;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectTimer;

import java.util.List;

@Configurable("nazgul")
public class NazgulMinecraftSkill extends MinecraftSkill {
    @ConfigValue("health-percentage-disable-threshold")
    private static double HEALTH_PERCENTAGE_DISABLE_THRESHOLD;
    @ConfigValue("maximum-effect-duration")
    private static int MAXIMUM_EFFECT_DURATION;
    @ConfigValue("wither-amplifier")
    private static int WITHER_AMPLIFIER;
    @ConfigValue("mining-fatigue-amplifier")
    private static int MINING_FATIGUE_AMPLIFIER;


    @InjectTimer(durationField = "MAXIMUM_EFFECT_DURATION")
    private MinecraftSkillTimer effectTimer;

    @EventHandler
    private void applyEffects(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player) || !isActiveFor(player) || e.isCancelled()) {
            return;
        }

        if (!(e.getEntity() instanceof Player target)) {
            return;
        }

        if (Util.healthPercentage(target) < 1.0) {
            return;
        }

        effectTimer.start(target);
        target.addPotionEffects(List.of(
                new PotionEffect(PotionEffectType.BLINDNESS, MAXIMUM_EFFECT_DURATION, 0, false, true, true),
                new PotionEffect(PotionEffectType.WITHER, MAXIMUM_EFFECT_DURATION, WITHER_AMPLIFIER, false, true, true),
                new PotionEffect(PotionEffectType.SLOW_DIGGING, MAXIMUM_EFFECT_DURATION, MINING_FATIGUE_AMPLIFIER, false, true, true)));
    }

    @EventHandler
    private void removeEffects(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player) || !effectTimer.isActive(player) || e.isCancelled()) {
            return;
        }

        if ((player.getHealth() - e.getFinalDamage()) / player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() > HEALTH_PERCENTAGE_DISABLE_THRESHOLD) {
            return;
        }

        effectTimer.cancel(player);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
    }
}
