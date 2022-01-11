package ui.view;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.apache.commons.lang3.tuple.Pair;
import ui.skilltree.SkillNode;
import ui.skilltree.SkillTree;

import java.util.List;


@Route("skills")
@SpringComponent
@UIScope
public class SkillsView extends VerticalLayout {

    private static final List<SkillNode> skillNodes = List.of(
            new SkillNode("Start", 0, 500, 500, true, "Dies ist der Start. Du kannst Skills der Reihenfolge nach, wie sie verbunden sind, freischalten."),
            new SkillNode("Barriere", 1, 600, 380, true, "Absorption bei 1 Minute ohne Schaden (3 Herzen)"),
            new SkillNode("Blutrausch", 2, 350, 300, false, "Töten gibt Schnelligkeit für ein paar Sekunden"),
            new SkillNode("Dauerläufer", 3, 800, 780, false, "Desto länger du am stück sprintest desto schneller wirst du"));
    private static final List<Pair<Integer, Integer>> skillConnections = List.of(
            Pair.of(0, 1),
            Pair.of(0, 2),
            Pair.of(0,3));


    public SkillsView() {
        setWidth("100%");
        setHeight("100%");
        setPadding(false);
        setMargin(false);

        SkillTree skillTree = new SkillTree();
        skillTree.setSkillNodes(skillNodes);
        skillConnections.forEach(pair -> skillTree.connect(pair.getLeft(), pair.getRight()));
        skillTree.build();

        add(skillTree);
    }
}
