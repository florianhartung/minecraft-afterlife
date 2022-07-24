package skill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Configurable("carpenter")
public class CarpenterMinecraftSkill extends MinecraftSkill {
    private static final Map<Material, TreeType> TREE_TYPE_BY_SAPLING = Map.of(Material.OAK_SAPLING, TreeType.TREE, Material.ACACIA_SAPLING, TreeType.ACACIA, Material.BIRCH_SAPLING, TreeType.BIRCH, Material.DARK_OAK_SAPLING, TreeType.DARK_OAK, Material.JUNGLE_SAPLING, TreeType.SMALL_JUNGLE, Material.MANGROVE_PROPAGULE, TreeType.MANGROVE, Material.SPRUCE_SAPLING, TreeType.REDWOOD);
    private static final List<Material> AXES = List.of(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE);

    @ConfigValue("axe-durability-ignored-chance")
    private static double AXE_DURABILITY_IGNORED_CHANCE;
    @ConfigValue("sapling-grow-delay-min")
    private static int SAPLING_GROW_DELAY_MIN;
    @ConfigValue("sapling-grow-delay-max")
    private static int SAPLING_GROW_DELAY_MAX;
    @InjectPlugin
    private Plugin plugin;

    private final Random random = new Random();

    @EventHandler
    public void onSaplingPlace(BlockPlaceEvent e) {
        if (!isActiveFor(e.getPlayer())) {
            return;
        }

        if (e.getPlayer().isSneaking()) {
            return;
        }

        Material saplingMaterial = e.getBlockPlaced().getType();
        TreeType treeType = TREE_TYPE_BY_SAPLING.get(saplingMaterial);
        if (treeType == null) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (e.getBlockPlaced().getType() == saplingMaterial) {
                e.getBlockPlaced().setType(Material.AIR);
                if (!e.getBlockPlaced().getWorld().generateTree(e.getBlockPlaced().getLocation(), treeType)) {
                    e.getBlockPlaced().setType(saplingMaterial);
                }
            }
        }, randomSaplingGrowthDelay());
    }

    private int randomSaplingGrowthDelay() {
        return random.nextInt(SAPLING_GROW_DELAY_MIN, SAPLING_GROW_DELAY_MAX);
    }

    @EventHandler
    public void onDurabilityChange(PlayerItemDamageEvent e) {
        if (!isActiveFor(e.getPlayer())) {
            return;
        }

        if (!AXES.contains(e.getItem().getType())) {
            return;
        }

        if (random.nextDouble() < AXE_DURABILITY_IGNORED_CHANCE) {
            e.setCancelled(true);
        }
    }
}
