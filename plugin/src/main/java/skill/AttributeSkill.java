package skill;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * This class represents a single skill, that modifies an attribute of the player
 */
public abstract class AttributeSkill extends Skill {

    private final Attribute attribute;
    private final UUID uuid;
    private final String name;
    private final AttributeModifier.Operation operation;

    public AttributeSkill(Attribute attribute, UUID uuid, String name, AttributeModifier.Operation operation) {
        this.attribute = attribute;
        this.uuid = uuid;
        this.name = name;
        this.operation = operation;
    }

    /**
     * Sets the amount of this attribute modifier
     *
     * @param player A player to which the attribute modifier is applied to
     * @param amount The amount
     */
    protected void setAttributeAmount(Player player, double amount) {
        if (!checkAttributeCompatibility(player)) {
            return;
        }

        AttributeInstance attributeInstance = player.getAttribute(attribute);
        assert attributeInstance != null;

        Optional<AttributeModifier> optionalAttributeModifier = attributeInstance.getModifiers()
                .stream()
                .filter(modifier -> modifier.getUniqueId().equals(uuid))
                .findFirst();

        optionalAttributeModifier.ifPresent(attributeInstance::removeModifier);
        if (amount != 0) {
            attributeInstance.addModifier(newAttributeModifier(amount));
        }
    }

    /**
     * Removes this attribute modifier from the given player
     *
     * @param player A player from which the attribute modifier is removed from
     */
    protected void removeAttribute(Player player) {
        double a = switch (operation) {
            case ADD_NUMBER, MULTIPLY_SCALAR_1 -> 0.0d;
            case ADD_SCALAR -> 1.0d;
        };
        setAttributeAmount(player, a);
    }

    /**
     * Creates a new attribute modifier from the information given in the constructor and a specific amount
     *
     * @param amount The amount
     * @return The attribute modifier
     */
    private AttributeModifier newAttributeModifier(double amount) {
        return new AttributeModifier(uuid, name, amount, operation);
    }

    /**
     * Checks whether the given attribute for this class is compatible with the given player
     *
     * @param player The player
     * @return true if they are compatible, otherwise false
     */
    private boolean checkAttributeCompatibility(Player player) {
        if (player.getAttribute(attribute) == null) {
            Bukkit.getLogger().warning("Can not get attribute " + attribute + " from player " + player.getDisplayName() + ".\nAttribute name is " + name);
            return false;
        }
        return true;
    }
}
