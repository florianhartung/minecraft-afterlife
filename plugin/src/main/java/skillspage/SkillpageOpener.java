package skillspage;

import config.Config;
import config.ConfigType;
import main.ChatHelper;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.function.BiConsumer;

import static main.Util.unsafeListCast;

public class SkillpageOpener implements Listener {

    private static final double MIN_DISTANCE_TO_BLOCK = 6;

    private final FileConfiguration skillBlocksConfig;

    private final BiConsumer<FileConfiguration, String> configSaver; // TODO Remove

    private final List<Location> skillBlockLocations;

    public SkillpageOpener(BiConsumer<FileConfiguration, String> configSaver) {
        this.skillBlocksConfig = Config.get(ConfigType.SKILL_BLOCKS);
        this.configSaver = configSaver;

        skillBlockLocations = unsafeListCast(skillBlocksConfig.getList("blocks"));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.getMessage().equals("skillblock")) {
            Location newSkillBlock = e.getPlayer().getTargetBlock(null, 6).getLocation();
            List<Location> locations = unsafeListCast(skillBlocksConfig.getList("blocks"));
            locations.add(newSkillBlock);

            configSaver.accept(skillBlocksConfig, "skillblocks" + System.currentTimeMillis() + ".yml");
            e.getPlayer().sendMessage("Successfully saves skillblock");
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
            List<Location> locations = unsafeListCast(skillBlocksConfig.getList("blocks"));
            if (locations.contains(e.getClickedBlock().getLocation())) {
                String newToken = TokenManager.newToken(e.getPlayer());
                ChatHelper.sendSkillsURL(e.getPlayer(), newToken);
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (TokenManager.tokenByPlayer(e.getPlayer()) == null) {
            return;
        }

        Location playerLoc = e.getPlayer().getLocation();
        List<Double> distances = skillBlockLocations.stream()
                .filter(loc -> playerLoc.getWorld().getUID().equals(loc.getWorld().getUID()))
                .map(loc -> loc.distance(playerLoc))
                .toList();

        int indexOfSmallestDistance = indexOfSmallest(distances);
        if (distances.get(indexOfSmallestDistance) > MIN_DISTANCE_TO_BLOCK) {
            TokenManager.revokeToken(e.getPlayer());
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (TokenManager.tokenByPlayer(e.getPlayer()) != null) {
            TokenManager.revokeToken(e.getPlayer());
        }
    }

    private int indexOfSmallest(List<Double> list) {
        int minIndex = -1;
        double minValue = Double.MAX_VALUE;
        for (int i = 0; i < list.size(); i++) {
            double value = list.get(i);
            if (value < minValue) {
                minValue = value;
                minIndex = i;
            }
        }
        return minIndex;
    }
}
