package techeart.thadv.content.entity.entities.basic;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class EntityMultipart extends PathfinderMob
{
    private final List<EntityMultipartPart> parts = new ArrayList<>();

    public EntityMultipart(EntityType<? extends PathfinderMob> type, Level level)
    {
        super(type, level);
    }

    public EntityMultipartPart addPart(EntityMultipartPart part)
    {
        for (EntityMultipartPart p : parts)
            if(p.getPartId().equals(part.getPartId()))
                throw new IllegalArgumentException("Can't add duplicate part: " + part.getPartId());
        parts.add(part);
        return part;
    }

    public boolean removePart(String partId) { return parts.removeIf(p -> p.getPartId().equals(partId)); }
    public boolean removePart(EntityMultipartPart part) { return parts.remove(part); }

    @Nullable
    public EntityMultipartPart getPart(String partId)
    {
        for (EntityMultipartPart p : parts)
            if(p.getPartId().equals(partId)) return p;
        return null;
    }

    @Override
    public void aiStep()
    {
        super.aiStep();
        moveParts();
    }

    protected void moveParts()
    {
        for (EntityMultipartPart part : parts)
            movePart(part, part.getOffset());
    }

    @Override
    public void recreateFromPacket(@Nonnull ClientboundAddMobPacket packet)
    {
        super.recreateFromPacket(packet);

        PartEntity<?>[] parts = this.getParts();
        for(int i = 0; i < parts.length; ++i)
            parts[i].setId(i + packet.getId());
    }

    @Override
    @Nonnull
    public PartEntity<?>[] getParts() { return this.parts.toArray(new EntityMultipartPart[] {}); }

    @Override
    public boolean isMultipartEntity() { return true; }

    @Override
    public boolean isPickable() { return false; }

    @Override
    @Nonnull
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddMobPacket(this);
    }

    protected void movePart(EntityMultipartPart part, Vec3 offset) { movePart(part, offset.x, offset.y, offset.z); }

    protected void movePart(EntityMultipartPart part, double offsetForward, double offsetUp, double offsetRight)
    {
        Vec3 lastPos = new Vec3(part.getX(), part.getY(), part.getZ());
        Vec3 forward = Vec3.directionFromRotation(0.0f, yBodyRot).normalize();
        //forward = new Vec3(forward.x(), 0.0d, forward.z()).normalize();
        part.setPos(
                position()
                .add(forward.scale(offsetForward))
                .add(forward.yRot(90.0f).scale(offsetRight))
                .add(0, offsetUp, 0)
        );

        part.xo = lastPos.x;
        part.yo = lastPos.y;
        part.zo = lastPos.z;
        part.xOld = lastPos.x;
        part.yOld = lastPos.y;
        part.zOld = lastPos.z;
    }
}
