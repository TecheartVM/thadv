package techeart.thadv.content.entity.entities.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class EntityLavaBall extends ThrowableProjectile
{
    public static final EntityType<EntityLavaBall> TYPE = EntityType.Builder.<EntityLavaBall>of(
            EntityLavaBall::new,
            MobCategory.MISC
    ).sized(0.3f, 0.3f).fireImmune().clientTrackingRange(4).updateInterval(10).build("lava_ball");

    private static final BlockState LAVA = Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 1);

    public EntityLavaBall(EntityType<? extends ThrowableProjectile> type, Level level) { super(type, level); }

    public EntityLavaBall(double x, double y, double z, Level level) { super(TYPE, x, y, z, level); }

    public EntityLavaBall(Level level, LivingEntity owner) { super(TYPE, owner, level); }

    @Override
    protected void defineSynchedData() { }

    @Override
    protected void onHitEntity(@Nonnull EntityHitResult hitResult)
    {
        super.onHitEntity(hitResult);
        Entity entity = hitResult.getEntity();
        if(entity.hurt(DamageSource.thrown(this, this.getOwner()), 6.0f))
            entity.setSecondsOnFire(6);
        playSound(SoundEvents.LAVA_EXTINGUISH, 1.0f, 1.0f);
    }

    @Override
    public void tick()
    {
        super.tick();
        level.addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0.0D, 0.02D, 0.0D);
        level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.01D, 0.0D);
    }

    @Override
    protected void onHit(@Nonnull HitResult hitResult)
    {
        super.onHit(hitResult);
        if (!this.level.isClientSide)
        {
            this.level.broadcastEntityEvent(this, (byte)3);
            this.discard();
            if(hitResult.getType() == HitResult.Type.BLOCK)
            {
                Vec3 hiPoint =  hitResult.getLocation();
                BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(hiPoint.x, hiPoint.y, hiPoint.z);
                placeLavaBlock(level, pos);
                for(Direction dir : Direction.values())
                    placeLavaBlock(level, pos.move(dir));
            }
        }
    }

    protected boolean placeLavaBlock(Level level, BlockPos pos)
    {
        if(level.getBlockState(pos).canBeReplaced(Fluids.FLOWING_LAVA))
            return level.setBlockAndUpdate(pos, LAVA);
        return false;
    }
}
