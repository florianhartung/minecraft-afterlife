package skill.generic;

import hud.HudManager;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;

public class MinecraftSkillTimer {
    private final Map<UUID, List<Integer>> taskIds;

    @Setter
    private int durationInTicks;
    @Setter
    private HudManager.HudEntry hudEntry = null;
    private final Plugin plugin;

    @Setter
    private Consumer<Player> onTimerFinished;

    public MinecraftSkillTimer(Plugin plugin) {
        this.plugin = plugin;
        taskIds = new HashMap<>();
    }

    public void start(Player player) {
        start(player, this.durationInTicks);
    }

    public void start(Player player, int durationInTicks) {
        cancel(player);
        taskIds.computeIfAbsent(player.getUniqueId(), uuid -> new ArrayList<>());

        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            taskIds.remove(player.getUniqueId());
            if (onTimerFinished != null) {
                onTimerFinished.accept(player);
            }
            if (hudEntry != null) {
                HudManager.set(player, hudEntry, 7);
            }
        }, durationInTicks);
        taskIds.get(player.getUniqueId()).add(taskId);

        if (hudEntry != null) {
            HudManager.set(player, hudEntry, 0);
            int lastDelay = -1;
            final float delay = durationInTicks / 7.0F;
            for (int i = 1; i < 7; i++) {
                int thisDelay = (int) (delay * i);
                if (thisDelay == lastDelay) {
                    continue;
                }

                int finalI = i;
                int hudEntryUpdateTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    HudManager.set(player, hudEntry, finalI);
                }, thisDelay);
                taskIds.get(player.getUniqueId()).add(hudEntryUpdateTaskId);
            }
        }
    }

    public void cancel(Player player) {
        if (hudEntry != null) {
            HudManager.set(player, hudEntry, 0);
        }
        Optional.ofNullable(taskIds.remove(player.getUniqueId()))
                .ifPresent(taskIdList -> taskIdList.forEach(Bukkit.getScheduler()::cancelTask));
    }

    public List<UUID> cancelAll() {
        List<UUID> removedPlayers = taskIds.keySet().stream().toList();
        taskIds.forEach((uuid, idList) -> idList.forEach(Bukkit.getScheduler()::cancelTask));
        taskIds.clear();

        return removedPlayers;
    }

    public Set<UUID> players() {
        return taskIds.keySet();
    }

    public boolean isActive(Player player) {
        return taskIds.containsKey(player.getUniqueId());
    }
}
