package skill.globalmodifiers;

import main.Util;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

@Configurable("global.regeneration")
public class NaturalRegeneration extends MinecraftSkill {

    @ConfigValue("regen-chance.day")
    private double REGEN_CHANCE_DAY;
    @ConfigValue("regen-chance.night")
    private double REGEN_CHANCE_NIGHT;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRegen(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        boolean isNighttime = Util.isNighttime(player.getWorld().getTime());
        double regenChance = switch (e.getRegainReason()) {
            case SATIATED, EATING -> isNighttime ? REGEN_CHANCE_NIGHT : REGEN_CHANCE_DAY;
            default -> 1;
        };
        if (Math.random() > regenChance) {
            e.setCancelled(true);
        }
    }
}
