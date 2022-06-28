package skill.generic;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class represents a single skill in-game. It can be applied or removed from a specific player.
 * It is a Spigot Listener, which gets registered via the plugin, so all skills can interact with Spigot Events.
 */
public abstract class MinecraftSkill implements Listener {
    public final List<UUID> activePlayers;

    protected MinecraftSkill() {
        activePlayers = new ArrayList<>();
    }

    public void apply(Player player) {
        if (player == null) {
            System.out.println("Can not apply MinecraftSkill because player is null");
            return;
        }

        if (!activePlayers.contains(player.getUniqueId())) {
            activePlayers.add(player.getUniqueId());
        }
    }

    public void remove(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    protected boolean isActiveFor(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }
}
