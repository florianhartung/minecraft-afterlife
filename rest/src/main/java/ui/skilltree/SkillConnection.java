package ui.skilltree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SkillConnection {
    private int fromId;
    private int toId;
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private boolean unlocked;
    private boolean skillable;
}
