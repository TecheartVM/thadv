package techeart.thadv.content.entity.entities.basic;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public abstract class EntityDummy extends Entity
{
    public EntityDummy(EntityType<?> type, Level level) { super(type, level); }

    @Override
    public boolean isPickable() { return false; }

    @Override
    public boolean isInvulnerableTo(@Nonnull DamageSource damageSource) { return damageSource != DamageSource.OUT_OF_WORLD; }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightningBolt) { }

    @Override
    public boolean isNoGravity() { return true; }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public void push(double p_20286_, double p_20287_, double p_20288_) { }

    @Override
    public boolean canBeCollidedWith() { return false; }

    @Override
    public boolean isIgnoringBlockTriggers() { return true; }

    @Override
    public boolean isPassenger() { return false; }

    @Override
    protected boolean canRide(Entity entity) { return false; }

    @Override
    public boolean isVehicle() { return false; }

    @Override
    public boolean ignoreExplosion() { return true; }

    @Override
    public boolean isOnFire() { return false; }

    @Override
    public boolean shouldShowName() { return false; }

    @Override
    public boolean canChangeDimensions() { return false; }

    @Override
    public boolean isAttackable() { return false; }

    @Override
    public boolean isSprinting() { return false; }

    @Override
    public boolean isSwimming() { return false; }

    @Override
    protected void doWaterSplashEffect() { }

    //Entity
    @Override
    protected void defineSynchedData() { }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) { }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) { }

    //spawn
    @Override
    @Nonnull
    public Packet<?> getAddEntityPacket() { return new ClientboundAddEntityPacket(this); }
}
