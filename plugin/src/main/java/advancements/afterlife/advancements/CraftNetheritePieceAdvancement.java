package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.SmithItemEvent;

public class CraftNetheritePieceAdvancement extends AdvancementListener {
    public CraftNetheritePieceAdvancement() {
        super("afterlife", "netherite");
    }

    @EventHandler
    public void onSmith(SmithItemEvent e) {
        if (e.getWhoClicked() instanceof Player player) {
            grantAdvancement(player);
        }
    }
}
