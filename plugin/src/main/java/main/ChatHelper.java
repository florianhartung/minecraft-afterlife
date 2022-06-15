package main;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.minecraft.network.chat.Component;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ChatHelper {

    private static final String PREFIX = "§f[§2Afterlife§f] §c";

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
        TextComponent text = new TextComponent(PREFIX + ChatColor.GOLD + ChatColor.ITALIC + "Klicke hier, um deine Skillpunkte auszugeben");
        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new BaseComponent[]{new TranslatableComponent("chat.link.open")})));
        text.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, skillPageUrl.formatted(token)));
        return text;
    }

    public static void setConfig(FileConfiguration config) {
        skillPageUrl = config.getString("server.root") + config.getString("server.skills-page");
    }

    public static void send(Player player, Component component) {
        ((CraftPlayer) player).getHandle().sendSystemMessage(component);
    }
}
