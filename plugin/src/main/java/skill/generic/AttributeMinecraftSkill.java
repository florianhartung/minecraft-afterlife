package skill.generic;

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
public class AttributeMinecraftSkill extends MinecraftSkill {

    private final Attribute attribute;
    private final UUID uuid;
    private final String name;
    private final AttributeModifier.Operation operation;
    private final double amount;
    private final boolean autoApply;

    public AttributeMinecraftSkill(Attribute attribute, UUID uuid, String name, double amount, AttributeModifier.Operation operation, boolean autoApply) {
        this.attribute = attribute;
        this.uuid = uuid;
        this.name = name;
        this.amount = amount;
        this.operation = operation;
        this.autoApply = autoApply;
    }

    /**
     * Sets the amount of this attribute modifier
     *
     * @param player A player to which the attribute modifier is applied to
     * @param amount The amount
     */
    protected void setAttributeAmount(Player player, double amount) {
        if (isIncompatibleWith(player)) {
            return;
        }

        AttributeInstance attributeInstance = player.getAttribute(attribute);
        assert attributeInstance != null;

        Optional<AttributeModifier> optionalAttributeModifier = attributeInstance.getModifiers()
                .stream()
                .filter(modifier -> modifier.getUniqueId().equals(uuid))
                .findFirst();
        if (optionalAttributeModifier.isPresent()) {
            AttributeModifier attributeModifier = optionalAttributeModifier.get();
            if (attributeModifier.getAmount() != amount) {
                attributeInstance.removeModifier(attributeModifier);
                attributeInstance.addModifier(newAttributeModifier(amount));
            }
        } else {
            attributeInstance.addModifier(newAttributeModifier(amount));
        }
    }

    /**
     * Removes this attribute modifier from the given player
     *
     * @param player A player from which the attribute modifier is removed from
     */
    protected void removeAttribute(Player player) {
        if (isIncompatibleWith(player)) {
            return;
        }
        AttributeInstance attributeInstance = player.getAttribute(attribute);
        assert attributeInstance != null;


        attributeInstance.getModifiers()
                .stream()
                .filter(modifier -> modifier.getUniqueId().equals(uuid))
                .findFirst()
                .ifPresent(attributeInstance::removeModifier);
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
    private boolean isIncompatibleWith(Player player) {
        if (player.getAttribute(attribute) == null) {
            Bukkit.getLogger().warning("Can not get attribute " + attribute + " from player " + player.getDisplayName() + ".\nAttribute name is " + name);
            return true;
        }
        return false;
    }

    @Override
    public void apply(Player player) {
        super.apply(player);
        if (autoApply) {
            setAttributeAmount(player, amount);
        }
    }

    @Override
    public void remove(Player player) {
        removeAttribute(player);
    }
}
