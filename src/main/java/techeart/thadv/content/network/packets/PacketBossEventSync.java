package techeart.thadv.content.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import techeart.thadv.content.MainClass;
import techeart.thadv.content.world.event.ModBossEvent;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketBossEventSync
{
    private final UUID id;
    private final IOperation operation;

    private PacketBossEventSync(UUID id, IOperation operation)
    {
        this.id = id;
        this.operation = operation;
    }

    public static void encode(PacketBossEventSync msg, FriendlyByteBuf buf)
    {
        buf.writeUUID(msg.id);
        buf.writeEnum(msg.operation.getType());
        msg.operation.encode(buf);
    }

    public static PacketBossEventSync decode(FriendlyByteBuf buf)
    {
        return new PacketBossEventSync(buf.readUUID(), buf.readEnum(OperationType.class).decode(buf));
    }

    public static void handle(PacketBossEventSync msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            MainClass.HUD_BOSS_HEALTH_OVERLAY.update(msg);
        });
        ctx.get().setPacketHandled(true);
    }

    public static PacketBossEventSync createAddPacket(ModBossEvent e) { return new PacketBossEventSync(e.getId(), new OperationAdd(e)); }
    public static PacketBossEventSync createRemovePacket(UUID id) { return new PacketBossEventSync(id, new OperationRemove()); }
    public static PacketBossEventSync createSetProgressPacket(ModBossEvent e) { return new PacketBossEventSync(e.getId(), new OperationSetProgress(e.getProgress())); }
    public static PacketBossEventSync createSetColorsPacket(ModBossEvent e) { return new PacketBossEventSync(e.getId(), new OperationSetColors(e.getColorPrimary(), e.getColorSecondary())); }
    public static PacketBossEventSync createSetNamePacket(ModBossEvent e) { return new PacketBossEventSync(e.getId(), new OperationSetName(e.getName())); }
    public static PacketBossEventSync createSetTexturePacket(ModBossEvent e) { return new PacketBossEventSync(e.getId(), new OperationSetTexture(e.getBarTexture(), e.getBarWidth(), e.getBarHeight())); }

    public void handle(Handler handler) { operation.decode(id, handler); }

    protected static class OperationAdd implements IOperation
    {
        private final Component name;
        private final float progress;

        protected OperationAdd(ModBossEvent e)
        {
            name = e.getName();
            progress = e.getProgress();
        }

        private OperationAdd(FriendlyByteBuf buf)
        {
            name = buf.readComponent();
            progress = buf.readFloat();
        }
        @Override
        public OperationType getType() { return OperationType.ADD; }

        @Override
        public void encode(FriendlyByteBuf buf)
        {
            buf.writeComponent(name);
            buf.writeFloat(progress);
        }

        @Override
        public void decode(UUID eventId, Handler handler)
        {
            handler.add(eventId, name, progress);
        }
    }

    protected static class OperationRemove implements IOperation
    {
        @Override
        public OperationType getType() { return OperationType.REMOVE; }

        @Override
        public void encode(FriendlyByteBuf buf) { }

        @Override
        public void decode(UUID eventId, Handler handler) { handler.remove(eventId); }
    }

    protected static class OperationSetProgress implements IOperation
    {
        private final float progress;

        protected OperationSetProgress(float progress) { this.progress = progress; }

        private OperationSetProgress(FriendlyByteBuf buf) { progress = buf.readFloat(); }

        @Override
        public OperationType getType() { return OperationType.SET_PROGRESS; }

        @Override
        public void encode(FriendlyByteBuf buf) { buf.writeFloat(progress); }

        @Override
        public void decode(UUID eventId, Handler handler) { handler.setProgress(eventId, progress); }
    }

    protected static class OperationSetColors implements IOperation
    {
        private final int primary;
        private final int secondary;

        public OperationSetColors(int primary, int secondary)
        {
            this.primary = primary;
            this.secondary = secondary;
        }

        private OperationSetColors(FriendlyByteBuf buf)
        {
            primary = buf.readInt();
            secondary = buf.readInt();
        }

        @Override
        public OperationType getType() { return OperationType.SET_COLORS; }

        @Override
        public void encode(FriendlyByteBuf buf)
        {
            buf.writeInt(primary);
            buf.writeInt(secondary);
        }

        @Override
        public void decode(UUID eventId, Handler handler) { handler.setColors(eventId, primary, secondary); }
    }

    protected static class OperationSetName implements IOperation
    {
        private final Component name;

        protected OperationSetName(Component name) { this.name = name; }

        private OperationSetName(FriendlyByteBuf buf) { name = buf.readComponent(); }

        @Override
        public OperationType getType() { return OperationType.SET_NAME; }

        @Override
        public void encode(FriendlyByteBuf buf) { buf.writeComponent(name); }

        @Override
        public void decode(UUID eventId, Handler handler) { handler.setName(eventId, name); }
    }

    protected static class OperationSetTexture implements IOperation
    {
        private final ResourceLocation location;
        private final int width;
        private final int height;

        protected OperationSetTexture(ResourceLocation location, int barWidth, int barHeight)
        {
            this.location = location;
            width = barWidth;
            height = barHeight;
        }

        private OperationSetTexture(FriendlyByteBuf buf)
        {
            location = buf.readResourceLocation();
            width = buf.readInt();
            height = buf.readInt();
        }

        @Override
        public OperationType getType() { return OperationType.SET_TEXTURE; }

        @Override
        public void encode(FriendlyByteBuf buf)
        {
            buf.writeResourceLocation(location);
            buf.writeInt(width);
            buf.writeInt(height);
        }

        @Override
        public void decode(UUID eventId, Handler handler) { handler.setTexture(eventId, location, width, height); }
    }

    private enum OperationType
    {
        ADD(OperationAdd::new),
        REMOVE(buf -> new OperationRemove()),
        SET_PROGRESS(OperationSetProgress::new),
        SET_COLORS(OperationSetColors::new),
        SET_NAME(OperationSetName::new),
        SET_TEXTURE(OperationSetTexture::new);

        private final Function<FriendlyByteBuf, IOperation> decoder;

        OperationType(Function<FriendlyByteBuf, IOperation> decoder) { this.decoder = decoder; }

        public IOperation decode(FriendlyByteBuf buf) { return decoder.apply(buf); }
    }

    protected interface IOperation
    {
        OperationType getType();
        void encode(FriendlyByteBuf buf);
        void decode(UUID eventId, Handler handler);
    }

    public interface Handler
    {
        default void add(UUID eventId, Component nameText, float progress) { }
        default void remove(UUID eventId) { }
        default void setProgress(UUID eventId, float progress) { }
        default void setColors(UUID eventId, int primary, int secondary) { }
        default void setName(UUID eventId, Component name) { }
        default void setTexture(UUID eventId, ResourceLocation location, int barWidth, int barHeight) { }
    }
}
