package techeart.thadv.content;

import net.minecraft.client.renderer.entity.EntityRenderers;
import techeart.thadv.content.entities.renderers.*;

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
