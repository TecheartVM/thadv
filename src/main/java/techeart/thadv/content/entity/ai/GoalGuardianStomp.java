package techeart.thadv.content.entity.ai;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import techeart.thadv.content.entity.entities.basic.IAdvancedAttackMob;
import techeart.thadv.content.network.PacketHandler;
import techeart.thadv.content.network.packets.PacketParticleCircleWave;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GoalGuardianStomp<T extends PathfinderMob & IAdvancedAttackMob> extends GoalAdvancedAttack<T>
{
    protected final float waveSpeed;
    protected final float waveLength;
    private List<Entity> attackedEntities;

    private float initialWaveRadius = 0f;

    protected Vec3 center;
    protected DamageSource damageSource;
    private float waveRadius = 0f;
    private float particleDensity = 1f;

    public GoalGuardianStomp(@Nonnull T executor, float areaHeight, float areaRadius, float areaCenterForwardOffset, float waveSpreadTime, float waveLength, float damage, Function<T, Boolean> useCondition)
    {
        super(executor, areaHeight, areaRadius, areaCenterForwardOffset, damage, useCondition);
        this.waveSpeed = waveSpreadTime <= 0 ? areaRadius : areaRadius / waveSpreadTime;
        this.waveLength = waveLength;
    }

    public GoalGuardianStomp<T> withInitialWaveRadius(float r) { initialWaveRadius = r; return this; }

    @Override
    protected void tickFinishing()
    {
        super.tickFinishing();
        if(waveRadius >= areaRadius) return;

        waveRadius += waveSpeed;
        if(attackedEntities != null)
            for (Entity e : attackedEntities)
            {
                Vec3 ePos = e.position();
                double d = ePos.distanceTo(center);
                if(d < waveRadius && d > waveRadius - waveLength)
                    if(ePos.y() < center.y() + areaHeight)
                        dealDamageToEntity(e);
            }
        spawnParticles(center, waveRadius);
    }

    protected void spawnParticles(Vec3 center, float radius)
    {
        PacketHandler.sendToTracking(new PacketParticleCircleWave(center, radius), executor);
//        for (float f = 0; f < 360; f += (2/particleDensity))
//        {
//            mob.level.addParticle(
//                    ParticleTypes.CLOUD,
//                    radius*Math.cos(f) + center.x(), center.y(), radius*Math.sin(f) + center.z(),
//                    0.0D, 0.02D, 0.0D
//            );
//        }
    }

    protected boolean dealDamageToEntity(Entity e)
    {
        if(stripShield && e instanceof Player player)
            player.disableShield(true);

        boolean hurt = e.hurt(damageSource, attackDamage);
        if(forceKnock || (hurt && knockBackPower > 0))
        {
            Vec3 knockDir = knockUpwards ? new Vec3(0, 1, 0) : getAreaCenter().vectorTo(e.position());
            e.setDeltaMovement(knockDir.normalize().scale(knockBackPower));
        }

        return hurt;
    }

    @Override
    protected void performAttack()
    {
        AABB area = calculateArea();
        DamageSource damage = DamageSource.mobAttack(executor);
        attackedEntities = new ArrayList<>();

        for (Entity entity : executor.level.getEntities(executor, area, e -> !e.is(executor)))
        {
            boolean knock = !entity.isInvulnerableTo(damage);
            if(forceKnock || knock) attackedEntities.add(entity);
        }

        damageSource = DamageSource.mobAttack(executor);
        center = getAreaCenter();
        waveRadius = initialWaveRadius;

        executor.onAttackPerformed(attackedEntities);
    }
}
