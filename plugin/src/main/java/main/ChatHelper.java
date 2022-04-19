package main;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class ChatHelper {

    private static final String PREFIX = "§f[§2Skills§f] §c";

    private static String skillPageUrl;

    public static void sendMessage(Player player, String message) {
        send(player, new TextComponent(PREFIX + message));
    }

    public static void sendSkillsURL(Player player, String token) {
        send(player, skillsUrlMessage(token));
    }

    private static void send(Player player, TextComponent textComponent) {
        player.spigot().sendMessage(textComponent);
    }

    private static TextComponent skillsUrlMessage(String token) {
        TextComponent text = new TextComponent(PREFIX + "§6§oKlicke hier, um deine Skillpunkte auszugeben");
        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, skillPageUrl.formatted(token)));
        return text;
    }

    public static void setConfig(FileConfiguration config) {
        skillPageUrl = config.getString("server.root") + config.getString("server.skills-page");
    }
}
