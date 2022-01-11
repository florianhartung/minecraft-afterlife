package ui.skilltree;

import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.EventHandler;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Tag("skill-tree")
@JsModule("./src/skill-tree.js")
public class SkillTree extends PolymerTemplate<SkillTreeModel> {

    private List<SkillNode> skillNodes;
    private List<SkillConnection> skillConnections;

    public SkillTree() {
        skillNodes = new ArrayList<>();
        skillConnections = new LinkedList<>();
        getModel().setSkillNodes(skillNodes);
        getModel().setSkillConnections(skillConnections);
    }

    @EventHandler
    public void handleSkillClick() {
        System.out.println("Skill clicked");
    }

    public void setSkillNodes(List<SkillNode> skillNodes) {
        getModel().setSkillNodes(skillNodes);
        this.skillNodes = skillNodes;
    }

    public List<SkillNode> getSkillNodes() {
        return skillNodes;
    }

    public void connect(int from, int to) {
        SkillNode fromNode = skillNodes.get(from);
        SkillNode toNode = skillNodes.get(to);
        skillConnections.add(new SkillConnection(fromNode.getX(), fromNode.getY(), toNode.getX(), toNode.getY()));
    }

    public void build() {
        getModel().setSkillNodes(skillNodes);
        getModel().setSkillConnections(skillConnections);
    }

    public List<SkillConnection> getSkillConnections() {
        return skillConnections;
    }
}
