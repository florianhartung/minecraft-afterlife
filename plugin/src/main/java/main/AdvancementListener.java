package main;

import data.PlayerEntity;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static main.ChatHelper.sendMessage;

public class AdvancementListener implements Listener {

    private static final String ADVANCEMENT_NAMESPACE = "skills";

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent e) {
        UUID playerUUID = e.getPlayer().getUniqueId();
        NamespacedKey key = e.getAdvancement().getKey();
        if (key.getNamespace().equalsIgnoreCase(ADVANCEMENT_NAMESPACE)) {
            ResponseEntity<PlayerEntity> response = RestService.addSkillpoint(playerUUID);
            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                e.getPlayer().sendMessage("Es konnte dir kein Skillpunkt gegeben werden. Bitte kontaktiere einen Administrator.");
                Bukkit.getLogger().warning("Couldn't give skillpoint to player " + e.getPlayer().getDisplayName() + " from achievement " + key);
                return;
            }
            TextComponent actionBarText = new TextComponent("+1 Skillpunkt");
            actionBarText.setColor(ChatColor.GOLD);
            e.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, actionBarText);

            sendMessage(e.getPlayer(), "Du hast jetzt " + response.getBody().getSkillPoints() + " Skillpunkte.");
        }
    }
}
