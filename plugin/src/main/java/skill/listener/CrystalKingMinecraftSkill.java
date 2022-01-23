package skill.listener;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import skill.generic.PlayerMinecraftSkill;

import java.util.List;
import java.util.Random;

public class CrystalKingMinecraftSkill extends PlayerMinecraftSkill {
    private static final List<Material> MATERIALS = List.of(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.AMETHYST_CLUSTER, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE);

    private static final double STDDEV = 0.5d;
    private static final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (isActiveFor(e.getPlayer()) && !e.isCancelled()) {
            if (MATERIALS.contains(e.getBlock().getType())) {
                ItemStack itemInMainHand = e.getPlayer().getInventory().getItemInMainHand();
                if (!itemInMainHand.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                    List<ItemStack> drops = e.getBlock()
                            .getDrops(itemInMainHand, e.getPlayer())
                            .stream()
                            .toList();
                    double lootFactor = Math.abs(random.nextGaussian(0, STDDEV));
                    e.getPlayer().sendMessage(String.valueOf(lootFactor));

                    drops.forEach(drop -> drop.setAmount((int) Math.round(drop.getAmount() * lootFactor)));
                    drops.forEach(drop -> e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop));
                }
            }
        }
    }
}
