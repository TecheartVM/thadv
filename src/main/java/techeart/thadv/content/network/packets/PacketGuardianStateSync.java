package techeart.thadv.content.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import techeart.thadv.content.entities.EntityBossStoneGuardian;

import java.util.function.Supplier;

public class PacketGuardianStateSync
{
    private final EntityBossStoneGuardian.State state;
    private final int entityId;

    public PacketGuardianStateSync(EntityBossStoneGuardian.State state, EntityBossStoneGuardian entity) { this(state, entity.getId()); }

    public PacketGuardianStateSync(EntityBossStoneGuardian.State state, int entityId)
    {
        this.state = state;
        this.entityId = entityId;
    }

    public static void encode(PacketGuardianStateSync msg, FriendlyByteBuf buf)
    {
        buf.writeEnum(msg.state);
        buf.writeInt(msg.entityId);
    }

    public static PacketGuardianStateSync decode(FriendlyByteBuf buf)
    {
        return new PacketGuardianStateSync(buf.readEnum(EntityBossStoneGuardian.State.class), buf.readInt());
    }

    public static void handle(PacketGuardianStateSync msg, Supplier<NetworkEvent.Context> ctx)
    {
        Minecraft mc = Minecraft.getInstance();
        ctx.get().enqueueWork(() -> {
            EntityBossStoneGuardian e = (EntityBossStoneGuardian)mc.level.getEntity(msg.entityId);
            if(e != null) e.setState(msg.state);
        });
        ctx.get().setPacketHandled(true);
    }
}
