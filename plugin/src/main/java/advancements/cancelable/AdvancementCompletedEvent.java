package advancements.cancelable;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@Getter
public class AdvancementCompletedEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Setter
    private boolean cancelled = false;

    private final Advancement advancement;

    public AdvancementCompletedEvent(Player who, Advancement advancement) {
        super(who);
        this.advancement = advancement;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
