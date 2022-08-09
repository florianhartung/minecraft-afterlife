package main;

import data.PlayerEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import skill.SkillUpdater;


public class PlayerRegistrationListener implements Listener {

    private final Plugin plugin;

    public PlayerRegistrationListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(PlayerLoginEvent e) {
        if (e.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }

        Bukkit.getScheduler()
                .runTaskAsynchronously(plugin, () -> tryLoadSkills(e.getPlayer()));
    }

    private void tryLoadSkills(Player player) {
        String uuid = player.getUniqueId().toString();

        ChatHelper.sendMessage(player, "Verbindung zum Gral wird hergestellt..");
        try {
            RestService.getPlayer(uuid);
        } catch (RestClientException exception) {
            if (!createNewPlayer(uuid)) {
                player.kickPlayer("Du konnest nicht im System registriert werden. Bitte kontaktiere einen Admin.");
                return;
            }
        }
        SkillUpdater.reloadSkillsForPlayer(player);
        ChatHelper.sendMessage(player, "Deine Fähigkeiten wurden geladen.");

    }

    private boolean createNewPlayer(String uuid) {
        ResponseEntity<PlayerEntity> saveResponse = RestService.saveNewPlayer(uuid);
        return saveResponse.getStatusCode() == HttpStatus.OK && saveResponse.getBody() != null;
    }
}
