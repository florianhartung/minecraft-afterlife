package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public class CoalceptionAdvancement extends AdvancementListener {
    public CoalceptionAdvancement() {
        super("afterlife", "coalception");
    }

    @EventHandler
    public void onFurnaceSmelt(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (e.getInventory().getType() != InventoryType.FURNACE) {
            return;
        }

        FurnaceInventory furnace = (FurnaceInventory) e.getInventory();
        if (e.getSlotType() == InventoryType.SlotType.RESULT && isCoal(furnace.getFuel()) && isCoal(furnace.getResult())) {
            grantAdvancement(player);
        }
    }

    private boolean isCoal(@Nullable ItemStack itemStack) {
        return Optional.ofNullable(itemStack).map(ItemStack::getType).map(type -> type == Material.COAL).orElse(false);
    }
}
