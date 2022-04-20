package skillspage;

import data.TokenEntity;
import main.RestService;
import org.bukkit.entity.Player;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class TokenManager {

    private static final Map<String, TokenEntity> activeTokensByPlayer = new HashMap<>();

    public static String newToken(Player player) {
        String playerUUID = player.getUniqueId().toString();

        ResponseEntity<TokenEntity> response = RestService.newToken(playerUUID);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Could not create new token for player " + playerUUID + "!");
        }


        TokenEntity token = response.getBody();
        activeTokensByPlayer.put(playerUUID, token);

        return token.getTokenCode();
    }

    public static TokenEntity tokenByPlayer(Player player) {
        return activeTokensByPlayer.get(player.getUniqueId().toString());
    }

    public static void revokeToken(Player player) {
        RestService.revokeToken(player.getUniqueId().toString());
        activeTokensByPlayer.remove(player.getUniqueId().toString());
    }
}
