package skill.skills;

import main.Util;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import skill.generic.AttributeMinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectTimer;

import java.util.List;
import java.util.UUID;

@Configurable("insect")
public class InsectMinecraftSkill extends AttributeMinecraftSkill {
    private static final List<Material> INSECT_GROUND_MATERIALS = List.of(Material.GRASS_BLOCK, Material.MOSS_BLOCK);

    @ConfigValue("damage-absorption")
    private static double DAMAGE_ABSORPTION;
    @ConfigValue("movement-speed")
    private static double MOVEMENT_SPEED;

    @InjectTimer(duration = 15, onTimerFinished = "removeSpeed")
    private MinecraftSkillTimer speedRemoveTimer;


    public InsectMinecraftSkill() {
        super(Attribute.GENERIC_MOVEMENT_SPEED, UUID.fromString("0afef0c4-a2e3-4bab-8cae-e3f075dfc89d"), "Insect skill", 0, AttributeModifier.Operation.ADD_NUMBER, false);
    }


    @EventHandler
    public void onFall(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        if (!isActiveFor(player)) {
            return;
        }

        if (e.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        Material blockBelow = e.getEntity().getLocation().add(0, -1, 0).getBlock().getType();
        if (!INSECT_GROUND_MATERIALS.contains(blockBelow)) {
            return;
        }


        e.setDamage(e.getDamage() * DAMAGE_ABSORPTION);
    }

    @EventHandler
    public void onRunOnGrass(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!isActiveFor(player)) {
            return;
        }

        if (Util.isTheSameLocation(e.getFrom(), e.getTo())) {
            return;
        }

        Material blockBelow = player.getLocation().add(0, -1, 0).getBlock().getType();
        if (blockBelow == Material.AIR || !blockBelow.isSolid()) {
            return;
        }

        if (!INSECT_GROUND_MATERIALS.contains(blockBelow)) {
            if (!speedRemoveTimer.isActive(player)) {
                speedRemoveTimer.start(player, 40);
            }
            return;
        }

        speedRemoveTimer.cancel(player);

        player.getWorld().spawnParticle(Particle.REDSTONE, e.getTo(), 3, 0.2, 0.05, 0.2, new Particle.DustOptions(Color.fromRGB(20, 100, 0), 0.75f));
        setAttributeAmount(player, MOVEMENT_SPEED);
    }

    @SuppressWarnings("unused")
    public void removeSpeed(Player player) {
        removeAttribute(player);
    }
}
