package skill.listener;

import main.ChatHelper;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.Configurable;
import skill.generic.CooldownMinecraftSkill;


public class AdrenalineMinecraftSkill extends CooldownMinecraftSkill implements Configurable {
    private static int HEAL_DURATION; //ticks
    private static int HEAL_AMPLIFIER;
    private static double ACTIVATION_HEALTH; // half hearts


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
    public void setConfig(ConfigurationSection config) {
        HEAL_DURATION = config.getInt("heal-duration");
        HEAL_AMPLIFIER = config.getInt("heal-amplifier");
        ACTIVATION_HEALTH = config.getDouble("activation-health");
        setCooldown(config.getInt("cooldown"));
    }

    @Override
    public String getConfigPath() {
        return "adrenaline";
    }
}
