package controller;

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

// TODO Integrate into PlayerController
@RestController
@RequestMapping("/api/skill")
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

    @GetMapping("/{uuid}/removeall")
    public ResponseEntity<List<SkillEntity>> removeAll(@PathVariable("uuid") String uuid) {
        int skillCount = skillRepository.findAllByUuid(uuid).size();
        if (addSkillpoint(uuid, skillCount)) {
            skillRepository.deleteAllByUuid(uuid);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    private boolean subtractSkillpoint(String uuid) {
        return addSkillpoint(uuid, -1);
    }

    private void addSkillpoint(String uuid) {
        addSkillpoint(uuid, 1);
    }

    private boolean addSkillpoint(String uuid, int amount) {
        return playerRepository.findById(uuid)
                .map(player -> {
                    int newSkillpoints = player.getSkillPoints() + amount;
                    if (newSkillpoints < 0) {
                        return false;
                    }
                    player.setSkillPoints(newSkillpoints);
                    playerRepository.save(player);
                    return true;
                }).orElse(false);
    }
}
