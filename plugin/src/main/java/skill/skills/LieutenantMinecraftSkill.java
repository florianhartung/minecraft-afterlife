package skill.skills;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import performancereport.PerfReport;
import skill.generic.AttributeMinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

import java.util.UUID;

@Configurable("lieutenant")
public class LieutenantMinecraftSkill extends AttributeMinecraftSkill {

    @ConfigValue("health-per-player")
    private static int HEALTH_PER_PLAYER;
    @ConfigValue("max-range")
    private static double MAX_RANGE;
    @InjectPlugin(postInject = "startTickTimer")
    private Plugin plugin;

    public LieutenantMinecraftSkill() {
        super(Attribute.GENERIC_MAX_HEALTH, UUID.fromString("5450c60b-b0e4-4ccc-9ced-5713db64e891"), "Lieutenant skill", 0, AttributeModifier.Operation.ADD_NUMBER, false);
    }

    public void onTick() {
        PerfReport.startTimer("lieutenant.tick");
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(this::isActiveFor)
                .forEach(player -> {
                    int n = getNearbyPlayerCount(player);
                    setAttributeAmount(player, n * HEALTH_PER_PLAYER);
                });
        PerfReport.endTimer("lieutenant.tick");
    }

    @SuppressWarnings("unused")
    public void startTickTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onTick, 0, 3);
    }

    private int getNearbyPlayerCount(Player player) {
        return player.getNearbyEntities(MAX_RANGE, MAX_RANGE, MAX_RANGE)
                .stream()
                .filter(e -> e instanceof Player)
                .filter(p -> p.getLocation().distance(player.getLocation()) < MAX_RANGE)
                .toList()
                .size();
    }
}
