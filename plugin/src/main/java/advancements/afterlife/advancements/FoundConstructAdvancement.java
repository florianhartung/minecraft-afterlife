package advancements.afterlife.advancements;

import advancements.afterlife.AdvancementListener;
import config.Config;
import config.ConfigType;
import main.ChatHelper;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import performancereport.PerfReport;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static main.Util.unsafeListCast;

public class FoundConstructAdvancement extends AdvancementListener {

    private static final double MIN_DISTANCE = 5;

    private List<Location> skillBlockLocations;

    public FoundConstructAdvancement() {
        super("afterlife", "find_construct_shard");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        PerfReport.startTimer("advancement.foundconstruct");
        if (hasAchievedAdvancement(e.getPlayer())) {
            PerfReport.endTimer("advancement.foundconstruct");
            return;
        }

        Location playerLocation = e.getPlayer().getLocation();
        Optional<Location> nearest = skillBlockLocations.stream()
                .filter(location -> e.getPlayer().getWorld().equals(location.getWorld()))
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
            e.getPlayer().discoverRecipe(Objects.requireNonNull(NamespacedKey.fromString("afterlife:construct_shard_tracker")));
            ChatHelper.sendMessage(e.getPlayer(), "Du kannst nun einen Gral-Kompass herstellen.\nSieh ihn dir in deinem Rezeptbuch an.");
        }
        PerfReport.endTimer("advancement.foundconstruct");
    }

    @Override
    public void init(Plugin plugin) {
        super.init(plugin);

        FileConfiguration skillBlocksConfig = Config.get(ConfigType.SKILL_BLOCKS);
        skillBlockLocations = unsafeListCast(skillBlocksConfig.getList("blocks"));
    }
}
