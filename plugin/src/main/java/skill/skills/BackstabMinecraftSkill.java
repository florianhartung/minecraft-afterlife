package skill.skills;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

@Configurable("backstab")
public class BackstabMinecraftSkill extends MinecraftSkill {
    @ConfigValue("max-angle")
    private double MAX_ANGLE;
    @ConfigValue("damage-amplification")
    private double DAMAGE_AMPLIFICATION;

    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (e.getDamager() instanceof Player player && isActiveFor(player)) {
            Location targetLocation = e.getEntity().getLocation();
            Location damagerLocation = e.getDamager().getLocation();

            Vector damagerTarget = targetLocation.subtract(damagerLocation).toVector().setY(0);
            Vector targetLookingDirection = targetLocation.getDirection().setY(0);

            double angle = damagerTarget.angle(targetLookingDirection);
            double angleInDegrees = angle / Math.PI * 180;

            if (angleInDegrees < MAX_ANGLE) {
                e.setDamage(e.getFinalDamage() * DAMAGE_AMPLIFICATION);
            }
        }
    }
}
