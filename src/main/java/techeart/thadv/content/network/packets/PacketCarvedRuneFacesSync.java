package techeart.thadv.content.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import techeart.thadv.content.entities.EntityCarvedRune;
import techeart.thadv.content.misc.Rune;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketCarvedRuneFacesSync
{
    private final int entityId;
    private final EnumMap<Direction, Rune> carvedFaces = new EnumMap<>(Direction.class);

    public PacketCarvedRuneFacesSync(EnumMap<Direction, Rune> carvedFaces, EntityCarvedRune entity) { this(carvedFaces, entity.getId()); }

    public PacketCarvedRuneFacesSync(EnumMap<Direction, Rune> carvedFaces, int entityId)
    {
        this.carvedFaces.putAll(carvedFaces);
        this.entityId = entityId;
    }

    public static void encode(PacketCarvedRuneFacesSync msg, FriendlyByteBuf buf)
    {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.carvedFaces.size());
        for(Map.Entry<Direction, Rune> e : msg.carvedFaces.entrySet())
        {
            buf.writeEnum(e.getKey());
            buf.writeInt(e.getValue().hashCode());
        }
    }

    public static PacketCarvedRuneFacesSync decode(FriendlyByteBuf buf)
    {
        int eId = buf.readInt();
        EnumMap<Direction, Rune> faces = new EnumMap<>(Direction.class);
        int runeCount = buf.readInt();
        for(int i = 0; i < runeCount; i++)
        {
            Direction face = buf.readEnum(Direction.class);
            Rune rune = Rune.identify(buf.readInt());
            faces.put(face, rune);
        }
        return new PacketCarvedRuneFacesSync(faces, eId);
    }

    public static void handle(PacketCarvedRuneFacesSync msg, Supplier<NetworkEvent.Context> ctx)
    {
        Minecraft mc = Minecraft.getInstance();
        ctx.get().enqueueWork(() -> {
            EntityCarvedRune e = (EntityCarvedRune) mc.level.getEntity(msg.entityId);
            if(e != null) e.setFaces(msg.carvedFaces);
        });
        ctx.get().setPacketHandled(true);
    }
}
