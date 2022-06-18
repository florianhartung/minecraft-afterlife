package skill.generic;

import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;

public class MinecraftSkillTimer {
    private final Map<UUID, Integer> taskIds;

    @Setter
    private int durationInTicks;
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
        int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            taskIds.remove(player.getUniqueId());
            if (onTimerFinished != null) {
                onTimerFinished.accept(player);
            }
        }, durationInTicks);
        taskIds.put(player.getUniqueId(), taskId);
    }

    public void cancel(Player player) {
        Optional.ofNullable(taskIds.remove(player.getUniqueId()))
                .ifPresent(Bukkit.getScheduler()::cancelTask);
    }

    public List<UUID> cancelAll() {
        List<UUID> removedPlayers = taskIds.keySet().stream().toList();
        taskIds.forEach((uuid, id) -> Bukkit.getScheduler().cancelTask(id));
        taskIds.clear();

        return removedPlayers;
    }

    public boolean isActive(Player player) {
        return taskIds.containsKey(player.getUniqueId());
    }
}
