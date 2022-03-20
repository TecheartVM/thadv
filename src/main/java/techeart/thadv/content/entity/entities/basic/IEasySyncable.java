package techeart.thadv.content.entity.entities.basic;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import techeart.thadv.content.network.PacketHandler;
import techeart.thadv.content.network.packets.PacketEasySync;

public interface IEasySyncable
{
    default <T extends Entity & IEasySyncable> void callOnClients(T entity, String eventId)
    {
        PacketHandler.sendToTracking(new PacketEasySync(entity, eventId), entity);
    }

    @OnlyIn(Dist.CLIENT)
    default void handleEvent(String eventId) { }
}
