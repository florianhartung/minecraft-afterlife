package skill.generic;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import skill.PluginConsumer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class ChargingMinecraftSkill extends PlayerMinecraftSkill implements PluginConsumer {
    private final Map<UUID, Integer> taskIds;
    protected Plugin plugin;
    private int chargeTime;

    protected ChargingMinecraftSkill() {
        taskIds = new HashMap<>();
    }

    @Override
    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    protected void beginCharging(Player player) {
        UUID uuid = player.getUniqueId();
        Integer taskId = taskIds.get(uuid);
        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> chargingFinished(player), chargeTime);
        taskIds.put(uuid, taskId);
    }

    protected void stopCharging(Player player) {
        taskIds.remove(player.getUniqueId());
    }

    private void chargingFinished(Player player) {
        boolean isPlayerStillOnServer = Bukkit.getOnlinePlayers()
                .stream()
                .map(Player::getUniqueId)
                .anyMatch(p -> p.equals(player.getUniqueId()));
        stopCharging(player);
        if (isPlayerStillOnServer) {
            onChargingFinished(player);
        }
    }

    protected void setChargeTime(int chargeTime) {
        this.chargeTime = chargeTime;
    }

    protected abstract void onChargingFinished(Player player);
}
