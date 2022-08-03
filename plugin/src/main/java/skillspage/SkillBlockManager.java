package skillspage;

import config.Config;
import config.ConfigType;
import main.ChatHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.List;
import java.util.function.Consumer;

import static main.Util.unsafeListCast;

public class SkillBlockManager implements Listener, CommandExecutor {

    private static final double MIN_DISTANCE_TO_BLOCK = 6;

    private final FileConfiguration skillBlocksConfig;

    private final List<Location> skillBlockLocations;

    public SkillBlockManager() {
        this.skillBlocksConfig = Config.get(ConfigType.SKILL_BLOCKS);

        skillBlockLocations = unsafeListCast(skillBlocksConfig.getList("blocks"));
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
        List<Double> distances = skillBlockLocations.stream().filter(loc -> playerLoc.getWorld().getUID().equals(loc.getWorld().getUID())).map(loc -> loc.distance(playerLoc)).toList();

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

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("This command can only be used by players");
            return false;
        }

        if (!command.getName().equalsIgnoreCase("skillblock")) {
            return false;
        }

        if (!player.isOp()) {
            return false;
        }

        if (strings.length != 1) {
            return false;
        }

        String subCommand = strings[0];
        if (subCommand.equalsIgnoreCase("add")) {
            Location newSkillBlockLocation = player.getLocation().getBlock().getLocation();
            changeSkillBlocks(locations -> locations.add(newSkillBlockLocation));
            newSkillBlockLocation.getBlock().setType(Material.COMMAND_BLOCK);

            // Needed for compass that tracks skillblocks
            Location lodestoneLocation = newSkillBlockLocation.clone();
            lodestoneLocation.setY(-64);
            lodestoneLocation.getBlock().setType(Material.LODESTONE);
            player.sendMessage("Successfully added skillblock");
        } else if (subCommand.equalsIgnoreCase("delete")) {
            Block target = player.getTargetBlockExact(6);
            if (target == null) {
                player.sendMessage("You are not targeting a block!");
                return true;
            }

            Location skillBlockLocation = target.getLocation();

            changeSkillBlocks(locations -> locations.remove(skillBlockLocation));
            skillBlockLocation.getBlock().setType(Material.AIR);

            // No longer needed for compasses
            Location anchorLocation = skillBlockLocation.clone();
            anchorLocation.setY(-64);
            anchorLocation.getBlock().setType(Material.BEDROCK);

            player.sendMessage("Successfully deleted skillblock");
        }

        return true;
    }

    private void changeSkillBlocks(Consumer<List<Location>> listMutator) {
        listMutator.accept(skillBlockLocations);

        skillBlocksConfig.set("blocks", skillBlockLocations);
        Config.save(ConfigType.SKILL_BLOCKS, skillBlocksConfig);
    }
}
