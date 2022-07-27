package skill.skills;

import hud.HudManager;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;
import skill.injection.InjectTimer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configurable("sprint-burst")
public class SprintBurstMinecraftSkill extends MinecraftSkill {
    @ConfigValue("sprint-duration")
    private static int SPRINT_DURATION;
    @ConfigValue("sprint-amplifier")
    private static int SPRINT_AMPLIFIER;
    @ConfigValue("sprint-cooldown")
    private static int SPRINT_COOLDOWN;
    @InjectPlugin
    private Plugin plugin;
    @InjectTimer(durationField = "SPRINT_COOLDOWN", hudEntry = HudManager.HudEntry.SPRINT_BURST)
    private MinecraftSkillTimer cooldownTimer;
    @InjectTimer(durationField = "SPRINT_DURATION", onTimerFinished = "cancelParticleTimer")
    private MinecraftSkillTimer particleTimer;

    private final Map<UUID, Integer> sprintParticleTasks = new HashMap<>();

    @EventHandler
    public void onMove(PlayerToggleSprintEvent e) {
        if (!isActiveFor(e.getPlayer())) {
            return;
        }

        if (!e.isSprinting()) {
            cooldownTimer.start(e.getPlayer());
            return;
        }

        if (!cooldownTimer.isActive(e.getPlayer())) {
            startSprintEffect(e.getPlayer());
        } else {
            cooldownTimer.cancel(e.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!isActiveFor(e.getPlayer())) {
            return;
        }
        if (sprintParticleTasks.containsKey(e.getPlayer().getUniqueId())) {
            cancelParticleTimer(e.getPlayer());
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!isActiveFor(e.getEntity())) {
            return;
        }

        if (sprintParticleTasks.containsKey(e.getEntity().getUniqueId())) {
            cancelParticleTimer(e.getEntity());
        }
    }

    private void startSprintEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, SPRINT_DURATION, SPRINT_AMPLIFIER, false, false, true));

        Particle.DustTransition particleOptions = new Particle.DustTransition(Color.fromRGB(255, 255, 255), Color.fromRGB(0, 200, 255), 1);
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, player.getLocation().add(0, 0.2, 0), 15, 0.1, 0.15, 0.1, particleOptions);
        }, 0, 2);
        sprintParticleTasks.put(player.getUniqueId(), taskId);
        particleTimer.start(player);
    }

    public void cancelParticleTimer(Player player) {
        HudManager.set(player, HudManager.HudEntry.SPRINT_BURST, 0);
        UUID uuid = player.getUniqueId();
        if (sprintParticleTasks.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(sprintParticleTasks.get(uuid));
            sprintParticleTasks.remove(uuid);
        }
    }

    @Override
    public void apply(Player player) {
        super.apply(player);

        HudManager.set(player, HudManager.HudEntry.SPRINT_BURST, 7);
    }

    @Override
    public void remove(Player player) {
        super.remove(player);
        cooldownTimer.cancel(player);
        HudManager.remove(player, HudManager.HudEntry.SPRINT_BURST);
    }
}
