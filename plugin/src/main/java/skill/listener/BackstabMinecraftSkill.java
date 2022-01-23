package skill.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import skill.generic.PlayerMinecraftSkill;

public class BackstabMinecraftSkill extends PlayerMinecraftSkill {
    private static final double MAX_ANGLE = 60.0d;
    private static final double DAMAGE_AMPLIFICATION = 2.0d;


    @EventHandler
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player && isActiveFor(player)) {
            Location targetLocation = e.getEntity().getLocation();
            Location damagerLocation = e.getDamager().getLocation();

            Vector damagerTarget = targetLocation.subtract(damagerLocation)
                    .toVector()
                    .setY(0);
            Vector targetLookingDirection = targetLocation.getDirection()
                    .setY(0);

            double angle = damagerTarget.angle(targetLookingDirection);
            double angleInDegrees = angle / Math.PI * 180;

            if (angleInDegrees < MAX_ANGLE) {
                e.setDamage(e.getFinalDamage() * DAMAGE_AMPLIFICATION);
            }
        }
    }
}
