package config;

import lombok.Getter;

public enum ConfigType {
    DEFAULT("config.yml"),
    SKILL_BLOCKS("skillblocks.yml"),
    ADVANCEMENT_PARENTS("advancement-parents.yml"),
    SKILLS("skills.yml"),
    PLAYERS("player-settings.yml"),
    PLAY_TIMES("play-times.yml"),
    PROJECT_START("project-start.yml");

    @Getter
    private final String filename;

    ConfigType(String filename) {
        this.filename = filename;
    }
}
