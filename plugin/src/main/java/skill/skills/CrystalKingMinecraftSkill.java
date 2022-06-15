package skill.skills;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import java.util.List;
import java.util.Random;

@Configurable("crystal-king")
public class CrystalKingMinecraftSkill extends MinecraftSkill {
    @ConfigValue(value = "materials", mapper = "mapMaterials")
    private List<Material> MATERIALS;
    @ConfigValue("stddev")
    private double STDDEV;

    private final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (isActiveFor(e.getPlayer()) && !e.isCancelled()) {
            if (MATERIALS.contains(e.getBlock().getType())) {
                ItemStack itemInMainHand = e.getPlayer().getInventory().getItemInMainHand();
                if (!itemInMainHand.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                    List<ItemStack> drops = e.getBlock()
                            .getDrops(itemInMainHand, e.getPlayer())
                            .stream()
                            .filter(itemStack -> itemStack.getType() != Material.AIR)
                            .toList();
                    double lootFactor = Math.abs(random.nextGaussian(0, STDDEV));

                    drops.forEach(drop -> drop.setAmount((int) Math.round(drop.getAmount() * lootFactor)));
                    drops.forEach(drop -> e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), drop));
                }
            }
        }
    }

    private List<Material> mapMaterials(List<String> materials) {
        return materials.stream()
                .map(Material::valueOf)
                .toList();
    }
}
