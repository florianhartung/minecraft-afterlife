package config;

import lombok.Getter;

public enum ConfigType {
    DEFAULT("config.yml"),
    SKILL_BLOCKS("skillblocks.yml"),
    ADVANCEMENT_PARENTS("advancement-parents.yml"),
    SKILLS("skills.yml");

    @Getter
    private final String filename;

    ConfigType(String filename) {
        this.filename = filename;
    }
}
