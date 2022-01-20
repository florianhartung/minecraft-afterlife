package skill.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;
import skill.generic.MinecraftSkill;

public class HitmanSkill extends MinecraftSkill {

    private int taskId = -1;
    private final Plugin plugin;

    public HitmanSkill(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent e) {
        System.out.println("toggle");
        System.out.println(e.isSneaking());
        if (e.isSneaking()) {
            taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                e.getPlayer().sendMessage("You successfully sneaked for 5 seconds");
                taskId = -1;
            }, 5 * 20);
        } else if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    private Entity target = null;

    @EventHandler
    public void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            target = e.getEntity();
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (target != null) {
            Vector playerTarget = target.getLocation().toVector().subtract(e.getPlayer().getLocation().toVector());
            double angle = e.getPlayer().getLocation().getDirection().angle(playerTarget);
            double angleInDegrees = angle / Math.PI * 180;
            if (angleInDegrees < 30) {
                if (e.getPlayer().hasLineOfSight(target)) {
                    e.getPlayer().sendMessage(angleInDegrees + ": " + ChatColor.GREEN + "yes");
                }

            } else {

                e.getPlayer().sendMessage(angleInDegrees + ": " + ChatColor.RED + "no");
            }
        }
    }

    @Override
    public void apply(Player player) {

    }

    @Override
    public void remove(Player player) {

    }
}
