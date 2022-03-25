package techeart.thadv.utils;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import techeart.thadv.content.RegistryHandler;
import techeart.thadv.content.block.entity.renderers.RendererCoreTrap;
import techeart.thadv.content.entity.renderers.*;

public class RenderHandler
{
    public static void registerEntityRenderers()
    {
        EntityRenderers.register(RegistryHandler.Entities.STONE_GUARDIAN.get(), RendererStoneGuardian::new);
        EntityRenderers.register(RegistryHandler.Entities.LAVA_BALL.get(), RendererLavaBall::new);
        EntityRenderers.register(RegistryHandler.Entities.LAVA_ERUPTION.get(), RendererEruptionSource::new);
        EntityRenderers.register(RegistryHandler.Entities.CARVED_RUNE.get(), RendererCarvedRune::new);
    }

    public static void registerBlockEntityRenderers()
    {
        BlockEntityRenderers.register(RegistryHandler.BlockEntities.CORE_TRAP.get(), RendererCoreTrap::new);
    }
}
