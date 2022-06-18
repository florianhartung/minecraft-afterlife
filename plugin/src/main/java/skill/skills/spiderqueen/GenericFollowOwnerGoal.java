package skill.skills.spiderqueen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.event.entity.EntityTeleportEvent;

import java.util.EnumSet;
import java.util.Objects;

public class GenericFollowOwnerGoal extends Goal {
    private final Mob mob;
    private final Player owner;
    private final Level level;
    private final double speedModifier;
    private final PathNavigation navigation;
    private int timeToRecalcPath;
    private final float stopDistance;
    private final float startDistance;
    private float oldWaterCost;

    public GenericFollowOwnerGoal(Mob mob, Player player, double speedModifier, float startDistance, float stopDistance) {
        this.mob = mob;
        this.owner = player;
        this.level = mob.level;
        this.speedModifier = speedModifier;
        this.navigation = mob.getNavigation();
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        if (!owner.level.getWorld().getUID().equals(level.getWorld().getUID())) {
            return false;
        } else if (owner.isSpectator()) {
            return false;
        } else if (this.mob.distanceToSqr(owner.position()) < (double) (this.startDistance * this.startDistance)) {
            return false;
        } else {
            return true;
        }
    }

    public boolean canContinueToUse() {
        return !this.navigation.isDone() && this.mob.distanceToSqr(this.owner) > (double) (this.stopDistance * this.stopDistance);
    }

    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.mob.getPathfindingMalus(BlockPathTypes.WATER);
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
    }

    public void stop() {
        this.navigation.stop();
        this.mob.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
    }

    public void tick() {
        this.mob.getLookControl().setLookAt(this.owner, 10.0F, (float) this.mob.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            if (!this.mob.isLeashed() && !this.mob.isPassenger()) {
                if (this.mob.distanceToSqr(this.owner) >= 144.0D) {
                    this.teleportToOwner();
                } else {
                    this.navigation.moveTo(this.owner, this.speedModifier);
                }
            }
        }

    }

    private void teleportToOwner() {
        BlockPos blockposition = this.owner.blockPosition();

        for (int i = 0; i < 10; ++i) {
            int j = this.randomIntInclusive(-3, 3);
            int k = this.randomIntInclusive(-1, 1);
            int l = this.randomIntInclusive(-3, 3);
            boolean flag = this.maybeTeleportTo(blockposition.getX() + j, blockposition.getY() + k, blockposition.getZ() + l);
            if (flag) {
                return;
            }
        }

    }

    private boolean maybeTeleportTo(int i, int j, int k) {
        if (Math.abs((double) i - this.owner.getX()) < 2.0D && Math.abs((double) k - this.owner.getZ()) < 2.0D) {
            return false;
        } else if (!this.canTeleportTo(new BlockPos(i, j, k))) {
            return false;
        } else {
            CraftEntity entity = this.mob.getBukkitEntity();
            Location to = new Location(entity.getWorld(), i + 0.5D, j, k + 0.5D, this.mob.getYRot(), this.mob.getXRot());
            EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
            this.mob.level.getCraftServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            } else {
                to = event.getTo();
                Objects.requireNonNull(to);
                this.mob.moveTo(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
                this.navigation.stop();
                return true;
            }
        }
    }

    private boolean canTeleportTo(BlockPos blockposition) {
        BlockPathTypes pathtype = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, blockposition.mutable());
        if (pathtype != BlockPathTypes.WALKABLE) {
            return false;
        } else {
            BlockState iblockdata = this.level.getBlockState(blockposition.below());
            if (iblockdata.getBlock() instanceof LeavesBlock) {
                return false;
            } else {
                BlockPos blockposition1 = blockposition.subtract(this.mob.blockPosition());
                return this.level.noCollision(this.mob, this.mob.getBoundingBox().move(blockposition1));
            }
        }
    }

    private int randomIntInclusive(int i, int j) {
        return this.mob.getRandom().nextInt(j - i + 1) + i;
    }
}
