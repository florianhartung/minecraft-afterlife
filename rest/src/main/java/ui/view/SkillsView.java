package ui.view;

import com.google.gson.Gson;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import data.Skill;
import ui.skilltree.config.ConnectionConfig;
import ui.skilltree.config.SkillConfig;
import ui.skilltree.config.SkillTreeConfiguration;
import ui.skilltree.SkillConnection;
import ui.skilltree.SkillNode;
import ui.skilltree.SkillTree;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


@Route("skills")
@SpringComponent
@UIScope
public class SkillsView extends VerticalLayout {

    private final SkillTreeConfiguration skillTreeConfiguration;

    private final SkillTree skillTree;

    public SkillsView(SkillTreeConfiguration skillTreeConfiguration) {
        this.skillTreeConfiguration = skillTreeConfiguration;

        setWidth("100%");
        setHeight("100%");
        setPadding(false);
        setMargin(false);

        skillTree = new SkillTree();

        // use to load from file
        load();

        // use to load empty skills from {@link Skill} enum
        //loadFromSkillsEnum();

        skillTree.build();

        Button saveButton = new Button("Save");
        saveButton.getStyle().set("position", "absolute");
        saveButton.getStyle().set("top", "0");
        saveButton.getStyle().set("right", "0");
        saveButton.addClickListener(buttonClickEvent -> System.out.println(skillTreeToJson()));

        add(skillTree, saveButton);
    }

    @Deprecated
    private void loadFromSkillsEnum() {
        List<SkillNode> skillNodes = new LinkedList<>();
        Skill[] allSkills = Skill.values();
        SkillNode startNode = new SkillNode("Start", 0, 10, 10, false, "EMPTY", null, true);
        skillNodes.add(startNode);
        for (int i = 1; i <= allSkills.length; i++) {
            double a = i * (2 * Math.PI) / 4.5d;
            double dist = 85 * a / (2 * Math.PI);
            int x = (int) (dist * Math.sin(a));
            int y = (int) (dist * Math.cos(a));


            Skill skill = allSkills[i - 1];
            SkillNode skillNode = new SkillNode(skill.name(), i , x, y, false, "EMPTY", skill.name(), false);
            skillNodes.add(skillNode);
        }
        skillTree.setSkillNodes(skillNodes);
    }

    private void load() {
        List<SkillNode> skillNodes = Stream.of(skillTreeConfiguration.getSkills())
                .map(node -> new SkillNode(node.getLabel(), node.getId(), node.getX(), node.getY(), false, node.getDescription(), node.getSkillName(), false))
                .toList();
        skillNodes.get(0).setStart(true);

        skillTree.setSkillNodes(skillNodes);

        Stream.of(skillTreeConfiguration.getConnections())
                .forEach(connection -> skillTree.connect(connection.getFrom(), connection.getTo()));
    }

    private String skillTreeToJson() {
        List<SkillNode> skills = skillTree.getSkillNodes();
        List<SkillConnection> connections = skillTree.getSkillConnections();

        SkillConfig[] skillConfigs = skills.stream()
                .map(skillNode -> new SkillConfig(skillNode.getId(), skillNode.getLabel(), skillNode.getX(), skillNode.getY(), skillNode.getDescription(), skillNode.getSkill()))
                .toArray(SkillConfig[]::new);

        ConnectionConfig[] connectionConfigs = connections.stream()
                .map(connection -> new ConnectionConfig(connection.getFromId(), connection.getToId()))
                .toArray(ConnectionConfig[]::new);
        SkillTreeConfiguration skillTreeConfiguration = new SkillTreeConfiguration(skillConfigs, connectionConfigs);

        return new Gson().toJson(skillTreeConfiguration);
    }
}
