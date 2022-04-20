package skill.generic;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class PlayerMinecraftSkill implements MinecraftSkill {

    protected final List<UUID> activePlayers;

    protected PlayerMinecraftSkill() {
        activePlayers = new ArrayList<>();
    }

    @Override
    public void apply(Player player) {
        if (player == null) {
            System.out.println("Can not apply PlayerMinecraftSkill because player is null");
            return;
        }

        if (!activePlayers.contains(player.getUniqueId())) {
            activePlayers.add(player.getUniqueId());
        }
    }

    @Override
    public void remove(Player player) {
        activePlayers.remove(player.getUniqueId());
    }

    protected boolean isActiveFor(Player player) {
        return activePlayers.contains(player.getUniqueId());
    }
}
