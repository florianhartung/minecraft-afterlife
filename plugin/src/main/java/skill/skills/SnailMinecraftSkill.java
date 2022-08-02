package skill.skills;

import main.Util;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import skill.generic.AttributeMinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import java.util.UUID;

@Configurable("snail")
public class SnailMinecraftSkill extends AttributeMinecraftSkill {
    @ConfigValue("speed-amount")
    private static double SPEED_AMOUNT;

    public SnailMinecraftSkill() {
        super(Attribute.GENERIC_MOVEMENT_SPEED, UUID.fromString("ba795405-97f0-4907-9123-0f6910dd0825"), "Snail skill", 0, AttributeModifier.Operation.ADD_NUMBER, false);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (isActiveFor(e.getEntity())) {
            removeAttribute(e.getEntity());
        }
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent e) {
        if (!isActiveFor(e.getPlayer()) || e.isCancelled()) {
            return;
        }

        if (e.isSneaking()) {
            setAttributeAmount(e.getPlayer(), SPEED_AMOUNT);
        } else {
            removeAttribute(e.getPlayer());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (isActiveFor(e.getPlayer())) {
            removeAttribute(e.getPlayer());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!isActiveFor(e.getPlayer()) || e.isCancelled()) {
            return;
        }

        if (e.getTo() == null || Util.isTheSameLocation(e.getFrom(), e.getTo())) {
            return;
        }

        if (e.getPlayer().isSneaking() && Math.random() < 0.02) {
            e.getTo().getWorld().spawnParticle(Particle.WAX_OFF, e.getTo(), 2, 0.1d, 0.05d, 0.1d);
        }
    }

}
