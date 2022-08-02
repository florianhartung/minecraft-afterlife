package skill.skills;

import main.Util;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;
import skill.injection.InjectTimer;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Configurable("reaper")
public class ReaperMinecraftSkill extends MinecraftSkill {
    private static final List<Material> HOES = List.of(Material.WOODEN_HOE, Material.STONE_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE);

    @ConfigValue("stun-duration")
    private static int STUN_DURATION;
    @ConfigValue("health-stun-threshold")
    private static double HEALTH_STUN_THRESHOLD;
    @InjectPlugin(postInject = "startStunParticleTask")
    private Plugin plugin;
    @InjectTimer(durationField = "STUN_DURATION")
    public MinecraftSkillTimer stunTimer;

    @EventHandler(priority = EventPriority.LOW)
    private void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player) || !isActiveFor(player) || e.isCancelled()) {
            return;
        }

        if (!(e.getEntity() instanceof Player target)) {
            return;
        }

        if (!HOES.contains(player.getInventory().getItemInMainHand().getType())) {
            return;
        }

        double targetHealthPercentage = Util.healthPercentage(target);
        if (targetHealthPercentage < HEALTH_STUN_THRESHOLD) {
            stunTimer.start(target);
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        if (stunTimer.isActive(e.getPlayer())) {
            if (!Util.isTheSameLocation(e.getFrom(), e.getTo())) {
                Location newLocation = new Location(e.getTo().getWorld(), e.getFrom().getX(), Math.min(e.getTo().getY(), e.getFrom().getY()), e.getFrom().getZ(), e.getTo().getYaw(), e.getTo().getPitch());
                e.setTo(newLocation);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onBlockBreak(BlockBreakEvent e) {
        if (stunTimer.isActive(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onBlockPlace(BlockPlaceEvent e) {
        if (stunTimer.isActive(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onConsume(PlayerItemConsumeEvent e) {
        if (stunTimer.isActive(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player && stunTimer.isActive(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onInteract(PlayerInteractEvent e) {
        if (stunTimer.isActive(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent e) {
        stunTimer.cancel(e.getEntity());
    }

    @SuppressWarnings("unused")
    private void startStunParticleTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::spawnStunParticles, 0, 5);
    }

    private void spawnStunParticles() {
        if (stunTimer == null) {
            return;
        }

        stunTimer.players().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(player -> {
            spawnParticleRing(Particle.SPELL_WITCH, player.getLocation(), 0.6d, 40);
            player.getLocation().getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation(), 10, 0.15, 0.15, 0.15, 0.0);
        });
    }

    @SuppressWarnings("SameParameterValue")
    private void spawnParticleRing(Particle particle, Location center, double radius, int particleCount) {
        if (center.getWorld() == null) {
            return;
        }

        Consumer<Location> spawnParticle = location -> center.getWorld().spawnParticle(particle, location, 1, 0, 0, 0, 0);

        for (int i = 0; i < particleCount; i++) {
            double radians = i * 2.0 * Math.PI / particleCount;
            double xOff = Math.sin(radians) * radius;
            double zOff = Math.cos(radians) * radius;

            spawnParticle.accept(center.clone().add(xOff, 0, zOff));
        }
    }
}
