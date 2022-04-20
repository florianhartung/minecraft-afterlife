package ui.skilltree;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.EventHandler;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("deprecation")
@Tag("skill-tree")
@JsModule("./src/skill-tree.js")
public class SkillTree extends PolymerTemplate<SkillTreeModel> {

    private List<SkillNode> skillNodes;
    private final List<SkillConnection> skillConnections;
    private int skillpoints;
    private final Runnable tokenValidityCheckRunnable;

    public SkillTree(Runnable tokenValidityCheckRunnable) {
        this.tokenValidityCheckRunnable = tokenValidityCheckRunnable;
        skillNodes = new ArrayList<>();
        skillConnections = new LinkedList<>();
        updateNodes();
        updateConnections();
    }

    @ClientCallable
    public void tokenCheck() {
        tokenValidityCheckRunnable.run();
    }

    @EventHandler
    public void handleSkillClick(@EventData("event.target.id") String idString) {
        int id = idFromSkillNodeId(idString).orElse(-1);
        if (id == -1) {
            return;
        }
        SkillNode target = getNodeById(id);

        fireEvent(new SkillClickEvent(this, false, target));
    }

    public void setSkillpoints(int skillpoints) {
        this.skillpoints = skillpoints;
        getModel().setSkillpoints(skillpoints);
    }

    @SuppressWarnings("UnusedReturnValue")
    public Registration addSkillClickListener(ComponentEventListener<SkillClickEvent> listener) {
        return addListener(SkillClickEvent.class, listener);
    }

    public void setSkillNodes(List<SkillNode> skillNodes) {
        this.skillNodes = skillNodes;
        updateNodes();
    }

    public void connect(int from, int to) {
        Optional<SkillConnection> connection = skillConnections.stream()
                .filter(skillConnection -> (skillConnection.getFromId() == from && skillConnection.getToId() == to) || (skillConnection.getFromId() == to && skillConnection.getToId() == from))
                .findFirst();
        if (connection.isPresent()) {
            skillConnections.remove(connection.get());
        } else {
            SkillNode fromNode = getNodeById(from);
            SkillNode toNode = getNodeById(to);
            boolean skillable = fromNode.isSkillable() || toNode.isSkillable();
            skillConnections.add(new SkillConnection(fromNode.getId(), toNode.getId(), fromNode.getX(), fromNode.getY(), toNode.getX(), toNode.getY(), false, skillable));
        }
    }

    public void build() {
        updateNodes();
        updateConnections();
    }

    public void updateNodes() {
        skillNodes.forEach(skillNode -> {
            boolean skillable = skillConnections.stream()
                    .anyMatch(skillConnection -> {
                        SkillNode fromNode = getNodeById(skillConnection.getFromId());
                        SkillNode toNode = getNodeById(skillConnection.getToId());

                        if (skillNode.getId() == fromNode.getId()) {
                            return toNode.isUnlocked() || toNode.isStart();
                        } else if (skillNode.getId() == toNode.getId()) {
                            return fromNode.isUnlocked() || fromNode.isStart();
                        }
                        return false;
                    });
            skillNode.setSkillable(skillpoints > 0 && !skillNode.isUnlocked() && !skillNode.isStart() && skillable);
        });

        getModel().setSkillNodes(skillNodes);
    }

    public void updateConnections() {
        skillConnections.forEach(skillConnection -> {
            SkillNode fromNode = getNodeById(skillConnection.getFromId());
            SkillNode toNode = getNodeById(skillConnection.getToId());

            skillConnection.setX1(fromNode.getX());
            skillConnection.setY1(fromNode.getY());
            skillConnection.setX2(toNode.getX());
            skillConnection.setY2(toNode.getY());

            boolean unlocked = (fromNode.isUnlocked() || fromNode.isStart()) && (toNode.isUnlocked() || toNode.isStart());
            boolean skillable = !unlocked && (fromNode.isUnlocked() || fromNode.isStart() || toNode.isStart() || toNode.isUnlocked());
            skillConnection.setUnlocked(unlocked);
            skillConnection.setSkillable(skillpoints > 0 && skillable);
        });
        getModel().setSkillConnections(skillConnections);
    }

    public List<SkillNode> getSkillNodes() {
        return skillNodes;
    }

    private static Optional<Integer> idFromSkillNodeId(String idString) {
        return Optional.of(idString.split("-"))
                .filter(splitId -> splitId.length == 2)
                .map(splitId -> Integer.parseInt(splitId[1]));
    }

    private SkillNode getNodeById(int id) {
        return skillNodes.stream()
                .filter(skillNode -> skillNode.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Can't find skill node for id" + id));
    }
}
