package ui.view;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import controller.PlayerController;
import controller.SkillController;
import data.PlayerEntity;
import data.Skill;
import data.SkillEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Route("skillsOld")
@SpringComponent
@RequiredArgsConstructor
@UIScope
public class SkillsViewOld extends VerticalLayout implements HasUrlParameter<String> {

    private final PlayerController playerController;
    private final SkillController skillController;

    private PlayerEntity currentPlayer;


    @Override
    public void setParameter(BeforeEvent event,
                             String parameter) {
        ResponseEntity<PlayerEntity> response = playerController.getByUUID(parameter);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            currentPlayer = response.getBody();
            createButtons();
            Button resetSkills = new Button("Reset all skills");
            resetSkills.addClickListener(e -> {
                ResponseEntity<List<SkillEntity>> responseAcquiredSkills = skillController.findBy(currentPlayer.getUuid());
                if (responseAcquiredSkills.getStatusCode() != HttpStatus.OK || responseAcquiredSkills.getBody() == null) {
                    add(new Text("Can't get skills of player"));
                    return;
                }
                responseAcquiredSkills.getBody()
                        .stream()
                        .map(SkillEntity::getSkill)
                        .forEach(s -> skillController.remove(currentPlayer.getUuid(), s.name()));

            });
            add(resetSkills);
        } else {
            removeAll();
            add(new Text("Can't find player"));
        }
    }

    private void createButtons() {
        List<Skill> allSkills = List.of(Skill.values());
        ResponseEntity<List<SkillEntity>> responseAcquiredSkills = skillController.findBy(currentPlayer.getUuid());
        if (responseAcquiredSkills.getStatusCode() != HttpStatus.OK || responseAcquiredSkills.getBody() == null) {
            add(new Text("Can't get skills of player"));
            return;
        }
        List<Skill> acquiredSkills = responseAcquiredSkills.getBody()
                .stream()
                .map(SkillEntity::getSkill)
                .toList();

        allSkills.stream()
                .map(skill -> {
                    Button button = new Button(skill.name());
                    button.setEnabled(!acquiredSkills.contains(skill));
                    button.addClickListener(e -> {
                        skillController.add(currentPlayer.getUuid(), skill.name());
                        button.setEnabled(false);
                    });
                    return button;
                })
                .forEach(this::add);
    }
}