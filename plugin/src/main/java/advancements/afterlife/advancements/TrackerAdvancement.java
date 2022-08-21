package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import config.Config;
import config.ConfigType;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import performancereport.PerfReport;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static main.Util.unsafeListCast;

public class TrackerAdvancement extends AdvancementListener {

    private static final NamespacedKey TRACKER_ITEM_KEY = NamespacedKey.fromString("afterlife:construct_shard_tracker");

    public ShapedRecipe constructShardTrackerRecipe;
    public final List<Location> skillBlockLocations;

    public TrackerAdvancement() {
        super("afterlife", "craft_construct_shard_tracker");

        skillBlockLocations = unsafeListCast(Config.get(ConfigType.SKILL_BLOCKS).getList("blocks"));

        registerConstructShardTrackerRecipe();
    }

    private void registerConstructShardTrackerRecipe() {
        ItemStack constructShardTracker = new ItemStack(Material.COMPASS);

        ItemMeta meta = Objects.requireNonNull(constructShardTracker.getItemMeta());
        meta.setDisplayName(ChatColor.GOLD + "Gral-Kompass");

        CompassMeta compassMeta = (CompassMeta) meta;
        compassMeta.setLodestoneTracked(true);
        compassMeta.setLodestone(skillBlockLocations.get(0));

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(TRACKER_ITEM_KEY, PersistentDataType.BYTE, (byte) 1);

        constructShardTracker.setItemMeta(meta);

        constructShardTrackerRecipe = new ShapedRecipe(TRACKER_ITEM_KEY, constructShardTracker);
        constructShardTrackerRecipe.shape("lgl", "cCc", "ttt");
        constructShardTrackerRecipe.setIngredient('t', Material.GRAY_GLAZED_TERRACOTTA);
        constructShardTrackerRecipe.setIngredient('g', Material.GLASS);
        constructShardTrackerRecipe.setIngredient('c', Material.CUT_COPPER);
        constructShardTrackerRecipe.setIngredient('C', Material.COMPASS);
        constructShardTrackerRecipe.setIngredient('l', Material.LIGHTNING_ROD);

        Bukkit.getServer()
                .addRecipe(constructShardTrackerRecipe);
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        if (e.getRecipe() instanceof Keyed keyed && keyed.getKey().equals(constructShardTrackerRecipe.getKey())) {
            if (e.getWhoClicked() instanceof Player player) {
                grantAdvancement(player);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        PerfReport.startTimer("advancement.tracker");
        Location playerLocation = e.getTo();
        if (playerLocation == null) {
            PerfReport.endTimer("advancement.tracker");
            return;
        }

        skillBlockLocations.stream()
                .filter(location -> Optional.ofNullable(location.getWorld()).map(w -> w.equals(playerLocation.getWorld())).orElse(false))
                .min((o1, o2) -> {
                    double d1 = playerLocation.distanceSquared(o1);
                    double d2 = playerLocation.distanceSquared(o2);

                    return Double.compare(d1, d2);
                })
                .map(nearest -> new Location(nearest.getWorld(), nearest.getX(), -64, nearest.getZ())) // Points to lodestone at y=-64, so minecraft won't convert the tracker back to a normal compass due to the non-existing lodestone
                .ifPresent(nearest -> updateTrackersInInventory(e.getPlayer().getInventory(), nearest));

        PerfReport.endTimer("advancement.tracker");
    }

    public void updateTrackersInInventory(PlayerInventory inventory, Location nearestSkillBlock) {
        StreamSupport.stream(inventory.spliterator(), false)
                .filter(this::isTracker)
                .forEach(trackerItem -> {
                    CompassMeta compassMeta = (CompassMeta) trackerItem.getItemMeta();
                    if (compassMeta == null) {
                        return;
                    }

                    compassMeta.setLodestone(nearestSkillBlock);

                    trackerItem.setItemMeta(compassMeta);
                });
    }

    public boolean isTracker(@Nullable ItemStack item) {
        return Optional.ofNullable(item)
                .map(ItemStack::getItemMeta)
                .map(ItemMeta::getPersistentDataContainer)
                .map(persistentDataContainer -> persistentDataContainer.get(TRACKER_ITEM_KEY, PersistentDataType.BYTE))
                .filter(b -> b == 1)
                .isPresent();
    }

}
