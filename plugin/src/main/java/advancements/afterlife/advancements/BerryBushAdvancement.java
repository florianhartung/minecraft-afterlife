package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Optional;

public class BerryBushAdvancement extends AdvancementListener {
    public BerryBushAdvancement() {
        super("afterlife", "killed_by_berry_bush");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (isLastDamageContactDamage(player) && isStandingInBerryBush(player)) {
            grantAdvancement(e.getEntity());
        }
    }

    private boolean isLastDamageContactDamage(Player player) {
        return Optional.ofNullable(player.getLastDamageCause())
                .map(EntityDamageEvent::getCause)
                .map(cause -> cause == EntityDamageEvent.DamageCause.CONTACT)
                .orElse(false);
    }

    private boolean isStandingInBerryBush(Player player) {
        return player.getLocation().getBlock().getType() == Material.SWEET_BERRY_BUSH;
    }
}
