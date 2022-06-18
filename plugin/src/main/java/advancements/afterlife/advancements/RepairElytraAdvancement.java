package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class RepairElytraAdvancement extends AdvancementListener {
    public RepairElytraAdvancement() {
        super("afterlife", "repair_elytra");
    }

    @EventHandler
    public void onRepair(CraftItemEvent e) {
        if (e.getClickedInventory() == null) {
            return;
        }

        if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.ELYTRA) {
            if (e.getWhoClicked() instanceof Player player) {
                grantAdvancement(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(e.getInventory() instanceof AnvilInventory anvilInventory)) {
            return;
        }

        // Check if slot is result slot
        int rawSlot = e.getRawSlot();
        if (rawSlot != e.getView().convertSlot(rawSlot) || rawSlot != 2) {
            return;
        }

        // all three items in the anvil inventory
        ItemStack[] items = anvilInventory.getContents();

        boolean isFirstElytra = Optional.ofNullable(items[0]).map(item -> item.getType() == Material.ELYTRA).orElse(false);
        boolean isSecondRepairMaterial = Optional.ofNullable(items[1]).map(item -> item.getType() == Material.ELYTRA || item.getType() == Material.PHANTOM_MEMBRANE).orElse(false);

        if (isFirstElytra && isSecondRepairMaterial) {
            grantAdvancement(player);
        }

//        ItemStack item3 = e.getCurrentItem();

//        if (item3 != null) {
//            ItemMeta meta = item3.getItemMeta();

//            if (meta != null) {
//                // get the repairable interface to obtain the repair cost
//                if (meta instanceof Repairable) {
//                    Repairable repairable = (Repairable) meta;
//                    int repairCost = repairable.getRepairCost();

//                    if (player.getLevel() >= repairCost) {
//// success
//                    } else {
//// bugger
//                    }
//                }
//            }
    }
}
