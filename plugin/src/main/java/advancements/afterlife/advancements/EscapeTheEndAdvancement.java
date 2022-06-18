package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class EscapeTheEndAdvancement extends AdvancementListener {
    public EscapeTheEndAdvancement() {
        super("afterlife", "escape_end");
    }

    @EventHandler
    public void onEndEscape(PlayerChangedWorldEvent e) {
        if (e.getFrom().getEnvironment() == World.Environment.THE_END) {
            World.Environment newEnvironment = e.getPlayer().getWorld().getEnvironment();
            if (newEnvironment == World.Environment.NORMAL || newEnvironment == World.Environment.CUSTOM) {
                grantAdvancement(e.getPlayer());
            }
        }
    }
}
