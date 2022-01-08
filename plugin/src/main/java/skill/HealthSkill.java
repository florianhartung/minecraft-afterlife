package skill;


import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * This class represents a single skill, that adds a given amount of max health to a player
 * <br>
 * The default max health of a player is {@code 20.0d}. Every {@code 1.0d} equals half a heart
 */
public class HealthSkill extends AttributeSkill {

    /**
     * The amount of max health this specific skills adds to a player's max health
     */
    private final double healthAmount;

    public HealthSkill(double amount) {
        super(Attribute.GENERIC_MAX_HEALTH, UUID.fromString("afc648b0-4421-4f05-b170-70daba2d2e08"), "HealthSkill", AttributeModifier.Operation.ADD_NUMBER);
        this.healthAmount = amount;
    }

    @Override
    public void apply(Player player) {
        setAttributeAmount(player, healthAmount);
    }

    @Override
    public void remove(Player player) {
        removeAttribute(player);
    }
}
