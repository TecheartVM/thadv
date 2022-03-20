package techeart.thadv.content.entity.renderers;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import techeart.thadv.content.entity.entities.boss.EntityBossStoneGuardian;
import techeart.thadv.content.entity.models.ModelStoneGuardian;
import techeart.thadv.content.entity.renderers.layers.GlowLayer;
import techeart.thadv.content.entity.renderers.layers.LayerStoneGuardianCoreAura;

@OnlyIn(Dist.CLIENT)
public class RendererStoneGuardian extends GeoEntityRenderer<EntityBossStoneGuardian>
{
    public RendererStoneGuardian(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new ModelStoneGuardian());
        this.shadowRadius = 1.0f;
        addLayer(new GlowLayer<>(this, ModelStoneGuardian.CORE_TEXTURE));
        addLayer(new LayerStoneGuardianCoreAura(this));
    }
}
