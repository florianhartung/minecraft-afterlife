package performancereport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class PerfReportCommandExecutor implements CommandExecutor {
    private final Plugin plugin;

    public PerfReportCommandExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!command.getName().equalsIgnoreCase("perfreport")) {
            return false;
        }

        if (!commandSender.isOp()) {
            commandSender.sendMessage("You do not have the required permission to use this command!");
            return true;
        }

        commandSender.sendMessage("-- Starting performance report! --");
        PerfReport.clear();

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            commandSender.sendMessage("-- Performance Report Results --");

            PerfReport.calculateAverageExecutionTimes()
                    .forEach((key, value) -> commandSender.sendMessage(ChatColor.YELLOW + key + ": " + ChatColor.WHITE + String.format("%.2f", value) + "ms"));

        }, 10 * 20);

        return true;
    }
}
