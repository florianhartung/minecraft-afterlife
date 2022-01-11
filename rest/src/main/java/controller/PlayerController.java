package controller;

import data.PlayerEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repository.PlayerRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PlayerController {
    private final PlayerRepository playerRepository;

    @GetMapping("/all")
    public ResponseEntity<List<PlayerEntity>> findAll() {
        return ResponseEntity.ok(
                StreamSupport
                        .stream(playerRepository.findAll().spliterator(), false)
                        .toList());
    }

    @GetMapping("/new")
    public ResponseEntity<PlayerEntity> newPlayer() {
        PlayerEntity player = new PlayerEntity();
        player.setUuid(UUID.randomUUID().toString());
        player.setSkillPoints((int) (Math.random() * 100));
        PlayerEntity saved = playerRepository.save(player);
        return ResponseEntity.ok(saved);

    }

    @GetMapping("/{uuid}")
    public ResponseEntity<PlayerEntity> getByUUID(@PathVariable("uuid") String uuid) {
        return ResponseEntity.of(playerRepository.findById(uuid));
    }

    @GetMapping("/{uuid}/add")
    public ResponseEntity<PlayerEntity> addSkillpoint(@PathVariable("uuid") String uuid) {
        Optional<PlayerEntity> optionalPlayer = playerRepository.findById(uuid);
        if (optionalPlayer.isPresent()) {
            PlayerEntity player = optionalPlayer.get();
            player.setSkillPoints(player.getSkillPoints() + 1);
            PlayerEntity saved = playerRepository.save(player);
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }
}
