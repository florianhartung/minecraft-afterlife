package skill.skills;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

import java.util.*;

@Configurable("hitman")
public class HitmanSkill extends MinecraftSkill {
    @ConfigValue("max-range")
    private static int MAX_RANGE; // in x,y,z directions (range is calculated as a rectangle around player)
    @ConfigValue("max-angle")
    private static int MAX_ANGLE; // in relative units
    @ConfigValue("stalk-rate")
    private static double STALK_RATE; // per tick
    @ConfigValue("stalk-decrease-rate")
    private static double STALK_DECREASE_RATE; // per tick
    @ConfigValue("stalk-update-delay")
    private static int STALK_UPDATE_DELAY; // ticks between each update of stalk progresses
    @ConfigValue("stalk-in-hunt-decrease-rate")
    private static double STALK_IN_HUNT_DECREASE_RATE;
    @ConfigValue("hunt-damage-multiplier")
    private static double HUNT_DAMAGE_MULTIPLIER;
    @ConfigValue("hunt-speed")
    private static double HUNT_SPEED;
    @ConfigValue("hunt-slow")
    private static double HUNT_SLOW;
    @ConfigValue("sneak-duration")
    private static int SNEAK_DURATION; // time before player has to sneak for the stalking to begin in ticks
    @InjectPlugin(postInject = "scheduleStalkTimer")
    private Plugin plugin;

    private final Map<UUID, BukkitTask> sneakTimers = new HashMap<>();
    private final List<UUID> sneakingPlayers = new ArrayList<>();
    private BukkitTask stalkProgressUpdater;
    private final HashMap<UUID, PlayerStalkInformation> stalkProgresses = new HashMap<>();

