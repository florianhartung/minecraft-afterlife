package skill.skills;

import net.minecraft.world.food.FoodProperties;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

@Configurable("saturated")
public class SaturatedMinecraftSkill extends MinecraftSkill {

    @ConfigValue("effect-duration")
    private static int EFFECT_DURATION;


    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e) {
        if (!isActiveFor(e.getPlayer()) || e.isCancelled()) {
            return;
        }


        FoodProperties foodProperties = getFoodProperties(e.getItem());
        if (foodProperties == null) {
            return;
        }

        e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, EFFECT_DURATION, 0, true, true));
        e.getPlayer().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, e.getPlayer().getLocation().clone().add(0, 1, 0), 10, 0.3d, 0.4d, 0.3d);
    }

    private FoodProperties getFoodProperties(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.getItem().getFoodProperties();
    }
}
