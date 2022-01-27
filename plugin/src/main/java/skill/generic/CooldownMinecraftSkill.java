package skill.generic;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class CooldownMinecraftSkill extends PlayerMinecraftSkill {

    private final Map<UUID, Long> lastActivation;

    private int cooldown;

    protected CooldownMinecraftSkill() {
        lastActivation = new HashMap<>();
    }

    protected void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    protected void startCooldown(Player player) {
        lastActivation.put(player.getUniqueId(), System.currentTimeMillis());
    }

    protected void skipCooldown(Player player) {
        lastActivation.remove(player.getUniqueId());
    }

    protected boolean isOnCooldown(Player player) {
        return Optional.ofNullable(lastActivation.get(player.getUniqueId()))
                .map(last -> System.currentTimeMillis() - last < cooldown)
                .orElse(false);
    }
}
