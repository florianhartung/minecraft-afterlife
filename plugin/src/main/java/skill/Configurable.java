package skill;

import org.bukkit.configuration.ConfigurationSection;

public interface Configurable {
    void setConfig(ConfigurationSection config);

    String getConfigPath();
}
