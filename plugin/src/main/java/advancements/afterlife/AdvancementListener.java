package advancements.afterlife;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class AdvancementListener implements Listener {

    protected Plugin plugin;
    protected Advancement advancement;

    @SuppressWarnings("deprecation")
    public AdvancementListener(String advancementNamespace, String advancementKey) {
        NamespacedKey namespacedKey = new NamespacedKey(advancementNamespace, advancementKey);
        this.advancement = Bukkit.getAdvancement(namespacedKey);
        if (advancement == null) {
            System.err.println("Could not find advancement by namespaced key " + namespacedKey);
        }
    }

    public boolean hasAchievedAdvancement(Player playerToCheck) {
        return playerToCheck.getAdvancementProgress(advancement).isDone();
    }

    public void grantAdvancement(Player receivingPlayer) {
        if (hasAchievedAdvancement(receivingPlayer)) {
            return;
        }

        AdvancementProgress progress = receivingPlayer.getAdvancementProgress(advancement);
        progress.getRemainingCriteria().forEach(progress::awardCriteria);
    }

    public void init(Plugin plugin) {
        this.plugin = plugin;
    }
}
