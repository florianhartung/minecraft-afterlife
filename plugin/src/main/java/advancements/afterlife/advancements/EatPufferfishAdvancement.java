package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class EatPufferfishAdvancement extends AdvancementListener {
    public EatPufferfishAdvancement() {
        super("afterlife", "eat_pufferfish");
    }

    @EventHandler
    public void onEatPufferfish(PlayerItemConsumeEvent e) {
        if (e.getItem().getType() == Material.PUFFERFISH) {
            grantAdvancement(e.getPlayer());
        }
    }
}
