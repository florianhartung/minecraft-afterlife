package skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectTimer;

@Configurable("barrier")
public class BarrierMinecraftSkill extends MinecraftSkill {
    @ConfigValue("absorption-amount")
    private double ABSORPTION_AMOUNT;
    @ConfigValue("charge-time")
    private int CHARGE_TIME;
    @InjectTimer(durationField = "CHARGE_TIME", onTimerFinished = "onChargingFinished")
    private MinecraftSkillTimer chargingTimer;

    @Override
    public void apply(Player player) {
        super.apply(player);

        chargingTimer.start(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (isActiveFor(e.getPlayer())) {
            chargingTimer.start(e.getPlayer());
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player && isActiveFor(player)) {
            chargingTimer.start(player);
        }
    }

    private void onChargingFinished(Player player) {
        PotionEffect currentEffect = player.getPotionEffect(PotionEffectType.ABSORPTION);
        double maxAbsorptionFromEffects = 0;
        if (currentEffect != null) {
            maxAbsorptionFromEffects = currentEffect.getAmplifier() * 4.0d + 4.0d;
        }
        player.setAbsorptionAmount(Math.min(player.getAbsorptionAmount() + ABSORPTION_AMOUNT, maxAbsorptionFromEffects + ABSORPTION_AMOUNT));
    }
}
