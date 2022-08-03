package hud;

import config.PlayerDataConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

@Singleton
public class HudManager implements CommandExecutor {

    private static final char[] PROGRESS_SYMBOLS = new char[]{'▁', '▂', '▃', '▄', '▅', '▆', '▇', '█'};

    @Getter
    private static HudManager instance;

    private final Map<UUID, Map<HudEntry, Integer>> playerHuds = new HashMap<>();

    private final Map<UUID, Boolean> hudEnabled = new HashMap<>();


    public static HudManager init(Plugin plugin) {
        HudManager hudManager = new HudManager();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, hudManager::tick, 0, 1);

        instance = hudManager;
        return hudManager;
    }

    private void tick() {
        Bukkit.getOnlinePlayers()
                .stream()
                .filter(player -> {
                    Map<HudEntry, Integer> m = playerHuds.get(player.getUniqueId());
                    return m != null && !m.isEmpty();
                })
                .filter(player -> hudEnabled.computeIfAbsent(player.getUniqueId(), uuid -> PlayerDataConfig.get(player).isHudEnabled()))
                .forEach(player -> {
                    Map<HudEntry, Integer> hudEntries = playerHuds.get(player.getUniqueId());

                    TextComponent actionBarText = new TextComponent();
                    boolean first = true;
                    for (Map.Entry<HudEntry, Integer> hudEntry : hudEntries.entrySet()) {

                        StringBuilder hudEntryString = new StringBuilder();

                        if (first) {
                            first = false;
                        } else {
                            hudEntryString.append("  ");
                        }

                        hudEntryString.append(hudEntry.getValue() == 7 ? hudEntry.getKey().getColor() : ChatColor.GRAY);
                        hudEntryString.append(hudEntry.getKey().getName());
                        hudEntryString.append(PROGRESS_SYMBOLS[hudEntry.getValue()]);


                        TextComponent hudEntryComponent = new TextComponent(hudEntryString.toString());
                        hudEntryComponent.setColor(hudEntry.getKey().getColor());
                        actionBarText.addExtra(hudEntryComponent);
                    }

                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBarText);
                });
    }

    /**
     * Sets the HUD progress of a specific player for a specific entry
     *
     * @param player   the player
     * @param entry    the entry
     * @param progress a progress value ranging from 0 to 7 with 7 meaning that the entry gets colored
     */
    public static void set(Player player, HudEntry entry, int progress) {
        if (progress < 0 || progress > 7) {
            Bukkit.getLogger().log(Level.WARNING, "Progress of HudEntry can only range from 0 to 7, given progress: " + progress);
            return;
        }

        if (entry == HudEntry.NONE) {
            return;
        }

        instance.playerHuds.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>());
        Map<HudEntry, Integer> prevEntries = instance.playerHuds.get(player.getUniqueId());

        for (Map.Entry<HudEntry, Integer> e : prevEntries.entrySet()) {
            if (e.getKey() == entry) {
                prevEntries.put(entry, progress);
                return;
            }
        }

        prevEntries.put(entry, progress);
    }

    public static void remove(Player player, HudEntry entry) {
        instance.playerHuds.computeIfAbsent(player.getUniqueId(), uuid -> new HashMap<>());
        instance.playerHuds.get(player.getUniqueId()).remove(entry);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("You have to be a player to use this command!");
            return true;
        }

        if (command.getName().equals("hud")) {
            PlayerDataConfig.PlayerData data = PlayerDataConfig.get(player);
            data.setHudEnabled(!data.isHudEnabled());
            PlayerDataConfig.set(player, data);
            hudEnabled.put(player.getUniqueId(), data.isHudEnabled());
            return true;
        }

        return false;
    }


    @AllArgsConstructor
    @Getter
    public enum HudEntry {
        NONE("none", net.md_5.bungee.api.ChatColor.BLACK), // needed as default value for MinecraftSkillTimer
        SPRINT_BURST("SPR", net.md_5.bungee.api.ChatColor.AQUA),
        ADRENALINE("ADR", net.md_5.bungee.api.ChatColor.DARK_RED),
        GHOST_SOUL("GEI", net.md_5.bungee.api.ChatColor.DARK_PURPLE),
        BARRIER("BAR", net.md_5.bungee.api.ChatColor.GOLD);


        private final String name;
        private final net.md_5.bungee.api.ChatColor color;

    }
}
