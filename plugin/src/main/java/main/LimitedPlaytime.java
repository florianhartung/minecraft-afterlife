package main;

import config.Config;
import config.ConfigType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LimitedPlaytime implements Listener, CommandExecutor {

    private static final int PLAY_TIME_CHECK_INTERVAL = 20;
    private static final DateTimeFormatter CONFIG_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<Long> PLAYTIME_NOTIFICATION_THRESHOLDS = List.of(10_000L, 60_000L, 300_000L, 600_000L);

    private final FileConfiguration playTimesConfig;
    private final long millisecondsPerDay;
    private final int bankedDays;
    private final Plugin plugin;
    private final LocalTime serverOpenTime;
    private final LocalTime serverCloseTime;

    private final HashMap<UUID, PlayTimeData> onlinePlayers = new HashMap<>();

    private int byPassLoginCheckTaskId = 0;
    private LocalTime lastServerTimeCheck = LocalTime.MIN;
    private final ZoneOffset zoneOffset;

    public LimitedPlaytime(Plugin plugin) {
        this.plugin = plugin;

        playTimesConfig = Config.get(ConfigType.PLAY_TIMES);
        FileConfiguration config = Config.get(ConfigType.DEFAULT);
        millisecondsPerDay = config.getLong("limited-playtime.milliseconds-per-day");
        bankedDays = config.getInt("limited-playtime.banked-days");
        serverOpenTime = LocalTime.parse(config.getString("limited-playtime.global.open-time"), CONFIG_TIME_FORMATTER);
        serverCloseTime = LocalTime.parse(config.getString("limited-playtime.global.close-time"), CONFIG_TIME_FORMATTER);
        String zoneIdString = config.getString("limited-playtime.global.time-zone-id");

        LocalDateTime now = LocalDateTime.now();
        ZoneId zone = ZoneId.of(zoneIdString);
        zoneOffset = zone.getRules().getOffset(now);

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::checkPlayTimes, 0, PLAY_TIME_CHECK_INTERVAL);
    }

    public void resetAllData() {
        onlinePlayers.clear();

        playTimesConfig.set("play-times", null);

        Bukkit.getOnlinePlayers()
                .forEach(player -> onlinePlayers.put(player.getUniqueId(), readFromConfig(player.getUniqueId())));
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        Optional.ofNullable(onlinePlayers.remove(e.getPlayer().getUniqueId())).ifPresent(this::savePlayedTimeToConfig);
        Config.save(ConfigType.PLAY_TIMES, playTimesConfig);
    }

    @EventHandler
    private void onDisable(PluginDisableEvent e) {
        onlinePlayers.forEach(((uuid, data) -> savePlayedTimeToConfig(data)));
        onlinePlayers.clear();

        Config.save(ConfigType.PLAY_TIMES, playTimesConfig);
    }

    private void checkPlayTimes() {
        LocalTime now = currentTime();
        // Skip midnight
        if (now.isBefore(lastServerTimeCheck)) {
            lastServerTimeCheck = now;
        }

        if (lastServerTimeCheck.isBefore(serverCloseTime) && now.isAfter(serverCloseTime)) {
            Bukkit.getOnlinePlayers().forEach(
                    player -> player.kickPlayer("Der Server ist nun geschlossen. Er öffnet morgen um " + serverOpenTime.format(CONFIG_TIME_FORMATTER) + " Uhr erneut.")
            );
            lastServerTimeCheck = now;
            return;
        }


        Instant nowInstant = Instant.now();
        onlinePlayers.forEach(((uuid, data) -> {
            long timePlayed = data.getLastCalculation().until(nowInstant, ChronoUnit.MILLIS);
            data.setLastCalculation(Instant.now());

            data.currentRemainingPlayTimeMillis -= timePlayed;
            if (data.currentRemainingPlayTimeMillis <= 0) {
                data.setCurrentRemainingPlayTimeMillis(0);
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    Optional.ofNullable(Bukkit.getPlayer(data.getPlayer())).ifPresent(player -> player.kickPlayer("Deine tägliche Spielzeit ist abgelaufen!"));
                });
            }
            PLAYTIME_NOTIFICATION_THRESHOLDS.stream()
                    .filter(playtime -> playtime > data.getCurrentRemainingPlayTimeMillis() && playtime < data.getCurrentRemainingPlayTimeMillis() + timePlayed)
                    .findFirst()
                    .ifPresent(playtime -> {
                        ChatHelper.sendMessage(data.getPlayer(), "Du hast noch " + convertDurationToString(playtime) + " deiner Spielzeit übrig.");
                    });

            List<Long> playTimes = data.getRemainingMillisPlayTimes();
            long timeToDistribute = timePlayed;
            while (timeToDistribute > 0 && playTimes.size() > 0) {
                long l = playTimes.remove(0) - timeToDistribute;
                if (l <= 0) {
                    timeToDistribute = -l;
                } else {
                    playTimes.add(0, l);
                    timeToDistribute -= l;
                }
            }
        }));
    }

    @EventHandler
    private void onLogin(PlayerLoginEvent e) {
        if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }

        if (e.getPlayer().isOp() && byPassLoginCheckTaskId > 0) {
            e.setResult(PlayerLoginEvent.Result.ALLOWED);
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> ChatHelper.sendMessage(e.getPlayer(), "Du hast aufgrund deiner Rechte die Spielzeit-Sperre umgangen."));
            return;
        }
        LocalTime now = LocalTime.now(ZoneId.of("Europe/Berlin"));

        if (now.isBefore(serverOpenTime)) {
            e.setKickMessage("Der Server öffnet erst um " + serverOpenTime.format(CONFIG_TIME_FORMATTER) + " Uhr.");
            e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            enableBypassIfOperator(e);
            return;
        }
        if (now.isAfter(serverCloseTime)) {
            e.setKickMessage("Der Server ist für heute geschlossen und öffnet morgen um " + serverOpenTime.format(CONFIG_TIME_FORMATTER) + " Uhr erneut.");
            e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            enableBypassIfOperator(e);
            return;
        }

        PlayTimeData data = readFromConfig(e.getPlayer().getUniqueId());
        if (data.getCurrentRemainingPlayTimeMillis() <= 0) {
            e.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            e.setKickMessage("Du hast deine tägliche Spielzeit aufgebraucht.");
            enableBypassIfOperator(e);
            return;
        }

        onlinePlayers.put(e.getPlayer().getUniqueId(), data);
    }

    public void enableBypassIfOperator(PlayerLoginEvent e) {
        if (e.getPlayer().isOp()) {
            if (byPassLoginCheckTaskId > 0) {
                Bukkit.getScheduler().cancelTask(byPassLoginCheckTaskId);
            }
            byPassLoginCheckTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> byPassLoginCheckTaskId = 0, 30 * 20);
            e.setKickMessage(e.getKickMessage() + ChatColor.AQUA + "\n Du hast als Operator nun ein 30 Sekunden Zeitfenster in dem du trotzdem den Server betreten kannst.");
        }
    }

    private PlayTimeData readFromConfig(UUID uuid) {
        PlayTimeData data = new PlayTimeData();
        data.setPlayer(uuid);

        ConfigurationSection playerSection = playTimesConfig.getConfigurationSection("play-times." + uuid.toString());
        if (playerSection == null) {
            data.setLastPlayed(currentDate());
            data.setLastCalculation(Instant.now());
            data.setRemainingMillisPlayTimes(new ArrayList<>(List.of(millisecondsPerDay)));
            data.setCurrentRemainingPlayTimeMillis(millisecondsPerDay);
            return data;
        }

        List<Long> remainingPlayTimes = playerSection.getLongList("remaining-play-times");
        List<Long> newRemainingPlayTimes = new ArrayList<>();

        long lastPlayedEpochSeconds = playerSection.getLong("last-played");
        LocalDate lastPlayed = Instant.ofEpochSecond(lastPlayedEpochSeconds).atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDate firstSavedRemainingPlayTime = lastPlayed.minusDays(remainingPlayTimes.size() - 1);

        LocalDate today = currentDate();
        LocalDate tomorrow = today.plusDays(1);

        long remainingMillisForToday = 0;
        for (LocalDate date = firstSavedRemainingPlayTime; date.isBefore(tomorrow); date = date.plusDays(1)) {
            if (remainingPlayTimes.size() > 0) {
                long dayRemainingPlayTime = remainingPlayTimes.remove(0);
                if (date.until(today).getDays() <= bankedDays) {
                    remainingMillisForToday += dayRemainingPlayTime;
                    newRemainingPlayTimes.add(dayRemainingPlayTime);
                }
            } else {
                if (date.until(today).getDays() <= bankedDays) {
                    remainingMillisForToday += millisecondsPerDay;
                    newRemainingPlayTimes.add(millisecondsPerDay);
                }
            }
        }

        data.setLastPlayed(currentDate());
        data.setRemainingMillisPlayTimes(newRemainingPlayTimes);
        data.setCurrentRemainingPlayTimeMillis(remainingMillisForToday);
        data.setLastCalculation(Instant.now());

        return data;
    }

    private void savePlayedTimeToConfig(PlayTimeData data) {
        String playerUuid = data.getPlayer().toString();
        ConfigurationSection playerSection = playTimesConfig.getConfigurationSection("play-times." + playerUuid);
        if (playerSection == null) {
            playerSection = playTimesConfig.createSection("play-times." + playerUuid);
        }

        playerSection.set("last-played", data.getLastPlayed().toEpochSecond(currentTime(), OffsetDateTime.now(ZoneId.systemDefault()).getOffset()));
        playerSection.set("remaining-play-times", data.getRemainingMillisPlayTimes());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("playtime")) {

            if (!sender.isOp()) {
                return false;
            }

            if (args.length != 3) {
                return false;
            }


            String subCommand = args[0];
            if (!subCommand.equalsIgnoreCase("give") && !subCommand.equalsIgnoreCase("take")) {
                return false;
            }

            String playerName = args[1];

            OfflinePlayer target = Bukkit.getPlayer(playerName);

            if (target == null) {
                target = Arrays.stream(Bukkit.getOfflinePlayers()).filter(offlinePlayer -> playerName.equals(offlinePlayer.getName())).findFirst().orElse(null);
            }

            if (target == null) {
                sender.sendMessage("Player " + playerName + " could not be found!");
                return true;
            }

            String secondsString = args[2];
            long secondsAmount;
            try {
                secondsAmount = Long.parseLong(secondsString);
            } catch (NumberFormatException e) {
                sender.sendMessage("Given amount of seconds " + secondsString + " is not a number!");
                return true;
            }

            long amount = (subCommand.equalsIgnoreCase("give") ? 1 : -1) * secondsAmount * 1000;
            PlayTimeData data = null;
            if (target.isOnline()) {
                data = onlinePlayers.get(target.getUniqueId());
            }
            if (data == null) {
                data = readFromConfig(target.getUniqueId());
            }

            data.setCurrentRemainingPlayTimeMillis(Math.max(0, data.getCurrentRemainingPlayTimeMillis() + amount));
            List<Long> remainingPlayTimes = data.getRemainingMillisPlayTimes();
            if (remainingPlayTimes.size() > 0) {
                int lastIndex = remainingPlayTimes.size() - 1;
                remainingPlayTimes.set(lastIndex, Math.max(0, remainingPlayTimes.get(lastIndex) + amount));
            } else {
                remainingPlayTimes.add(amount);
            }

            sender.sendMessage(String.format("Changed play time successfully! %s now has %d seconds (%d minutes) left to play.", target.getName(), data.getCurrentRemainingPlayTimeMillis() / 1000, data.getCurrentRemainingPlayTimeMillis() / 60_000));

            savePlayedTimeToConfig(data);
            Config.save(ConfigType.PLAY_TIMES, playTimesConfig);
            return true;
        } else if (command.getName().equalsIgnoreCase("timeleft")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Du musst ein Spieler sein, um diesen Befehl benutzen zu können.");
                return true;
            }
            if (!onlinePlayers.containsKey(player.getUniqueId())) {
                ChatHelper.sendMessage(player, "Du verwendest aktuell nicht deine Spielzeit!");
                return true;
            }

            PlayTimeData data = onlinePlayers.get(player.getUniqueId());

            ChatHelper.sendMessage(player, "Du hast " + convertDurationToString(data.getCurrentRemainingPlayTimeMillis()) + " Spielzeit verbleibend.");
            return true;
        }
        return false;
    }

    private static String convertDurationToString(long millis) {
        if (millis < 60_000L) {
            return millis / 1_000L + " Sekunden";
        } else {
            return millis / 60_000L + " Minuten";
        }
    }

    private LocalTime currentTime() {
        return LocalTime.now(zoneOffset);
    }


    private LocalDate currentDate() {
        return LocalDate.now(zoneOffset);
    }

    @Setter
    @Getter
    @ToString
    static class PlayTimeData {
        private UUID player;
        private List<Long> remainingMillisPlayTimes;
        private LocalDate lastPlayed;
        private long currentRemainingPlayTimeMillis;
        private Instant lastCalculation;
    }
}
