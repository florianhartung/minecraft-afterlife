package skill.skills;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import performancereport.PerfReport;
import skill.generic.AttributeMinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;

@Configurable("werewolf")
public class WerewolfMinecraftSkill extends AttributeMinecraftSkill {
    private static final int UPDATE_DELAY = 40;

    @ConfigValue("speed-amount")
    private static double SPEED_AMOUNT;
    @ConfigValue("default-world-name")
    private static String DEFAULT_WORLD_NAME;
    @ConfigValue("additional-damage")
    private static double ADDITIONAL_DAMAGE;
    @ConfigValue("weakness-aura-amplifier")
    private static int WEAKNESS_AURA_AMPLIFIER;
    @ConfigValue("weakness-aura-range")
    private static double WEAKNESS_AURA_RANGE;

    @InjectPlugin(postInject = "startTickTimer")
    private Plugin plugin;

    private NightState nightState;

    public WerewolfMinecraftSkill() {
        super(Attribute.GENERIC_MOVEMENT_SPEED, UUID.fromString("1377514f-9e8e-47af-96df-c70eab130fcf"), "Werewolf skill", 0, AttributeModifier.Operation.ADD_NUMBER, false);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!isActiveFor(e.getPlayer())) {
            return;
        }

        removeEffects(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player) || !(e.getEntity() instanceof LivingEntity target) || e.isCancelled()) {
            return;
        }

        if (!isActiveFor(player)) {
            return;
        }

        if (!nightState.isNighttime() || nightState.getEffectStrength() == 0.0f) {
            return;
        }

        e.setDamage(e.getDamage() + ADDITIONAL_DAMAGE * nightState.getEffectStrength());

        if (nightState.getEffectStrength() == 1.0f) {
            target.getWorld().spawnParticle(Particle.BLOCK_CRACK, target.getLocation(), 10, 0.2, 0.3, 0.2, Material.REDSTONE_BLOCK.createBlockData());

        }
    }

    @SuppressWarnings("unused")
    public void startTickTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::update, 0, UPDATE_DELAY);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::showEffects, 0, 2);
    }

    private void showEffects() {
        PerfReport.startTimer("werewolf.effects");
        if (nightState == null) {
            PerfReport.endTimer("werewolf.effects");
            return;
        }

        if (!nightState.isNighttime() || nightState.getEffectStrength() == 0.0f) {
            PerfReport.endTimer("werewolf.effects");
            return;
        }

        int ashParticles = (int) (nightState.getEffectStrength() * 15);
        int redParticles = nightState.getMoonPhase() == 0 ? ((int) (nightState.getEffectStrength() * 3)) : 0;

        Consumer<Location> showParticlesAtLocation = loc -> {
            World world = loc.getWorld();

            assert world != null;
            world.spawnParticle(Particle.ASH, loc.add(0, 0.5, 0), ashParticles, 0.3, 0.5, 0.3);
            if (redParticles > 0) {
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, loc.add(0, 0.5, 0), redParticles, 4, 1.5, 4, new Particle.DustTransition(Color.fromRGB(255, 0, 0), Color.fromRGB(50, 0, 0), 2));
            }
        };

        Bukkit.getOnlinePlayers().stream().filter(this::isActiveFor).forEach(player -> {
            showParticlesAtLocation.accept(player.getLocation());
            if (Math.random() < 0.005)
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WOLF_GROWL, SoundCategory.PLAYERS, 0.3f, 0.8f);
        });
        PerfReport.endTimer("werewolf.effects");
    }

    private void update() {
        PerfReport.startTimer("werewolf.update");
        NightState newState = currentNightState();
        if (newState == null) {
            return;
        }

        if (!newState.equals(nightState)) {
            nightState = newState;
        }

        Bukkit.getOnlinePlayers().stream().filter(this::isActiveFor).forEach(player -> applyEffects(player, nightState));
        PerfReport.endTimer("werewolf.update");
    }

    private void applyEffects(Player player, NightState nightState) {
        if (!nightState.isNighttime() || nightState.getEffectStrength() == 0.0f) {
            removeEffects(player);
            return;
        }
        setAttributeAmount(player, nightState.getEffectStrength() * SPEED_AMOUNT);

        if (nightState.getEffectStrength() != 1.0f) {
            return;
        }
        player.getNearbyEntities(WEAKNESS_AURA_RANGE, WEAKNESS_AURA_RANGE, WEAKNESS_AURA_RANGE).stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).filter(target -> player.getLocation().distanceSquared(target.getLocation()) < WEAKNESS_AURA_RANGE * WEAKNESS_AURA_RANGE).forEach(target -> {
            if (target.getPotionEffect(PotionEffectType.WEAKNESS) == null) {
                target.playSound(player, Sound.ENTITY_WOLF_HOWL, 0.6f, 0.8f);
            }
            target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, UPDATE_DELAY + 20, WEAKNESS_AURA_AMPLIFIER, false, true, true));
        });
    }

    private void removeEffects(Player player) {
        setAttributeAmount(player, 0);
    }

    private NightState currentNightState() {
        World world = Bukkit.getWorld(DEFAULT_WORLD_NAME);
        if (world == null) {
            Bukkit.getLogger().log(Level.SEVERE, "[Afterlife] Could not find specified world " + DEFAULT_WORLD_NAME + " for werewolf skill! Using default 'world' instead.");
            return null;
        }

        boolean isNighttime = world.getTime() > 13200 && world.getTime() < 23000;
        int moonPhase = (int) (world.getFullTime() / 24000 % 8);
        float effectStrength = switch (moonPhase) {
            case 0 -> 1.0f;
            case 1, 7 -> 0.3f;
            case 2, 6 -> 0.15f;
            case 3, 5 -> 0.1f;
            default -> 0.0f;
        };

        return new NightState(isNighttime, moonPhase, effectStrength);
    }

    @AllArgsConstructor
    @Getter
    @Setter
    static class NightState {
        private boolean nighttime;
        private int moonPhase; // value from 0 - 7:   0->full moon   4->new moon
        private float effectStrength; // value from 0.0f - 1.0f with 1.0f being the maximum strength at full moon

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NightState that = (NightState) o;
            return nighttime == that.nighttime && moonPhase == that.moonPhase && Float.compare(that.effectStrength, effectStrength) == 0;
        }
    }
}
