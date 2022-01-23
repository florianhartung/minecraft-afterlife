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


public class AdrenalineMinecraftSkill extends CooldownMinecraftSkill {

    private static final int COOLDOWN = 20 * 1000; // ticks
    private static final int HEAL_DURATION = 1 * 20; //ticks
    private static final int HEAL_AMPLIFIER = 4;
    private static final double ACTIVATION_HEALTH = 3.0d; // half hearts

    public AdrenalineMinecraftSkill() {
        super(COOLDOWN);
    }

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
}
