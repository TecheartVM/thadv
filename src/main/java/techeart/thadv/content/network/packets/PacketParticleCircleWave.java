package techeart.thadv.content.network.packets;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketParticleCircleWave
{
    private final Vec3 center;
    private final float radius;

    public PacketParticleCircleWave(Vec3 center, float radius)
    {
        this.center = center;
        this.radius = radius;
    }

    public static void encode(PacketParticleCircleWave msg, FriendlyByteBuf buf)
    {
        buf.writeBlockPos(new BlockPos(msg.center));
        buf.writeFloat(msg.radius);
    }

    public static PacketParticleCircleWave decode(FriendlyByteBuf buf)
    {
        BlockPos p = buf.readBlockPos();
        return new PacketParticleCircleWave(new Vec3(p.getX(), p.getY(), p.getZ()), buf.readFloat());
    }

    public static void handle(PacketParticleCircleWave msg, Supplier<NetworkEvent.Context> ctx)
    {
        Minecraft mc = Minecraft.getInstance();
        ctx.get().enqueueWork(() -> {
            Vec3 center = msg.center;
            float radius = msg.radius;
            for (float f = 0; f < 360; f += 6)
            {
                mc.level.addParticle(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()),
                        radius*Math.cos(f) + center.x(), center.y()+0.5d, radius*Math.sin(f) + center.z(),
                        0.0D, -0.1D, 0.0D
                );
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
