package skill.skills;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;
import skill.injection.InjectTimer;

import java.util.*;

@Configurable("duskblade")
public class DuskbladeMinecraftSkill extends MinecraftSkill {

    @ConfigValue("invisibility-duration")
    private static int INVISIBILITY_DURATION;
    @ConfigValue("effects-delay")
    private static int EFFECTS_DELAY;


    @InjectPlugin
    private Plugin plugin;
    @InjectTimer(durationField = "INVISIBILITY_DURATION", onTimerFinished = "stopInvisibility")
    private MinecraftSkillTimer invisibilityTimer;


    private final Map<UUID, Integer> invisibilityTasks = new HashMap<>();

    @EventHandler
    public void onKill(EntityDeathEvent e) {
        EntityDamageEvent lastDamage = e.getEntity().getLastDamageCause();
        if (!(lastDamage instanceof EntityDamageByEntityEvent damageByEntityEvent)) {
            return;
        }

        if (!(damageByEntityEvent.getDamager() instanceof Player player)) {
            return;
        }

        if (!isActiveFor(player)) {
            return;
        }

        startInvisibility(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (isActiveFor(e.getPlayer())) {
            stopInvisibility(e.getPlayer());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        invisibilityTasks.keySet().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> e.getPlayer().hidePlayer(plugin, player));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(EntityDamageEvent e) {
        if (e instanceof Player player && isInvisible(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDealDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player && isInvisible(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        if (!isActiveFor(player)) {
            return;
        }

        stopInvisibility(player);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent e) {
        if (isInvisible(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (isInvisible(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    private boolean isInvisible(Player player) {
        return isActiveFor(player) && invisibilityTimer.isActive(player);
    }

    private void startInvisibility(Player player) {
        Optional.ofNullable(invisibilityTasks.remove(player.getUniqueId())).ifPresent(taskId -> Bukkit.getScheduler().cancelTask(taskId));

        invisibilityTimer.start(player);
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> showInvisibilityEffect(player), 0, EFFECTS_DELAY);
        invisibilityTasks.put(player.getUniqueId(), taskId);
        hidePlayer(player);
    }

    private void hidePlayer(Player toHide) {
        Bukkit.getOnlinePlayers().forEach(player -> player.hidePlayer(plugin, toHide));
    }

    private void showPlayer(Player toShow) {
        Bukkit.getOnlinePlayers().forEach(player -> player.showPlayer(plugin, toShow));
    }

    private void stopInvisibility(Player player) {
        Optional.ofNullable(invisibilityTasks.remove(player.getUniqueId())).ifPresent(Bukkit.getScheduler()::cancelTask);
        showPlayer(player);
    }

    private void showInvisibilityEffect(Player player) {
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation().add(0, 1, 0), 20, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(50, 0, 35), 2));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GOAT_STEP, 0.4f, 0.4f);
    }
}
