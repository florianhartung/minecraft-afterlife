package skill;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SkillToggleCommandExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!command.getName().equalsIgnoreCase("toggleskillupdates")) {
            return false;
        }

        if (!commandSender.isOp()) {
            return false;
        }

        SkillManager.skillUpdatesEnabled = !SkillManager.skillUpdatesEnabled;
        return true;
    }
}
