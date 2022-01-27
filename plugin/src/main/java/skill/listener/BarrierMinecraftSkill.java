package skill.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.ChargingMinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

@Configurable("barrier")
public class BarrierMinecraftSkill extends ChargingMinecraftSkill {

    @ConfigValue("absorption-amount")
    private double ABSORPTION_AMOUNT;
    @ConfigValue("charge-time")
    private int CHARGE_TIME;


    @Override
    public void apply(Player player) {
        super.apply(player);

        beginCharging(player);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        beginCharging(e.getPlayer());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player && isActiveFor(player)) {
            beginCharging(player);
        }
    }

    @Override
    protected void onChargingFinished(Player player) {
        PotionEffect currentEffect = player.getPotionEffect(PotionEffectType.ABSORPTION);
        double maxAbsorptionFromEffects = 0;
        if (currentEffect != null) {
            maxAbsorptionFromEffects = currentEffect.getAmplifier() * 4.0d + 4.0d;
        }
        player.setAbsorptionAmount(Math.min(player.getAbsorptionAmount() + ABSORPTION_AMOUNT, maxAbsorptionFromEffects + ABSORPTION_AMOUNT));
    }

    @Override
    protected void beginCharging(Player player) {
        setChargeTime(CHARGE_TIME);
        super.beginCharging(player);
    }
}
