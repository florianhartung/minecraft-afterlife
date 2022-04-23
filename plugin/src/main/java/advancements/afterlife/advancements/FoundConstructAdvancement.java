package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import config.Config;
import config.ConfigType;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;

import static main.Util.unsafeListCast;

public class FoundConstructAdvancement extends AdvancementListener {

    private static final double MIN_DISTANCE = 5;

    private List<Location> skillBlockLocations;

    public FoundConstructAdvancement() {
        super("afterlifetest", "adv1");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (hasAchievedAdvancement(e.getPlayer())) {
            return;
        }

        Location playerLocation = e.getPlayer().getLocation();
        Optional<Location> nearest = skillBlockLocations.stream()
                .min((o1, o2) -> {
                    double d1 = playerLocation.distanceSquared(o1);
                    double d2 = playerLocation.distanceSquared(o2);

                    return Double.compare(d1, d2);
                });
        boolean inRangeOfSkillBlock = nearest.map(location -> location.distance(playerLocation))
                .filter(distance -> distance < MIN_DISTANCE)
                .isPresent();

        if (inRangeOfSkillBlock) {
            grantAdvancement(e.getPlayer());
        }
    }

    @Override
    public void init(Plugin plugin) {
        super.init(plugin);

        FileConfiguration skillBlocksConfig = Config.get(ConfigType.SKILL_BLOCKS);
        skillBlockLocations = unsafeListCast(skillBlocksConfig.getList("blocks"));
    }
}
