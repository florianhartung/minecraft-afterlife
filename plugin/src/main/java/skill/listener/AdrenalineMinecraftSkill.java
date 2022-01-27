package skill.listener;

import main.ChatHelper;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.CooldownMinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

@Configurable("adrenaline")
public class AdrenalineMinecraftSkill extends CooldownMinecraftSkill {
    @ConfigValue("cooldown")
    private int COOLDOWN;
    @ConfigValue("heal-duration")
    private int HEAL_DURATION; //ticks
    @ConfigValue("heal-amplifier")
    private int HEAL_AMPLIFIER;
    @ConfigValue("activation-health")
    private double ACTIVATION_HEALTH; // half hearts


    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player && isActiveFor(player) && !isOnCooldown(player)) {
            double finalHealth = player.getHealth() - e.getFinalDamage();
            if (finalHealth > 0 && finalHealth <= ACTIVATION_HEALTH) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, HEAL_DURATION, HEAL_AMPLIFIER, false, true));
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation(), 20, 1, 1, 1);
                ChatHelper.sendMessage(player, ChatColor.LIGHT_PURPLE + "Du verspürst ganz plötzlich einen Adrenalinschub");
                startCooldown(player);
            }
        }
    }

    @Override
    protected void startCooldown(Player player) {
        setCooldown(COOLDOWN);
        super.startCooldown(player);
    }
}
