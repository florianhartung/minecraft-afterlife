package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;

import java.util.List;

public class CraftDiamondPieceAdvancement extends AdvancementListener {
    public static final List<Material> DIAMOND_PIECES = List.of(Material.DIAMOND_SWORD, Material.DIAMOND_AXE, Material.DIAMOND_HOE, Material.DIAMOND_PICKAXE, Material.DIAMOND_SHOVEL, Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS);


    public CraftDiamondPieceAdvancement() {
        super("afterlife", "craft_diamond_piece");
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (DIAMOND_PIECES.contains(e.getRecipe().getResult().getType())) {
            if (e.getWhoClicked() instanceof Player player) {
                grantAdvancement(player);
            }
        }
    }
}
