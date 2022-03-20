package techeart.thadv.content.entity.entities.misc;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import techeart.thadv.content.entity.entities.basic.EntityDummy;

import java.util.Random;

public class EntityEruptionSource extends EntityDummy
{
    public static final EntityType<EntityEruptionSource> TYPE = EntityType.Builder.<EntityEruptionSource>of(
            EntityEruptionSource::new,
            MobCategory.MISC
    ).sized(1.4f, 2.4f).fireImmune().clientTrackingRange(4).updateInterval(5).build("lava_eruption");

    private static final float DIRECT_DAMAGE = 2.0f;
    private static final int SET_ON_FIRE_SECONDS = 3;

    private int lifetime = 0;
    private final Random random = new Random();

    public EntityEruptionSource(EntityType<? extends Entity> type, Level level) { super(type, level); }

    public EntityEruptionSource(Level level, Vec3 pos, int lifetime)
    {
        super(TYPE, level);
        this.lifetime = lifetime;
        setPos(pos.x(), pos.y(), pos.z());
    }

    @Override
    public void tick()
    {
        if(level.isClientSide())
        {
            spawnParticles();
//            if(ticksExisted < 1)
//            {
//                level.playLocalSound(getX(), getY(), getZ(), SoundEvents.FIRECHARGE_USE, SoundSource.AMBIENT, 1.0f, 1.0f, false);
//                level.playLocalSound(getX(), getY(), getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.AMBIENT, 1.0f, 1.0f, false);
//            }
        }
        else
        {
            if(tickCount >= lifetime) discard();
            for(Entity e : level.getEntities(this, getBoundingBox()))
            {
                if(e.hurt(DamageSource.ON_FIRE, DIRECT_DAMAGE)) push(e);
                e.setSecondsOnFire(SET_ON_FIRE_SECONDS);
            }
        }

        super.tick();
    }

    @OnlyIn(Dist.CLIENT)
    protected void spawnParticles()
    {
        if(isInWater())
        {
            level.addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0.0D, 0.02D, 0.0D);
            //level.addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY(), this.getZ(), 0.0D, 0.01D, 0.0D);
        }
        else
        {
            level.addParticle(ParticleTypes.FLAME,
                    this.getX() + getRandomOffset() * 0.18D, this.getY(), this.getZ() + getRandomOffset() * 0.2D,
                    0.0D, 0.2D, 0.0D);
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Fluids.LAVA.defaultFluidState().createLegacyBlock()),
                    this.getX() + getRandomOffset() * 0.2D, this.getY(), this.getZ() + getRandomOffset() * 0.2D,
                    getRandomOffset() * 0.01D, 0.4D, getRandomOffset() * 0.01D);
            level.addParticle(ParticleTypes.LARGE_SMOKE,
                    this.getX() + getRandomOffset() * 0.2D, this.getY(), this.getZ() + getRandomOffset() * 0.2D,
                    0.0D, 0.001D, 0.0D);
        }
    }

    private double getRandomOffset() { return (random.nextDouble() - 0.5D) * 2; }
}
