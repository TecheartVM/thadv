package techeart.thadv.content.network.packets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import techeart.thadv.content.items.ItemPowerCrystal;
import techeart.thadv.content.misc.Rune;

import java.util.function.Supplier;

public class PacketPowerCrystalConfigure
{
    private final InteractionHand hand;
    private final Rune rune;

    public PacketPowerCrystalConfigure(InteractionHand hand, Rune rune)
    {
        this.hand = hand;
        this.rune = rune;
    }

    public static void encode(PacketPowerCrystalConfigure msg, FriendlyByteBuf buf)
    {
        buf.writeEnum(msg.hand);
        buf.writeInt(msg.rune.hashCode());
    }

    public static PacketPowerCrystalConfigure decode(FriendlyByteBuf buf)
    {
        return new PacketPowerCrystalConfigure(buf.readEnum(InteractionHand.class), Rune.identify(buf.readInt()));
    }

    public static void handle(PacketPowerCrystalConfigure msg, Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player == null) return;
            ItemStack heldItem = player.getItemInHand(msg.hand);
            if(heldItem.getItem() instanceof ItemPowerCrystal)
                ItemPowerCrystal.configure(heldItem, msg.rune);
        });
        ctx.get().setPacketHandled(true);
    }
}
