package skill;

import data.Skill;
import data.SkillEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import main.RestService;
import org.bukkit.entity.Player;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import skill.generic.MinecraftSkill;

import java.util.*;
import java.util.stream.Stream;

public class SkillUpdater {

    private static final Map<String, List<Skill>> acquiredSkills = new HashMap<>();


    public static void reloadSkillsForPlayer(Player player) {
        acquiredSkills.computeIfAbsent(player.getUniqueId().toString(), k -> new LinkedList<>());

        List<SkillChange> skillChanges = getSkillChanges(player);
        applySkillChanges(player, skillChanges);
    }

    private static void applySkillChanges(Player player, List<SkillChange> skillChanges) {
        skillChanges.forEach(skillChange -> {
            Skill skill = skillChange.getSkill();
            MinecraftSkill minecraftSkill = SkillHolder.getMinecraftSkill(skill);
            if (minecraftSkill == null) {
                return;
            }

            switch (skillChange.getOperation()) {
                case ADDED -> {
                    minecraftSkill.apply(player);
                    acquiredSkills.get(player.getUniqueId().toString()).add(skill);
                }
                case REMOVED -> {
                    minecraftSkill.remove(player);
                    acquiredSkills.get(player.getUniqueId().toString()).remove(skill);
                }
            }
        });
    }

    private static List<SkillChange> getSkillChanges(Player player) {
        String uuid = player.getUniqueId().toString();
        List<Skill> updated = getSkills(player.getUniqueId());

        List<Skill> acquired = Optional.ofNullable(acquiredSkills.get(uuid))
                .orElseGet(Collections::emptyList);
        List<Skill> allSkills = new ArrayList<>(acquired);
        allSkills.addAll(updated);

        List<SkillChange> skillChangesForPlayer = new LinkedList<>();

        allSkills.stream()
                .distinct()
                .forEach(skill -> {
                    boolean isInAcquired = acquired.contains(skill);
                    boolean isInUpdated = updated.contains(skill);

                    if (isInAcquired && !isInUpdated) {
                        skillChangesForPlayer.add(new SkillChange(SkillChange.SkillOperation.REMOVED, skill));
                    } else if (!isInAcquired && isInUpdated) {
                        skillChangesForPlayer.add(new SkillChange(SkillChange.SkillOperation.ADDED, skill));
                    }
                });
        return skillChangesForPlayer;
    }


    private static List<Skill> getSkills(UUID uuid) {
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
