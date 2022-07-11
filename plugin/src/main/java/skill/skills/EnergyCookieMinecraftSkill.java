package skill.skills;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;

import java.util.List;

@Configurable("energy-cookie")
public class EnergyCookieMinecraftSkill extends MinecraftSkill {
    @ConfigValue("speed-amplifier")
    private static int SPEED_AMPLIFIER;
    @ConfigValue("speed-duration")
    private static int SPEED_DURATION;
    @ConfigValue("jump-boost-amplifier")
    private static int JUMP_BOOST_AMPLIFIER;
    @ConfigValue("jump-boost-duration")
    private static int JUMP_BOOST_DURATION;

    @EventHandler
    public void onConsumeCookie(PlayerItemConsumeEvent e) {
        if (!isActiveFor(e.getPlayer())) {
            return;
        }

        if (e.getItem().getType() != Material.COOKIE) {
            return;
        }

        PotionEffect speed = new PotionEffect(PotionEffectType.SPEED, SPEED_DURATION, SPEED_AMPLIFIER, true, false, false);
        PotionEffect jumpBoost = new PotionEffect(PotionEffectType.JUMP, JUMP_BOOST_DURATION, JUMP_BOOST_AMPLIFIER, true, false, false);
        e.getPlayer().addPotionEffects(List.of(speed, jumpBoost));
    }
}
