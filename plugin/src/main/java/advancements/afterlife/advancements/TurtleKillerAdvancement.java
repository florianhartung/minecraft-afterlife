package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class TurtleKillerAdvancement extends AdvancementListener {
    public TurtleKillerAdvancement() {
        super("afterlife", "turtle_killer");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.TURTLE_EGG) {
            grantAdvancement(e.getPlayer());
        }
    }
}
