package techeart.thadv.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import techeart.thadv.content.network.PacketHandler;
import techeart.thadv.content.network.packets.PacketEntityPartOffsetSync;

import java.util.function.BiFunction;

public class EntityMultipartPart extends PartEntity<EntityMultipart>
{
    private final String id;
    private final EntityDimensions size;
    private Vec3 offset = Vec3.ZERO;

    private BiFunction<DamageSource, Float, Boolean> onHurtAction;

    public EntityMultipartPart(String id, EntityMultipart parent, float width, float height)
    {
        super(parent);
        this.size = EntityDimensions.scalable(width, height);
        refreshDimensions();
        this.id = id;

        onHurtAction = getParent()::hurt;
    }

    public EntityMultipartPart setOnHurtAction(BiFunction<DamageSource, Float, Boolean> action)
    {
        onHurtAction = action;
        return this;
    }

    public EntityMultipartPart withOffset(double y) { return withOffset(0, y, 0); }
    public EntityMultipartPart withOffset(double x, double y, double z)
    {
        offset = new Vec3(x, y, z);
        return this;
    }

    public void setOffset(double y) { setOffset(0, y, 0); }
    public void setOffset(Vec3 offset) { setOffset(offset.x(), offset.y(), offset.z()); }
    public void setOffset(double x, double y, double z)
    {
        offset = new Vec3(x, y, z);
        if(!level.isClientSide())
            PacketHandler.sendToTracking(new PacketEntityPartOffsetSync(getParent(), getPartId(), offset), getParent());
    }

    public String getPartId() { return id; }

    public Vec3 getOffset() { return offset; }

    @Override
    protected void defineSynchedData() { }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) { }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) { }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        return onHurtAction.apply(source, amount);
    }

    @Override
    public boolean is(Entity entity) { return this == entity || getParent() == entity; }

    @Override
    public EntityDimensions getDimensions(Pose pose) { return this.size; }

    @Override
    public boolean isPickable() { return true; }

    @Override
    public boolean shouldBeSaved() { return false; }

    @Override
    public Packet<?> getAddEntityPacket() { throw new UnsupportedOperationException("Multipart Entity Part instantiating isn't allowed!"); }
}
