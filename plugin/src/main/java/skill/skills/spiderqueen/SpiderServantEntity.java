package skill.skills.spiderqueen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class SpiderServantEntity extends Spider {

    private final int slownessStrength;
    private final int slownessDuration;
    private final Entity bukkitEntity;
    private final Player owner;

    @SuppressWarnings("ConstantConditions")
    public SpiderServantEntity(Location location, org.bukkit.entity.Player owner, int slownessStrength, int slownessDuration, double damage) {
        super(EntityType.CAVE_SPIDER, ((CraftWorld) Objects.requireNonNull(location.getWorld())).getHandle());
        this.slownessDuration = slownessDuration;
        this.slownessStrength = slownessStrength;
        this.owner = ((CraftPlayer) owner).getHandle();
        bukkitEntity = getBukkitEntity();

        getAttributes().getInstance(Attributes.MAX_HEALTH).setBaseValue(0.5d);
        getAttributes().getInstance(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        setPos(location.getX(), location.getY(), location.getZ());
        setHealth(getMaxHealth());
        setCustomName(Component.literal("Spinne von " + ChatColor.GOLD + owner.getDisplayName() + ChatColor.RESET));
        setCustomNameVisible(false);
        this.persist = false;

        this.goalSelector.removeAllGoals();
        this.targetSelector.removeAllGoals();
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ClimbOnTopOfPowderSnowGoal(this, this.level));
        this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.5F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.7D, true));
        this.goalSelector.addGoal(4, new GenericFollowOwnerGoal(this, this.owner, 1.7D, 4.0F, 10.0F));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 8.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.3D));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));


        this.targetSelector.addGoal(1, new NearestAttackableTargetGoalWithRange<>(this, SpiderServantEntity.class, true, livingEntity -> !((SpiderServantEntity) livingEntity).owner.getUUID().equals(owner.getUniqueId()), 15));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoalWithRange<>(this, net.minecraft.world.entity.player.Player.class, true, livingEntity -> !livingEntity.getUUID().equals(owner.getUniqueId()), 50));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoalWithRange<>(this, Monster.class, true, livingEntity -> !(livingEntity instanceof SpiderServantEntity), 15));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoalWithRange<>(this, Animal.class, true, 10));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SQUID_SQUIRT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damagesource) {
        return super.getHurtSound(damagesource);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRIDER_RETREAT;
    }

    @Override
    protected void playStepSound(BlockPos blockposition, BlockState iblockdata) {
        this.playSound(SoundEvents.FUNGUS_STEP, 0.2F, 2.0F);
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (random.nextInt(40) == 0) {
            bukkitEntity.getWorld().spawnParticle(Particle.ASH, bukkitEntity.getLocation(), 3, 1.0D, 0.5D, 0.2D, 0.5D);
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof Player) {
            if (super.doHurtTarget(entity)) {
                LivingEntity target = (LivingEntity) entity;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slownessDuration, slownessStrength, false, false), this, EntityPotionEffectEvent.Cause.ATTACK);
                target.addEffect(new MobEffectInstance(MobEffects.DARKNESS, (slownessDuration - 10 <= 0) ? slownessDuration : (slownessDuration - 10), 0, false, false), this, EntityPotionEffectEvent.Cause.ATTACK);
                target.addEffect(new MobEffectInstance(MobEffects.JUMP, slownessDuration, 100, false, false), this, EntityPotionEffectEvent.Cause.ATTACK);
                dead = true;
                this.discard();
                return true;
            }
        } else if (entity instanceof Animal) {
            amplifyDamage(2);
        } else if (entity instanceof Monster) {
            amplifyDamage(1.5);
        }

        if (super.doHurtTarget(entity)) {
            killSelf();
            return true;
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    private void amplifyDamage(double factor) {
        Supplier<String> modifierName = () -> "Spider servant damage bonus";
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), modifierName, factor, AttributeModifier.Operation.MULTIPLY_TOTAL);
        getAttributes().getInstance(Attributes.ATTACK_DAMAGE).addPermanentModifier(modifier);
    }

    private void killSelf() {
        dead = true;
        this.discard();
    }
}
