package skill.skills.spiderqueen;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class NearestAttackableTargetGoalWithRange<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

    private final double followDistance;

    public NearestAttackableTargetGoalWithRange(Mob entityinsentient, Class<T> oclass, boolean flag, double followDistance) {
        super(entityinsentient, oclass, flag);
        this.followDistance = followDistance;
    }

    public NearestAttackableTargetGoalWithRange(Mob entityinsentient, Class<T> oclass, boolean flag, Predicate<LivingEntity> predicate, double followDistance) {
        super(entityinsentient, oclass, flag, predicate);
        this.followDistance = followDistance;
    }

    public NearestAttackableTargetGoalWithRange(Mob entityinsentient, Class<T> oclass, boolean flag, boolean flag1, double followDistance) {
        super(entityinsentient, oclass, flag, flag1);
        this.followDistance = followDistance;
    }

    public NearestAttackableTargetGoalWithRange(Mob entityinsentient, Class<T> oclass, int i, boolean flag, boolean flag1, @Nullable Predicate<LivingEntity> predicate, double followDistance) {
        super(entityinsentient, oclass, i, flag, flag1, predicate);
        this.followDistance = followDistance;
    }


    @Override
    protected double getFollowDistance() {
        return followDistance;
    }
}
