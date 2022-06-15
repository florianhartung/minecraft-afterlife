package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftTrident;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.LightningStrikeEvent;

import java.util.Optional;

public class SummonLightningAdvancement extends AdvancementListener {
    public SummonLightningAdvancement() {
        super("afterlife", "summon_lightning");
    }


    @EventHandler
    public void onLightning(LightningStrikeEvent e) {
        if (e.getCause() == LightningStrikeEvent.Cause.TRIDENT) {
            Location location = e.getLightning().getLocation();
            Optional.ofNullable(location.getWorld())
                    .map(world -> world.getNearbyEntities(location, 3, 3, 3, entity -> entity instanceof Trident))
                    .flatMap(foundTridents -> foundTridents.stream().findFirst())
                    .map(tridentEntity -> (Trident) tridentEntity)
                    .flatMap(this::getOwnerOfTrident)
                    .ifPresent(this::grantAdvancement);

        }
    }

    public Optional<Player> getOwnerOfTrident(Trident trident) {
        Entity owner = ((CraftTrident) trident).getHandle().getOwner();
        if (owner instanceof net.minecraft.world.entity.player.Player player) {
            return Optional.ofNullable(Bukkit.getPlayer(player.getUUID()));
        }
        return Optional.empty();
    }
}
