package ui.skilltree;

import com.vaadin.flow.templatemodel.TemplateModel;

import java.util.List;

@SuppressWarnings("deprecation")
public interface SkillTreeModel extends TemplateModel {
    List<SkillNode> getSkillNodes();

    void setSkillNodes(List<SkillNode> skillNodes);

    List<SkillConnection> getSkillConnections();

    void setSkillConnections(List<SkillConnection> skillConnections);

    void setSkillpoints(int skillpoints);
}
