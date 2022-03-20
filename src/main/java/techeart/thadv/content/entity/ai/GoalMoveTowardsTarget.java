package techeart.thadv.content.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public class GoalMoveTowardsTarget<T extends PathfinderMob> extends Goal
{
    private final T mob;
    private final float speedModifier;
    private final float yMaxRotSpeed;
    private final float xMaxRotAngle;
    private final Function<T, Boolean> useCondition;
    private int pathRecalcTimer = 0;
    private double pathedTargetX;
    private double pathedTargetY;
    private double pathedTargetZ;

    public GoalMoveTowardsTarget(T executor, float speedModifier, float yMaxRotSpeed, Function<T, Boolean> useCondition)
    {
        this(executor, speedModifier, yMaxRotSpeed, 30.0f, useCondition);
    }

    public GoalMoveTowardsTarget(T mob, float speedModifier, float yMaxRotSpeed, float xMaxRotAngle, Function<T, Boolean> useCondition)
    {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.yMaxRotSpeed = yMaxRotSpeed;
        this.xMaxRotAngle = xMaxRotAngle;
        this.useCondition = useCondition;
    }

    @Override
    public boolean canUse()
    {
        if(!useCondition.apply(mob)) return false;

        LivingEntity target = mob.getTarget();
        if(target instanceof Player player && (player.isSpectator() || player.isCreative()))
            return false;
        return target != null && target.isAlive();
    }

    @Override
    public void start()
    {
        pathRecalcTimer = 0;
    }

    @Override
    public void stop()
    {
        mob.getNavigation().stop();
    }

    @Override
    public void tick()
    {
        LivingEntity target = mob.getTarget();
        mob.getLookControl().setLookAt(target, yMaxRotSpeed, xMaxRotAngle);
        double dist = mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
        pathRecalcTimer = Math.max(pathRecalcTimer - 1, 0);
        if(mob.getSensing().hasLineOfSight(target) && pathRecalcTimer <= 0
                && (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D
                    || target.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D
                || mob.getRandom().nextFloat() < 0.05F)
        )
        {
            this.pathedTargetX = target.getX();
            this.pathedTargetY = target.getY();
            this.pathedTargetZ = target.getZ();
            pathRecalcTimer = 4 + mob.getRandom().nextInt(7);

            if (dist > 1024.0D) pathRecalcTimer += 10;
            else if (dist > 256.0D) pathRecalcTimer += 5;

            if (!mob.getNavigation().moveTo(target, this.speedModifier))
                pathRecalcTimer += 15;
        }
    }
}
