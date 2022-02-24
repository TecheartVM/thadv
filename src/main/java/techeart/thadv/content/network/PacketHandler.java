package techeart.thadv.content.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fmllegacy.network.NetworkDirection;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import techeart.thadv.content.MainClass;
import techeart.thadv.content.network.packets.*;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketHandler
{
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MainClass.MODID, "main_channel"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    private static int lastId = 0;

    public static void register()
    {
        registerMessage(PacketEntityPartOffsetSync.class, PacketEntityPartOffsetSync::encode, PacketEntityPartOffsetSync::decode, PacketEntityPartOffsetSync::handle);
        registerMessage(PacketGuardianStateSync.class, PacketGuardianStateSync::encode, PacketGuardianStateSync::decode, PacketGuardianStateSync::handle);
        registerMessage(PacketParticleCircleWave.class, PacketParticleCircleWave::encode, PacketParticleCircleWave::decode, PacketParticleCircleWave::handle);
        registerMessage(PacketBossEventSync.class, PacketBossEventSync::encode, PacketBossEventSync::decode, PacketBossEventSync::handle);
        registerMessage(PacketEasySync.class, PacketEasySync::encode, PacketEasySync::decode, PacketEasySync::handle);
    }

    private static <MSG> void registerMessage(Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf > encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier< NetworkEvent.Context>> handler)
    {
        CHANNEL.registerMessage(lastId++, messageType, encoder, decoder, handler);
    }

    public static void sendToClient(Object msg, ServerPlayer player)
    {
        if(!(player instanceof FakePlayer))
            CHANNEL.sendTo(msg, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object msg) { CHANNEL.sendToServer(msg); }

    public static void sendToAll(Object msg, MinecraftServer server)
    {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer player : players)
            sendToClient(msg, player);
    }

    public static void sendToTracking(Object msg, Entity entity)
    {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), msg);
    }
}
