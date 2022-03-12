package techeart.thadv.content.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import techeart.thadv.api.EntityMultipart;
import techeart.thadv.api.EntityMultipartPart;

import java.util.function.Supplier;

public class PacketEntityPartOffsetSync
{
    private final int parentId;
    private final String partId;
    private final Vec3 offset;

    public PacketEntityPartOffsetSync(EntityMultipart parentEntity, String partId, Vec3 offset) { this(parentEntity.getId(), partId, offset); }

    public PacketEntityPartOffsetSync(int parentEntityId, String partId, Vec3 offset)
    {
        this.parentId = parentEntityId;
        this.partId = partId;
        this.offset = offset;
    }

    public static void encode(PacketEntityPartOffsetSync msg, FriendlyByteBuf buf)
    {
        buf.writeInt(msg.parentId);
        buf.writeUtf(msg.partId);
        buf.writeDouble(msg.offset.x());
        buf.writeDouble(msg.offset.y());
        buf.writeDouble(msg.offset.z());
    }

    public static PacketEntityPartOffsetSync decode(FriendlyByteBuf buf)
    {
        return new PacketEntityPartOffsetSync(
                buf.readInt(),
                buf.readUtf(),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())
        );
    }

    public static void handle(PacketEntityPartOffsetSync msg, Supplier<NetworkEvent.Context> ctx)
    {
        Minecraft mc = Minecraft.getInstance();
        ctx.get().enqueueWork(() -> {
            EntityMultipart parent = (EntityMultipart)mc.level.getEntity(msg.parentId);
            if(parent != null)
            {
                EntityMultipartPart part = parent.getPart(msg.partId);
                if(part != null) part.setOffset(msg.offset);
//                else System.out.println("No part to move");
            }
//            else System.out.println("No parent with give ID");
        });
        ctx.get().setPacketHandled(true);
    }
}
