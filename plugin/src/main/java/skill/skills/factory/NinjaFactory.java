package skill.skills.factory;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import skill.generic.AttributeMinecraftSkill;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import java.util.UUID;

@Configurable("ninja")
public class NinjaFactory implements StackableSkillFactory {

    @ConfigValue("attack-speed-amount")
    private static double AMOUNT;

    @Override
    public MinecraftSkill get(int i) {
        return switch (i) {
            case 1 -> of("Ninja1", UUID.fromString("2ce7927c-a9a5-4237-85ca-a61b88f0794a"));
            case 2 -> of("Ninja2", UUID.fromString("080bb78b-df4f-4d65-b387-db397d76fecd"));
            case 3 -> of("Ninja3", UUID.fromString("2d45425f-2aaa-429e-b3d4-3ceb9e6247fc"));
            case 4 -> of("Ninja4", UUID.fromString("bd9a6d15-f464-4f3a-983b-549b73cfabfd"));
            case 5 -> of("Ninja5", UUID.fromString("f1e324bb-a2b3-454b-a8f1-9a2b14462238"));
            default -> throw new IllegalArgumentException("Can't get MinecraftSkill Ninja" + i);
        };
    }

    private static AttributeMinecraftSkill of(String name, UUID uuid) {
        return new AttributeMinecraftSkill(Attribute.GENERIC_ATTACK_SPEED, uuid, name, AMOUNT, AttributeModifier.Operation.ADD_NUMBER);
    }
}