    @SuppressWarnings("unused")
    public void scheduleStalkTimer() {
        stalkProgressUpdater = Bukkit.getScheduler().runTaskTimer(plugin, this::updateStalkProgress, 0, STALK_UPDATE_DELAY);
    }

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        Bukkit.getScheduler().cancelTask(stalkProgressUpdater.getTaskId());
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent e) {
        if (!isActiveFor(e.getPlayer())) {
            return;
        }

        UUID playerUUID = e.getPlayer().getUniqueId();

        if (e.isSneaking()) {
            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> sneakingPlayers.add(playerUUID), SNEAK_DURATION);
            sneakTimers.put(playerUUID, task);
        } else {
            Optional.ofNullable(sneakTimers.get(playerUUID))
                    .map(BukkitTask::getTaskId)
                    .ifPresent(taskId -> {
                        Bukkit.getScheduler().cancelTask(taskId);
                        sneakTimers.remove(playerUUID);
                        sneakingPlayers.remove(playerUUID);
                    });

        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        sneakingPlayers.remove(e.getPlayer().getUniqueId());
        sneakTimers.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player damager) {
            if (e.getEntity() instanceof Player target) {
                PlayerStalkInformation stalkInfo = stalkProgresses.get(damager.getUniqueId());
                if (stalkInfo != null && target.getUniqueId().equals(e.getEntity().getUniqueId()) && stalkInfo.isInHunt()) {
                    e.setDamage(e.getDamage() * HUNT_DAMAGE_MULTIPLIER);
                }
            }
        }
    }


    public void updateStalkProgress() {
        activePlayers.forEach(playerUUID -> {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                updateStalkProgressForPlayer(player);
            }
        });

        stalkProgresses.forEach((stalker, stalkInfo) -> {
            if (stalkInfo.isInHunt()) {
                stalkInfo.setProgress(stalkInfo.getProgress() - STALK_IN_HUNT_DECREASE_RATE);
                if (stalkInfo.getProgress() <= 0) {
                    removeBossBar(stalkInfo);
                    stalkProgresses.remove(stalker);
                } else {
                    updateBossBar(stalkInfo);
                }
            }
        });
    }

    public void updateStalkProgressForPlayer(Player player) {
        PlayerStalkInformation stalkInfo = stalkProgresses.get(player.getUniqueId());
        if ((stalkInfo != null && stalkInfo.isInHunt()) || !sneakingPlayers.contains(player.getUniqueId())) {
            return;
        }


        Collection<Entity> stalkableEntities = player.getWorld().getNearbyEntities(player.getLocation(), MAX_RANGE, MAX_RANGE, MAX_RANGE);
        stalkableEntities.remove(player);


        for (Entity target : stalkableEntities) {
            if (target instanceof Player targetPlayer) {
                double angleInDegrees = stalkAngle(player, targetPlayer);

                angleInDegrees *= player.getLocation().distance(targetPlayer.getLocation()); // multiply with distance for better results with a large distance

                if (angleInDegrees < MAX_ANGLE && player.hasLineOfSight(targetPlayer)) {
                    increaseStalkProgress(player, targetPlayer);
                } else {
                    decreaseStalkProgress(player, targetPlayer);
                }
            }
        }
    }


    private void increaseStalkProgress(Player stalker, Player target) {
        PlayerStalkInformation stalkInfo = stalkProgresses.get(stalker.getUniqueId());

        if (stalkInfo == null) {
            stalkInfo = new PlayerStalkInformation(stalker, target, 0, false, null, null, null);
            createBossBar(stalkInfo);
            stalkProgresses.put(stalker.getUniqueId(), stalkInfo);
        } else if (!stalkInfo.getTarget().getUniqueId().equals(target.getUniqueId())) {
            return;
        } else if (stalkInfo.isInHunt()) {
            return;
        }


        stalkInfo.setProgress(stalkInfo.getProgress() + STALK_RATE);

        if (stalkInfo.getProgress() >= 100) {
            stalkInfo.setInHunt(true);
            stalkInfo.setProgress(100);
            fullStalkProgress(stalkInfo);
        }

        updateBossBar(stalkInfo);
    }

    private void decreaseStalkProgress(Entity stalker, Entity target) {
        PlayerStalkInformation stalkInfo = stalkProgresses.get(stalker.getUniqueId());

        if (stalkInfo == null) {
            return;
        } else if (!stalkInfo.getTarget().getUniqueId().equals(target.getUniqueId())) {
            return;
        } else if (stalkInfo.isInHunt()) {
            return;
        }

        stalkInfo.setProgress(stalkInfo.getProgress() - STALK_DECREASE_RATE);

        if (stalkInfo.getProgress() <= 0) {
            removeBossBar(stalkInfo);
            stalkProgresses.remove(stalker.getUniqueId());
            return;
        }

        updateBossBar(stalkInfo);
    }

    private void fullStalkProgress(PlayerStalkInformation stalkInfo) {
        Player stalker = stalkInfo.getStalker();
        Player target = stalkInfo.getTarget();

        BossBar bossBar = stalkInfo.getBossBar();
        bossBar.addFlag(BarFlag.DARKEN_SKY);
        bossBar.setColor(BarColor.RED);
        bossBar.setTitle(String.format("%sJagt auf %s", ChatColor.DARK_RED, stalkInfo.getTarget().getName()));
        bossBar.setStyle(BarStyle.SOLID);

        BossBar targetBossBar = Bukkit.createBossBar(String.format("%sDu wirst gejagt von %s", ChatColor.DARK_RED, stalkInfo.getStalker().getDisplayName()), BarColor.RED, BarStyle.SOLID, BarFlag.CREATE_FOG, BarFlag.DARKEN_SKY, BarFlag.PLAY_BOSS_MUSIC);
        targetBossBar.setProgress(1.0d);
        stalkInfo.setTargetBossBar(targetBossBar);
        targetBossBar.addPlayer(target);
        target.stopAllSounds();
        target.playSound(target.getLocation(), Sound.MUSIC_DRAGON, SoundCategory.PLAYERS, 1.0f, 1.0f);
        target.playSound(stalker.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.3f, 1.1f);

        stalker.playSound(stalker.getLocation(), Sound.ENTITY_WITHER_SPAWN, SoundCategory.PLAYERS, 0.3f, 1.1f);
        stalker.sendTitle(ChatColor.DARK_RED + target.getName(), ChatColor.WHITE + "wird nun gejagt", 3, 10, 5);


        BukkitTask huntParticlePlayer = Bukkit.getScheduler().runTaskTimer(plugin, () -> playHuntParticles(stalkInfo), 0, 1);
        stalkInfo.setHuntParticlePlayer(huntParticlePlayer);

        addSpeedEffect(stalkInfo);
    }

    private static void addSpeedEffect(PlayerStalkInformation stalkInfo) {
        AttributeInstance attributeInstance = stalkInfo.getStalker().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        assert attributeInstance != null;
        attributeInstance.addModifier(new AttributeModifier(UUID.fromString("d871eaf0-3c13-49c7-b56d-307fb7ef7959"),
                "Hitman hunt", HUNT_SPEED, AttributeModifier.Operation.ADD_NUMBER, null));


        AttributeInstance attributeInstance2 = stalkInfo.getTarget().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        assert attributeInstance2 != null;
        attributeInstance2.addModifier(new AttributeModifier(UUID.fromString("2252381b-4b16-40f4-a28d-9132ce624b15"),
                "Hitman hunt slow", -HUNT_SLOW, AttributeModifier.Operation.ADD_NUMBER, null));
    }

    private static void removeSpeedEffect(PlayerStalkInformation stalkInfo) {
        AttributeInstance attributeInstance = stalkInfo.getStalker().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        assert attributeInstance != null;
        attributeInstance.getModifiers()
                .stream()
                .filter(mod -> mod.getUniqueId().equals(UUID.fromString("d871eaf0-3c13-49c7-b56d-307fb7ef7959")))
                .forEach(attributeInstance::removeModifier);


        AttributeInstance attributeInstance2 = stalkInfo.getTarget().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        assert attributeInstance2 != null;
        attributeInstance2.getModifiers()
                .stream()
                .filter(mod -> mod.getUniqueId().equals(UUID.fromString("2252381b-4b16-40f4-a28d-9132ce624b15")))
                .forEach(attributeInstance::removeModifier);
    }

    private void playHuntParticles(PlayerStalkInformation stalkInfo) {
        stalkInfo.getTarget().spawnParticle(Particle.ASH, stalkInfo.getTarget().getLocation(), 20, 10, 5, 10);
        stalkInfo.getTarget().spawnParticle(Particle.SOUL, stalkInfo.getTarget().getLocation(), 2, 20, 5, 20);
    }

    private static void createBossBar(PlayerStalkInformation stalkInfo) {
        BossBar bossBar = Bukkit.createBossBar(String.format("%s%s wird beobachtet", ChatColor.WHITE, stalkInfo.getTarget().getName()), BarColor.WHITE, BarStyle.SEGMENTED_20);
        bossBar.setVisible(true);
        bossBar.addPlayer(stalkInfo.getStalker());
        stalkInfo.setBossBar(bossBar);
    }


    private static void updateBossBar(PlayerStalkInformation stalkInfo) {
        BossBar bossBar = stalkInfo.getBossBar();

        if (stalkInfo.isInHunt()) {
            bossBar.setProgress(stalkInfo.getProgress() / 100.0d);
        } else {
            int prevSegments = (int) Math.round(bossBar.getProgress() / 0.05d);
            int newSegments = (int) Math.round(stalkInfo.getProgress() / 5);
            if (prevSegments != newSegments) {
                bossBar.setProgress(newSegments * 0.05d);
                if (prevSegments < newSegments) {
                    stalkInfo.getStalker().playSound(stalkInfo.getStalker().getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, SoundCategory.PLAYERS, 0.3f, (float) stalkInfo.getProgress() / 100.0f + 1.0f);
                }
            }
        }
    }

    private static void removeBossBar(PlayerStalkInformation stalkInfo) {
        stalkInfo.getBossBar().removeAll();
        stalkInfo.setBossBar(null);
        if (stalkInfo.getTargetBossBar() != null) {
            stalkInfo.getTargetBossBar().removeAll();
            stalkInfo.setTargetBossBar(null);
        }
        if (stalkInfo.getHuntParticlePlayer() != null) {
            Bukkit.getScheduler().cancelTask(stalkInfo.getHuntParticlePlayer().getTaskId());
            stalkInfo.setHuntParticlePlayer(null);
        }
        stalkInfo.getTarget().stopSound(Sound.MUSIC_DRAGON, SoundCategory.PLAYERS);
        removeSpeedEffect(stalkInfo);
    }

    private static double stalkAngle(Entity stalker, Entity target) {
        Vector playerTarget = target.getLocation().toVector().subtract(stalker.getLocation().toVector());
        double angle = stalker.getLocation().getDirection().angle(playerTarget);


        return angle / Math.PI * 180;
    }

    @Data
    @AllArgsConstructor
    @ToString
    static class PlayerStalkInformation {
        private Player stalker;
        private Player target;
        private double progress;
        private boolean inHunt;
        private BossBar bossBar;
        private BossBar targetBossBar;
        private BukkitTask huntParticlePlayer;
    }
}