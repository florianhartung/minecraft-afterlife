package skill.skills;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skill.generic.MinecraftSkill;
import skill.generic.MinecraftSkillTimer;
import skill.injection.ConfigValue;
import skill.injection.Configurable;
import skill.injection.InjectPlugin;
import skill.injection.InjectTimer;

import java.util.*;
import java.util.stream.Stream;

@Configurable("dwarf")
public class DwarfMinecraftSkill extends MinecraftSkill {
    private static final int TOTAL_MULTIPLIER = 3;
    private static final Integer[] STAGE_BLOCK_COUNTS = Stream.of(0, 30, 180, 580, 1000, 2000, 3000).map(i -> i * TOTAL_MULTIPLIER).toArray(Integer[]::new);
    private static final List<Material> FAKE_BLOCKS = List.of(Material.DIAMOND_ORE, Material.GOLD_ORE, Material.EMERALD_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE_EMERALD_ORE, Material.ANCIENT_DEBRIS);

    @ConfigValue("effect-duration")
    private int EFFECT_DURATION; // in ticks
    @ConfigValue("effect-duration-per-stage")
    private int EFFECT_DURATION_PER_STAGE;
    @ConfigValue("exhaustion-duration-per-stage")
    private int EXHAUSTION_DURATION_PER_STAGE;
    @ConfigValue("exhaustion-mining-fatigue-amplifier")
    private int EXHAUSTION_MINING_FATIGUE_AMPLIFIER;
    @InjectTimer(durationField = "EFFECT_DURATION", onTimerFinished = "resetMinedBlocks")
    private MinecraftSkillTimer comboResetTimer;
    @InjectTimer(onTimerFinished = "removeMiningFatigue")
    private MinecraftSkillTimer exhaustionTimer;
    @InjectPlugin
    private Plugin plugin;

    private final Random random = new Random();
    private final Map<UUID, MiningProgress> playerProgresses = new HashMap<>();

    @EventHandler
    public void onDisable(PluginDisableEvent e) {
        if (e.getPlugin().getName().equals(plugin.getName())) {
            exhaustionTimer.cancelAll().forEach(uuid -> Optional.ofNullable(Bukkit.getPlayer(uuid)).ifPresent(player -> resetMinedBlocks(player, false)));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }

        Player player = e.getPlayer();
        if (isActiveFor(player) && !exhaustionTimer.isActive(player) && !isUsingSilkTouch(player)) {
            int blockValue = getBlockValue(e.getBlock().getType());
            if (blockValue == 1) {
                addMinedBlock(player);
            } else if (blockValue > 1) {
                addMinedBlocks(player, blockValue);
            }
        }
    }

