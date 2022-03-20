package techeart.thadv.content.entity.ai;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import techeart.thadv.content.entity.entities.basic.IAdvancedAttackMob;

import java.util.function.Function;

public class GoalRam<T extends PathfinderMob> extends Goal
{
    private final T executor;
    private final double speedModifier;
    private final Function<T, Boolean> useCondition;
    private final float damage;
    private final float knockback;

    private Vec3 inflating;
    private Vec3 offset;
    private Vec3 targetPos;

    public GoalRam(T executor, double speedModifier, float knockback, Function<T, Boolean> useCondition)
    {
        this.executor = executor;
        this.speedModifier = speedModifier;
        this.useCondition = useCondition;
        this.knockback = knockback;
        damage = (float)executor.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    public GoalRam<T> inflateAttackArea(double x, double y, double z) { inflating = new Vec3(x, y, z); return this; }

    public GoalRam<T> moveAttackArea(double forward, double up, double sideways) { offset = new Vec3(forward, up, sideways); return this; }

    @Override
    public boolean canUse()
    {
        if(!useCondition.apply(executor)) return false;
        LivingEntity target = executor.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse()
    {
        return executor.distanceToSqr(targetPos) > 1.0d && !executor.getNavigation().isDone();
    }

    @Override
    public void start()
    {
        LivingEntity target = executor.getTarget();
        if(target == null) return;
        targetPos = target.position();
        executor.getNavigation().moveTo(targetPos.x(), targetPos.y(), targetPos.z(), speedModifier);
        if(executor instanceof IAdvancedAttackMob mob) mob.onAttackStarted();
    }

    @Override
    public void stop()
    {
        if(executor instanceof IAdvancedAttackMob mob) mob.finishAttack();
    }

    @Override
    public void tick()
    {
        dealDamage();
    }

    protected void dealDamage()
    {
        AABB attackZone = executor.getBoundingBox();
        if(inflating != null) attackZone = attackZone.inflate(inflating.x(), inflating.y(), inflating.z());
        if(offset != null)
        {
            Vec3 vec = executor.getForward().normalize().multiply(offset);
            attackZone = attackZone.move(vec.x(), vec.y(), vec.z());
        }

        Vec3 areaCenter = attackZone.getCenter();
        for(Entity e : executor.level.getEntities(executor, attackZone, EntitySelector.NO_CREATIVE_OR_SPECTATOR.and(e -> e instanceof LivingEntity)))
        {
            e.hurt(DamageSource.mobAttack(executor), damage);
            Vec3 pushVector = areaCenter.vectorTo(e.position()).normalize().scale(knockback);
            e.push(pushVector.x(), pushVector.y(), pushVector.z());
        }
    }
}
