package ui.skilltree.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SkillConfig {
    private int id;
    private String label;
    private int x;
    private int y;
    private String description;
    private String skillName;
}
