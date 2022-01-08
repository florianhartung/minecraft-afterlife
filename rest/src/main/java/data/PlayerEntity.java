package data;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Data
@Entity
@Table(name = "Players")
public class PlayerEntity {
    @Id
    private String uuid;
    private int skillPoints;
}
