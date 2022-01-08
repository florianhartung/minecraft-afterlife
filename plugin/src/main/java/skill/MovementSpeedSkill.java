package skill;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * This class represents a single skill, that adds a given amount of movement speed to a player
 * <br>
 * The default movement speed of a player is {@code 1.0d}
 */
public class MovementSpeedSkill extends AttributeSkill {

    /**
     * The amount of movement speed this specific skills adds to a player's movement speed
     */
    private final double movementSpeedAmount;

    public MovementSpeedSkill(double amount) {
        super(Attribute.GENERIC_MOVEMENT_SPEED, UUID.fromString("65d95bab-a4c9-4d49-8aea-2487cf42638b"), "MovementSpeedSkill", AttributeModifier.Operation.ADD_NUMBER);
        this.movementSpeedAmount = amount;
    }

    @Override
    public void apply(Player player) {
        setAttributeAmount(player, movementSpeedAmount);
    }

    @Override
    public void remove(Player player) {
        removeAttribute(player);
    }
}
