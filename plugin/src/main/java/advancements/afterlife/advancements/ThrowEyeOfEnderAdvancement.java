package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class ThrowEyeOfEnderAdvancement extends AdvancementListener {
    public ThrowEyeOfEnderAdvancement() {
        super("afterlife", "throw_eye_of_ender");
    }

    @EventHandler
    public void onThrow(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getItem() != null && e.getItem().getType() == Material.ENDER_EYE) {
                if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.END_PORTAL_FRAME) {
                    return;
                }
                grantAdvancement(e.getPlayer());
            }
        }
    }
}
