package config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;

public class PlayerDataConfig {

    static {
        ConfigurationSerialization.registerClass(PlayerData.class);
    }

    public static PlayerData get(Player player) {
        FileConfiguration fileConfiguration = Config.get(ConfigType.PLAYERS);

        PlayerData data = fileConfiguration.getObject(player.getUniqueId().toString(), PlayerData.class);
        if (data == null) {
            data = new PlayerData();
            set(player, data);
        }
        return data;
    }

    public static void set(Player player, PlayerData data) {
        FileConfiguration fileConfiguration = Config.get(ConfigType.PLAYERS);
        fileConfiguration.set(player.getUniqueId().toString(), data);
        Config.save(ConfigType.PLAYERS, fileConfiguration);
    }

    @Getter
    @Setter
    public static class PlayerData implements ConfigurationSerializable {
        boolean hudEnabled = true;
        boolean radarEnabled = true;

        public PlayerData() {
        }

        public PlayerData(Map<String, Object> data) {
            Optional.ofNullable(data.get("hudEnabled")).ifPresent(hudEnabled -> this.hudEnabled = (boolean) hudEnabled);
            Optional.ofNullable(data.get("radarEnabled")).ifPresent(radarEnabled -> this.radarEnabled = (boolean) radarEnabled);
        }

        @Override
        public Map<String, Object> serialize() {
            return Map.of("hudEnabled", hudEnabled, "radarEnabled", radarEnabled);
        }
    }
}
