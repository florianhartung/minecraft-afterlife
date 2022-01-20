package skill;

import data.Skill;
import data.SkillEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import main.RestService;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import skill.generic.MinecraftSkill;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkillManager {
    private static final int UPDATE_INTERVAL = 5 * 20; // in ticks
    private static final Map<String, List<Skill>> acquiredSkills = new HashMap<>();

    private static Plugin plugin;
    private static int updaterTaskId = -1;

    public static void startUpdater() {
        if (updaterTaskId != -1) {
            throw new IllegalStateException("Skill updater is already running");
        }

        updaterTaskId = plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(plugin, () -> {
                    long timestampBefore = System.currentTimeMillis();
                    for (int i = 0; i < 20; i++) {
                        SkillManager.reloadSkills();
                    }
                    long timestampAfter = System.currentTimeMillis();
                    System.out.println("Reloading skills took " + (timestampAfter - timestampBefore) + " milliseconds");

                }, 0, UPDATE_INTERVAL).getTaskId();
    }

    public static void stopUpdater() {
        plugin.getServer().getScheduler().cancelTask(updaterTaskId);
        updaterTaskId = -1;
    }

    public static void init(Plugin plugin) {
        SkillManager.plugin = plugin;
        SkillHolder.init(plugin);

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        SkillHolder.getAllMinecraftSkills()
                .forEach(minecraftSkill -> pluginManager.registerEvents(minecraftSkill, plugin));
    }

    public static void reloadSkills() {
        Map<Player, List<SkillChange>> skillChanges = getSkillChangesForOnlinePlayers();
        skillChanges.forEach((player, skillChanges1) -> skillChanges1.forEach(System.out::println));


        skillChanges.forEach((player, playerSkillChanges) -> {
            String uuid = player.getUniqueId().toString();
            acquiredSkills.computeIfAbsent(uuid, k -> new LinkedList<>());
            playerSkillChanges.forEach(skillChange -> {
                Skill skill = skillChange.getSkill();
                MinecraftSkill minecraftSkill = SkillHolder.getMinecraftSkill(skill);
                if (minecraftSkill == null) {
                    return;
                }

                switch (skillChange.getOperation()) {
                    case ADDED -> {
                        minecraftSkill.apply(player);
                        acquiredSkills.get(uuid).add(skill);
                    }
                    case REMOVED -> {
                        minecraftSkill.remove(player);
                        acquiredSkills.get(uuid).remove(skill);
                    }
                }
            });
        });
    }


    private static Map<Player, List<SkillChange>> getSkillChangesForOnlinePlayers() {
        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

        Map<Player, List<Skill>> updatedSkills = players.stream()
                .map(player -> Pair.of(player, getSkillsForPlayer(player.getUniqueId())))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return updatedSkills.entrySet()
                .stream()
                .map(entry -> {
                    String uuid = entry.getKey().getUniqueId().toString();
                    List<Skill> skills = entry.getValue();

                    List<Skill> acquired = Optional.ofNullable(acquiredSkills.get(uuid))
                            .orElseGet(Collections::emptyList);
                    List<Skill> allSkills = new ArrayList<>(acquired);
                    allSkills.addAll(skills);

                    List<SkillChange> skillChangesForPlayer = new LinkedList<>();

                    allSkills.stream()
                            .distinct()
                            .forEach(skill -> {
                                boolean isInAcquired = acquired.contains(skill);
                                boolean isInUpdated = skills.contains(skill);

                                if (isInAcquired && !isInUpdated) {
                                    skillChangesForPlayer.add(new SkillChange(SkillChange.SkillOperation.REMOVED, skill));
                                } else if (!isInAcquired && isInUpdated) {
                                    skillChangesForPlayer.add(new SkillChange(SkillChange.SkillOperation.ADDED, skill));
                                }
                            });
                    return Pair.of(entry.getKey(), skillChangesForPlayer);
                })
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }


    private static List<Skill> getSkillsForPlayer(UUID uuid) {
        ResponseEntity<SkillEntity[]> response = RestService.getSkillsOfPlayer(uuid);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return Stream.of(response.getBody())
                    .map(SkillEntity::getSkill)
                    .toList();
        }
        return Collections.emptyList();
    }

    @Data
    @AllArgsConstructor
    static class SkillChange {
        private SkillOperation operation;
        private Skill skill;

        enum SkillOperation {
            ADDED,
            REMOVED
        }
    }
}
