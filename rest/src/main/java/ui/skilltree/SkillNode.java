package ui.skilltree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SkillNode {
    private String label;
    private int id;
    private int x;
    private int y;
    private boolean unlocked;
    private String description;
}
