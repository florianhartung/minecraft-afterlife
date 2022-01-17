package ui.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import controller.PlayerController;
import controller.SkillController;
import data.PlayerEntity;
import data.SkillEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ui.skilltree.SkillClickEvent;
import ui.skilltree.SkillNode;
import ui.skilltree.SkillTree;
import ui.skilltree.config.SkillTreeConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@Route("skills")
@SpringComponent
@UIScope
public class SkillsView extends VerticalLayout implements HasUrlParameter<String> {

    private final SkillTreeConfiguration skillTreeConfiguration;
    private final PlayerController playerController;
    private final SkillController skillController;

    private final SkillTree skillTree;
    private Button resetSkillsButton;

    private PlayerEntity currentPlayer;

    public SkillsView(SkillTreeConfiguration skillTreeConfiguration, PlayerController playerController, SkillController skillController) {
        this.skillTreeConfiguration = skillTreeConfiguration;
        this.playerController = playerController;
        this.skillController = skillController;
        init();

        skillTree = new SkillTree();
        loadSkillTree();
        skillTree.addSkillClickListener(this::skillClicked);

        init();

        add(skillTree, resetSkillsButton);
    }

    private void loadAcquiredSkills() {
        ResponseEntity<List<SkillEntity>> responseAcquiredSkills = skillController.findBy(currentPlayer.getUuid());
        if (responseAcquiredSkills.getStatusCode() == HttpStatus.OK && responseAcquiredSkills.getBody() != null) {
            List<SkillEntity> acquiredSkills = responseAcquiredSkills.getBody();
            skillTree.getSkillNodes().forEach(skillNode -> {
                for (SkillEntity skillEntity : acquiredSkills) {
                    if (skillNode.getSkill() != null) { // start node does not have a skill associated to it
                        if (skillEntity.getSkill().name().equals(skillNode.getSkill())) {
                            skillNode.setUnlocked(true);
                            return;
                        }
                    }
                }
                skillNode.setUnlocked(false);
            });
            skillTree.build();
        }

    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        Map<String, List<String>> parametersMap = queryParameters.getParameters();

        Optional<String> optionalPlayerUUID = Optional.ofNullable(parametersMap.get("uuid"))
                .flatMap(uuid -> uuid
                        .stream()
                        .findFirst());
        if (optionalPlayerUUID.isEmpty()) {
            error("No UUID given");
            return;
        }
        reloadData(optionalPlayerUUID.get());
    }

    public void reloadData(String playerUUID) {
        Optional<PlayerEntity> optionalPlayerEntity = Optional.ofNullable(playerController.getByUUID(playerUUID).getBody());
        if (optionalPlayerEntity.isEmpty()) {
            error("Player with UUID " + playerUUID + " not found");
            return;
        }
        currentPlayer = optionalPlayerEntity.get();
        loadAcquiredSkills();
        skillTree.setSkillpoints(currentPlayer.getSkillPoints());
        skillTree.build();
    }

    private void error(String message) {
        removeAll();
        add(new Label(message));
    }

    private void init() {
        setWidth("100%");
        setHeight("100%");
        setPadding(false);
        setMargin(false);

        resetSkillsButton = new Button("Reset skills");
        resetSkillsButton.getStyle()
                .set("position", "absolute")
                .set("top", "0")
                .set("right", "0")
                .set("color", "white")
                .set("font-size", "1.5em")
                .set("background-color", "#fff9");
        resetSkillsButton.addClickListener(event -> resetSkills());
    }

    private void loadSkillTree() {
        List<SkillNode> skillNodes = Stream.of(skillTreeConfiguration.getSkills())
                .map(node -> new SkillNode(node.getLabel(), node.getId(), node.getX(), node.getY(), false, false, node.getDescription(), node.getSkillName(), false))
                .toList();
        skillNodes.get(0).setStart(true);

        skillTree.setSkillNodes(skillNodes);

        Stream.of(skillTreeConfiguration.getConnections())
                .forEach(connection -> skillTree.connect(connection.getFrom(), connection.getTo()));
        skillTree.build();
    }

    private void resetSkills() {
        ResponseEntity<List<SkillEntity>> response = skillController.removeAll(currentPlayer.getUuid());
        if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
            reloadData(currentPlayer.getUuid());
            skillTree.build();
        }
    }

    private void skillClicked(SkillClickEvent event) {
        if (event.getTarget().isSkillable()) {
            ResponseEntity<SkillEntity> response = skillController.add(currentPlayer.getUuid(), event.getTarget().getSkill());
            if (response.getStatusCode() == HttpStatus.OK) {
                event.getTarget().setUnlocked(true);
                reloadData(currentPlayer.getUuid());
            }
        }
    }
}
