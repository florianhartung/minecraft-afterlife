package skill.skills;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.type.Jukebox;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import performancereport.PerfReport;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Configurable("vibing-cat")
public class VibingCatMinecraftSkill extends MinecraftSkill {
    private static final Map<Material, Integer> DISCS = new HashMap<>();

    static {
        DISCS.put(Material.MUSIC_DISC_OTHERSIDE, ticks(3, 15));
        DISCS.put(Material.MUSIC_DISC_CAT, ticks(3, 5));
        DISCS.put(Material.MUSIC_DISC_BLOCKS, ticks(5, 45));
        DISCS.put(Material.MUSIC_DISC_CHIRP, ticks(3, 5));
        DISCS.put(Material.MUSIC_DISC_FAR, ticks(2, 54));
        DISCS.put(Material.MUSIC_DISC_MALL, ticks(3, 17));
        DISCS.put(Material.MUSIC_DISC_MELLOHI, ticks(1, 36));
        DISCS.put(Material.MUSIC_DISC_PIGSTEP, ticks(2, 28));
        DISCS.put(Material.MUSIC_DISC_STAL, ticks(2, 30));
        DISCS.put(Material.MUSIC_DISC_STRAD, ticks(3, 8));
        DISCS.put(Material.MUSIC_DISC_WAIT, ticks(3, 51));
        DISCS.put(Material.MUSIC_DISC_WARD, ticks(4, 11));
    }

    @ConfigValue("regeneration-amplifier")
    private static int REGENERATION_AMPLIFIER;

    @InjectPlugin(postInject = "startTickTimer")
    private static Plugin plugin;

    private final Map<Location, Integer> activeJukeboxesTaskIds = new HashMap<>();

    @SuppressWarnings("unused")
    private void startTickTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0L, 10L);
    }

    private void tick() {
        PerfReport.startTimer("vibingcat.tick");
        activeJukeboxesTaskIds.keySet().stream().filter(l -> l.getBlock().getType() != Material.JUKEBOX).toList().forEach(activeJukeboxesTaskIds::remove);

        Bukkit.getOnlinePlayers().stream().filter(this::isActiveFor).forEach(player -> {
            Location l = player.getLocation();
            boolean isPlayingJukebox = activeJukeboxesTaskIds.keySet().stream().min(Comparator.comparingDouble(l::distanceSquared)).map(location -> l.distance(location) < 55).orElse(false);

            if (isPlayingJukebox) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 80, REGENERATION_AMPLIFIER, true, true, true));
                if (Math.random() < 0.08 * 3) {
                    player.getWorld().spawnParticle(Particle.NOTE, player.getLocation().add(0, 1.7, 0), 3, 0.4, 0.3, 0.4, 2);
                }
            }
        });
        PerfReport.endTimer("vibingcat.tick");
    }

    @EventHandler
    public void onDisc(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.JUKEBOX || e.useInteractedBlock() == Event.Result.DENY) {
            return;
        }

        boolean hasRecord = ((Jukebox) e.getClickedBlock().getBlockData()).hasRecord();
        if (hasRecord) {
            Optional.ofNullable(activeJukeboxesTaskIds.remove(e.getClickedBlock().getLocation())).ifPresent(taskId -> Bukkit.getScheduler().cancelTask(taskId));
            return;
        }

        if (e.getItem() == null || !DISCS.containsKey(e.getItem().getType())) {
            return;
        }

        int discLength = DISCS.get(e.getItem().getType());
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            activeJukeboxesTaskIds.remove(e.getClickedBlock().getLocation());
        }, discLength);

        activeJukeboxesTaskIds.put(e.getClickedBlock().getLocation(), taskId);
    }

    private static int ticks(int minutes, int seconds) {
        return minutes * 60 * 20 + seconds * 20;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    static class ActiveJukebox {
        private int endTaskId;
        private Location location;
    }
}
