package skill;

import data.SkillEntity;
import main.RestService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import skill.generic.MinecraftSkill;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * This is a temporary listener, that when a player types 'apply skills' or 'remove skills' in chat, all the given skills get applied to them or removed from them
 */
public record SkillApplier(List<MinecraftSkill> minecraftSkills) implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        ResponseEntity<SkillEntity[]> response = RestService.getSkillsOfPlayer(uuid);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Stream.of(response.getBody())
                    .map(SkillEntity::getSkill)
                    .map(SkillHolder::getMinecraftSkill)
                    .forEach(minecraftSkill -> Objects.requireNonNull(minecraftSkill).apply(e.getPlayer()));
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (e.getMessage().equals("apply")) {
            minecraftSkills.forEach(minecraftSkill -> minecraftSkill.apply(e.getPlayer()));
        } else if (e.getMessage().equals("remove")) {
            minecraftSkills.forEach(minecraftSkill -> minecraftSkill.remove(e.getPlayer()));
        } else if (e.getMessage().equals("update")) {
            SkillManager.reloadSkills();
            return;
        }
        e.setCancelled(true);
    }
}
