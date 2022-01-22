package main;

import data.PlayerEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;


public class PlayerRegistrationListener implements Listener {

    @EventHandler
    public void onLogin(PlayerLoginEvent e) {
        String uuid = e.getPlayer().getUniqueId().toString();
        try {
            RestService.getPlayer(uuid);
        } catch (RestClientException exception) {
            if (!createNewPlayer(uuid)) {
                e.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Du konnest nicht im System registriert werden. Bitte kontaktiere einen Admin.");
            }
        }
    }

    private boolean createNewPlayer(String uuid) {
        ResponseEntity<PlayerEntity> saveResponse = RestService.saveNewPlayer(uuid);
        return saveResponse.getStatusCode() == HttpStatus.OK && saveResponse.getBody() != null;
    }
}
