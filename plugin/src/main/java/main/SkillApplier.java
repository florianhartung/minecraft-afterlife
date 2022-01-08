package main;

import data.SkillEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import skill.Skill;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * This is a temporary listener, that when a player types 'apply skills' or 'remove skills' in chat, all the given skills get applied to them or removed from them
 */
public record SkillApplier(List<Skill> skills) implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<SkillEntity[]> response =
                restTemplate.getForEntity("http://localhost:80/skill/" + uuid, SkillEntity[].class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            System.out.println("response = " + response);
            Stream.of(response.getBody())
                    .map(SkillEntity::getSkill)
                    .map(SkillMapper::map)
                    .forEach(skill -> Objects.requireNonNull(skill).apply(e.getPlayer()));
        } else {
            System.out.println("response = " + response);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.getMessage().equals("apply")) {
            skills.forEach(skill -> skill.apply(e.getPlayer()));
        } else if (e.getMessage().equals("remove")) {
            skills.forEach(skill -> skill.remove(e.getPlayer()));
        } else {
            return;
        }
        e.setCancelled(true);
    }
}
