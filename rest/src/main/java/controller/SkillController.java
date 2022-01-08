package controller;

import data.PlayerEntity;
import data.Skill;
import data.SkillEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repository.PlayerRepository;
import repository.SkillRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/skill")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SkillController {
    private final SkillRepository skillRepository;
    private final PlayerRepository playerRepository;

    @GetMapping("/{uuid}")
    public ResponseEntity<List<SkillEntity>> findBy(@PathVariable("uuid") String uuid) {
        return ResponseEntity.ok(
                skillRepository.findAllByUuid(uuid)
                        .stream()
                        .toList());
    }

    @GetMapping("/{uuid}/add/{skill}")
    public ResponseEntity<SkillEntity> add(@PathVariable("uuid") String uuid, @PathVariable("skill") String skillString) {
        Skill skill = Skill.valueOf(skillString);

        boolean alreadyHasSkill = skillRepository.findAllByUuid(uuid).stream().anyMatch(se -> se.getSkill() == skill);
        if (alreadyHasSkill) {
            return ResponseEntity.status(HttpStatus.SEE_OTHER)
                    .build();
        }

        if (subtractSkillpoint(uuid)) {
            SkillEntity skillEntity = new SkillEntity();
            skillEntity.setSkill(skill);
            skillEntity.setUuid(uuid);

            SkillEntity saved = skillRepository.save(skillEntity);
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .build();

    }

    @GetMapping("/{uuid}/remove/{skill}")
    public ResponseEntity<SkillEntity> remove(@PathVariable("uuid") String uuid, @PathVariable("skill") String skillString) {
        Skill skill = Skill.valueOf(skillString);

        Optional<SkillEntity> skillEntity = skillRepository.findAllByUuid(uuid)
                .stream()
                .filter(se -> se.getSkill() == skill)
                .findFirst();
        if (skillEntity.isPresent()) {
            skillRepository.delete(skillEntity.get());
            addSkillpoint(uuid);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private boolean subtractSkillpoint(String uuid) {
        Optional<PlayerEntity> optionalPlayer = playerRepository.findById(uuid)
                .filter(p -> p.getSkillPoints() > 0);
        if (optionalPlayer.isPresent()) {
            PlayerEntity player = optionalPlayer.get();
            player.setSkillPoints(player.getSkillPoints() - 1);
            playerRepository.save(player);
            return true;
        }
        return false;
    }

    private void addSkillpoint(String uuid) {
        playerRepository.findById(uuid)
                .ifPresent(player -> {
                    player.setSkillPoints(player.getSkillPoints() + 1);
                    playerRepository.save(player);
                });
    }
}
