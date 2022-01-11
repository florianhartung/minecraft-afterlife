package main;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static main.ChatHelper.sendSkillsURL;

public class SkillsExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (label.equalsIgnoreCase("skills")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("This command can only be executed by players!");
                return true;
            }
            sendSkillsURL((Player) sender);
        }
        return false;
    }
}
