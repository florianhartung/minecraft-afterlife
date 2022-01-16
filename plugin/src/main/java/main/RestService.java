package main;

import data.PlayerEntity;
import data.SkillEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

public class RestService {
    private static RestTemplate restTemplate;

    static {
        restTemplate = new RestTemplate();
    }

    public static ResponseEntity<PlayerEntity> addSkillpoint(UUID playerUUID) {
        return restTemplate.getForEntity("http://localhost:80/api/player/" + playerUUID + "/add", PlayerEntity.class);
    }

    public static ResponseEntity<SkillEntity[]> getSkillsOfPlayer(UUID playerUUID) {
        return restTemplate.getForEntity("http://localhost:80/api/skill/" + playerUUID, SkillEntity[].class);
    }
}