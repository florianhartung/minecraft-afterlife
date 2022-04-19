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
    private boolean skillable;
    private String description;
    private String skill;
    private boolean start;
    private String color;
    private String icon;
}
