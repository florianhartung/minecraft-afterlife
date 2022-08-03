package skill.skills;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.Plugin;
import skill.generic.AttributeMinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Configurable("endurance")
public class EnduranceMinecraftSkill extends AttributeMinecraftSkill {

    @ConfigValue("ticks-per-stage")
    private static int TICKS_PER_STAGE;
    @ConfigValue("speed-per-stage")
    private static double SPEED_PER_STAGE;
    @InjectPlugin(postInject = "startTickTimer")
    private static Plugin plugin;

    private final Map<UUID, PlayerEnduranceState> playerEnduranceStates = new HashMap<>();

    public EnduranceMinecraftSkill() {
        super(Attribute.GENERIC_MOVEMENT_SPEED, UUID.fromString("372e4463-5c43-4b30-a320-ecccfea7769c"), "Endurance skill", 0, AttributeModifier.Operation.ADD_NUMBER, false);
    }


    @SuppressWarnings("unused")
    public void startTickTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 0, 1);
    }

    private void tick() {
        playerEnduranceStates.values().forEach(PlayerEnduranceState::tick);
    }

    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (!isActiveFor(e.getPlayer()) || e.getPlayer().isSwimming()) {
            return;
        }

        if (!e.isSprinting()) {
            reset(e.getPlayer());
            return;
        }

        playerEnduranceStates.put(e.getPlayer().getUniqueId(), new PlayerEnduranceState(e.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        reset(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        reset(e.getEntity());
    }

    private void reset(Player player) {
        Optional.ofNullable(playerEnduranceStates.remove(player.getUniqueId())).ifPresent(PlayerEnduranceState::reset);
    }

    @Getter
    @Setter
    class PlayerEnduranceState {
        private int stage;
        private int ticksUntilNextStage;
        private Player player;

        public PlayerEnduranceState(Player player) {
            this.player = player;
            ticksUntilNextStage = TICKS_PER_STAGE;
            stage = 0;
        }

        private void tick() {
            ticksUntilNextStage -= 1;
            if (ticksUntilNextStage == 0) {
                stage += 1;
                ticksUntilNextStage = TICKS_PER_STAGE;
                EnduranceMinecraftSkill.this.setAttributeAmount(player, stage * SPEED_PER_STAGE);
            }
        }

        private void reset() {
            EnduranceMinecraftSkill.this.removeAttribute(player);
        }
    }
}
