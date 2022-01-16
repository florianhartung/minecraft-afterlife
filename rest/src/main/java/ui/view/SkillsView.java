package ui.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import ui.skilltree.config.SkillTreeConfiguration;
import ui.skilltree.SkillNode;
import ui.skilltree.SkillTree;

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
        load();
        skillTree.build();

        add(skillTree);
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
}
