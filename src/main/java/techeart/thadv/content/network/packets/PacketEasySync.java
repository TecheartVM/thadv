package techeart.thadv.content.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import techeart.thadv.content.entities.IEasySyncable;

import java.util.function.Supplier;

public class PacketEasySync
{
    private final int entityId;
    private final String eventId;

    public <T extends Entity & IEasySyncable> PacketEasySync(T easySyncable, String eventId) { this(easySyncable.getId(), eventId); }

    protected PacketEasySync(int entityId, String eventId)
    {
        this.entityId = entityId;
        this.eventId = eventId;
    }

    public static void encode(PacketEasySync msg, FriendlyByteBuf buf)
    {
        buf.writeInt(msg.entityId);
        buf.writeUtf(msg.eventId);
    }

    public static PacketEasySync decode(FriendlyByteBuf buf) { return new PacketEasySync(buf.readInt(), buf.readUtf()); }

    public static void handle(PacketEasySync msg, Supplier<NetworkEvent.Context> ctx)
    {
        Minecraft mc = Minecraft.getInstance();
        ctx.get().enqueueWork(() -> {
            if(mc.level.getEntity(msg.entityId) instanceof IEasySyncable easySyncable)
                easySyncable.handleEvent(msg.eventId);
        });
        ctx.get().setPacketHandled(true);
    }
}
