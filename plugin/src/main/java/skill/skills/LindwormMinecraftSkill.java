package skill.skills;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import skill.generic.MinecraftSkill;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;

@Configurable("lindworm")
public class LindwormMinecraftSkill extends MinecraftSkill {

    @ConfigValue("lava-damage-modifier")
    private static double LAVA_DAMAGE_MODIFIER;
    @ConfigValue("water-damage-tick")
    private static double WATER_DAMAGE_TICK;
    @ConfigValue("damage-amplifiers.nether")
    private static double DAMAGE_AMPLIFIERS_NETHER;
    @ConfigValue("damage-amplifiers.burning")
    private static double DAMAGE_AMPLIFIERS_BURNING;
    @ConfigValue("damage-amplifiers.rain")
    private static double DAMAGE_AMPLIFIERS_RAIN;
    @ConfigValue("damage-amplifiers.water")
    private static double DAMAGE_AMPLIFIERS_WATER;
    @ConfigValue("damage-amplifiers.minimum-damage")
    private static double DAMAGE_AMPLIFIERS_MINIMUM_DAMAGE;


    @InjectPlugin(postInject = "startWaterDamageTimer")
    private Plugin plugin;


    @EventHandler(priority = EventPriority.LOW)
    public void onFireDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player) || !isActiveFor(player)) {
            return;
        }

        switch (e.getCause()) {
            case FIRE, FIRE_TICK -> e.setCancelled(true);
            case LAVA -> e.setDamage(e.getDamage() * LAVA_DAMAGE_MODIFIER);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDealDamage(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player player) || !isActiveFor(player)) {
            return;
        }

        double combinedDamageAmplifier = 1.0d;

        boolean isInNether = player.getWorld().getName().endsWith("_nether");
        if (isInNether) {
            combinedDamageAmplifier += DAMAGE_AMPLIFIERS_NETHER;
        }

        if (player.getFireTicks() > 0) {
            combinedDamageAmplifier += DAMAGE_AMPLIFIERS_BURNING;
        }

        boolean isInRain = player.getWorld().getHighestBlockAt(player.getLocation()).getY() < player.getLocation().getY() && !player.getWorld().isClearWeather();
        Biome biome = player.getLocation().getBlock().getBiome();
        boolean isInBiomeWithoutRain = biome == Biome.DESERT || biome == Biome.SAVANNA || biome == Biome.SAVANNA_PLATEAU || biome == Biome.WINDSWEPT_SAVANNA;
        if (isInRain && !isInBiomeWithoutRain && !isInNether) {
            combinedDamageAmplifier += DAMAGE_AMPLIFIERS_RAIN;
        }

        if (player.getLocation().getBlock().getType() == Material.WATER || player.getEyeLocation().getBlock().getType() == Material.WATER) {
            combinedDamageAmplifier += DAMAGE_AMPLIFIERS_WATER;
        }

        e.setDamage(Math.max(e.getDamage() * combinedDamageAmplifier, e.getDamage() * DAMAGE_AMPLIFIERS_MINIMUM_DAMAGE));
    }

    @SuppressWarnings("unused")
    private void startWaterDamageTimer() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::waterDamageTick, 0, 20);
    }

    private void waterDamageTick() {
        Bukkit.getOnlinePlayers().stream().filter(this::isActiveFor).filter(player -> player.getEyeLocation().getBlock().isLiquid()).forEach(player -> {
            player.damage(WATER_DAMAGE_TICK);
            player.getWorld().spawnParticle(Particle.SMOKE_NORMAL, player.getLocation().add(0, 0.4, 0), 20, 0.3, 0.4, 0.3, 0.01);
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, SoundCategory.PLAYERS, 0.4f, 1.8f);
        });
    }
}
