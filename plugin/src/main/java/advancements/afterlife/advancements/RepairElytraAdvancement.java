package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;

public class RepairElytraAdvancement extends AdvancementListener {
    public RepairElytraAdvancement() {
        super("afterlife", "repair_elytra");
    }

    @EventHandler
    public void onRepair(CraftItemEvent e) {
        if (e.getClickedInventory() == null) {
            return;
        }

        switch (e.getClickedInventory().getType()) {
            case CRAFTING, PLAYER -> {
                if (e.getRecipe().getResult().getType() == Material.ELYTRA) {
                    if (e.getWhoClicked() instanceof Player player) {
                        grantAdvancement(player);
                    }
                }
            }
            case ANVIL -> {
                if (e.getRecipe().getResult().getType() == Material.ELYTRA) {
                    if (e.getWhoClicked() instanceof Player player) {
                        grantAdvancement(player);
                    }
                }
            }
        }
    }
}
