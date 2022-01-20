package skill.generic;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/**
 * This class represents a single skill in-game. It can be applied or removed from a specific player.
 * It is a Spigot Listener, which gets registered via the plugin, so all skills can interact with Spigot Events.
 */
public abstract class MinecraftSkill implements Listener {
    /**
     * Applies this skill to the given player
     *
     * @param player The player
     */
    public abstract void apply(Player player);

    /**
     * Removes this skill from the given player
     *
     * @param player The player
     */
    public abstract void remove(Player player);
}
