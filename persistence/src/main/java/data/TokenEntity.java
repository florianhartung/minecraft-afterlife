package data;

import generator.TokenGenerator;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "SkillpageTokens")
public class TokenEntity {
    /**
     * Code used in the url to the skill page in the token field<br>
     * This token is in the form of 8 alphanumeric characters
     **/
    @Id
    @GeneratedValue(generator = TokenGenerator.name)
    @GenericGenerator(name = TokenGenerator.name, strategy = "generator.TokenGenerator")
    private String tokenCode;

    private String playerUUID;
}
