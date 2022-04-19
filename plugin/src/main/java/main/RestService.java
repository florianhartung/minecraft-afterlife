package main;

import data.PlayerEntity;
import data.SkillEntity;
import data.TokenEntity;
import org.bukkit.configuration.file.FileConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

public class RestService {
    private static final RestTemplate restTemplate;

    private static String rootUrl;
    private static String apiSubdomain;

    static {
        restTemplate = new RestTemplate();
    }

    public static ResponseEntity<PlayerEntity> addSkillpoint(UUID playerUUID) {
        String url = buildUrl("player/") + playerUUID + "/add";
        return restTemplate.getForEntity(url, PlayerEntity.class);
    }

    public static ResponseEntity<SkillEntity[]> getSkillsOfPlayer(UUID playerUUID) {
        String url = buildUrl("skill/") + playerUUID;
        return restTemplate.getForEntity(url, SkillEntity[].class);
    }

    public static ResponseEntity<PlayerEntity> saveNewPlayer(String playerUUID) {
        String url = buildUrl("player/new/") + playerUUID;
        return restTemplate.getForEntity(url, PlayerEntity.class);
    }

    public static ResponseEntity<PlayerEntity> getPlayer(String playerUUID) {
        String url = buildUrl("player/") + playerUUID;
        return restTemplate.getForEntity(url, PlayerEntity.class);
    }

    public static ResponseEntity<TokenEntity> newToken(String playerUUID) {
        String url = buildUrl("token/new/") + playerUUID;
        return restTemplate.getForEntity(url, TokenEntity.class);
    }

    public static ResponseEntity<TokenEntity> getToken(String tokenId) {
        String url = buildUrl("token/") + tokenId;
        return restTemplate.getForEntity(url, TokenEntity.class);
    }

    public static ResponseEntity<TokenEntity> revokeToken(String playerUUID) {
        String url = buildUrl("token/revoke/") + playerUUID;
        return restTemplate.getForEntity(url, TokenEntity.class);
    }

    private static String buildUrl(String endpoint) {
        return rootUrl + apiSubdomain + endpoint;
    }

    public static void setConfig(FileConfiguration config) {
        rootUrl = config.getString("server.root");
        apiSubdomain = config.getString("server.api");
    }
}