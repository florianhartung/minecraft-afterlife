package main;

import data.PlayerEntity;
import data.SkillEntity;
import org.bukkit.configuration.file.FileConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

public class RestService {
    private static final RestTemplate restTemplate;

    private static FileConfiguration config;
    private static String rootUrl;
    private static String apiSubdomain;

    static {
        restTemplate = new RestTemplate();
    }

    public static ResponseEntity<PlayerEntity> addSkillpoint(UUID playerUUID) {
        String url = buildUrl("player/") + playerUUID + "/add";
        System.out.println("addSkillpoint: url = " + url);
        return restTemplate.getForEntity(url, PlayerEntity.class);
    }

    public static ResponseEntity<SkillEntity[]> getSkillsOfPlayer(UUID playerUUID) {
        String url = buildUrl("skill/") + playerUUID;
        System.out.println("getSkillsOfPlayer url = " + url);
        return restTemplate.getForEntity(url, SkillEntity[].class);
    }

    public static ResponseEntity<PlayerEntity> saveNewPlayer(String playerUUID) {
        String url = buildUrl("player/new/") + playerUUID;
        System.out.println("saveNewPlayer url = " + url);
        return restTemplate.getForEntity(url, PlayerEntity.class);
    }

    public static ResponseEntity<PlayerEntity> getPlayer(String playerUUID) {
        String url = buildUrl("player/") + playerUUID;
        System.out.println("getPlayer url = " + url);
        return restTemplate.getForEntity(url, PlayerEntity.class);
    }

    private static String buildUrl(String endpoint) {
        return rootUrl + apiSubdomain + endpoint;
    }

    public static void setConfig(FileConfiguration config) {
        RestService.config = config;
        rootUrl = config.getString("server.root");
        apiSubdomain = config.getString("server.api");
        System.out.println("rootUrl = " + rootUrl);
        System.out.println("apiSubdomain = " + apiSubdomain);
    }
}