package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class FlyWithFireworkAdvancement extends AdvancementListener {
    public FlyWithFireworkAdvancement() {
        super("afterlife", "fly_with_firework");
    }


    @EventHandler
    public void onFlyWithFirework(PlayerInteractEvent e) {
        if (e.getPlayer().isGliding()) {
            if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getItem() != null && e.getItem().getType() == Material.FIREWORK_ROCKET) {
                    grantAdvancement(e.getPlayer());
                }
            }
        }
    }
}
