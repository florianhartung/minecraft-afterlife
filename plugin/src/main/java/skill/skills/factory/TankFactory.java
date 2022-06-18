package skill.skills.factory;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import skill.generic.AttributeMinecraftSkill;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import java.util.UUID;

@Configurable("tank")
public class TankFactory implements StackableSkillFactory {

    @ConfigValue("hp-amount")
    private static double AMOUNT;

    @Override
    public MinecraftSkill get(int i) {
        return switch (i) {
            case 1 -> of("Tank1", UUID.fromString("afc648b0-4421-4f05-b170-70daba2d2e08"));
            case 2 -> of("Tank2", UUID.fromString("6abc443d-57a7-4793-b260-a147e8f37ff0"));
            case 3 -> of("Tank3", UUID.fromString("e0e09627-306e-4050-a6bd-43ba0a995b37"));
            case 4 -> of("Tank4", UUID.fromString("029be596-6d28-449f-8988-0e77ee216fba"));
            case 5 -> of("Tank5", UUID.fromString("9fa251b5-269f-4d3d-af69-5b0efa4a1fb9"));
            default -> throw new IllegalArgumentException("Can't get MinecraftSkill Tank" + i);
        };
    }

    private static AttributeMinecraftSkill of(String name, UUID uuid) {
        return new AttributeMinecraftSkill(Attribute.GENERIC_MAX_HEALTH, uuid, name, AMOUNT, AttributeModifier.Operation.ADD_NUMBER);
    }
}
