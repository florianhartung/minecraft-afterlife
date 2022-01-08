package data;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "AcquiredSkills")
public class SkillEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String uuid;
    private Skill skill;
}
