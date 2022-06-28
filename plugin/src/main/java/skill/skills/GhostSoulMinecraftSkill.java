package skill.skills;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.springframework.data.util.Pair;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.*;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Configurable("ghost-soul")
public class GhostSoulMinecraftSkill extends MinecraftSkill {
    @ConfigValue("cooldown")
    private static int COOLDOWN; // in ticks
    @ConfigValue("activation-threshold")
    private static double ACTIVATION_THRESHOLD; // in half hearts
    @ConfigValue("activation-duration")
    private static int ACTIVATION_DURATION; // in ticks
    @ConfigValue("step-interval")
    private static int STEP_INTERVAL; // in ticks
    @InjectPlugin
    private Plugin plugin;
    @InjectTimer(durationField = "COOLDOWN")
    private MinecraftSkillTimer cooldownTimer;
    @InjectSkill
    private RadarMinecraftSkill radarSkill;

    private final HashMap<String, Pair<Integer, Integer>> activations = new HashMap<>();

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player && isActiveFor(player) && !cooldownTimer.isActive(player)) {
            if (!activations.containsKey(player.getUniqueId().toString())) {
                double finalHealth = player.getHealth() - event.getFinalDamage();
                if (finalHealth > 0 && finalHealth <= ACTIVATION_THRESHOLD) {
                    activateForPlayer(player);
                    cooldownTimer.start(player);
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler
    public void onDamageOtherEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (isInGhostForm(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTakeDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isInGhostForm(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isInGhostForm(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isInGhostForm(event.getPlayer())) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        activations.keySet()
                .stream()
                .map(uuid -> Bukkit.getPlayer(UUID.fromString(uuid)))
                .filter(Objects::nonNull)
                .forEach(p -> hidePlayer(p, player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        activations.keySet()
                .stream()
                .map(uuid -> Bukkit.getPlayer(UUID.fromString(uuid)))
                .filter(Objects::nonNull)
                .forEach(p -> showPlayer(p, player));
    }

    @EventHandler
    private void onDisable(PluginDisableEvent event) {
        activations.forEach((uuid, pair) -> {
            Optional.ofNullable(Bukkit.getPlayer(UUID.fromString(uuid)))
                    .ifPresent(this::deactivateForPlayer);
            Bukkit.getScheduler().cancelTask(pair.getFirst());
            Bukkit.getScheduler().cancelTask(pair.getSecond());
        });
    }

    private void activateForPlayer(Player player) {
        cooldownTimer.start(player);
        int taskId = Bukkit.getScheduler()
                .scheduleSyncDelayedTask(plugin, () -> deactivateForPlayer(player), ACTIVATION_DURATION);
        int taskIdEffects = Bukkit.getScheduler()
                .scheduleSyncRepeatingTask(plugin, () -> {
                    Location location = player.getLocation();
                    World world = player.getWorld();
                    world.playSound(location, Sound.ENTITY_ENDERMITE_STEP, SoundCategory.PLAYERS, 0.2f, 0.7f);
                }, STEP_INTERVAL, STEP_INTERVAL);
        activations.put(player.getUniqueId().toString(), Pair.of(taskId, taskIdEffects));

        Bukkit.getOnlinePlayers()
                .forEach(p -> hidePlayer(player, p));


        Location location = player.getLocation();
        World world = player.getWorld();
        world.spawnParticle(Particle.SQUID_INK, location.add(0, 1, 0), 50, 0.3d, 0.8d, 0.3d, 0);
        world.spawnParticle(Particle.SPELL_WITCH, location.add(0, 3, 0), 20, 0.0d, 2.0d, 0.0d, 0);
        world.playSound(location, Sound.ENTITY_ENDERMAN_AMBIENT, SoundCategory.PLAYERS, 1, 0);

    }

    private void deactivateForPlayer(Player player) {
        Bukkit.getScheduler().cancelTask(activations.get(player.getUniqueId().toString()).getSecond());
        activations.remove(player.getUniqueId().toString());
        Bukkit.getOnlinePlayers()
                .forEach(p -> showPlayer(player, p));

        Location location = player.getLocation();
        World world = player.getWorld();
        world.spawnParticle(Particle.SQUID_INK, location.add(0, 1, 0), 50, 0.3d, 0.8d, 0.3d, 0);
        world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1, 0);
    }

    private boolean isInGhostForm(Player player) {
        return isActiveFor(player) && activations.containsKey(player.getUniqueId().toString());
    }

    private void hidePlayer(Player playerInGhostSoul, Player observer) {
        if (radarSkill.isTrackingEntity(observer, playerInGhostSoul)) {
            return;
        }

        observer.hidePlayer(plugin, playerInGhostSoul);
    }

    private void showPlayer(Player playerInGhostSoul, Player observer) {
        observer.showPlayer(plugin, playerInGhostSoul);
    }
}
