package skill.listener.spiderqueen;

import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftSpider;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Spider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import skill.generic.PlayerMinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Configurable("spider-queen")
public class SpiderQueenMinecraftSkill extends PlayerMinecraftSkill {
    @SuppressWarnings("deprecation")
    private static final NamespacedKey SPAWN_EGG_KEY = new NamespacedKey("afterlife", "spider_servant");
    // Random field prevents stacking of multiple items
    @SuppressWarnings("deprecation")
    private static final NamespacedKey SPAWN_EGG_RANDOM_FIELD = new NamespacedKey("afterlife", "spider_servant_prevent_stacking");

    @ConfigValue("damage")
    private static double DAMAGE;
    @ConfigValue("slow-duration")
    private static int SLOW_DURATION; // in ticks
    @ConfigValue("slow-strength")
    private static int SLOW_STRENGTH;
    @ConfigValue("drop-chance")
    private static double DROP_CHANCE;
    @ConfigValue("hatch-time")
    private static int HATCH_TIME;
    @InjectPlugin
    private static Plugin plugin;

    private final Set<Location> placedEggs = new HashSet<>();

    @EventHandler
    public void onSpiderKill(EntityDeathEvent e) {
        Player killer = e.getEntity().getKiller();
        if (killer == null || !isActiveFor(killer)) {
            return;
        }
        System.out.println(e.getEntity());
        System.out.println(isSpiderServant(e.getEntity()));
        System.out.println(e instanceof Spider);
        System.out.println(e instanceof CaveSpider);
        if (e.getEntity() instanceof Spider killedSpider && !(killedSpider instanceof CaveSpider) && !isSpiderServant(e.getEntity())) {
            if (generateDrop()) {
                ItemStack i = new ItemStack(Material.CONDUIT, 1);
                ItemMeta meta = i.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.GOLD + "Spinnenei");
                    meta.getPersistentDataContainer().set(SPAWN_EGG_KEY, PersistentDataType.INTEGER, 1);
                    meta.getPersistentDataContainer().set(SPAWN_EGG_RANDOM_FIELD, PersistentDataType.DOUBLE, Math.random());
                    i.setItemMeta(meta);

                    e.getDrops().add(i);
                }
            }
        }
    }

    @EventHandler
    public void onEggBreak(BlockBreakEvent e) {
        if (e.getBlock().getType() == Material.CONDUIT) {
            if (placedEggs.contains(e.getBlock().getLocation())) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEggPlace(BlockPlaceEvent e) {
        if (!isActiveFor(e.getPlayer())) {
            return;
        }


        ItemStack placedItem = e.getItemInHand();
        if (placedItem.getType() != Material.CONDUIT) {
            return;
        }

        ItemMeta meta = placedItem.getItemMeta();
        if (meta == null) {
            return;
        }

        Integer i = meta.getPersistentDataContainer().get(SPAWN_EGG_KEY, PersistentDataType.INTEGER);
        if (i != null && i == 1) {
            Location loc = e.getBlockPlaced().getLocation().add(0.5, 0.5, 0.5);
            Player owner = e.getPlayer();


            placedEggs.add(e.getBlockPlaced().getLocation());
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                Optional.ofNullable(loc.getWorld())
                        .ifPresent(world -> world.playSound(loc, Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, SoundCategory.HOSTILE, 0.2f, 2f));

                int shriekDelay = 2;
                for (int j = 0; j < 3; j++) {
                    spawnShriekParticle(loc, j * shriekDelay);
                }
            }, HATCH_TIME * 20L - 10);

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                loc.getBlock().setType(Material.AIR);
                placedEggs.remove(e.getBlockPlaced().getLocation());
                SpiderServantEntity sse = new SpiderServantEntity(loc, owner, SLOW_STRENGTH, SLOW_DURATION, DAMAGE);
                sse.getLevel().addFreshEntity(sse, CreatureSpawnEvent.SpawnReason.CUSTOM);

            }, HATCH_TIME * 20L);
        }
    }

    private void spawnShriekParticle(Location location, int tickDelay) {
        Optional.ofNullable(location.getWorld())
                .ifPresent(world -> world.spawnParticle(Particle.SHRIEK, location, 1, 0, 0, 0, 0, tickDelay));
    }

    private boolean generateDrop() {
        return Math.random() <= DROP_CHANCE;
    }

    private boolean isSpiderServant(Entity entity) {
        if (entity instanceof CraftSpider craftSpider) {
            return craftSpider.getHandle() instanceof SpiderServantEntity;
        }
        return false;
    }
}
