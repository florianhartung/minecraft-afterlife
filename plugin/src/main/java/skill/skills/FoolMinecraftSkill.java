package skill.skills;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

import java.util.List;

@Configurable("fool")
public class FoolMinecraftSkill extends MinecraftSkill {

    @ConfigValue("duration")
    private static int DURATION;
    @ConfigValue("speed-amplifier")
    private static int SPEED_AMPLIFIER;
    @ConfigValue("jump-boost-amplifier")
    private static int JUMP_BOOST_AMPLIFIER;
    @InjectPlugin
    private Plugin plugin;

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (!isActiveFor(e.getPlayer())) {
            return;
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            e.getPlayer().addPotionEffects(List.of(
                    effect(PotionEffectType.DAMAGE_RESISTANCE, 1),
                    effect(PotionEffectType.SPEED, SPEED_AMPLIFIER),
                    effect(PotionEffectType.SLOW_FALLING, 0),
                    effect(PotionEffectType.JUMP, 1)
            ));
        }, 1);
    }

    private PotionEffect effect(PotionEffectType type, int amplifier) {
        return new PotionEffect(type, DURATION, amplifier, true, true, false);
    }
}
