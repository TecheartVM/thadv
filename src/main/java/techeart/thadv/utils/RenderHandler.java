package techeart.thadv.utils;

import net.minecraft.client.renderer.entity.EntityRenderers;
import techeart.thadv.content.RegistryHandler;
import techeart.thadv.content.entity.renderers.*;

public class RenderHandler
{
    public static void registerEntityRenderers()
    {
        EntityRenderers.register(RegistryHandler.STONE_GUARDIAN.get(), RendererStoneGuardian::new);
        EntityRenderers.register(RegistryHandler.LAVA_BALL.get(), RendererLavaBall::new);
        EntityRenderers.register(RegistryHandler.LAVA_ERUPTION.get(), RendererEruptionSource::new);
        EntityRenderers.register(RegistryHandler.CARVED_RUNE.get(), RendererCarvedRune::new);
    }
}
