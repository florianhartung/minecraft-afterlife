package skill.globalmodifiers;

import config.Config;
import config.ConfigType;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import skill.generic.MinecraftSkill;
import skill.injection.Command;

import java.util.Optional;

@Command("toggleend")
public class BlockedEnd extends MinecraftSkill implements CommandExecutor {

    private static final String CONFIG_PATH = "end-blocked";

    private final FileConfiguration config;
    private boolean endBlocked;

    public BlockedEnd() {
        this.config = Config.get(ConfigType.DEFAULT);
        endBlocked = Optional.ofNullable(config.getBoolean(CONFIG_PATH)).orElse(false);
    }

    @EventHandler
    public void onEnderEyeUse(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.END_PORTAL_FRAME) {
            return;
        }

        if (e.getItem() == null || e.getItem().getType() != Material.ENDER_EYE) {
            return;
        }

        if (endBlocked) {
            e.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String s, String[] strings) {
        if (!command.getName().equalsIgnoreCase("toggleend")) {
            return false;
        }
        if (commandSender.isOp()) {
            endBlocked = !endBlocked;
            config.set(CONFIG_PATH, endBlocked);
            Config.save(ConfigType.DEFAULT, config);
            commandSender.sendMessage("Das Ende ist nun " + (endBlocked ? "blockiert" : "geöffnet"));
        }

        return true;
    }
}