    private boolean isUsingSilkTouch(Player player) {
        return Optional.of(player.getInventory().getItemInMainHand())
                .filter(item -> item.getType() != Material.AIR)
                .map(item -> item.getEnchantments().containsKey(Enchantment.SILK_TOUCH))
                .orElse(false);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if (isActiveFor(e.getPlayer())) {
            resetMinedBlocks(e.getPlayer(), true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Optional.ofNullable(playerProgresses.remove(e.getEntity().getUniqueId())).ifPresent(MiningProgress::reset);
    }

    @EventHandler
    public void onMilkConsume(PlayerItemConsumeEvent e) {
        if (e.isCancelled()) {
            return;
        }

        if (e.getItem().getType() != Material.MILK_BUCKET) {
            return;
        }

        if (!exhaustionTimer.isActive(e.getPlayer())) {
            return;
        }

        int remainingExhaustionDuration = Optional.ofNullable(e.getPlayer().getPotionEffect(PotionEffectType.SLOW_DIGGING)).map(PotionEffect::getDuration).orElse(0);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, remainingExhaustionDuration, EXHAUSTION_MINING_FATIGUE_AMPLIFIER, true, false, true));
        });
    }

    private int getBlockValue(Material material) {
        return switch (material) {
            case DIRT, GRAVEL -> 1;
            case STONE, GRANITE, DIORITE, ANDESITE, CALCITE, TUFF, MOSSY_COBBLESTONE, COBBLESTONE, DEEPSLATE -> 2;
            case DRIPSTONE_BLOCK, POINTED_DRIPSTONE, COAL_ORE, IRON_ORE, COPPER_ORE -> 5;
            case OBSIDIAN, DEEPSLATE_COAL_ORE, DEEPSLATE_IRON_ORE, DEEPSLATE_COPPER_ORE, GOLD_ORE, REDSTONE_ORE, LAPIS_ORE -> 8;
            case DEEPSLATE_GOLD_ORE, DEEPSLATE_REDSTONE_ORE, DEEPSLATE_LAPIS_ORE, EMERALD_ORE -> 12;
            case DEEPSLATE_EMERALD_ORE, DIAMOND_ORE -> 20;
            case DEEPSLATE_DIAMOND_ORE -> 30;
            default -> 0;
        };
    }

    private int getCurrentEffectDuration(int stage) {
        return EFFECT_DURATION + EFFECT_DURATION_PER_STAGE * stage;
    }

    private void addMinedBlocks(Player player, int count) {
        for (int i = 0; i < count; i++) {
            addMinedBlock(player);
        }
    }

    private void addMinedBlock(Player player) {
        MiningProgress miningProgress = playerProgresses.computeIfAbsent(player.getUniqueId(), id -> {
            MiningProgress newMiningProgress = new MiningProgress(player);
            newMiningProgress.showBossBar(player);
            return newMiningProgress;
        });

        miningProgress.advance();
        comboResetTimer.start(player, getCurrentEffectDuration(miningProgress.getStage()));

        applyEffects(player, miningProgress);
    }

    private void applyEffects(Player player, MiningProgress miningProgress) {
        Map<PotionEffectType, Integer> effectAmplifiers = new HashMap<>();

        List<Runnable> stageEffects = List.of(() -> effectAmplifiers.put(PotionEffectType.FAST_DIGGING, 0), () -> effectAmplifiers.put(PotionEffectType.NIGHT_VISION, 0), () -> {
            effectAmplifiers.put(PotionEffectType.HUNGER, 0);
            effectAmplifiers.put(PotionEffectType.SPEED, 0);
        }, () -> {
            effectAmplifiers.put(PotionEffectType.HUNGER, 2);
            effectAmplifiers.put(PotionEffectType.FAST_DIGGING, 1);
        }, () -> {
            effectAmplifiers.remove(PotionEffectType.NIGHT_VISION);
            effectAmplifiers.put(PotionEffectType.HUNGER, 3);
            effectAmplifiers.put(PotionEffectType.DARKNESS, 0);
            effectAmplifiers.put(PotionEffectType.FAST_DIGGING, 2);
        }, () -> {
            effectAmplifiers.put(PotionEffectType.FAST_DIGGING, 3);
            effectAmplifiers.put(PotionEffectType.CONFUSION, 0);
            effectAmplifiers.put(PotionEffectType.WITHER, 1);
        });
        int stage = miningProgress.getStage();
        if (stage > 0) {
            stageEffects.subList(0, stage).forEach(Runnable::run);
        }

        effectAmplifiers.entrySet().stream().filter(entry -> entry.getValue() != null).forEach(entry -> {
            int effectDuration = getCurrentEffectDuration(stage);
            if (entry.getKey() == PotionEffectType.NIGHT_VISION) {
                effectDuration += 10 * 20; // add 10 seconds because for this effect the screen starts flickering at a low duration
            }

            player.addPotionEffect(new PotionEffect(entry.getKey(), effectDuration, entry.getValue(), true, false, true));

        });

        if (stage == STAGE_BLOCK_COUNTS.length - 1) {
            if (Math.random() < 0.01) {
                spawnFakeBlock(player);
            }
        }

        double threshold = 0.0d;
        if (stage == STAGE_BLOCK_COUNTS.length - 1) {
            threshold = 0.01d;
        } else if (stage == STAGE_BLOCK_COUNTS.length - 2) {
            threshold = 0.005d;
        }
        if (threshold > 0.0d) {
            if (Math.random() < threshold) {
                playRandomCaveSound(player);
            }
        }
    }

    private void spawnFakeBlock(Player player) {
        Material randomMaterial = FAKE_BLOCKS.get(random.nextInt(FAKE_BLOCKS.size()));
        player.spawnParticle(Particle.BLOCK_MARKER, player.getLocation(), 2, 6, 4, 6, randomMaterial.createBlockData());
    }

    private void playRandomCaveSound(Player player) {
        player.playSound(randomOffset(player.getLocation(), new Location(null, 10, 5, 10)), Sound.AMBIENT_CAVE, SoundCategory.AMBIENT, 0.3f, 1.0f);
    }

    private Location randomOffset(Location from, Location maximumOffset) {
        double dx = (Math.random() - 0.5d) * maximumOffset.getX();
        double dy = (Math.random() - 0.5d) * maximumOffset.getY();
        double dz = (Math.random() - 0.5d) * maximumOffset.getZ();

        return from.add(dx, dy, dz);
    }

    private void resetMinedBlocks(Player player) {
        resetMinedBlocks(player, true);
    }

    private void resetMinedBlocks(Player player, boolean exhaustion) {
        Optional.ofNullable(playerProgresses.remove(player.getUniqueId())).ifPresent(miningProgress -> {
            if (exhaustion) {
                int exhaustionDuration = miningProgress.getStage() * EXHAUSTION_DURATION_PER_STAGE;
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, exhaustionDuration, 1, true, false, true));
                exhaustionTimer.start(player, exhaustionDuration);
            }
            miningProgress.reset();
        });
    }

    private void removeMiningFatigue(Player player) {
        player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
    }

    @Getter
    @Setter
    static class MiningProgress {
        private int minedBlocks = 0;
        private int untilNextStage = 0;
        private BossBar infoBossBar;
        private int stage = 0;
        private Player player;

        public MiningProgress(Player player) {
            this.player = player;

            infoBossBar = Bukkit.createBossBar(ChatColor.GRAY + "Zwerg", BarColor.WHITE, BarStyle.SEGMENTED_20);
            infoBossBar.setVisible(true);
        }

        public void advance() {
            if (stage == STAGE_BLOCK_COUNTS.length - 1) {
                return;
            }

            minedBlocks += 1;
            untilNextStage -= 1;

            if (untilNextStage == 0) {
                stage += 1;
                untilNextStage = -1;
                if (stage == STAGE_BLOCK_COUNTS.length - 2) {
                    infoBossBar.setColor(BarColor.YELLOW);
                }
                if (stage == STAGE_BLOCK_COUNTS.length - 1) {
                    infoBossBar.setColor(BarColor.RED);
                    infoBossBar.setTitle(ChatColor.DARK_RED.toString() + ChatColor.MAGIC + "XXX" + ChatColor.RESET + ChatColor.DARK_RED + ChatColor.BOLD + " Zwerg " + ChatColor.RESET + ChatColor.DARK_RED + ChatColor.MAGIC + "XXX");
                    infoBossBar.setProgress(1.0f);
                    player.playSound(player, Sound.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 0.3f, 0.7f);
                } else {
                    player.playSound(player, Sound.ITEM_TOTEM_USE, SoundCategory.PLAYERS, 0.3f, 0.0f);
                }
            }

            if (untilNextStage == -1) {
                for (Integer stageBlockCount : STAGE_BLOCK_COUNTS) {
                    if (minedBlocks < stageBlockCount) {
                        untilNextStage = stageBlockCount - minedBlocks;
                        break;
                    }
                }
            }
            int from = STAGE_BLOCK_COUNTS[stage];
            int to = STAGE_BLOCK_COUNTS[stage + 1];

            float progress = (float) (minedBlocks - from) / (to - from);
            infoBossBar.setProgress(progress);
        }

        public void showBossBar(Player player) {
            infoBossBar.addPlayer(player);
        }

        public void reset() {
            infoBossBar.removeAll();
        }
    }
}