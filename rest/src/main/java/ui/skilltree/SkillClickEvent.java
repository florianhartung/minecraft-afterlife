package ui.skilltree;

import com.vaadin.flow.component.ComponentEvent;
import lombok.Getter;

@Getter
public class SkillClickEvent extends ComponentEvent<SkillTree> {
    private final SkillNode target;

    public SkillClickEvent(SkillTree source, boolean fromClient, SkillNode target) {
        super(source, fromClient);
        this.target = target;
    }
}
