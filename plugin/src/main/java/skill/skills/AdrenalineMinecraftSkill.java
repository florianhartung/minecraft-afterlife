package skill.skills;

import hud.HudManager;
import main.ChatHelper;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectTimer;

@Configurable("adrenaline")
public class AdrenalineMinecraftSkill extends MinecraftSkill {
    @ConfigValue("cooldown")
    private int COOLDOWN;
    @ConfigValue("heal-duration")
    private int HEAL_DURATION; //ticks
    @ConfigValue("heal-amplifier")
    private int HEAL_AMPLIFIER;
    @ConfigValue("activation-health")
    private double ACTIVATION_HEALTH; // half hearts
    @InjectTimer(durationField = "COOLDOWN", hudEntry = HudManager.HudEntry.ADRENALINE)
    private MinecraftSkillTimer cooldownTimer;

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player && isActiveFor(player) && !cooldownTimer.isActive(player)) {
            double finalHealth = player.getHealth() - e.getFinalDamage();
            if (finalHealth > 0 && finalHealth <= ACTIVATION_HEALTH) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, HEAL_DURATION, HEAL_AMPLIFIER, false, true));
                player.getWorld().spawnParticle(Particle.HEART, player.getLocation(), 20, 1, 1, 1);
                ChatHelper.sendMessage(player, ChatColor.LIGHT_PURPLE + "Du verspürst ganz plötzlich einen Adrenalinschub");
                cooldownTimer.start(player);
            }
        }
    }

    @Override
    public void apply(Player player) {
        super.apply(player);

        HudManager.set(player, HudManager.HudEntry.ADRENALINE, 7);
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        cooldownTimer.cancel(player);
        HudManager.remove(player, HudManager.HudEntry.ADRENALINE);
    }
}
