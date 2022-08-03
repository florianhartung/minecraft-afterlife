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

import static skill.skills.HeavyMetalMinecraftSkill.duplicateBlockBreakDrops;

@Configurable("crystal-king")
public class CrystalKingMinecraftSkill extends MinecraftSkill {
    @ConfigValue(value = "materials", mapper = "mapMaterials")
    private List<Material> MATERIALS;
    @ConfigValue("stddev")
    private double STDDEV;

    private final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        if (isActiveFor(e.getPlayer()) && !e.isCancelled()) {
            if (MATERIALS.contains(e.getBlock().getType())) {
                ItemStack itemInMainHand = e.getPlayer().getInventory().getItemInMainHand();
                if (!itemInMainHand.getEnchantments().containsKey(Enchantment.SILK_TOUCH)) {
                    double lootFactor = Math.abs(random.nextGaussian(0, STDDEV));
                    duplicateBlockBreakDrops(e, e.getPlayer().getInventory().getItemInMainHand(), lootFactor);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private List<Material> mapMaterials(List<String> materials) {
        return materials.stream()
                .map(Material::valueOf)
                .toList();
    }
}
