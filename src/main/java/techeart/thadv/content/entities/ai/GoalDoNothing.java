package techeart.thadv.content.entities.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import techeart.thadv.content.entities.IAdvancedAttackMob;

import java.util.function.Function;

public class GoalDoNothing<T extends PathfinderMob & IAdvancedAttackMob> extends Goal
{
    protected final T mob;
    protected final int time;
    protected final Function<T, Boolean> useCondition;

    protected Vec3 lookPos;
    protected int timer;

    public GoalDoNothing(T executor, Function<T, Boolean> useCondition) { this(executor, -1, useCondition); }

    public GoalDoNothing(T executor, int timeInTicks, Function<T, Boolean> useCondition)
    {
        mob = executor;
        time = timeInTicks;
        this.useCondition = useCondition;
    }

    @Override
    public boolean canUse() { return useCondition.apply(mob) && !(time > 0 && timer <= 0); }

    @Override
    public void start()
    {
        mob.onAttackStarted();
        lockLookPos();
        timer = time;
    }

    @Override
    public void stop()
    {
        mob.finishAttack();
    }

    @Override
    public void tick()
    {
        mob.getLookControl().setLookAt(lookPos);
        if(timer > 0) timer--;
    }

    protected void lockLookPos()
    {
        if(mob.getLookControl().isHasWanted())
            lookPos = new Vec3(
                    mob.getLookControl().getWantedX(),
                    mob.getLookControl().getWantedY(),
                    mob.getLookControl().getWantedZ()
            );
        else lookPos = mob.getEyePosition().add(mob.getForward());
    }
}
