package skill.skills.factory;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import skill.generic.AttributeMinecraftSkill;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import java.util.UUID;

@Configurable("fast")
public class FastFactory implements StackableSkillFactory {

    @ConfigValue("speed-amount")
    private static double AMOUNT;

    @Override
    public MinecraftSkill get(int i) {
        return switch (i) {
            case 1 -> of("Fast1", UUID.fromString("65d95bab-a4c9-4d49-8aea-2487cf42638b"));
            case 2 -> of("Fast2", UUID.fromString("e420bd10-b3cf-4c0b-bb77-eafcd667c666"));
            case 3 -> of("Fast3", UUID.fromString("f9b364b8-b55d-43f0-9ae1-9e4b36de5165"));
            default -> throw new IllegalArgumentException("Can't get MinecraftSkill Fast" + i);
        };
    }

    private static AttributeMinecraftSkill of(String name, UUID uuid) {
        return new AttributeMinecraftSkill(Attribute.GENERIC_MOVEMENT_SPEED, uuid, name, AMOUNT, AttributeModifier.Operation.ADD_NUMBER);
    }
}
