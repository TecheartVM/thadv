package techeart.thadv.content.entities.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import techeart.thadv.content.entities.EntityBossStoneGuardian;
import techeart.thadv.content.entities.EntityEruptionSource;
import techeart.thadv.utils.ServerLevelEvent;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

public class GoalGuardianSmash extends GoalAdvancedAttack<EntityBossStoneGuardian>
{
    private static final int SET_ON_FIRE_CHANCE = 40;
    private static final int SET_ON_FIRE_SECONDS = 3;
    private static final int ERUPTION_TICKS = 1240;
    private static final float ERUPTION_RADIUS = 12.0f;
    private static final int ERUPTION_MAX_SOURCES = 20;
    private static final int SOURCES_LIFETIME = 40;

    private final Random random = new Random();

    public GoalGuardianSmash(EntityBossStoneGuardian executor, Function<EntityBossStoneGuardian, Boolean> useCondition)
    {
        super(executor, 2.0f, 3.0f, 2.5f, 24.0f, useCondition);
        damageDelay = 35;
        finishingTime = 70;
        stripShield = true;
    }

    @Override
    protected void performAttack()
    {
        super.performAttack();
        if(executor.getPhase() > 1)
            new Eruption((ServerLevel) executor.level, executor.position(), ERUPTION_TICKS, ERUPTION_RADIUS, ERUPTION_MAX_SOURCES, SOURCES_LIFETIME).run();
    }

    @Override
    protected boolean performOnEntity(Entity entity, DamageSource damageSource)
    {
        boolean flag = super.performOnEntity(entity, damageSource);
        if(flag && executor.getPhase() > 1 && random.nextInt(100) + 1 < SET_ON_FIRE_CHANCE)
            entity.setSecondsOnFire(SET_ON_FIRE_SECONDS);
        return flag;
    }

    protected static class Eruption extends ServerLevelEvent
    {
        private static final float MIN_DIST = 2.0f;

        private final Vec3 centerPos;
        private final int timeInTicks;
        private final float radius;
        private final int maxSources;
        private final int sourceLifetime;

        private final Random random = new Random();
        private final Map<Vec3, EntityEruptionSource> currentSources = new HashMap<>();
        private int ticksExisted = 0;

        public Eruption(ServerLevel level, Vec3 position, int timeInTicks, float radius, int maxSources, int sourceLifetime)
        {
            super(level);
            this.centerPos = position;
            this.timeInTicks = timeInTicks;
            this.radius = radius;
            this.maxSources = maxSources;
            this.sourceLifetime = sourceLifetime;
        }

        //used for loading from file by ServerLevelEvent
        public Eruption(ServerLevel level, CompoundTag nbt)
        {
            super(level, nbt);
            centerPos = new Vec3(
                    nbt.getFloat("PosX"),
                    nbt.getFloat("PosY"),
                    nbt.getFloat("PosZ")
            );
            timeInTicks = nbt.getInt("Time");
            radius = nbt.getFloat("Radius");
            maxSources = nbt.getInt("MaxSources");
            sourceLifetime = nbt.getInt("SourceLifetime");
            ticksExisted = nbt.getInt("ElapsedTime");
        }

        @Override
        public boolean isFinished() { return ticksExisted >= timeInTicks; }

        @Override
        protected void tick()
        {
            if(!getLevel().hasChunkAt(new BlockPos(centerPos))) return;

            //removing dead entities from the sources list
            currentSources.values().removeIf(EntityEruptionSource::isRemoved);

            if(currentSources.size() < maxSources && random.nextInt(100) < 10)
            {
                Vec3 pos = getPosInRadius();
                if(pos != null)
                {
                    EntityEruptionSource source = new EntityEruptionSource(getLevel(), pos, sourceLifetime);
                    if(getLevel().addFreshEntity(source)) currentSources.put(pos, source);
                }
            }
            ticksExisted++;
        }

        @Override
        protected CompoundTag save(@Nonnull CompoundTag nbt)
        {
            nbt.putFloat("PosX", (float) centerPos.x());
            nbt.putFloat("PosY", (float) centerPos.y());
            nbt.putFloat("PosZ", (float) centerPos.z());
            nbt.putInt("Time", timeInTicks);
            nbt.putFloat("Radius", radius);
            nbt.putInt("MaxSources", maxSources);
            nbt.putInt("SourceLifetime", sourceLifetime);
            nbt.putInt("ElapsedTime", ticksExisted);
            return nbt;
        }

        protected Vec3 getPosInRadius()
        {
            for(int i = 0; i < 10; i++)
            {
                float r = random.nextFloat() * radius;
                float a = random.nextFloat() * 360;
                Vec3 pos = centerPos.add(Vec3.directionFromRotation(0, a).normalize().scale(r).add(new Vec3(0, 4, 0)));
                BlockPos blockPos = new BlockPos(pos);
                if(getLevel().getBlockState(blockPos).isAir())
                {
                    BlockHitResult hit = getLevel().clip(
                            new ClipContext(
                                    pos,
                                    pos.subtract(new Vec3(0,8,0)),
                                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null
                            )
                    );
                    if(hit.getType() != HitResult.Type.BLOCK) continue;
                    blockPos = hit.getBlockPos();
                }
                else if(!getLevel().getBlockState(blockPos.above()).isAir()) continue;
                blockPos = blockPos.above();
                pos = new Vec3(blockPos.getX() + random.nextFloat(), blockPos.getY(), blockPos.getZ() + random.nextFloat());
                if(noSourcesNearby(pos)) return pos;
            }
            return null;
        }

        private boolean noSourcesNearby(Vec3 pos)
        {
            for(Vec3 source : currentSources.keySet()) if(source.distanceTo(pos) < MIN_DIST) return false;
            return true;
        }
    }
}
