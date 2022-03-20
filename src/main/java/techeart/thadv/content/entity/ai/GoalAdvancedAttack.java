package techeart.thadv.content.entity.ai;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import techeart.thadv.content.entity.entities.basic.IAdvancedAttackMob;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GoalAdvancedAttack<T extends PathfinderMob & IAdvancedAttackMob> extends Goal
{
    protected final T executor;
    protected float attackDamage;
    protected float areaHeight;
    protected float areaRadius;
    protected float areaForwardOffset;
    protected final Function<T, Boolean> useCondition;

    protected float areaUpOffset = 0;
    protected float knockBackPower = 0;
    protected int damageDelay = 0;
    protected int finishingTime = 0;
    protected boolean knockUpwards = false;
    protected boolean forceKnock = false;
    protected boolean stripShield = false;

    private int damageTimer;
    private int finishTimer;
    private boolean finishing = false;
    protected Vec3 lookPos;

    public GoalAdvancedAttack(T executor, float areaHeight, float areaRadius, float areaCenterForwardOffset, float damage, Function<T, Boolean> useCondition)
    {
        this.executor = executor;
        this.attackDamage = damage;
        this.areaHeight = areaHeight;
        this.areaRadius = areaRadius;
        this.areaForwardOffset = areaCenterForwardOffset;
        this.useCondition = useCondition;
    }

    public GoalAdvancedAttack<T> withKnockback(float value) { knockBackPower = value; return this; }
    public GoalAdvancedAttack<T> withHeightOffset(float value) { areaUpOffset = value; return this; }
    public GoalAdvancedAttack<T> withDamageDelay(int value) { damageDelay = value; return this; }
    public GoalAdvancedAttack<T> withFinishingTime(int value) { finishingTime = value; return this; }
    public GoalAdvancedAttack<T> knockUpwards() { knockUpwards = true; return this; }
    public GoalAdvancedAttack<T> forceKnock() { forceKnock = true; return this; }
    public GoalAdvancedAttack<T> stripShield() { stripShield = true; return this; }

    @Override
    public boolean canUse()
    {
        if(!useCondition.apply(executor)) return false;
        LivingEntity target = executor.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() { return canUse() && !isFinished(); }

    @Override
    public void start()
    {
        executor.onAttackStarted();
        damageTimer = damageDelay;
        finishTimer = finishingTime;
        finishing = false;
        lockLookPos();
    }

    @Override
    public void stop()
    {
        executor.finishAttack();
    }

    @Override
    public void tick()
    {
        if(finishing && finishTimer > 0) tickFinishing();

        executor.getLookControl().setLookAt(lookPos);
        if(damageTimer > 0) damageTimer--;
        else if (!finishing)
        {
            performAttack();
            finishing = true;
        }
    }

    protected void tickFinishing() { finishTimer--; }

    protected void performAttack()
    {
        DamageSource damage = DamageSource.mobAttack(executor);
        List<Entity> entities = new ArrayList<>();
        for (Entity entity : executor.level.getEntities(executor, calculateArea(), e -> !e.is(executor)))
        {
            boolean flag = performOnEntity(entity, damage);
            if(flag || forceKnock) entities.add(entity);
        }

        executor.onAttackPerformed(entities);
    }

    protected boolean performOnEntity(Entity entity, DamageSource damageSource)
    {
        if(stripShield && entity instanceof Player player) player.disableShield(true);

        boolean hurt = entity.hurt(damageSource, attackDamage);
        if(forceKnock || (hurt && knockBackPower > 0))
        {
            Vec3 knockDir = knockUpwards ? new Vec3(0, 1, 0) : getAreaCenter().vectorTo(entity.position());
            entity.setDeltaMovement(knockDir.normalize().scale(knockBackPower));
        }
        return hurt;
    }

    protected Vec3 getAreaCenter()
    {
        Vec3 offsetDir = executor.position().vectorTo(lookPos);
        return executor.position().add(offsetDir.normalize().scale(areaForwardOffset));
    }

    protected AABB calculateArea()
    {
        Vec3 center = getAreaCenter();
        Vec3 min = center.subtract(new Vec3(areaRadius, 0, areaRadius));
        min = new Vec3(min.x, executor.position().y + areaUpOffset, min.z);
        Vec3 max = center.add(new Vec3(areaRadius, 0, areaRadius));
        max = new Vec3(max.x, min.y + areaHeight, max.z);
        return new AABB(min, max);
    }

    protected void lockLookPos() { if(executor.getTarget() != null) lookPos = executor.getTarget().position(); }

    protected boolean isFinishing() { return finishing; }

    protected boolean isFinished() { return finishing && finishTimer <= 0; }
}
