package techeart.thadv.content.world.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import techeart.thadv.content.network.PacketHandler;
import techeart.thadv.content.network.packets.PacketBossEventSync;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class ModBossEventServer extends ModBossEvent
{
    private final Set<ServerPlayer> players = Sets.newHashSet();
    private final Set<ServerPlayer> unmodifiablePlayers = Collections.unmodifiableSet(this.players);
    private boolean visible = true;

    public ModBossEventServer(Component name) { super(Mth.createInsecureUUID(), name); }

    @Override
    public void setProgress(float value)
    {
        if(value != progress)
        {
            super.setProgress(value);
            broadcast(PacketBossEventSync::createSetProgressPacket);
        }
    }

    @Override
    public void setColors(int primary, int secondary)
    {
        if(colorPrimary != primary || colorSecondary != secondary)
        {
            super.setColors(primary, secondary);
            broadcast(PacketBossEventSync::createSetColorsPacket);
        }
    }

    @Override
    public void setName(Component name)
    {
        if(this.name != name)
        {
            super.setName(name);
            broadcast(PacketBossEventSync::createSetNamePacket);
        }
    }

    @Override
    public void setBarTexture(ResourceLocation texture, int barWidth, int barHeight)
    {
        if(barTexture != texture || this.barWidth != barWidth || this.barHeight != barHeight)
        {
            super.setBarTexture(texture, barWidth, barHeight);
            broadcast(PacketBossEventSync::createSetTexturePacket);
        }
    }

    private void broadcast(Function<ModBossEvent, PacketBossEventSync> eventSup)
    {
        if(visible)
        {
            PacketBossEventSync pkt = eventSup.apply(this);
            for(ServerPlayer player : players)
                PacketHandler.sendToClient(pkt, player);
        }
    }

    public void addPlayer(ServerPlayer player)
    {
        if(visible && players.add(player))
            PacketHandler.sendToClient(PacketBossEventSync.createAddPacket(this), player);
    }

    public void removePlayer(ServerPlayer player)
    {
        if(visible && players.remove(player))
            PacketHandler.sendToClient(PacketBossEventSync.createRemovePacket(getId()), player);
    }

    public void removeAllPlayers()
    {
        if(!players.isEmpty())
            for(ServerPlayer serverplayer : Lists.newArrayList(players))
                removePlayer(serverplayer);
    }

    public boolean isVisible() { return visible; }

    public void setVisible(boolean value)
    {
        if(value != visible)
        {
            visible = value;
            PacketBossEventSync pkt = value ? PacketBossEventSync.createAddPacket(this) : PacketBossEventSync.createRemovePacket(getId());
            for(ServerPlayer player : players)
                PacketHandler.sendToClient(pkt, player);
        }
    }

    public Collection<ServerPlayer> getPlayers() { return unmodifiablePlayers; }
}
