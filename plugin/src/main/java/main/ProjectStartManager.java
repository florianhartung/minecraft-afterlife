package main;

import config.Config;
import config.ConfigType;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProjectStartManager implements Listener, CommandExecutor {

    private final Plugin plugin;

    private final FileConfiguration config;
    private boolean hasStarted;
    private final List<Location> randomSpawnPoints;
    private final Location lobbySpawn;
    private final LimitedPlaytime limitedPlaytime;

    public ProjectStartManager(Plugin plugin, LimitedPlaytime limitedPlaytime) {
        this.plugin = plugin;
        this.limitedPlaytime = limitedPlaytime;
        config = Config.get(ConfigType.PROJECT_START);
        hasStarted = config.getBoolean("has-started");
        randomSpawnPoints = Util.unsafeListCast(config.getList("random-spawns"));
        lobbySpawn = config.getLocation("lobby-spawn");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (hasStarted) {
            e.getPlayer().setGameMode(GameMode.SURVIVAL);


            NamespacedKey rootAdvancementKey = NamespacedKey.fromString("afterlife:afterlife_root");
            Advancement rootAdvancement = Bukkit.getAdvancement(rootAdvancementKey);
            assert rootAdvancement != null;

            AdvancementProgress progress = e.getPlayer().getAdvancementProgress(rootAdvancement);
            if (!progress.isDone()) {
                progress.getRemainingCriteria().forEach(progress::awardCriteria);
                return;
            }
            return;
        }

        e.getPlayer().setGameMode(GameMode.ADVENTURE);
        e.getPlayer().teleport(lobbySpawn);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!hasStarted && e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if (!hasStarted) {
            e.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!command.getName().equals("startafterlife")) {
            return false;
        }

        if (!sender.isOp()) {
            return false;
        }

        if (hasStarted) {
            sender.sendMessage("Das Projekt Afterlife hat bereits gestartet.");
            return true;
        }

        hasStarted = true;
        config.set("has-started", true);
        Config.save(ConfigType.PROJECT_START, config);
        giveBlindnessToAllPlayers();
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.getInventory().clear();
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
            player.setGameMode(GameMode.SURVIVAL);
        });
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this::teleportPlayersToRandomSpawns, 30);
        giveRootAdvancement();
        limitedPlaytime.resetAllData();
        World world = Bukkit.getWorld("world");
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        world.setTime(23500);

        return true;
    }

    private void giveRootAdvancement() {
        NamespacedKey rootAdvancementKey = NamespacedKey.fromString("afterlife:afterlife_root");
        Advancement rootAdvancement = Bukkit.getAdvancement(rootAdvancementKey);
        assert rootAdvancement != null;

        Bukkit.getOnlinePlayers()
                .forEach(player -> {
                    AdvancementProgress progress = player.getAdvancementProgress(rootAdvancement);
                    progress.getRemainingCriteria().forEach(progress::awardCriteria);
                });
    }

    private void teleportPlayersToRandomSpawns() {
        List<Location> availableRandomSpawns = new ArrayList<>(randomSpawnPoints);

        Random random = new Random();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (availableRandomSpawns.size() <= 0) {
                availableRandomSpawns.addAll(randomSpawnPoints);
            }
            Location randomLocation = availableRandomSpawns.remove(random.nextInt(availableRandomSpawns.size()));
            player.teleport(randomLocation);
        }
    }

    private void giveBlindnessToAllPlayers() {
        Bukkit.getOnlinePlayers()
                .forEach(player -> player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 70, 0, false, false, false)));
    }
}
